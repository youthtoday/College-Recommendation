package com.nihe.spark.service.impl

import com.nihe.spark.service.MysqlService
import com.nihe.spark.util.GetProperties.getProps

import java.sql.{Connection, DriverManager, ResultSet}

/** Created on 2022/4/29 19:50
 *
 * @version 1.0
 * @author nihe
 */
object MysqlServiceImpl extends MysqlService {
  /**
   * 获取数据库连接
   * @param dbName 数据库的名称
   * @param dbType 数据库的类型
   * @return db连接
   */
  override def getDBConnect(dbName: String= "test1", dbType:String="mysql"): Connection = {
    // TODO 使用properties配置数据库连接
    val props = getProps(dbType)//使用util包中的方法获取properties内容
    Class.forName(props.getProperty("driverClassName"))
    DriverManager.getConnection(
      props.getProperty("url")+dbName+"?useSSL=false&useUnicode=true&characterEncoding=utf-8",
      props.getProperty("username"),
      props.getProperty("password")
    )
  }

  /**
   * 从数据库获取结果
   * @param sql sql语句
   * @param conn 数据库连接
   * @return 查询结果
   */
  override def queryFromDB(sql: String, conn: Connection): ResultSet = {
    // TODO 查询数据库获得结果集
    val preparedStatement = conn.prepareStatement(sql)
    preparedStatement.executeQuery()
  }
}
