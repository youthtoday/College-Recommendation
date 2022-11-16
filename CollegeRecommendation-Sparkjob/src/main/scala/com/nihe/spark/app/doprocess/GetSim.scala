package com.nihe.spark.app.doprocess

import com.nihe.spark.app.function.RecommendationAlgorithm.getCommonSim
import com.nihe.spark.common.GetMatrix.getRatingMatrix
import com.nihe.spark.common.GetSchema.{getSchemaCorr, getSchemaName}
import com.nihe.spark.service.impl.DataWriterImpl.writeToDB
import com.nihe.spark.service.impl.SparkServiceImpl.getSparkSession
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}

import scala.collection.mutable

/** Created on 2022/5/7 22:49
 * 计算学校推荐
 * @version 1.0
 * @author nihe
 */
object GetSim {
  val neighbourNum = 21
  val funcList = List("ier")
  //"pearson", "spearman", "ac", "chebyshev", "cosine", "euclidean", "IPWR1", "itr", "manhattan", "quasi"

  /**
   * 计算学校推荐，结果存入新建db
   * @param db 新数据库
   */
  def getSim(spark:SparkSession,db:String): Unit = {

    // TODO 从数据库中生成评分矩阵
     val ratingMatrixMap = getRatingMatrix(db)

    // TODO 将评分矩阵内存评分元素的集合从Map[List]转换为Map[RDD],方便Spark Mllib计算
    val RDDMap = ratingMatrixMap

    // TODO 创建存储相似度矩阵的Schema,用以生成DataFrame,此处学校的名称和相似度分开存储成两个表

    val schemaName = getSchemaName(neighbourNum)
    val schemaCorr = getSchemaCorr(neighbourNum)

    // TODO 计算相似度矩阵并,存入数据库
    val keysList = ratingMatrixMap.keys.toList
    val sim = getCommonSim(RDDMap, neighbourNum, keysList,spark)
    println(s"Start storing  similarity...")
    // TODO 把sim转换成DataFrame格式便与存储
    val rowsName = spark.sparkContext.makeRDD(sim.map(d => {
      Row.fromSeq(d.map(d => {
        d._1
      }))
    }))
    val rowsCorr = spark.sparkContext.makeRDD(sim.map(d => {
      Row.fromSeq(d.head._1 +: d.map(d => {
        d._2
      }).tail)
    }))
    val nameDataFrame = spark.createDataFrame(rowsName, schemaName)
    val corrDataFrame = spark.createDataFrame(rowsCorr, schemaCorr)

    // TODO 存储结果
    val name = "name_ier"
    val corr = "corr_ier"
    writeToDB(nameDataFrame, name, db)
    writeToDB(corrDataFrame, corr, db)
    println(s" similarity is successfully stored in database.")

  }
}
