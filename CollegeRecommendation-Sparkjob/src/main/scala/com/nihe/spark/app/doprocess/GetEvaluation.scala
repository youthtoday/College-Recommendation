package com.nihe.spark.app.doprocess

import com.nihe.spark.app.function.ResultEvaluation.evaluation
import com.nihe.spark.common.GetMatrix.{getCorrFromDB, getRatingMatrix}
import com.nihe.spark.common.GetSchema.getSchemaEva
import com.nihe.spark.service.impl.DataWriterImpl.writeToDB
import com.nihe.spark.service.impl.SparkServiceImpl.getSparkSession
import org.apache.spark.sql.{Row, SparkSession}

import java.sql.Connection
import scala.collection.mutable.ListBuffer

/** Created on 2022/5/9 16:05
 * 评估学校相似度
 * @version 1.0
 * @author nihe
 */
object GetEvaluation {
  val methodList = List("pearson", "spearman", "ac", "chebyshev", "cosine", "euclidean", "IPWR1", "itr", "manhattan", "quasi","ier")
  val neighbourNumList = List(1, 2, 3, 5, 10, 15, 20, 35, 50)

  /**
   * 从新生成的数据库中取值评估计算
   * @param db 数据库值
   */
  def getEvaluation(spark:SparkSession,db:String): Unit = {

    // TODO 从数据库中生成评分矩阵
    val ratingMatrixMap = getRatingMatrix(db)

    // TODO 每行生成不同近邻数,不同方法对应评估分数
    val rowList: ListBuffer[Row] = ListBuffer()
    for (i <- methodList) {
      for (j <- neighbourNumList) {
        rowList.append(Row.merge(Row(i, j), Row.fromSeq(evaluation(ratingMatrixMap, getCorrFromDB(i, j,db)))))
      }
    }

    // TODO 生成RDD
    val corrRDD = spark.sparkContext.makeRDD(rowList.toList)

    // TODO 获取Schema生成DataFrame,NaN值设为10000
    val evaSchema = getSchemaEva(methodList)
    val corrDataFrame = spark.createDataFrame(corrRDD, evaSchema).na.fill(100000) //f1有问题

    // TODO 存入数据库
    writeToDB(corrDataFrame,"evaluation",db)

  }

}
