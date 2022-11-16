package com.nihe.spark.app.function

import com.nihe.spark.service.impl.DataReaderImpl.readFromDB
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.mutable

/** Created on 2022/4/29 19:55
 *
 * 具体算法公式详见:参考相似度以及评价指标.md.
 *
 * 本文件中共列出10种相似度计算方法,其中前9种可以有两个RDD直接计算结果.
 *
 * 最后一种需要RDD所在的全部数据的平均值生成RDD参与计算
 *
 * @version 1.0
 * @author nihe
 */
object RecommendationAlgorithm {

  /**
   * 生成一般相似算法的相似矩阵
   *
   * @param RDDMap       目标RDDMap
   * @param neighbourNum 相似矩阵每行长度
   * @param list         指定RDDMap中的特定行的列表
   * @param simFuncType  指定一般相似度算法
   * @return 返回相似度矩阵
   */
  def getCommonSim(RDDMap: mutable.Map[Int, List[Double]], neighbourNum: Int, list: List[Int],spark:SparkSession): List[List[(Int, Double)]] = {
    // TODO 开始计算相似度矩阵
    val all_college_major = readFromDB(spark, "4_all_college_major")
    list.map(i => {
      getSort(RDDMap.keys.map(j => {
        (j, commonSim(RDDMap(i), RDDMap(j)))
      }).toList, neighbourNum)
    })
  }

  /**
   * 对产生的List[(学校,相似度)]列表进行排序,选取出前几个返回
   *
   * @param RcmdList     (学校,相似度)列表
   * @param neighbourNum 返回列表长度
   * @return 排序后长度指定的列表
   */
  def getSort(RcmdList: List[(Int, Double)], neighbourNum: Int): List[(Int, Double)] = {
    RcmdList.sortWith(_._2 > _._2)
      .take(neighbourNum)
  }


  /**
   * 一般相似度进入方法
   *
   * @param rddX        目标rddX
   * @param rddY        目标rddY
   * @param simFuncType 选取的相似度方法
   * @return 相似度
   */
  def commonSim(rddX: List[Double], rddY: List[Double]): Double = {
    val xAvg = rddX.sum / 95
    val yAvg = rddY.sum / 95
    val temp1 = xAvg - yAvg
    val temp2 = math.sqrt(rddX.map(d => math.pow(d - xAvg, 2)).sum / 95) - math.sqrt(rddY.map(d => math.pow(d - yAvg, 2)).sum / 95)
    val exp = math.exp(math.abs(temp1 * temp2))
    val eu = 1 / (1 + math.sqrt(rddX.zip(rddY).map(d => math.pow(math.abs(d._1 - d._2), 2)).sum))
    val out = eu * (1 / (1 + exp))
    out

  }
}
