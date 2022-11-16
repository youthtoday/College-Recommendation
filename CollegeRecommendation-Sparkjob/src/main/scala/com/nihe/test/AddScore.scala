package com.nihe.test

import com.nihe.spark.service.impl.DataReaderImpl.readFromDB
import com.nihe.spark.service.impl.DataWriterImpl.writeToDB
import com.nihe.spark.service.impl.SparkServiceImpl.getSparkSession

/** Created on 2022/5/17 15:14
 *
 * @version 1.0
 * @author nihe
 */
object AddScore {
  def main(args: Array[String]): Unit = {
    val spark = getSparkSession("yarn", "add")
    val all_college_major = readFromDB(spark, "4_all_college_major")
    val college_major_rating = readFromDB(spark, "college_major_rating")

    college_major_rating.createOrReplaceTempView("CMR")
    all_college_major.createOrReplaceTempView("ACM")

    val frame = spark.sql(
      """
        | SELECT
        | ACM.*,CMR.rating
        | FROM ACM,CMR
        | WHERE ACM.school_id = CMR.school_id
        | AND ACM.class_id = CMR.c_code
        |""".stripMargin).na.fill(0)
    writeToDB(frame, "all_college_major")

  }

}
