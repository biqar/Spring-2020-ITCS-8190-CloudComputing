package utils

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import java.io.FileNotFoundException
import org.apache.hadoop.mapred.InvalidInputException

class FileInputs(attributesFilePath : String, dataFilePath : String, sc : SparkContext) {
  
  val commonStrings = new StringsDAO()
  val commonFunctions = new CommonFunctions()
  
  var attributeNames : Array[String] = null
  
  //This map is to store on which lines the attribute value is present
  var attributeValues : collection.mutable.Map[ListBuffer[String],scala.collection.Set[String]]
                                    = collection.mutable.Map() 
  //This map stores attribute names as "Key" and their distinct values as "Value"
  var distinctAttributeValues : collection.mutable.Map[String,scala.collection.Set[String]] 
                                    = collection.mutable.Map()
  var fullDataSupport : collection.mutable.Map[List[String],Int]
                                    = collection.mutable.Map()
  
  try{
    val attributeFileLines = sc.textFile(attributesFilePath) 
    
    /*
     * Reads attribute names from a file
     * Works if attributes are given in a single line with " " delimeter
     * Also works if each attribute is given in separate lines
     * */
    attributeNames = {
      if(attributeFileLines.count() == 1){
        attributeFileLines.flatMap { line => line.split(" ") }
      }else{
        attributeFileLines.map { line => line }
      }
    }.collect()
    
    val attributeBroadcast = sc.broadcast(attributeNames)

  } catch{
    case a : InvalidInputException => {
      println(commonStrings.ATTRIBUTES_FILE_NOT_FOUND)
    }
  } 
//  finally{
//    try{
//      val dataFileLines = sc.textFile(dataFilePath)
//      
//      /*
//       * Reads data from a data files
//       * Splits both "," and "tab" separated data
//       * */
//      val splitData = dataFileLines.map { line => line.split("\t|,").toList }.collect().toList
//      var lineIndex : Int = 0
//      
//      splitData.foreach { list => {
//        if(!commonFunctions.checkPresenceOfEmptyValues(list)){
//          lineIndex += 1
//          
//          var tempList = new ListBuffer[String]()
//                    
//          /*
//           * For loop to fill Distinct Attribute Values Map,
//           * which stores attributeName as "key" and distinctAttributeValues as "value"
//           * */
//          for(i <- 0 until list.length){
//            var currentAttribute : String = attributeNames(i)
//        
//            /*
//             * Attribute Name is added to each individual attribute value
//             * Because this will be useful when storing support of each value
//             * */
//            tempList += (currentAttribute + "_" + list(i))
//            var currentAttributeValues = scala.collection.Set[String](currentAttribute + "_" + list(i)) 
//            
//            if(distinctAttributeValues.contains(currentAttribute)){
//              currentAttributeValues = currentAttributeValues ++ distinctAttributeValues.apply(currentAttribute)
//            }
//            
//            //Scala method of adding elements into a map
//            distinctAttributeValues += (currentAttribute -> currentAttributeValues)
//          }
//          
//          /*
//           * This module is used to store support of each data line in fullDataSupport Map
//           * By having this map, duplicates can be filtered out
//           * And we have support of each data line including duplicates
//           * */
//          if(fullDataSupport.contains(list)){
//            fullDataSupport += (list -> (fullDataSupport.apply(list)+1))          
//          } else{
//            fullDataSupport += (list -> 1)  
//            fillAttributeValues(tempList,lineIndex)
//          }
//     
//        }
//      } }
//        
//    } catch{
//      case a : InvalidInputException => {
//        println(commonStrings.ATTRIBUTES_FILE_NOT_FOUND)
//      }
//    }
//   
//    attributeValues.foreach(f => println("Key: " + f._1 + " & Value: " + f._2))
//  }
  
  /*
   * fillAttributeValues() function is used to put support of each and every 
   * attribute values.
   * Example: 
   * */
  def fillAttributeValues(list : ListBuffer[String] , lineIndex : Int) : Unit = {
    list.foreach { value => {
      var tempList = new ListBuffer[String]()
      tempList += value
      
      var tempSet : scala.collection.Set[String] = Set("x" + lineIndex)
      
      if(attributeValues.contains(tempList)){
        tempSet = tempSet ++ attributeValues.apply(tempList)        
      }
      
      attributeValues += (tempList -> tempSet)
    } }
  }
}