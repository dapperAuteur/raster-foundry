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

object Export extends SparkJob with LazyLogging {

  case class Params(
    jobDefinition: URI = new URI(""),
    testRun: Boolean = false,
    overwrite: Boolean = false,
    windowSize: Int = 1024,
    partitionsPerFile: Int = 8
  )

  type RfLayerWriter = (Reader[LayerId, RDD[(SpatialKey, MultibandTile)] with Metadata[TileLayerMetadata[SpatialKey]]])

  /** Get a LayerReader and an attribute store for the catalog located at the provided URI
    *
    * @param exportLayerDef export job's layer definition
    */
  def getRfLayerManagement(exportLayerDef: ExportLayerDefinition)(implicit sc: SparkContext): (RfLayerWriter, AttributeStore) =
    exportLayerDef.ingestLocation.getScheme match {
      case "s3" | "s3a" | "s3n" =>
        val (bucket, prefix) = S3.parse(exportLayerDef.ingestLocation)
        val layerReader = S3LayerReader(bucket, prefix)
        val reader = layerReader.reader[SpatialKey, MultibandTile, TileLayerMetadata[SpatialKey]]
        (reader, layerReader.attributeStore)
      case "file" =>
        val layerReader = FileLayerReader(exportLayerDef.ingestLocation.getPath)
        val reader = layerReader.reader[SpatialKey, MultibandTile, TileLayerMetadata[SpatialKey]]
        (reader, layerReader.attributeStore)
    }

  def exportProject(params: Params)(exportDefinition: ExportDefinition)(implicit sc: SparkContext) = {

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
      //ingestDefinition.layers.foreach(ingestLayer(params))
      //if (params.testRun) { ingestDefinition.layers.foreach(Validation.validateCatalogEntry) }
    } finally {
      sc.stop
    }
  }
}

