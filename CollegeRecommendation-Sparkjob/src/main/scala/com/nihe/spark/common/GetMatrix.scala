package com.nihe.spark.common

import com.nihe.spark.service.impl.MysqlServiceImpl.{getDBConnect, queryFromDB}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/** Created on 2022/5/10 14:09
 *
 * @version 1.0
 * @author nihe
 */
object GetMatrix {

  /**
   * 从数据库中生成评分矩阵
   *
   * @param conn 数据库连接
   */
  def getRatingMatrix(db:String): mutable.Map[Int, List[Double]] = {
    val conn = getDBConnect(db)
    val resultSet = queryFromDB(s"select * from $db.college_major_rating order by school_id,c_code", conn)
    val ratingMatrixMap: mutable.Map[Int, List[Double]] = mutable.Map() //存放矩阵，键是school_i
    // 在mysql存储数据时,选择的存储结构将学校-学科对应评分的二维矩阵扁平化存储成一维三列学校、学科、评分来存储
    // 故想恢复要读取生成map形式的矩阵
    var j = 0
    while (j < 460) {
      val tempList: ListBuffer[Double] = ListBuffer()
      j += 1
      var i = 0
      while (i < 95) {
        i += 1
        if (resultSet.next()) {
          tempList.append(resultSet.getDouble("rating"))
        }
      }
      val school_id = resultSet.getInt("school_id")
      ratingMatrixMap.put(school_id, tempList.toList)
    }
    ratingMatrixMap
  }

  /**
   * 从数据库读出学校相似度数据
   *
   * @param name         相似度方法名
   * @param neighbourNum 近邻数
   * @return 相似度矩阵
   */
  def getCorrFromDB(name: String, neighbourNum: Int,db:String): List[List[(Int, Double)]] = {
    val conn = getDBConnect(db)
    val nameSet = queryFromDB(s"SELECT * FROM $db.name_" + name + " ORDER BY school_id", conn)
    val corrSet = queryFromDB(s"SELECT * FROM $db.corr_" + name + " ORDER BY school_id", conn)
    val corrList: ListBuffer[List[(Int, Double)]] = ListBuffer() //存放矩阵，键是school_id
    while (nameSet.next() && corrSet.next()) {
      var i = 1
      val nameCorr: ListBuffer[(Int, Double)] = ListBuffer((nameSet.getInt("school_id"), 1))
      while (i <= neighbourNum) {
        nameCorr.append((nameSet.getInt("sim" + i)
          , corrSet.getDouble("sim" + i)))
        i += 1
      }
      corrList.append(nameCorr.toList)
    }
    corrList.toList
  }

  /**
   * 从数据库中生成评分矩阵（带有c_id）
   * k是s_id, v是List[c_id, rating]
   *
   * @return
   */
  def getRatingWithClassMatrix(db:String): mutable.Map[Int, List[(Int, Double)]] = {
    val conn = getDBConnect(db)
    val resultSet = queryFromDB(s"select * from $db.college_major_rating order by school_id,c_code", conn)
    val ratingMatrixMap: mutable.Map[Int, List[(Int, Double)]] = mutable.Map() //存放矩阵，键是school_i, 值是List[(c_id, rating)]
    // 在mysql存储数据时,选择的存储结构将学校-学科对应评分的二维矩阵扁平化存储成一维三列学校、学科、评分来存储
    // 故想恢复要读取生成map形式的矩阵
    var j = 0
    while (j < 460) {
      val tempList: ListBuffer[(Int, Double)] = ListBuffer()
      j += 1
      var i = 0
      while (i < 95) {
        i += 1
        if (resultSet.next()) {
          tempList.append((resultSet.getInt("c_code"), resultSet.getDouble("rating")))
        }
      }
      val school_id = resultSet.getInt("school_id")
      ratingMatrixMap.put(school_id, tempList.toList)
    }
    ratingMatrixMap
  }


}
