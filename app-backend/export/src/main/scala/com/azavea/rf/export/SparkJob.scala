package com.azavea.rf.export

import org.apache.spark._

trait SparkJob {
  // Some of these options can be set by way of the spark-submit command
  val conf: SparkConf = new SparkConf()
    .setAppName("Raster Foundry Export")
    .set("spark.serializer", classOf[org.apache.spark.serializer.KryoSerializer].getName)
    .set("spark.kryo.registrator", classOf[geotrellis.spark.io.kryo.KryoRegistrator].getName)
    .setIfMissing("spark.master", "local[*]")
}
