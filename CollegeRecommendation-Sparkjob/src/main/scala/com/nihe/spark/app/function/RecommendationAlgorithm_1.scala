package com.nihe.spark.app.function

import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.rdd.RDD

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
object RecommendationAlgorithm_1 {

  /**
   * 生成一般相似算法的相似矩阵
   *
   * @param RDDMap       目标RDDMap
   * @param neighbourNum 相似矩阵每行长度
   * @param list         指定RDDMap中的特定行的列表
   * @param simFuncType  指定一般相似度算法
   * @return 返回相似度矩阵
   */
  def getCommonSim(RDDMap: mutable.Map[Int, RDD[Double]], neighbourNum: Int, list: List[Int], simFuncType: String): List[List[(Int, Double)]] = {
    // TODO 开始计算相似度矩阵
    list.map(i => {
      getSort(RDDMap.keys.map(j => {
        (j, commonSim(RDDMap(i), RDDMap(j), simFuncType))
      }).toList, neighbourNum)
    })
  }

  /**
   *
   * @param RDDMap 目标RDDMap
   * @param neighbourNum 相似矩阵每行长度
   * @param list 指定RDDMap中的特定行的列表
   * @param rddAvg RDD平均值
   * @param Alpha 参数α
   * @param Beta 参数β
   * @return 返回相似度矩阵
   */
  def getIPWRSim(RDDMap: mutable.Map[Int, RDD[Double]], neighbourNum: Int, list: List[Int], rddAvg: RDD[Double], Alpha: Double = 0.4, Beta: Double = 0.6): List[List[(Int, Double)]] = {
    // TODO 开始计算相似度矩阵
    list.map(i => {
      getSort(RDDMap.keys.map(j => {
        (j, IPWRSim(RDDMap(i), RDDMap(j), rddAvg, Alpha, Beta))
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
  def commonSim(rddX: RDD[Double], rddY: RDD[Double], simFuncType: String): Double = {
    val simFunc = simFuncType match {
      case "pearson" => pearsonSim(rddX, rddY)
      case "spearman" => spearmanSim(rddX, rddY)
      case "manhattan" => manhattanSim(rddX, rddY)
      case "euclidean" => euclideanSim(rddX, rddY)
      case "cosine" => cosineSim(rddX, rddY)
      case "quasi" => quasiSim(rddX, rddY)
      case "chebyshev" => chebyshevSim(rddX, rddY)
      case "itr" => itrSim(rddX, rddY)
      case "ac" => acSim(rddX, rddY)
      case "ier" => ierSim(rddX, rddY)
    }
    simFunc
  }

  /**
   * 通用相似度计算函数,仅计算两个RDD间的相关度
   *
   * @param rddX 目标rddX
   * @param rddY 目标rddY
   * @return 相似度
   */

  def pearsonSim(rddX: RDD[Double], rddY: RDD[Double]): Double =
    Statistics.corr(rddX, rddY, "pearson")

  def spearmanSim(rddX: RDD[Double], rddY: RDD[Double]): Double =
    Statistics.corr(rddX, rddY, "spearman")

  def manhattanSim(rddX: RDD[Double], rddY: RDD[Double]): Double =
    1 / (1 + rddX.zip(rddY).map(d => math.abs(d._1 - d._2)).sum)

  def euclideanSim(rddX: RDD[Double], rddY: RDD[Double]): Double =
    1 / (1 + math.sqrt(rddX.zip(rddY).map(d => math.pow(math.abs(d._1 - d._2), 2)).sum))

  def cosineSim(rddX: RDD[Double], rddY: RDD[Double]): Double = {
    val member = rddX.zip(rddY).map(d => d._1 * d._2).sum
    val temp1 = math.sqrt(rddX.map(d => {
      math.pow(d, 2)
    }).sum)
    val temp2 = math.sqrt(rddY.map(d => {
      math.pow(d, 2)
    }).sum)
    member / (temp1 * temp2)
  }

  def quasiSim(rddX: RDD[Double], rddY: RDD[Double]): Double = {
    val ratingLength = rddX.count() //rdd的长度
    ratingLength - rddX.zip(rddY).map(d => math.abs(d._1 - d._2) / 5).sum
  }

  def chebyshevSim(rddX: RDD[Double], rddY: RDD[Double]): Double = {
    1 / (1 + rddX.zip(rddY).map(d => math.abs(d._1 - d._2)).max)
  }

  def itrSim(rddX: RDD[Double], rddY: RDD[Double]): Double = {
    val ratingLength = rddX.count() //rdd的长度
    val rxAvg = rddX.sum / ratingLength //rddX的平均数
    val ryAvg = rddY.sum / ratingLength //rddY的平均数
    (1 - math.sqrt(rddX.zip(rddY).map(d => math.pow(d._1 - d._2, 2)).sum) /
      (math.sqrt(rddX.map(d => math.pow(d, 2)).sum) + math.sqrt(rddY.map(d => math.pow(d, 2)).sum))
      ) *
      (1 - 1 / (1 + math.exp(-math.abs(rddX.sum / 95 - rddY.sum / 95) *
        (math.sqrt(rddX.map(d => math.pow(d - rxAvg, 2)).sum / 95) -
          math.sqrt(rddY.map(d => math.pow(d - ryAvg, 2)).sum / 95)))))
  }

  def acSim(rddX: RDD[Double], rddY: RDD[Double]): Double = {
    rddX.zip(rddY).map(d => d._1 * d._2).sum /
      (math.sqrt(rddX.map(math.pow(_, 2)).sum) * math.sqrt(rddY.map(math.pow(_, 2)).sum)) *
      Statistics.corr(rddX, rddY, "pearson")
  }
  def ierSim(rddX:RDD[Double], rddY:RDD[Double]):Double={
    val xAvg = rddX.sum / 95
    val yAvg = rddY.sum / 95
    val temp1 = xAvg-yAvg
    val temp2 = math.sqrt(rddX.map(d => math.pow(d - xAvg, 2)).sum/95)-math.sqrt(rddY.map(d => math.pow(d - yAvg, 2)).sum / 95)
    val exp = math.exp(math.abs(temp1*temp2))
    val eu = 1 / (1 + math.sqrt(rddX.zip(rddY).map(d => math.pow(math.abs(d._1 - d._2), 2)).sum))
    val out = eu*(1/(1+exp))
    out
  }

  /**
   * 特殊相关度计算函数
   *
   * @param rddX   目标rddX
   * @param rddY   目标rddY
   * @param rddAvg rddX和rddY所在矩阵的所有rdd的每项平均值
   * @param Alpha  RPB的权重参数
   * @param Beta   IPCC相似度的权重
   * @return 相似度
   */

  def IPWRSim(rddX: RDD[Double], rddY: RDD[Double], rddAvg: RDD[Double], Alpha: Double, Beta: Double): Double = {
    // TODO 计算前期准备
    val ratingLength = rddX.count() //rdd的长度
    val rxAvg = rddX.sum / ratingLength //rddX的平均数
    val ryAvg = rddY.sum / ratingLength //rddY的平均数

    // TODO 分别计算rddX和rddY的SD,其目的是
    val SDX = math.sqrt(rddX.map(r => {
      math.pow(r - rxAvg, 2)
    }).sum() / ratingLength)
    val SDY = math.sqrt(rddY.map(r => {
      math.pow(r - ryAvg, 2)
    }).sum() / ratingLength)

    // TODO 计算RPB,其目的是
    val RPB = math.cos(math.abs(ryAvg - rxAvg) * math.abs(SDX - SDY))

    // TODO 计算IPCC,其目的是
    val rdd = rddX.zip(rddY).zip(rddAvg)
    // 公式上部分
    val U = rdd.map(d => {
      (d._1._1 * rxAvg - d._1._1 * d._2) * (d._1._2 * ryAvg - d._1._2 * d._2)
    }).sum()
    // 公式左下部分
    val DL = math.sqrt(rdd.map(d => {
      math.pow(d._1._1 * rxAvg - d._1._1 * d._2, 2)
    }).sum())
    // 公式右下部分
    val DR = math.sqrt(rdd.map(d => {
      math.pow(d._1._2 * ryAvg - d._1._2 * d._2, 2)
    }).sum())
    val IPCCSim = U / (DL * DR)

    // TODO 计算IPWR
    Alpha * RPB + Beta * IPCCSim
  }






}
