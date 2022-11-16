package com.nihe.spark.util

import com.nihe.spark.service.impl.SparkServiceImpl.getClass

import java.util.Properties

/** Created on 2022/5/1 11:59
 *
 * @version 1.0
 * @author nihe
 */
object GetProperties {
  /**
   * 从maven项目的resource文件目录下读取properties文件配置
   * @param name 文件名
   * @return
   */
  def getProps(name:String): Properties ={
    val props = new Properties
    props.load(getClass
      .getClassLoader
      .getResourceAsStream(name + ".properties"))
    props
  }
}
