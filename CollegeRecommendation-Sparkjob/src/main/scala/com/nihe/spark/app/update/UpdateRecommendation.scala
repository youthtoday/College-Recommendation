package com.nihe.spark.app.update

import com.nihe.spark.app.doprocess.GetFPGrowth.getFPGrowth
import com.nihe.spark.app.doprocess.GetRec.getRec
import com.nihe.spark.app.doprocess.GetSim.getSim
import com.nihe.spark.dbprocess.CollegeMajorRating.collegeMajorRating
import com.nihe.spark.dbprocess.McodeSet.mcodeSet
import com.nihe.spark.service.impl.SparkServiceImpl.getSparkSession
import org.apache.log4j.{Level, Logger}


/** Created on 2022/5/16 23:32
 * SpringBoot 调用Spark接口,实现数据更新
 *
 * @version 1.0
 * @author nihe
 */
object UpdateRecommendation {
  /**
   * 新建存储
   *
   * @param args args(0)传入db名称
   */
  def main(args: Array[String]): Unit = {
//    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
//    Logger.getLogger("org.apache.jetty.server").setLevel(Level.OFF)
    // TODO 获取Spark连接
    val spark = getSparkSession("yarn", "spark")
    // TODO 接收数据库参数
    val sparkDB = args(0) //输入数据库
    val db = args(1) //输出数据库
//    val sparkDB = "sparkJobDB" //输入数据库
//    val db = "new1" //输出数据库
    // TODO 数据清洗
    collegeMajorRating(spark, sparkDB, db)
    mcodeSet(spark,sparkDB, db)
    // TODO 挖掘关联规则并存储
    getFPGrowth(spark, sparkDB, db)
    // TODO 计算学校推荐
    getSim(spark, db)
    // TODO 计算学科推荐
    getRec(spark, db)
  }
}
