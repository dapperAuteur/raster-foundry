package com.azavea.rf.export.model

import spray.json.DefaultJsonProtocol._

case class Render(operation: String, bands: Array[Int])

object Render {
  implicit val jsonFormat = jsonFormat2(Render.apply _)
}

