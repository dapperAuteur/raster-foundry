package com.azavea.rf.export.util

import geotrellis.spark.io.s3.S3InputFormat
import java.net.URI

object S3 {
  /** Parse an S3 URI unto its bucket and prefix portions */
  def parse(uri: URI): (String, String) = {
    val S3InputFormat.S3UrlRx(_, _, bucket, prefix) = uri.toString
    (bucket, prefix)
  }
}
