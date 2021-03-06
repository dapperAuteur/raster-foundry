package com.azavea.rf.datamodel

import java.util.UUID
import java.sql.Timestamp


case class Organization(
  id: UUID,
  createdAt: Timestamp,
  modifiedAt: Timestamp,
  name: String
)

object Organization {

  def tupled = (Organization.apply _).tupled

  def create = Create.apply _

  case class Create(name: String) {
    def toOrganization(): Organization = {
      val id = java.util.UUID.randomUUID()
      val now = new Timestamp((new java.util.Date()).getTime())
      Organization(id, now, now, name)
    }
  }
}
