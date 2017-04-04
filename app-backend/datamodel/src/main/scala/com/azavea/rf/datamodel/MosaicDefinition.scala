package com.azavea.rf.datamodel

import java.util.UUID

case class MosaicDefinition(sceneId: UUID, colorCorrections: Option[ColorCorrect.Params])

object MosaicDefinition {
  def fromScenesToProjects(scenesToProjects: Seq[SceneToProject]): Seq[MosaicDefinition] = {
    scenesToProjects.map { case SceneToProject(sceneId, _, _, _, colorCorrection) =>
      MosaicDefinition(sceneId, colorCorrection)
    }
  }
}
