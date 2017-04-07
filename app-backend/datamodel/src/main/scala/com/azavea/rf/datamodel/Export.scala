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
  fileType: FileType,
  uploadType: UploadType,
  files: List[String],
  datasource: UUID,
  metadata: Json,
  visibility: Visibility
)

object Export {

  def tupled = (Upload.apply _).tupled

  def create = Upload.apply _

  case class Create(
    organizationId: UUID,
    uploadStatus: ExportStatus,
    fileType: FileType,
    uploadType: UploadType,
    files: List[String],
    datasource: UUID,
    metadata: Json,
    visibility: Visibility
  ) {
    def toExport(userId: String): Export = {
      val id = UUID.randomUUID()
      val now = new Timestamp((new java.util.Date()).getTime())
      Export(
        id = id,
        createdAt = now, // createdAt
        userId, // createdBy
        now, // modifiedAt
        userId, // modifiedBy
        this.organizationId,
        this.uploadStatus,
        this.fileType,
        this.uploadType,
        this.files,
        this.datasource,
        this.metadata,
        this.visibility
      )
    }
  }
}
