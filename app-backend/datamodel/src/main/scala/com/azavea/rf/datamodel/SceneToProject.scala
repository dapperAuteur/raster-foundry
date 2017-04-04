package com.azavea.rf.datamodel

import java.util.UUID

case class SceneToProject(
  sceneId: UUID,
  projectId: UUID,
  accepted: Boolean, /* Has a Scene been accepted from an AOI check? */
  sceneOrder: Option[Int] = None,
  colorCorrectParams: Option[ColorCorrect.Params] = None
)

case class SceneCorrectionParams(sceneId: UUID, params: ColorCorrect.Params)
case class BatchParams(items: List[SceneCorrectionParams])
