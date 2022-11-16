package com.nihe.spark.service.impl

import org.apache.spark.sql.{DataFrame, SparkSession}
import com.nihe.spark.service.DataReader
import com.nihe.spark.util.GetProperties.getProps

/** Created on 2022/5/6 17:33
 *
 * @version 1.0
 * @author nihe
 */
object DataReaderImpl extends DataReader{
  /**
   *
   * @param spark SparkSession
   * @param name parquet路径
   * @return DataFrame
   */
  override def readFromParquet(spark: SparkSession, name: String): DataFrame = {
    // TODO 读取
    spark.read.format("parquet").load("/dataset/"+name )
  }

  /**
   * 从数据库中读出DataFrame
   * @param spark SparkSession
   * @param dbType  读取的数据库类型
   * @return DataFrame
   */
  override def readFromDB(spark: SparkSession,dbTableName: String,dbName:String = "test1",dbType:String="mysql"): DataFrame = {
    // TODO 使用properties配置数据库连接
    val props = getProps(dbType)//使用util包中的方法获取properties内容

    // TODO 将数据读取成Spark
    val dataFrame = spark
      .read
      .format("jdbc")
      .option("url", props.getProperty("url")+dbName)
      .option("driver", props.getProperty("driverClassName"))
      .option("user", props.getProperty("username"))
      .option("password", props.getProperty("password"))
      .option("dbtable",dbTableName)
      .load()
    dataFrame
  }
}
