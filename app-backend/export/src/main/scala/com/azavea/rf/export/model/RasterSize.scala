package com.azavea.rf.export.model

import spray.json.DefaultJsonProtocol._

case class RasterSize(height: Int, width: Int)

object RasterSize {
  implicit val jsonFormat = jsonFormat2(RasterSize.apply _)
}