package com.nihe.spark.dbprocess

import com.nihe.spark.service.impl.DataReaderImpl.readFromDB
import com.nihe.spark.service.impl.DataWriterImpl.writeToDB
import com.nihe.spark.service.impl.SparkServiceImpl.getSparkSession
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/** Created on 2022/5/10 14:37
 *
 * @version 1.0
 * @author nihe
 */
object CollegeMajorRating {
  /**
   * 关于学科排名说明
   * A+,前2%或前2名   9.8~10.0= 9.9
   * A,2%～5%        9.5~9.8 = 9.65
   * A-,5%～10%      9.0~9.5 = 9.25
   * B+,10%～20%     8.0~9.0 = 8.5
   * B,20%～30%      7.0~8.0 = 7.5
   * B-,30%～40%     6.0~7.0 = 6.5
   * C+,40%～50%     5.0~6.0 = 5.5
   * C,50%～60%      4.0~5.0 = 4.5
   * C-,60%～70%     3.0~4.0 = 3.5
   * 未上榜70~100%    0.0~3.0 = 1.5
   * 未开设 0         0
   */
  def collegeMajorRating(spark:SparkSession,sparkDB:String,db:String): Unit = {
    // TODO 读数据库
    val major_collegeSet = readFromDB(spark,"discipline_ranking",sparkDB)
    val majorSet = readFromDB(spark,"major",sparkDB)
    val all_college_majorSet = readFromDB(spark,"college_major",sparkDB)

    // TODO 创建SparkSql临时表
    major_collegeSet.createOrReplaceTempView("major_c") //学校专业评分表
    majorSet.createOrReplaceTempView("major") //专业信息表对照学科评估xid表
    all_college_majorSet.createOrReplaceTempView("all") //学校开设专业信息表

    // TODO 获取全部学校代码
    spark.sql(
      """
        | SELECT DISTINCT
        | scode FROM major_c
        |""".stripMargin).createOrReplaceTempView("college")
    // TODO 数据转换scode学校代码,xid学科代码
    val dfMC = spark.sql(
      """
        | SELECT scode ,xid,
        | CASE
        | WHEN result = 'A+' THEN 9.9
        | WHEN result = 'A' THEN 9.65
        | WHEN result = 'A-' THEN 9.25
        | WHEN result = 'B+' THEN 8.5
        | WHEN result = 'B' THEN 7.5
        | WHEN result = 'B-' THEN 6.5
        | WHEN result = 'C+' THEN 5.5
        | WHEN result = 'C' THEN 4.5
        | WHEN result = 'C-' THEN 3.5
        | ElSE 0
        | END AS rating
        | FROM major_c
        | ORDER BY xid
        |""".stripMargin)
    dfMC.createOrReplaceTempView("mc")

    // TODO 转换学科代码成统一的教育部评级格式
    val dfMCR = spark.sql(
      """
        | SELECT
        | mc.scode as school_id,
        | major.mid as c_code,
        | mc.rating as rating
        | FROM mc, college, major
        | WHERE
        | college.scode = mc.scode
        | AND
        | major.id = mc.xid
        | ORDER BY school_id,c_code
        |""".stripMargin)
    dfMCR.createOrReplaceTempView("mcr")

    // TODO 生成学科开设情况
    val open= spark.sql(
      """
        | SELECT school_id,class_id,1.5 as rating
        | from all
        |""".stripMargin)
    open.createOrReplaceTempView("all_open")

    // TODO 为了展示所有的学校学科开设情况,创建笛卡尔积
    val de = spark.sql(
      """
        | SELECT
        | major.mid AS c_code,college.scode AS school_id
        | FROM
        | major JOIN college
        | ORDER BY school_id,c_code
        |""".stripMargin)
    de.createOrReplaceTempView("de")

    // TODO 连接笛卡尔积和评分情况
    val de_rating = spark.sql(
      """
        | SELECT
        | de.school_id,de.c_code,mcr.rating
        | FROM
        | de LEFT JOIN mcr ON
        | de.school_id=mcr.school_id
        | AND
        | de.c_code = mcr.c_code
        | ORDER BY c_code ASC
        |""".stripMargin).na.fill(0)
    de_rating.createOrReplaceTempView("deRT")

    // TODO 连接笛卡尔积和学校开设情况
    val de_open = spark.sql(
      """
        | SELECT
        | de.school_id ,de.c_code,all_open.rating
        | FROM
        | de LEFT JOIN all_open ON
        | de.school_id = all_open.school_id
        | AND
        | de.c_code = all_open.class_id
        | ORDER BY c_code ASC
        |""".stripMargin
    ).na.fill(0)
    de_open.createOrReplaceTempView("deOP")

    // TODO 合并开设情况和评分情况笛卡尔积
    //  最后得到的数据为:
    //  某学校开设了某学科且有评分时,rating项是对应评分
    //  某学校开设了某学科但无平分时,rating项对应1.5分
    //  某学校没有开设某学科,rating对应0分
    val deMC = spark.sql(
      """
        | SELECT DISTINCT
        | deOP.school_id,deOP.c_code,
        | CASE
        | WHEN deRT.rating = 0 THEN deOP.rating
        | ELSE deRT.rating
        | END AS rating
        | FROM deOP,deRT
        | WHERE deOP.school_id = deRT.school_id
        | AND
        | deOP.c_code = deRT.c_code
        | ORDER BY school_id,c_code
        |""".stripMargin)

    // TODO 将结果写入数据库
    writeToDB(deMC,"college_major_rating",db)
  }

}
