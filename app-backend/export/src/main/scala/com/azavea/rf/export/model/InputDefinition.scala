package com.azavea.rf.export.model

import spray.json.DefaultJsonProtocol._

import java.util.UUID

case class InputDefinition(
  projectId: UUID,
  layers: Array[ExportLayerDefinition]
)

object InputDefinition {
  implicit val jsonFormat = jsonFormat2(InputDefinition.apply _)
}
