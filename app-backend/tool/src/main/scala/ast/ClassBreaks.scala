package com.azavea.rf.tool.ast

import geotrellis.raster.render._
import geotrellis.raster._
import geotrellis.util._
import spire.algebra._
import spire.math.Sorting
import spire.std.any._
import spire.syntax.order._

abstract class ClassBreaks[A: Order, B: Order](classMap: Map[A, B], boundaryType: ClassBoundaryType, ndColor: B, fallbackColor: B) {
  lazy val mapStrategy =
    new MapStrategy[B](boundaryType, ndColor, fallbackColor, false)

  def toBreakMap(ndCheck: A => Boolean) =
    new BreakMap[A, B](classMap, mapStrategy, ndCheck)
}

case class IntClassBreaks(
  classMap: Map[Double, Int],
  boundaryType: ClassBoundaryType = LessThanOrEqualTo,
  ndColor: Int = NODATA,
  fallbackColor: Int = NODATA
) extends ClassBreaks(classMap, boundaryType, ndColor, fallbackColor)

case class DoubleClassBreaks(
  classMap: Map[Double, Double],
  boundaryType: ClassBoundaryType = LessThanOrEqualTo,
  ndColor: Double = doubleNODATA,
  fallbackColor: Double = doubleNODATA
) extends ClassBreaks(classMap, boundaryType, ndColor, fallbackColor)

