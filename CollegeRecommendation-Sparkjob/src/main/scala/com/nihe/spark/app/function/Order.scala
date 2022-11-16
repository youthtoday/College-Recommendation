package com.nihe.spark.app.function

import org.apache.spark.sql.Row

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Order {
  /**
   * 结合专业关联度和学校相似度对学校进行排序，同秩序值择学校相似度高的排在前面，此处算法是计算学校结合专业的新秩序值
   * @param inList List(学校ID,秩序值)
   * @return 排序好的Map
   */
  def ordering(inList:List[(Int, Double)]): mutable.Map[Int, Double] ={
    val simList = inList.sortWith(_._2>_._2)
    val length = simList.size
    val rank:Array[Double] = Array.range(1,length+1 ).map(d => d.toDouble).reverse
    var left = 0
    var right = 1
    while (right < simList.length){
      if (simList(left)==simList(right)){
        right += 1
      }else {
        val intro = (rank(left)+rank(right-1))/2
        for (i <- left until right){
          rank(i) = intro
        }
        right += 1
        left = right-1
      }
    }
    val rankedMap =mutable.Map[Int, Double]()
    for(i <- simList.indices){
      rankedMap.put(simList(i)._1, rank(i)/length)
    }
    rankedMap
  }

}
