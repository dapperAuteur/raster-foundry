package com.azavea.rf.datamodel

import io.circe._

import java.util.UUID
import java.sql.Timestamp

case class Export(
  id: UUID,
  createdAt: Timestamp,
  createdBy: String,
  modifiedAt: Timestamp,
  modifiedBy: String,
  organizationId: UUID,
  exportStatus: ExportStatus,
  exportType: ExportType,
  visibility: Visibility,
  exportOptions: Json
)

object Export {

  def tupled = (Export.apply _).tupled

  def create = Export.apply _

  case class Create(
    organizationId: UUID,
    exportStatus: ExportStatus,
    exportType: ExportType,
    visibility: Visibility,
    exportOptions: Json
  ) {
    def toExport(userId: String): Export = {
      val id = UUID.randomUUID()
      val now = new Timestamp((new java.util.Date()).getTime())
      Export(
        id = id,
        createdAt = now,
        createdBy = userId,
        modifiedAt = now,
        modifiedBy = userId,
        organizationId = this.organizationId,
        exportStatus = this.exportStatus,
        exportType = this.exportType,
        visibility = this.visibility,
        exportOptions = this.exportOptions
      )
    }
  }
}
