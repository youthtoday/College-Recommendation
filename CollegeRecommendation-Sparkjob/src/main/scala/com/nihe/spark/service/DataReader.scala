package com.nihe.spark.service

import org.apache.spark.sql.{DataFrame, SparkSession}

/** Created on 2022/5/5 15:49
 *
 * 针对各种格式的数据的读取方案
 * @version 1.0
 * @author nihe
 */
trait DataReader {
  //从parquet读取内容
  def readFromParquet(spark:SparkSession, name:String): DataFrame
  //从数据库读取内容
  def readFromDB(sparkSession:SparkSession,dbTableName:String,dbName:String,dbType:String):DataFrame
}
