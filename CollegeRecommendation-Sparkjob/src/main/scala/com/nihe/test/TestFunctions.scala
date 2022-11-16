package com.nihe.test



/** Created on 2022/4/29 19:57
 *
 * @version 1.0
 * @author nihe
 */
object TestFunctions {
  def main(args: Array[String]): Unit = {
    val list1 = List(1,  3, 4, 6, 7, 8)
    val list2 = List(8, 2, 3, 5)
    val list = list1 intersect Nil
    println(list)
  }
}
