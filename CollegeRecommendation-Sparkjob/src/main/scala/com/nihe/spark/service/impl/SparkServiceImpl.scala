package com.nihe.spark.service.impl

import org.apache.spark.sql.{DataFrame, DataFrameReader, SparkSession}
import com.nihe.spark.service.SparkService
import com.nihe.spark.util.GetProperties.getProps

/** Created on 2022/4/29 19:50
 *
 * @version 1.0
 * @author nihe
 */
object SparkServiceImpl extends SparkService{

  /**
   * 获取SparkSession
   * @param master  设置运行的方式
   * @param appName 设置程序名称
   * @return SparkSession
   */
  override def getSparkSession(master: String, appName: String): SparkSession = {
    // TODO 简历SparkSession
    val sparkSession = SparkSession
      .builder
      .appName(appName)
    if (master == "local") sparkSession.master("local")
    sparkSession.getOrCreate()
  }

}

