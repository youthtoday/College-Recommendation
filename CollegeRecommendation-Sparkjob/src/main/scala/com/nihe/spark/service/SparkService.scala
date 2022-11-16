package com.nihe.spark.service

import org.apache.spark.sql.{DataFrame, DataFrameReader, SparkSession}

/** Created on 2022/4/28 19:19
 *
 * @version 1.0
 * @author nihe
 */
trait SparkService {
  //获取Spark连接
  def getSparkSession(master:String,appName:String):SparkSession



}
