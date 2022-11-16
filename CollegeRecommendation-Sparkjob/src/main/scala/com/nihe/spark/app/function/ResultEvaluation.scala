package com.nihe.spark.app.function

import org.apache.spark.sql.Row

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/** Created on 2022/4/29 20:00
 *
 * @version 1.0
 * @author nihe
 */
object ResultEvaluation {
  /**
   * 评估函数
   *
   * @param matrix 传入的评分矩阵
   * @param sim    传入的相似度矩阵
   * @return 返回mae, rmse, r_squared, p, r, f1评估值
   */
  def evaluation(matrix: mutable.Map[Int, List[Double]], sim: List[List[(Int, Double)]]): List[Double] = {
    // TODO 获取相似矩阵大小
    val size = matrix.size

    // TODO 计算加权平均
    val mae = sim.map(d => {
      calMAE(matrix(d.head._1).toArray, getPred(d, matrix))
    }).sum / size
    val rmse = sim.map(d => {
      calRMSE(matrix(d.head._1).toArray, getPred(d, matrix))
    }).sum / size
    val r_squared = sim.map(d => {
      calRsquared(matrix(d.head._1).toArray, getPred(d, matrix))
    }).sum / size
    val p = sim.map(d => {
      calPRF1(matrix(d.head._1).toArray, getPred(d, matrix))._1
    }).sum / size
    val r = sim.map(d => {
      calPRF1(matrix(d.head._1).toArray, getPred(d, matrix))._2
    }).sum / size
    val f1 = sim.map(d => {
      calPRF1(matrix(d.head._1).toArray, getPred(d, matrix))._3
    }).sum / size

    List(mae, rmse, r_squared, p, r, f1)

  }

  /**
   * 从相似度最高的前几个学校里反向算出目标学校的学科预估评估分数，用于后面计算
   *
   * @param neighbours 选取近邻数
   * @param matrix     评分矩阵
   * @return
   */
  def getPred(neighbours: Seq[(Int, Double)], matrix: mutable.Map[Int, List[Double]]): Array[Double] = {
    val target = neighbours.head._1 //预测目标
    val subjectNum = matrix(matrix.keys.head).size //学科数
    //kv是后10个相似度元组,看kv._1是school_id,kv._2是相似度
    val pred: ListBuffer[Double] = ListBuffer()
    val sumSim = neighbours.tail.map(kv => kv._2).sum
    val neighbourMap = neighbours.tail.toMap
    //i是Matrix对应的每一个学科的评分
    var i = 0
    while (i < subjectNum) {
      //计算加权平均数
      pred.append(neighbourMap.keys.map(u => {
        (matrix(u)(i) - matrix(u).sum / subjectNum) * neighbourMap(u)
      }).sum / sumSim + matrix(target).sum / subjectNum)
      i += 1
    }
    pred.toArray
  }


  /**
   * MAE评估指标计算方法
   *
   * @param gt   学校真实评分列表
   * @param pred 预测学科分数列表
   * @return
   */
  def calMAE(gt: Array[Double], pred: Array[Double]): Double = {
    val error = gt.zip(pred).map(value => (value._1 - value._2).abs).sum
    error / gt.length
  }

  /**
   * RMSE评估指标计算方法
   *
   * @param gt   学校真实评分列表
   * @param pred 预测学科分数列表
   * @return
   */
  def calRMSE(gt: Array[Double], pred: Array[Double]): Double = {
    math.sqrt(gt.zip(pred).map(d => math.pow(d._1 - d._2, 2)).sum / gt.length)
  }

  /**
   * R_squared评估指标计算方法
   *
   * @param gt   学校真实评分列表
   * @param pred 预测学科分数列表
   * @return
   */
  def calRsquared(gt: Array[Double], pred: Array[Double]): Double = {
    val gtAvg = gt.sum / gt.size
    1 - gt.zip(pred).map(d => math.pow(d._1 - d._2, 2)).sum / gt.map(d => math.pow(d - gtAvg, 2)).sum
  }


  /**
   * P,R,F1评估指标计算方法
   *
   * @param gt   学校真实评分列表
   * @param pred 预测学科分数列表
   * @return
   */
  def calPRF1(gt: Array[Double], pred: Array[Double]): (Double, Double, Double) = {
    val th = 1 //设置阈值

    def calBiClass(gt: Array[Double], pred: Array[Double], th: Double) = {
      //tc是true class，是二分类中判断为true的类别；range是分段范围距离的一半
      val TP = gt.zip(pred).count(d => d._1 >= th & d._2 >= th)
      val FP = gt.zip(pred).count(d => d._1 < th & d._2 >= th)
      val FN = gt.zip(pred).count(d => d._1 < th & d._2 < th)
      val P = TP.toDouble / (TP + FP)
      val R = TP.toDouble / (TP + FN)
      val F1 = 2 * P * R / (P + R)
      (P, R, F1)
    }

    calBiClass(gt, pred, th)
  }


}
