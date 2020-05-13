package org.ActionRules

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext


object Main {
  def main(args : Array[String]) : Unit = {
    /****************Delete following lines while running in a cluster******************/
//    val commonStrings = new StringsDAO()
    System.setProperty("hadoop.home.dir", "H:\\Hadoop\\hadoop-common-2.2.0-bin-master\\")
    
    val conf = new SparkConf().setAppName("SparkAction").setMaster("local")
    val sc = new SparkContext(conf)
    
    val performLERS = new LERS(args(0),args(2),args(1),args(3),sc)
//    val readFiles = new FileInputs("attribute.txt","data.txt",sc)
//    readFiles.printAttributes()
//    println("Data Read:")
//    val dataLines = sc.textFile("data.txt")
//    
//    dataLines.foreach { x => println(x) }
  }
}