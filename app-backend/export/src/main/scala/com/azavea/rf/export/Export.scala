package com.azavea.rf.export

import com.azavea.rf.export.model._
import com.azavea.rf.export.util._

import geotrellis.raster._
import geotrellis.raster.io.geotiff.{GeoTiff, MultibandGeoTiff}
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.hadoop._
import geotrellis.spark.io.file._
import geotrellis.spark.io.s3._

import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.fs.Path
import org.apache.spark._
import spray.json._

import java.net.URI

object Export extends SparkJob with LazyLogging {

  case class Params(
    jobDefinition: URI = new URI(""),
    testRun: Boolean = false,
    overwrite: Boolean = false
  )

  // Functions for combine step
  def createTiles(tile: MultibandGeoTiff): Seq[MultibandGeoTiff]                                       = Seq(tile)
  def mergeTiles1(tiles: Seq[MultibandGeoTiff], tile: MultibandGeoTiff): Seq[MultibandGeoTiff]         = tiles :+ tile
  def mergeTiles2(tiles1: Seq[MultibandGeoTiff], tiles2: Seq[MultibandGeoTiff]): Seq[MultibandGeoTiff] = tiles1 ++ tiles2

  /** Get a LayerReader and an attribute store for the catalog located at the provided URI
    *
    * @param exportLayerDef export job's layer definition
    */
  def getRfLayerManagement(exportLayerDef: ExportLayerDefinition)(implicit @transient sc: SparkContext): (FilteringLayerReader[LayerId], AttributeStore) =
    exportLayerDef.ingestLocation.getScheme match {
      case "s3" | "s3a" | "s3n" =>
        val (bucket, prefix) = S3.parse(exportLayerDef.ingestLocation)
        val reader = S3LayerReader(bucket, prefix)
        (reader, reader.attributeStore)
      case "file" =>
        val reader = FileLayerReader(exportLayerDef.ingestLocation.getPath)
        (reader, reader.attributeStore)
    }

  def exportProject(params: Params)(exportDefinition: ExportDefinition)(implicit @transient sc: SparkContext) = {
    val wrappedConfiguration = HadoopConfiguration(S3.setCredentials(sc.hadoopConfiguration))
    val (input, output) = exportDefinition.input -> exportDefinition.output
    input.layers.map { ld =>
      val (reader, attributeStore) = getRfLayerManagement(ld)
      val layerId = LayerId(ld.layerId.toString, input.resolution)
      val md = attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId)

      val query = {
        val q = reader.query[SpatialKey, MultibandTile, TileLayerMetadata[SpatialKey]](layerId)
        input.mask.fold(q)(mp => q.where(Intersects(mp)))
      }

      query
        .result
        .mapValues { tile =>
          output.render.map(_.bands.toSeq) match {
            case Some(seq) if seq.nonEmpty => tile.subsetBands(seq)
            case _ => tile
          }
        }
        .map { case (key, tile) =>
          (key, output.crs match {
            case Some(crs) => GeoTiff(tile.reproject(md.mapTransform(key), md.crs, crs), crs)
            case _ => GeoTiff(tile, md.mapTransform(key), md.crs)
          })
        }
    }.reduce(_ union _).combineByKey(createTiles, mergeTiles1, mergeTiles2).map { case (key, seq) =>
      val head = seq.head
      key -> GeoTiff(seq.map(_.tile).reduce(_ merge _), head.extent, head.crs)
    }.foreachPartition { iter =>
      val configuration = wrappedConfiguration.get
      iter.foreach { case (key, tile) =>
        tile.write(
          new Path(s"${output.source.toString}/${input.resolution}-${key.col}-${key.row}.tiff"),
          configuration
        )
      }
    }
  }

  /**
    *  Sample ingest definitions can be found in the accompanying test/resources
    *
    * @param args Arguments to be parsed by the tooling defined in [[CommandLine]]
    */
  def main(args: Array[String]): Unit = {
    val params = CommandLine.parser.parse(args, Export.Params()) match {
      case Some(params) =>
        params
      case None =>
        throw new Exception("Unable to parse command line arguments")
    }

    val exportDefinition = readString(params.jobDefinition).parseJson.convertTo[ExportDefinition]

    implicit val sc = new SparkContext(conf)
    try {
      exportProject(params)(exportDefinition)
    } finally {
      sc.stop
    }
  }
}
