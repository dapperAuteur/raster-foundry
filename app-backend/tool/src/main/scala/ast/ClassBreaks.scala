package com.azavea.rf.tool.ast

import geotrellis.raster.render._
import geotrellis.raster._
import geotrellis.util._
import spire.algebra._
import spire.math.Sorting
import spire.std.any._
import spire.syntax.order._

case class ClassBreaks(
  classMap: Map[Double, Int],
  boundaryType: ClassBoundaryType = LessThanOrEqualTo,
  ndValue: Int = NODATA,
  fallback: Int = NODATA
) {
  lazy val mapStrategy =
    new MapStrategy(boundaryType, ndValue, fallback, false)

  def toBreakMap =
    new BreakMap(classMap, mapStrategy, { i: Double => isNoData(i) })
}
