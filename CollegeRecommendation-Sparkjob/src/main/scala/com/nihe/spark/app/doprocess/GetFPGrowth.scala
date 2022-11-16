package com.nihe.spark.app.doprocess

import com.nihe.spark.common.GetSchema.getSchemaConfidence
import com.nihe.spark.service.impl.DataReaderImpl.readFromDB
import com.nihe.spark.service.impl.DataWriterImpl.writeToDB
import com.nihe.spark.service.impl.SparkServiceImpl.getSparkSession
import org.apache.spark.sql.{Row, SparkSession}

import scala.collection.mutable.ListBuffer

/** Created on 2022/5/11 10:33
 * 挖掘学科关联
 *
 * @version 1.0
 * @author nihe
 */
object GetFPGrowth {

  /**
   * 挖掘学科关联
   *
   * @param db 结果存入db
   */
  def getFPGrowth(spark: SparkSession, sparkDB: String, db: String): Unit = {



    // TODO 读取数据库,将mcodeset读入mcodeSetMap
    val mcodeSetDF = readFromDB(spark, "mcodeSet", db)
    mcodeSetDF.createOrReplaceTempView("mcodeSet")
    val allMcode = spark.sql(
      s"""
         | SELECT
         | class_id, CONCAT_WS('-',COLLECT_SET(mcode_set))
         | AS mcode_set
         | FROM mcodeSet
         | GROUP BY class_id
         |""".stripMargin)

    // TODO 生成mcodeSetMap: Map[class_id, Array[每个大学对应开设专业列表]]
    val mcodeSetMap = allMcode.rdd.map(array => {
      (array(0).toString.toInt, array(1).toString)
    }).collect().map(kv => {
      (kv._1, kv._2.split("-").map(str => {
        str.split(",")
      }))
    }).toMap

    // TODO 读入所有学科数据,生成(class_id,WrappedArray(mcode))形式mcodeMap
    val classMajorDF = readFromDB(spark, "class_major", sparkDB)
    classMajorDF.createOrReplaceTempView("classMajor")
    val mCodeList = spark.sql(
      s"""
         | SELECT
         | class_id, CONCAT_WS(',',COLLECT_SET(mcode))
         | AS mcode_set
         | FROM classMajor
         | GROUP BY class_id
         |""".stripMargin)

    // TODO 生成mcodeMap: Map[class_id, 对应开设专业列表]
    val mcodeMap = mCodeList.rdd.map(array => {
      (array(0).toString.toInt, array(1).toString)
    }).collect().map(kv => {
      (kv._1, kv._2.split(","))
    }).toMap

    // TODO 生成mcode对
    val mcodePairMap = mcodeMap.map(kv => {
      val length = kv._2.length
      val mcodePairList: ListBuffer[(String, String)] = ListBuffer()
      var i = 0
      while (i < length) {
        var j = 0
        while (j < length) {
          mcodePairList.append((kv._2(i), kv._2(j)))
          j += 1
        }
        i += 1
      }
      (kv._1, mcodePairList)
    })

    // TODO 计算FP-growth的置信度

    // TODO 计算A类包含数量:Map[class_id, 对应开设下级学科在各个大学中的计数Map]
    //  其中至少开设一个学科,为0是数据错误,手动进行了更改
    val containA = mcodeSetMap.map(kv => {
      (kv._1, mcodeMap(kv._1).map(mcode => {
        var countA = mcodeSetMap(kv._1).count(_.contains(mcode)).toDouble
        if (countA == 0) countA = 1
        (mcode, countA)
      }).toMap)
    })

    // TODO 计算AB类包含数量:Map[class_id, 对应开设下级学科对在各个大学中的计数Map]
    val containAB = mcodeSetMap.map(kv => {
      (kv._1, mcodePairMap(kv._1).map(mcodePair => {
        (mcodePair, mcodeSetMap(kv._1).count(d => {
          d.contains(mcodePair._1) && d.contains(mcodePair._2)
        }).toDouble)
      }).toMap)
    })

    // TODO 计算置信度
    val confidenceSet = containAB.map(ABNumSet => {
      val mcodeMap = ABNumSet._2.map(mcodePair => {
        val conficence = mcodePair._2 / containA(ABNumSet._1)(mcodePair._1._1)
        (mcodePair._1._1, mcodePair._1._2, conficence)
      }).groupBy(_._1).map(d => {
        (d._1, d._2.toList.sortWith(_._3 > _._3).map(r => {
          r._2
        }).mkString(","))
      })
      (ABNumSet._1, mcodeMap)
    })

    // TODO 存成ROW格式并转换成RDD
    val rowList: ListBuffer[Row] = ListBuffer()
    for (elem <- confidenceSet) {
      for (mcodeA <- elem._2) {
        rowList.append(Row(elem._1, mcodeA._1, mcodeA._2))
      }
    }
    val confRDD = spark.sparkContext.makeRDD(rowList.toList)
    val confDF = spark.createDataFrame(confRDD, getSchemaConfidence) //调用GetSchema里方法
    writeToDB(confDF, "FPGrowth", db)
    println("complete FPGrowth")


  }
}
