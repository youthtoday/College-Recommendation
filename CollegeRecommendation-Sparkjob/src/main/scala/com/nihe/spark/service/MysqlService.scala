package com.nihe.spark.service

import java.sql.{Connection, ResultSet}

/** Created on 2022/4/29 19:49
 *
 * @version 1.0
 * @author nihe
 */
trait MysqlService {
  //获取数据库连接
  def getDBConnect(dbName:String,dbType:String):Connection
  //从数据库查询结果集
  def queryFromDB(sql:String,conn:Connection):ResultSet
}
