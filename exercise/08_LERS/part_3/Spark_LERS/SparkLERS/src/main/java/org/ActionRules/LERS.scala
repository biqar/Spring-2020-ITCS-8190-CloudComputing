package org.ActionRules

import org.apache.spark.SparkContext
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await



class LERS (attributesFilePath : String, dataFilePath : String, 
    parametersFilePath : String,outputPath : String, sc : SparkContext) extends java.io.Serializable{
  
  var attributeNames : Array[String] = null 
  var stableAttributes : Array[String] = null
  
  
  
  /*
   * Reads attribute names from a file
   * Works if attributes are given in a single line with "TAB" delimeter
   * Also work if each attribute is given in separate lines
   * */
  val attributeFileLines = sc.textFile(attributesFilePath)
  
  
  attributeNames = {
    if(attributeFileLines.count() == 1){
      attributeFileLines.flatMap { line => line.split("\t") }
    }else{
      attributeFileLines.map { line => line }
    }
  }.collect()
  
//  println("Attributes SIZE: " + attributeNames.length)
    
  /*
   * Broadcasting the read attributes
   * So that it can accessed in all executors
   * */
  val attributeBroadcast = sc.broadcast(attributeNames)
  
  
  
  /*
   * Reading parameter names from a file
   * Works if attributes are given in a single line with "TAB" delimeter
   * Also works if each attribute is given in separate lines
   * */
  val parameterFileLines = sc.textFile(parametersFilePath,1)
  val parameters = parameterFileLines.map { x => x.split("\t") }.collect()
  
  println(parameters.length)
  
  val parameterBroadcast = sc.broadcast(parameters)
  
  
  /*
   * Reading a data file and caching it
   * */
  val dataFileLines = sc.textFile(dataFilePath).cache()

  
  /*
   * MapPartition works on each partition of data
   * So inside mapPartition, both LERS and ARoGS algorithms are implemented
   * */
  var mainRDD = dataFileLines.mapPartitions(partition => 
    {
      
      //accessing broadcasted attribute names and parameters
      val attributes = attributeBroadcast.value
      val parameters = parameterBroadcast.value
      
      //separating stable attributes and decision attributes from other parameters
      val stableAttributes = parameters(0)
      val decisionAttribute = parameters(1)(0)
      
      val decisionFrom = decisionAttribute + "_" + parameters(2)(0)
      val decisionTo = decisionAttribute + "_" + parameters(2)(1)
      
      //Getting minimum support and minimum confidence set in the parameters
      val userSupport = parameters(3)(0).toInt
      val userConfidence = parameters(3)(1).toDouble
      
      //Set storing distinct stable attribute values
      val distinctStableAttributeValues : collection.mutable.Set[String] = collection.mutable.Set[String]()
      
      //This map is to store on which lines the attribute value is present
      var attributeValues : collection.mutable.Map[scala.collection.Set[String],scala.collection.Set[String]]
                                    = collection.mutable.Map() 
      //This map stores attribute names as "Key" and their distinct values as "Value"
      var distinctAttributeValues : collection.mutable.Map[String,scala.collection.Set[String]] 
                                    = collection.mutable.Map()
      //This map stores distinct decision attribute values as "Keys" and their support as "Values"
      var decisionAttributeValues : Map[String,Set[String]]
                                    = Map()
                                    
      var fullDataSupport : collection.mutable.Map[List[String],Int]
                                    = collection.mutable.Map()
      
       /*
       * Reads data from the current partition
       * Splits both "," and "tab" separated data
       * and filtering out the line if the data count is greater than attributes
       * */
      var splitData = partition.map { dataLine => dataLine.split("\t|,").toList }
                               .filterNot { x => x.length<attributes.length 
//                                 || x.contains("?") || x.contains("") //Code is modified to accept empty values in the data
                                 }
                               
      var lineIndex = 0
      
      splitData.foreach { list => {
              
          lineIndex += 1
          var tempList = new ListBuffer[String]()
                    
          /*
           * For loop to fill Distinct Attribute Values Map,
           * which stores attributeName as "key" and distinctAttributeValues as "value"
           * */
          
          for(i <- 0 until list.length){
            if(list(i).nonEmpty && !list(i).equals("?")){
              var currentAttribute : String = attributes(i)
          
              
              /*
               * Attribute Name is added to each individual attribute value in the form of: attributeName_attributeValue
               * Because this will be useful when storing support of each value
               * */
              var modifiedData = currentAttribute + "_" + list(i)
                       
              tempList += modifiedData
              var currentAttributeValues = scala.collection.Set[String](modifiedData) 
              
              // Adding a new member to the existing list
              if(distinctAttributeValues.contains(currentAttribute)){
                currentAttributeValues = currentAttributeValues ++ distinctAttributeValues.apply(currentAttribute)
              }
              
              //Scala method of adding elements into a map
              distinctAttributeValues += (currentAttribute -> currentAttributeValues)
            }
          }
      
          
           /*
           * This module is used to store support of each data line in fullDataSupport Map
           * By having this map, duplicates can be filtered out
           * And we have support of each data line including duplicates
           * */
          var finalList = tempList.toList
          if(fullDataSupport.contains(finalList)){
            fullDataSupport += (finalList -> (fullDataSupport.apply(finalList)+1))          
          } else{
            fullDataSupport += (finalList -> 1)  
            fillAttributeValues(attributeValues,tempList,lineIndex)  
          }
          
      } }

      
      /*
       * Setting distinct stable attribute values
       * */
      stableAttributes.foreach { stableAttribute =>
        {
          if(stableAttribute.nonEmpty)
            distinctStableAttributeValues ++= distinctAttributeValues(stableAttribute)
        } 
      }
            
      
      /*
       * Setting distinct attribute value map
       * */ 
      distinctAttributeValues(decisionAttribute).foreach { decisionValue => {
        var decisionValueList = scala.collection.mutable.Set(decisionValue)
        decisionAttributeValues += (decisionValue -> attributeValues(decisionValueList).toSet)
      
        attributeValues.remove(decisionValueList)
      } }

      
      /*
       * Module to find support of an action rule
       * */
      def calculateSupport(checkThis : List[String]): Int = {
        var support : Int = 0
        
        fullDataSupport.foreach(data => {
          if(checkThis.forall(data._1.contains)){
            support += data._2
          }
        })    
        
        support
      }
      
      
      /*
       * Module to find confidence of an action rule
       * */
      def calculateConfidence(checkThis : List[String]): Double = {
        var conf : Double = 0.00
        
        var numSupport = calculateSupport(checkThis.toList)
        var denSupport = calculateSupport((checkThis.toSet.dropRight(1)).toList)
        
        if(denSupport != 0)
          conf = numSupport.toDouble/denSupport.toDouble
          
        conf
      }
     
      
      
      var certainRules : collection.mutable.Map[String, String] = 
          performLERS(attributeValues,decisionAttributeValues,distinctStableAttributeValues,decisionFrom,decisionTo,userSupport)           

      certainRules.toIterator      
    }  
    
  )
  
  mainRDD.groupByKey(1).map(actionRule => {
    val key = actionRule._1
    var value = ""
    
    actionRule._2.foreach { decision => {
      value = decision
    } }
    
    key + " ==> " + value
  })
  .saveAsTextFile(outputPath)

  def fillAttributeValues(attributeValues : collection.mutable.Map[scala.collection.Set[String],scala.collection.Set[String]], 
      list : ListBuffer[String] , lineIndex : Int) : Unit = {
    list.foreach { value => {
      var tempList : scala.collection.Set[String] = Set(value)
      
      var tempSet : scala.collection.Set[String] = Set("x" + lineIndex)
      
      if(attributeValues.contains(tempList)){
        tempSet = tempSet ++ attributeValues.apply(tempList)     
      }
      
      attributeValues += (tempList -> tempSet)
    } }
    
  }
  
  
  
  /*
   * This function produces all possible certain rules and possible rules
   * */
  def performLERS(attributeValues : collection.mutable.Map[scala.collection.Set[String],scala.collection.Set[String]],
                  decisionAttributeValues : Map[String,Set[String]], distinctStableAttributeValues : collection.mutable.Set[String], 
                  decisionFrom: String, decisionTo: String, userSupport : Int)
                : collection.mutable.Map[String, String] = {
    var loopCount = 0
    
    var localAttributeValues = attributeValues.clone()
    
    /*
     * Maps to store certain rules in the format - 
     * List of attribute values as "Key" and decision attribute as "Value"
     */
    var certainRules : collection.mutable.Map[String, String] = collection.mutable.Map()
    
    var mainAttrValKeys = attributeValues.keySet.toList
    
    /*
     * The following function acts as a recursive function like
     * It takes each attributeValue support(AVS) and checks against decisionAttributeValue support(DAVS)
     * IF AVS is a subset of DAVS, the attributeValue key and decisionAttributeValue key will be added into certainRules
     * */
    while(!localAttributeValues.isEmpty){
    
      localAttributeValues.map(attributeValue => {  
        var attributeValueKey = attributeValue._1.toList
        
        /*
         * To use break in scala:
         * Use import scala.util.control.Breaks._
         * And put loop to be breaked into breakable()
         */
        breakable( decisionAttributeValues.foreach(decisionAttributeValue => {
          
          
          /*
           * 
           * 
           * Old version module
           * 
           * 
           * */
          /*
           * Reducing the number of certain rules
           * Following first two conditions checks if the decision value is either decisionFrom or decisionTo
           * In this way, we will get only the certain rules pointing to decisionFrom or decisionTo
           * Other certain rules can be eliminated
           * */
          
//          if((decisionAttributeValue._1.equals(decisionFrom) || decisionAttributeValue._1.equals(decisionTo))
//              && attributeValue._2.subsetOf(decisionAttributeValue._2) ){
            
          
          
          
          /*
           * 
           * 
           * New Version if condition
           * 
           * 
           * */
          /*
           * Following module reduces certain rules in different method
           * if condition only checks if the attributeValue is a subset of decisionAttributeValue, 
           * 						no matter what the decisionAttribute is 
           * By this method, attributeValues matching decisionAttributeValues other than decisionFrom and decisionTo 
           * 						will enter the module but will be removed in later steps
           * If this method is not performed, the attributeValue will not be removed and goes to combine with other attributeValues  
           * Inside this module, certain rules containing only decisionTo value is selected
           * */
       
//          if(attributeValue._2.size < userSupport)
//            localAttributeValues.remove(attributeValueKey.toSet)
//          else 
//          println("Checking " + attributeValue._1.toString() + " - " + attributeValue._2.toString() + " with " +
//              decisionAttributeValue._1 + " - " + decisionAttributeValue._2 + " : " + attributeValue._2.subsetOf(decisionAttributeValue._2))
            if(attributeValue._2.subsetOf(decisionAttributeValue._2) ){
          
            /*
             * Following module checks if certain rules is a subset of another rule
             * AttributeValue entering this module will be removed
             * 
             * -----------------> This is expensive <-------------------
             * ------------> Find efficient method in future <---------- 
             * */
            var certainRuleCount : Int = 0
            breakable( certainRules.foreach(rule => {
              
              if(rule._1.forall(attributeValueKey.contains)){
                
                localAttributeValues.remove(attributeValueKey.toSet)
                break
              }
              else certainRuleCount += 1
            }) )
            
            
            if(certainRuleCount == certainRules.size && decisionAttributeValue._1.equals(decisionTo)){ 
              /*
               * This if-condition check if a certain rule contains only stable attributes
               * Action rules cannot be formed if there are only stable attributes
               * Flexible attributes are mandatory for action rules
               */
              if (!attributeValueKey.forall (distinctStableAttributeValues.contains )){
                  certainRules += (attributeValueKey.toSet.mkString(" & ") -> decisionAttributeValue._1)
                  
              }
              
            }
            
            localAttributeValues.remove(attributeValueKey.toSet)
            break
          }
        }))
        
      })
      
      
      var reducedAttributeValues : collection.mutable.Map[scala.collection.Set[String],scala.collection.Set[String]]
                                  = collection.mutable.Map() 
      var attributeValuesKeys = localAttributeValues.keySet.toList

      
      for(i <- 0 until attributeValuesKeys.length){

        for(j <- 0 until mainAttrValKeys.length){
            var attributeValuesKeysFirst = attributeValuesKeys(i)
             var attributeValuesKeysSecond = mainAttrValKeys(j)
                    
             var commonValues = localAttributeValues.apply(attributeValuesKeysFirst).
                                   intersect(attributeValues.apply(attributeValuesKeysSecond))            
               
              if(!commonValues.isEmpty){
                var combinedValues = (attributeValuesKeysFirst.toSet.union(attributeValuesKeysSecond.toSet)).toList.sorted
                
                if(combinedValues.size == attributeValuesKeysFirst.size + 1 && 
                    !reducedAttributeValues.contains(combinedValues.toSet)){
                  reducedAttributeValues += (combinedValues.toSet  -> commonValues)
                  
                  
                }
              }
          
        }
        
      }
      
      localAttributeValues.clear()
      localAttributeValues = reducedAttributeValues.clone()
      reducedAttributeValues.clear()
      
    }
    certainRules
  }
  
  def containsSameGroupValues(combinedValues : Set[String]) : Boolean = {
    var attributeNames : ListBuffer[String] = ListBuffer[String]()
    var result = false
    
    breakable(
      combinedValues.foreach { value => {
        var currentAttribute = value.split("_")(0)
        
        if(attributeNames.contains(currentAttribute)){
          result = true
          break
        }
        else attributeNames += currentAttribute
      } }
          
    )
    
    result
  }
 
}