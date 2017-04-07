package com.azavea.rf.datamodel

import geotrellis.slick.Projected
import geotrellis.vector.MultiPolygon

import io.circe._

case class ExportOptions(
  area: Projected[MultiPolygon],
  stitch: Boolean,
  crop: Boolean,
  bands: Seq[Int],
  rasterSize: Int,
  crs: Int
)
