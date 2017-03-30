package com.azavea.rf.export

import com.azavea.rf.export.model._
import com.azavea.rf.export.util._

import geotrellis.raster._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.file._
import geotrellis.spark.io.s3._

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark._
import org.apache.spark.rdd._
import spray.json._

import java.net.URI

import geotrellis.raster.io.geotiff.GeoTiff
import org.apache.hadoop.fs.Path

object Export extends SparkJob with LazyLogging {

  case class Params(
    jobDefinition: URI = new URI(""),
    testRun: Boolean = false,
    overwrite: Boolean = false
  )

  /** Get a LayerReader and an attribute store for the catalog located at the provided URI
    *
    * @param exportLayerDef export job's layer definition
    */
  def getRfLayerManagement(exportLayerDef: ExportLayerDefinition)(implicit sc: SparkContext): (FilteringLayerReader[LayerId], AttributeStore) =
    exportLayerDef.ingestLocation.getScheme match {
      case "s3" | "s3a" | "s3n" =>
        val (bucket, prefix) = S3.parse(exportLayerDef.ingestLocation)
        val reader = S3LayerReader(bucket, prefix)
        (reader, reader.attributeStore)
      case "file" =>
        val reader = FileLayerReader(exportLayerDef.ingestLocation.getPath)
        (reader, reader.attributeStore)
    }

  def exportProject(params: Params)(exportDefinition: ExportDefinition)(implicit sc: SparkContext) = {
    val input = exportDefinition.input
    val output = exportDefinition.output
    input.layers.foreach { ld =>
      val (reader, attributeStore) = getRfLayerManagement(ld)
      val layerId = LayerId(ld.layerId.toString, input.resolution)
      val md = attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId)

      val query = {
        val q = reader.query[SpatialKey, MultibandTile, TileLayerMetadata[SpatialKey]](layerId)
        input.mask.fold(q)(mp => q.where(Intersects(mp)))
      }

      query
        .result
        .mapValues { _.subsetBands(output.render.map(_.bands.toSeq).getOrElse(Seq())) }
        .foreachPartition { _.foreach { case (key, tile) =>
          val tiff = GeoTiff(tile.reproject(md.mapTransform(key), md.crs, output.crs), output.crs)
          tiff.write(new Path(output.source))
        } }

    }

  }

  /** Sample ingest definitions can be found in the accompanying test/resources
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

