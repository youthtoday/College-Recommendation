package com.nihe.spark.common

import org.apache.spark.sql.types.{DoubleType, IntegerType, StringType, StructField, StructType}

import scala.collection.mutable.ListBuffer

/** Created on 2022/5/9 16:50
 *
 * 从object里生成各种schema用以从rdd生成dataframe
 *
 * @version 1.0
 * @author nihe
 */
object GetSchema {
  /**
   * 获得学校-相似学校名称存储矩阵的schema
   *
   * @param neighbourNum 近邻数
   * @return 返回schema
   */
  def getSchemaName(neighbourNum: Int): StructType = { //生成存储相似大学school_id的表头
    val struct = ListBuffer(StructField("school_id", IntegerType))
    for (i <- 1 until neighbourNum) {
      struct.append(StructField(s"sim$i", IntegerType))
    }
    StructType(struct.toList)
  }

  /**
   * 获得学校-相似学校相似度存储矩阵的schema
   *
   * @param neighbourNum 近邻数
   * @return 返回schema
   */
  def getSchemaCorr(neighbourNum: Int): StructType = { //生成存储相似大学sim值的表头
    val struct = ListBuffer(StructField("school_id", IntegerType))
    for (i <- 1 until neighbourNum) {
      struct.append(StructField(s"sim$i", DoubleType))
    }
    StructType(struct.toList)
  }

  /**
   * 生成评估schema
   *
   * @param evaList 评估方法列表,其中包含 mae rmse r_squared p r f1
   * @return 返回schema
   */
  def getSchemaEva(evaList: List[String]): StructType = {
    StructType(List(
      StructField("method", StringType),
      StructField("neighbour_num", IntegerType)) ::: evaList.map(str => {
      StructField(str, DoubleType)
    }))
  }

  /**
   * 生成置信度schema
   * @return 返回schema
   */
  def getSchemaConfidence:StructType={
    StructType(List(
      StructField("class_id",IntegerType),
      StructField("mcode",StringType),
      StructField("confidence",StringType)
    ))
  }

  /**
   * 生成存储推荐大学及专业的表头
   * @param recNum 推荐学校的数量
   * @return 返回schema
   */
  def getSchemaSM: StructType = {
    val struct = ListBuffer(
      StructField("school_id", IntegerType),
      StructField("class_id", IntegerType),
      StructField("mcode", StringType),
      StructField("recommend", StringType))
    StructType(struct.toList)
  }

}
