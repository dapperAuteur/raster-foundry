package com.azavea.rf.datamodel

import java.sql.Timestamp
import java.util.UUID

import cats.syntax.either._
import geotrellis.slick.Projected
import geotrellis.vector.Geometry
import geotrellis.vector.io.json.GeoJsonSupport
import io.circe._
import cats.implicits._

// --- //

case class Project(
  id: UUID,
  createdAt: Timestamp,
  modifiedAt: Timestamp,
  organizationId: UUID,
  createdBy: String,
  modifiedBy: String,
  name: String,
  slugLabel: String,
  description: String,
  visibility: Visibility,
  tileVisibility: Visibility,
  isAOIProject: Boolean,
  aoiCadenceMillis: Long, /* Milliseconds */
  aoisLastChecked: Timestamp,
  tags: List[String] = List.empty,
  extent: Option[Projected[Geometry]] = None,
  manualOrder: Boolean = true
)

/** Case class for project creation */
object Project extends GeoJsonSupport {

  /* One week, in milliseconds */
  val DEFAULT_CADENCE: Long = 604800000

  def tupled = (Project.apply _).tupled

  def create = Create.apply _

  def slugify(input: String): String = {
    import java.text.Normalizer
    Normalizer.normalize(input, Normalizer.Form.NFD)
      .replaceAll("[^\\w\\s-]", ""
        .replace('-', ' ')
        .trim
        .replaceAll("\\s+", "-")
        .toLowerCase)
  }

  case class Create(
    organizationId: UUID,
    name: String,
    description: String,
    visibility: Visibility,
    tileVisibility: Visibility,
    isAOIProject: Boolean,
    aoiCadenceMillis: Long,
    tags: List[String]
  ) {
    def toProject(userId: String): Project = {
      val now = new Timestamp((new java.util.Date()).getTime())
      Project(
        UUID.randomUUID, // primary key
        now, // createdAt
        now, // modifiedAt
        organizationId,
        userId, // createdBy
        userId, // modifiedBy
        name,
        slugify(name),
        description,
        visibility,
        tileVisibility,
        isAOIProject,
        aoiCadenceMillis,
        new Timestamp(now.getTime - aoiCadenceMillis),
        tags
      )
    }
  }

  object Create {
    /** Custon Circe decoder for [[Create]], to handle default values. */
    implicit val projectDecoder: Decoder[Create] = Decoder.instance(c =>
      (c.downField("organizationId").as[UUID]
        |@| c.downField("name").as[String]
        |@| c.downField("description").as[String]
        |@| c.downField("visibility").as[Visibility]
        |@| c.downField("tileVisibility").as[Visibility]
        |@| c.downField("isAOIProject").as[Option[Boolean]].map(_.getOrElse(false))
        |@| c.downField("aoiCadenceMillis").as[Option[Long]].map(_.getOrElse(DEFAULT_CADENCE))
        |@| c.downField("tags").as[List[String]]
      ).map(Create.apply)
    )
  }
}
