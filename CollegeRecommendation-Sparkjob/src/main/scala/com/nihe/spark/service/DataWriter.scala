package com.nihe.spark.service

import org.apache.spark.sql.DataFrame

/** Created on 2022/5/5 15:52
 *
 *  针对各种格式的数据的写入方案
 * @version 1.0
 * @author nihe
 */
trait DataWriter {
  //将DataFrame写入Parquet
  def writeToParquet(dataFrame:DataFrame,name:String):Unit
  //将DataFrame写入数据库
  def writeToDB(dataFrame:DataFrame,dbTableName:String,dbName:String,dbType:String): Unit


}
