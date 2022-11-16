package com.nihe.spark.service.impl

import org.apache.spark.sql.{DataFrame, SaveMode}
import com.nihe.spark.service.DataWriter
import com.nihe.spark.util.GetProperties.getProps

/** Created on 2022/5/6 17:35
 *
 * @version 1.0
 * @author nihe
 */
object DataWriterImpl extends DataWriter{
  /**
   * 将DataFrame写入Parquet
   * @param dataFrame 数据
   * @param name 存储名称
   */
  override def writeToParquet(dataFrame: DataFrame,name:String): Unit = {
    // TODO 存储
    dataFrame.write.format("parquet").save("/dataset/" + name+".parquet")
  }

  /**
   * 将DataFrame写入到数据库中
   * @param dataFrame 数据
   * @param dbType  存储数据库的类型
   * @param dbTableName 存储的数据表名
   */
  override def writeToDB(dataFrame: DataFrame, dbTableName: String,dbName:String = "test1",dbType:String="mysql"): Unit = {
    // TODO 使用properties配置数据库连接
    val props = getProps(dbType)//使用util包中的方法获取properties内容

    // TODO 将DataFrame存储为指定数据库类型的表
    dataFrame.write
      .format("jdbc")
      .mode(SaveMode.Ignore)
      .option("url", props.getProperty("url")+dbName)
      .option("driver", props.getProperty("driverClassName"))
      .option("user", props.getProperty("username"))
      .option("password", props.getProperty("password"))
      .option("dbtable",dbTableName)
      .save()
  }
}
