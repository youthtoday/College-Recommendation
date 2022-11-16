package com.nihe.spark.dbprocess

import com.nihe.spark.service.impl.DataReaderImpl.readFromDB
import com.nihe.spark.service.impl.DataWriterImpl.writeToDB
import com.nihe.spark.service.impl.SparkServiceImpl.getSparkSession
import org.apache.spark.sql.SparkSession


/** Created on 2022/5/8 16:25
 *
 * @version 1.0
 * @author nihe
 */
object McodeSet {

  def mcodeSet(spark: SparkSession, sparkDB: String, db: String): Unit = {
    val all_college_major = readFromDB(spark, "all_college_major", sparkDB)
    all_college_major.createOrReplaceTempView("ACM")
    val frame = spark.sql(
      """
        | SELECT
        | school_id,class_id, CONCAT_WS(',',COLLECT_SET(mcode))
        | AS mcode_set
        | FROM ACM
        | GROUP BY school_id,class_id
        |""".stripMargin).na.fill(0)
    frame.show()
    writeToDB(frame, "mcodeSet", db)
  }

}
