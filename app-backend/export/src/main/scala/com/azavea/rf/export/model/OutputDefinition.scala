package com.azavea.rf.export.model

import geotrellis.proj4.CRS
import geotrellis.vector._
import geotrellis.vector.io._

import spray.json.DefaultJsonProtocol._

import java.net.URI

/**
  * Output definition
  * @param crs output [[CRS]]
  * @param rasterSize output size of each raster chunk
  * @param render [[Render]] options
  * @param crop crop result rasters
  * @param stitch stitch result raster into one
  * @param source output source [[URI]]
  * @param resolution resolution of the desired raster (currently it's zoom level)
  * @param mask [[MultiPolygon]] to query
  */
case class OutputDefinition(
  crs: CRS,
  rasterSize: Option[RasterSize],
  render: Option[Render],
  crop: Boolean,
  stitch: Boolean,
  source: URI,
  resolution: Int,
  mask: Option[MultiPolygon]
)

object OutputDefinition {
  implicit val jsonFormat = jsonFormat8(OutputDefinition.apply _)
}
