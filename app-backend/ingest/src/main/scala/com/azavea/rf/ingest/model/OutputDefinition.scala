package com.azavea.rf.ingest.model

import geotrellis.proj4.CRS
import geotrellis.raster._
import geotrellis.raster.resample._
import geotrellis.spark.io.{CRSFormat => _, _} // We must shade the default CRSFormat
import geotrellis.spark._
import geotrellis.spark.io.index._

import spray.json._
import DefaultJsonProtocol._
import java.net.URI

/** This class holds the information necessary to take a series of inputs and store them in a shared catalog
  *
  * @param uri              The URI which tells an ingest where to persist this layer
  * @param crs              The CRS of the projection to target in tiling
  * @param cellType         A GeoTrellis CellType to be used in storing a tile layer
  * @param cellSize         The expected 'native resolution' size of each cell
  * @param histogramBuckets The number of bins with which to construct histograms used in coloring
  * @param pyramid          Whether or not to pyramid multiple zoom levels on ingest
  * @param native           Whether or not to keep around a copy of the 'native resolution' images
  * @param ndPattern        The pattern for determining which cells should be treated as ND
  * @param resampleMethod   The type of GeoTrellis resample method to use for cell value estimation
  * @param keyIndexMethod   The GeoTrellis KeyIndexMethod to use when writing output indices
  */
case class OutputDefinition(
  uri: URI,
  crs: CRS,
  cellType: CellType,
  histogramBuckets: Int = 256,
  tileSize: Int = 256,
  pyramid: Boolean = true,
  native: Boolean = false,
  ndPattern: Option[OutputDefinition.NoDataPattern] = None,
  resampleMethod: ResampleMethod = NearestNeighbor,
  keyIndexMethod: KeyIndexMethod[SpatialKey] = ZCurveKeyIndexMethod // TODO: read, no write
)

object OutputDefinition {
  case class NoDataPattern(pattern: Map[Int, Double] = Map()) {
    /** This function creates the masking tile for setting NODATA values
      *  It is separated out to make testing easier
      */
    def createMask(mbTile: MultibandTile): Tile = {
      // Ensure that we have enough bands for this definition
      try {
        assert(mbTile.bands.length >= pattern.toList.length)
      } catch {
        case e: java.lang.AssertionError =>
          throw new java.lang.IllegalStateException(s"Not enough bands for provided NoData pattern application (found ${mbTile.bands.length}; needed ${pattern.toList.length})")
      }

      val bandsOfInterest = pattern.keys.toList.sorted

      if (mbTile.cellType.isFloatingPoint) {
        val expectedValues = bandsOfInterest.map(pattern(_).toDouble)
        val combiner = { values: Seq[Double] => if (values == expectedValues) NODATA else 1.0 }
        mbTile.combineDouble(bandsOfInterest)(combiner)
      } else {
        val expectedValues = bandsOfInterest.map(pattern(_).toInt)
        val combiner = { values: Seq[Int] => if (values == expectedValues) NODATA else 1 }
        mbTile.combine(bandsOfInterest)(combiner)
      }
    }

    /** Apply an instance of this pattern to set NoData values */
    def apply(mbTile: MultibandTile): MultibandTile = {
      if (pattern.isEmpty) {
        mbTile
      } else {
        val mask = createMask(mbTile)
        val intFunc = { (i: Int, j: Int) => if (isNoData(j)) NODATA else i }
        val doubleFunc = { (i: Double, j: Double) => if (isNoData(j)) NODATA else i }

        MultibandTile(mbTile.bands.map { tile => tile.dualCombine(mask)(intFunc)(doubleFunc) })
      }
    }
  }

  object NoDataPattern {
    implicit object NoDataPatternJsonFormat extends JsonFormat[NoDataPattern] {
      def write(ndPattern: NoDataPattern) = JsObject(
        "pattern" -> ndPattern.pattern.toJson
      )

      def read(js: JsValue): NoDataPattern = js.asJsObject.getFields("pattern") match {
        case Seq(patternObject) =>
          try {
            val pattern = patternObject.asJsObject.fields.map({ case (k: String, v: JsValue) =>
              k.toInt -> v.convertTo[Double]
            })
            NoDataPattern(pattern)
          } catch {
            case e: java.lang.NumberFormatException =>
              deserializationError(s"Unable to parse all keys as integers in: $patternObject")
          }
      }
    }
  }

  implicit val jsonFormat = jsonFormat10(OutputDefinition.apply _)
}
