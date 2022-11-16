package com.nihe.spark.app.doprocess

import com.nihe.spark.app.function.Order.ordering
import com.nihe.spark.common.GetMatrix.getRatingWithClassMatrix
import com.nihe.spark.common.GetSchema.getSchemaSM
import com.nihe.spark.service.impl.DataReaderImpl.readFromDB
import com.nihe.spark.service.impl.DataWriterImpl.writeToDB
import com.nihe.spark.service.impl.SparkServiceImpl.getSparkSession
import org.apache.spark.sql.{Row, SparkSession}

import scala.collection.mutable.ListBuffer

/** Created on 2022/5/12 22:49
 * 计算专业推荐
 *
 * @version 1.0
 * @author nihe
 */
object GetRec {
  val method = "ier" //使用ier算法计算相似度
  val neighbourNum = 20

  /**
   * 计算专业推荐
   * @param db 结果写入新生成的结果数据库中
   */
  def getRec(spark:SparkSession,db:String): Unit = {
    // TODO 将460个学校的前20相似学校school_id读出到Map
    val neighbourSchoolsDF = readFromDB(spark, s"name_$method", db)
    val neighbourSchool = neighbourSchoolsDF.rdd.collect().map(d => {
      d.toSeq.toList
    }).map(d => {
      (d.head.toString.toInt, d.tail.map(_.toString.toInt).take(neighbourNum)) //只取20
    }).toMap

    // TODO 从数据库生成评分矩阵（带有c_id）
    val ratingMatrixWithCID = getRatingWithClassMatrix(db)
    val in = ratingMatrixWithCID.map(d => {
      (d._1, d._2.toMap)
    })
    // TODO 从数据库生成专业关联规则
    val FP = readFromDB(spark, "FPGrowth", db)
    val FPMap = FP.rdd.collect().map(r => { //Map[class_id, Map[mcode, Array[mcode]]]
      (r(0).toString.toInt, (r(1).toString, r(2).toString))
    }).groupBy(d => {
      d._1
    }).map(kv => {
      (kv._1, kv._2.map(d => {
        d._2
      }))
    }).map(kv => {
      (kv._1, kv._2.map(ord => {
        (ord._1, ord._2.split(','))
      }).toMap)
    })


    // TODO 生成mcodeSetMap: Map[school_id, Map[class_id, Array[每个大学对应开设专业列表]]
    val mcodeSet = readFromDB(spark, "mcodeSet", db)
    val mcodeSetMap = mcodeSet.rdd.collect().map(r => { //Map[school_id, Map[class_id, Array[,_code]]]
      (r(0).toString.toInt, r(1).toString.toInt, r(2).toString)
    }).groupBy(_._1).map(tuples => {
      (tuples._1, tuples._2.groupBy(_._2).map(d => {
        (d._1, d._2.flatMap(f => f._3.split(",")))
      }))
    })


    // TODO 计算学校秩序相似度,相似度按照取neighbour数的倒数为分隔递减
    val schoolRank = neighbourSchool.map(schoolkv => {
      var i = 0.0
      (schoolkv._1, schoolkv._2.map(d => {
        val rtn = (d, 1 - i / schoolkv._2.size)
        i += 1
        rtn
      }).toMap)
    })

    // TODO 计算每个学校下相似学校和目标学校的每个专业分数差值
    val diffRatingMap = neighbourSchool.map(schoolkv => {
      // kv是(目标学校,List[相似学校])
      (schoolkv._1, schoolkv._2.map(sid => {
        //kv._1是目标学校的id,sid是相似学校的id,计算每个相似学学校和每个专业对目标学校的专业的分数差的倒数
        (sid, ratingMatrixWithCID(sid).zip(ratingMatrixWithCID(schoolkv._1)).map(tuples => {
          (tuples._1._1, 1 / (math.abs(tuples._1._2 - tuples._2._2) + 1))
        }))
      }).flatMap(kv => { //　TODO 矩阵变换,转成class_id为Key的Map
        kv._2.map(d => {
          (kv._1, d._1, d._2)
        })
      }).groupBy(d => d._2).map(kv => {
        (kv._1, kv._2.map(d => {
          (d._1, d._3)
        }))
      }))
    })


    // TODO 对每个学校下对应的每个专业下对应的相似学校给予秩顺序值
    //  同差值的学校相似度高的给的秩顺序值更高kv(学校id,学校对应的对应每个专业下相似学校的分数差集合)
    val classRank = diffRatingMap.map(schoolkv => {
      // TODO 对每个专业操作
      (schoolkv._1, schoolkv._2.map(ratingList => {
        (ratingList._1, ordering(ratingList._2)) //使用ordering排序
      }))
    })
    // TODO 对每个学校计算学校相似度和相似度的加权值,获得getherMap
    //  schoolkv是(学校id,对每个专业下与其相似学校的专业分数秩序值map的map)
    //  classMap(专业id,相似学校的专业分数秩序值Map)
    //  sidRank是学校,秩序值元组
    val gatherMap = classRank.map(schoolkv => {
      (schoolkv._1, schoolkv._2.map(classMap => {
        (classMap._1, classMap._2.map(
          sidRank => { //d._1目标学校id
            (sidRank._1, sidRank._2 * schoolRank(schoolkv._1)(sidRank._1)) // TODO 对结果值按照秩序值排序,只返回学校顺序
          }).toList.sortWith(_._2 > _._2).map(d => {
          d._1
        }))
      }))
    })


    // TODO 得到最终推荐结果矩阵

    val recROWList: ListBuffer[Row] = ListBuffer()
    mcodeSetMap.map(sidMap => { // TODO 从mcodeset取值保证不会有未开设情况
      sidMap._2.map(classMap => {
        classMap._2.map(mcode => {
          if (in.contains(sidMap._1) && in(sidMap._1).contains(classMap._1)) {
            val rec = gatherMap(sidMap._1)(classMap._1).map(sid => { // TODO 取得顺序相似学校id
              sid + ":" + (FPMap(classMap._1)(mcode) intersect (
                if (mcodeSetMap.contains(sid)&&mcodeSetMap(sid).contains(classMap._1))
                  mcodeSetMap(sid)(classMap._1)
                else
                  Nil)).mkString(",")
            }).mkString("-")
            recROWList.append(Row(sidMap._1, classMap._1, mcode, rec))
          }
        })
      })
    })

    // TODO 生成ROW格式并转换为RDD

    val confRDD = spark.sparkContext.makeRDD(recROWList.toList)
    val confDF = spark.createDataFrame(confRDD, getSchemaSM)
    writeToDB(confDF, "SMRecommendation", db)
  }
}
