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
      
//      distinctAttributeValues.foreach(attributeValue => {
//        println(attributeValue._1 + " - " + attributeValue._2)
//      })
      
      
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
      
      
      
      /*
       * Following module is to generate Action Schema
       * Single certain rule is taken and the corresponding action rule
       * ActionFrom and ActionTo values are also generated to calculate support and confidence of action rules
       * 
       * This function returns a map of type (ActionRule -> Array[ActionFrom,ActionTo]) 
       * */
      def generateActionRules(certainRules: collection.mutable.Map[List[String], String],
                              distinctStableAttributeValues : collection.mutable.Set[String],
                              decisionFrom: String, decisionTo: String) : Map[String,Array[ListBuffer[String]]] = {
        var decisionAttribute = decisionFrom.split("_")(0)
        var decisionFromValue = decisionFrom.split("_")(1)
        var decisionToValue = decisionTo.split("_")(1)
        
        var actionRules : Map[String,Array[ListBuffer[String]]] = Map[String,Array[ListBuffer[String]]]()
        
        var extraActionRules : Map[String,Array[ListBuffer[String]]] = 
                              Map[String,Array[ListBuffer[String]]]()
        
        certainRules.foreach(rule => {
          var certainRuleValues : List[String] = rule._1
          var actions : ListBuffer[String] = collection.mutable.ListBuffer[String]()
          var actionFrom : ListBuffer[String] = collection.mutable.ListBuffer[String]()
          var actionTo : ListBuffer[String] = collection.mutable.ListBuffer[String]()
          
          certainRuleValues.foreach { value => {
            //Splitting the attribute value by '_' to get attributeName and its corresponding attributeValue
            var attributeValues : Array[String] = value.split("_")
            var attributeName : String = attributeValues(0)
          
            actionTo += value
            
            /*
             * IF the certainRuleValue is a stableAttributeValue, then the action takes a form of (attributeName, certainRuleValue -> certainRuleValue)
             * ELSE the action takes a form of (attributeName, -> certainRuleValue)
             * */
            if(distinctStableAttributeValues.contains(value)){
              actionFrom += value
                
              actions += createAction(attributeName, attributeValues(1), attributeValues(1))
            } else{
              actionFrom += ""
              
              actions += createAction(attributeName, "", attributeValues(1))
            }
          } }
        
//          var actionReturnKey = actions.sorted.mkString(" ^ ") + " => " + "(" + decisionAttribute + ", " + decisionFromValue + " -> " + decisionToValue + ")"
//          var actionReturnValue = Array(actionFrom,actionTo)
          
          //Appending decisionFrom and decisionTo values to actionFrom and actionTo
          var actionFromWithDecisionFrom = actionFrom += decisionFrom
          var actionToWithDecisionTo = actionTo += decisionTo
          
          extraActionRules = performARoGS(actions,actionFromWithDecisionFrom,actionToWithDecisionTo,decisionAttribute,decisionFromValue,decisionToValue)
          
          
//          actionRules += (actionReturnKey -> actionReturnValue)
          
          extraActionRules.foreach(rule => {
            /*
             * rule._1 is an action rule
             * rule._2 is an array consisting of actionFrom and actionTo
             * */
            actionRules += (rule._1 -> rule._2)
          })
        })
        
        actionRules
      }
      
      //This module is to create an action of form (attributeName, value1 -> value2)
      def createAction(attributeName : String, actionFrom : String, actionTo : String): String = {
        
        if(actionFrom.equals(actionTo))
          "(" + attributeName + " = " + actionTo + ")"
        else
          "(" + attributeName + ", "+ actionFrom + " -> " + actionTo + ")"
      }
      
      
      
      /*
       * Following module is to perform ARoGS algorithm
       * It takes each action schema and generates extra action rules for those
       * */
      def performARoGS(actions : ListBuffer[String], actionFrom : collection.mutable.ListBuffer[String], 
                actionTo : collection.mutable.ListBuffer[String],
                decisionAttribute : String, decisionFromValue : String, decisionToValue : String)
                    : Map[String,Array[ListBuffer[String]]] = {
        var newActionRules : Map[String,Array[collection.mutable.ListBuffer[String]]] =
                            Map[String,Array[collection.mutable.ListBuffer[String]]]()
        
        //Finding stable attributes in an action rule - intersecting distinct decision attribute values and actionTo 
        var stableValues = distinctStableAttributeValues.intersect(actionTo.toSet)
      
        //Extracting found stable values support 
        var stableValuesSupport : Set[String] = Set[String]()
        if(stableValues.size > 0)
          stableValuesSupport = stableValuesSupport ++ attributeValues.apply(collection.mutable.Set(stableValues.head))

        stableValues.foreach { value => {
          stableValuesSupport = stableValuesSupport.intersect(attributeValues.apply(collection.mutable.Set(value)))
        } }
        
        /*
         * Getting an action rule's support 
         * Here action rule's support is rows in the data containing stable attributes in a certain rule 
         * and decisionFrom value
         */
        var localAttributeValues = decisionAttributeValues.apply(decisionFrom)
        if(stableValues.size > 0)
          localAttributeValues = localAttributeValues.intersect(stableValuesSupport)
        
        //Finding new attribute values for an action rule
        var newAttributeValues : Set[Set[String]] = Set[Set[String]]()
        distinctAttributeValues.foreach(value => {
          if(value._2.intersect(actionTo.toSet).nonEmpty){
            //>>>>>>>>>>>>>>>>>>>Needs proper implementation of adding new stable attribute values
//            if(value._2.forall(distinctStableAttributeValues.contains)){
//              newAttributeValues += value._2.toSet
//            }
//          } else{
            if(!value._2.forall(distinctStableAttributeValues.contains) && !value._1.equals(decisionAttribute)){
              newAttributeValues += (value._2 -- value._2.intersect(actionTo.toSet)).toSet
            }
          } 
          
        })
        
        
        //NEW ALGORITHM - takes cartesian product of each value
        if(newAttributeValues.nonEmpty){
          //Finding all combinations of new attribute values
          val newAttributes2 = newAttributeValues.map (_.map(scala.collection.Set(_)))
          var cartesian = newAttributes2.reduceLeft((xs,ys) => for { x <- xs; y <- ys} yield x ++ y)
               
          /*
           * For each newly created combination, data matching such pattern is identified
           * The final support is intersected with previously found action rule support
           * */
          cartesian.foreach { possibleCombinations => {
            var actionFromSupport = stableValuesSupport
            var canForm = true
            
            breakable( possibleCombinations.foreach { value => {
              if(actionFromSupport.isEmpty)
                actionFromSupport = attributeValues.apply(scala.collection.Set(value)).toSet
              else
                actionFromSupport = actionFromSupport.intersect(attributeValues.apply(scala.collection.Set(value)))
              
              if(actionFromSupport.isEmpty){
                canForm = false
                break
              }
            } })
            
            var finalActionFromSupport = localAttributeValues.intersect(actionFromSupport)
            
            if(finalActionFromSupport.isEmpty)
              canForm = false
      
            //Algorithm takes further steps only if the final support is non empty
            if(canForm){
              var extraActionFrom : collection.mutable.ListBuffer[String] = collection.mutable.ListBuffer[String]()
              var extraActionTo : collection.mutable.ListBuffer[String] = collection.mutable.ListBuffer[String]()
              var extraActions : ListBuffer[String] = collection.mutable.ListBuffer[String]()

              
              possibleCombinations.foreach { combination => {
                var splitValues = combination.split("_")
                
                /*
                 * If the newAttribute is a stable attribute,
                 * same value is put into actionFrom and actionTo and corresponding action is created
                 * 
                 * Else action is created with the new attribute value with its corresponding toValue
                 * */
                if(distinctStableAttributeValues.contains(combination)){
                                   
//                  extraActionFrom = actionFrom.clone()
//                  extraActionFrom += combination
//                  extraActionTo = actionTo.clone()
//                  extraActionTo += combination
//                  
//                  extraActions = actions.clone()
//                  extraActions += createAction(splitValues(0), splitValues(1) , splitValues(1))
                }
                else{
                  breakable(for(i <- 0 until actionTo.length){
                    var currentActionTo = actionTo.apply(i)
                    var currentActionToSplits = currentActionTo.split("_")
                    
                    if(splitValues.head.equals(currentActionToSplits.head)){
                      extraActionFrom += combination  
                      
                      extraActions += createAction(splitValues(0), splitValues(1), currentActionToSplits(1))
                      break
                    } 
                  })
                  
                  extraActionTo = actionTo.clone()
                }
              }}
              
              //Appending already existing stable attribute values to the new action rule
              stableValues.foreach { stableValue => {
                  var splitValues = stableValue.split("_")
                  extraActionFrom += stableValue
                  extraActions += createAction(splitValues(0), splitValues(1) , splitValues(1))
                }
              }
              extraActionFrom += decisionFrom
              
              //Formatting the final action rule and appending it's corresponding actionFrom and actionTo values
              if(extraActions.size>0){
                var mapKey = extraActions.sorted.mkString(" ^ ") + " => (" + decisionAttribute + ", " + decisionFromValue + " -> " + decisionToValue + ")"
                var mapValue = Array(extraActionFrom,extraActionTo)
//                println(mapKey)
                newActionRules += (mapKey -> mapValue)
              }
              
            }
            
          } }
        }
        
       
        
        
        
        
        
        //OLDALGORITHM
        /*
         * Following module checks if the support of found new attribute value merged with stableValues is a subset of support of localAttributeValues
         * After that extraActions, extraActionFrom and extraActionTo are generated 
         * */
//        var toBeAddedAttributes : Set[String] = Set[String]()
//        newAttributeValues.foreach { value => {
//            var splitValues = value.split("_")
//          
//            if(stableValuesSupport.intersect(attributeValues.apply(collection.mutable.Set(value))).intersect(localAttributeValues).size>0){
//              var extraActionFrom : collection.mutable.ListBuffer[String] = collection.mutable.ListBuffer[String]()
//              var extraActionTo : collection.mutable.ListBuffer[String] = collection.mutable.ListBuffer[String]()
//              var extraActions : ListBuffer[String] = collection.mutable.ListBuffer[String]()
//              
//              /*
//               * If the newAttribute is a stable attribute,
//               * new action is added to the current actions
//               * */
//              if(distinctStableAttributeValues.contains(value)){
//                extraActionFrom = actionFrom.clone()
//                extraActionFrom += value
//                extraActionTo = actionTo.clone()
//                extraActionTo += value
//                
//                extraActions = actions.clone()
//                extraActions += createAction(splitValues(0), splitValues(1) , splitValues(1))
//              } 
//              
//              /*
//               * Otherwise corresponding action is modified in the available actions
//               * */
//              else{
//                for(i <- 0 until actionTo.length-1){
//                  var currentActionTo = actionTo.apply(i)
//                  var currentActionToSplits = currentActionTo.split("_")
//                  
//                  if(value.head.equals(currentActionTo.head)){
//                    extraActionFrom += value  
//                    
//                    extraActions += createAction(splitValues(0), splitValues(1), currentActionToSplits(1))
//                  } else{
//                    extraActionFrom += actionFrom.apply(i)
//                  
//                    extraActions += createAction(currentActionToSplits(0), "", currentActionToSplits(1))
//                  }
//                }
//                
//                extraActionTo = actionTo.clone()
//              }
//              
//   
//              if(extraActions.size>0){
//                var mapKey = extraActions.sorted.mkString(" ^ ") + " => (" + decisionAttribute + ", " + decisionFromValue + " -> " + decisionToValue + ")"
//                var mapValue = Array(extraActionFrom,extraActionTo)
//                
//                newActionRules += (mapKey -> mapValue)
//              }
//            }
//        } }
       
        
        newActionRules
      }
      
      var certainRules : collection.mutable.Map[List[String], String] = 
          performLERS(attributeValues,decisionAttributeValues,distinctStableAttributeValues,decisionFrom,decisionTo,userSupport) 
     
      var actionRules = generateActionRules(certainRules,distinctStableAttributeValues,decisionFrom,decisionTo)
           
      
      var actionRulesWithSupport : collection.mutable.Map[String,String] = collection.mutable.Map[String,String]()
      actionRules.foreach(rule => {
        var actionFrom = rule._2(0)
        var actionTo = rule._2(1)
        
        actionFrom += decisionFrom
        actionTo += decisionTo
        
        var support = calculateSupport(actionTo.toList)
        
        /*
         * Finding Old and New Confidence
         * ActionFrom contains empty "" strings. So filtering out
         */
        var oldConfidence = (calculateConfidence(actionFrom.toList.filterNot(_.equals(""))) * calculateConfidence(actionTo.toList)*100).toInt
        var newConfidence = (calculateConfidence(actionTo.toList)*100).toInt
            
        actionRulesWithSupport += (rule._1 -> (support + "," + oldConfidence + "," + newConfidence))

      })

      actionRulesWithSupport.toIterator      
    }  
    
  )
  
  mainRDD.groupByKey(1).map(actionRule => {
    val parameters = parameterBroadcast.value
    val userSupport = parameters(3)(0).toInt
    val userConfidence = parameters(3)(1).toDouble
    
    var printableActionRule = actionRule._1
    var actionRuleSupport : Int = 0
    var actionRuleOldConfidence : Int = 0
    var actionRuleNewConfidence : Int = 0
    var numPartitions = actionRule._2.size
    
    actionRule._2.foreach { support => {
      var supportSplit = support.split(",")
      
      actionRuleSupport += supportSplit.apply(0).toInt
      actionRuleOldConfidence += supportSplit.apply(1).toInt
      actionRuleNewConfidence += supportSplit.apply(2).toInt      
    } }
    
    var finalOldConfidence = actionRuleOldConfidence/numPartitions
    
    if(actionRuleSupport >= userSupport && finalOldConfidence >= userConfidence){
      printableActionRule + 
        " [Support: " + actionRuleSupport + ", Old Confidence: " + finalOldConfidence +
        "%, New Confidence: " + (actionRuleNewConfidence/numPartitions) +"%]"
    } else{
      None
    }
  }).filter { x => x!=None }
  .saveAsTextFile(outputPath)
//    .collect() //COMMENT THIS ONCE TESTING IS DONE
  
  /*
   * fillAttributeValues() function is used to put support of each and every 
   * attribute values.
   * Example: 
   * 		a_1 = {x1,x2,x4,...}
   * 		b_2 = {x1,x3,x4,x5,.....}
   * */
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
                : collection.mutable.Map[List[String], String] = {
    var loopCount = 0
    
    var localAttributeValues = attributeValues.clone()
    
    /*
     * Maps to store certain rules in the format - 
     * List of attribute values as "Key" and decision attribute as "Value"
     */
    var certainRules : collection.mutable.Map[List[String], String] = collection.mutable.Map()
    
    var mainAttrValKeys = attributeValues.keySet.toList
    
    /*
     * The following function acts as a recursive function like
     * It takes each attributeValue support(AVS) and checks against decisionAttributeValue support(DAVS)
     * IF AVS is a subset of DAVS, the attributeValue key and decisionAttributeValue key will be added into certainRules
     * */
    while(!localAttributeValues.isEmpty){
//      println("***********NEW ITERATION************")
//      localAttributeValues.foreach(x => println(x._1.toString() + " - " + x._2.toString()))
      
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
                  certainRules += (attributeValueKey -> decisionAttributeValue._1)
                  
                  
//                  if(attributeValueKey.size == 1){
//                    mainAttrValKeys.filterNot { x => x == attributeValueKey.toSet }
//                  }
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
      
//      var attributeValuesKeysTest = localAttributeValues.keySet
//      var checkThis = attributeValuesKeysTest.map (_.map(scala.collection.Set(_)))
//                                         .reduceLeft(((xs,ys) => for { x <- xs; y <- ys} yield x ++ y))
//      println(" Check this:  " + checkThis.size)
//      checkThis.foreach { x => println(x.toString()) }
      
      for(i <- 0 until attributeValuesKeys.length){
//        for(j <- (i+1) until attributeValuesKeys.length){
      
        for(j <- 0 until mainAttrValKeys.length){
            var attributeValuesKeysFirst = attributeValuesKeys(i)
             var attributeValuesKeysSecond = mainAttrValKeys(j)
//            var attributeValuesKeysSecond = attributeValuesKeysss(j) 
                    
             var commonValues = localAttributeValues.apply(attributeValuesKeysFirst).
                                   intersect(attributeValues.apply(attributeValuesKeysSecond))
   
               // FOLLOWING IF CONDITION NEEDS CHECKING - NOT GIVING FINAL SET ACTION RULES
//             if(!commonValues.isEmpty && commonValues.size>=userSupport){
               
              if(!commonValues.isEmpty){
                var combinedValues = (attributeValuesKeysFirst.toSet.union(attributeValuesKeysSecond.toSet)).toList.sorted
                
                if(combinedValues.size == attributeValuesKeysFirst.size + 1 && 
                    !reducedAttributeValues.contains(combinedValues.toSet)){
                  reducedAttributeValues += (combinedValues.toSet  -> commonValues)
                  
                  
                }
              }
            
          
          
//           var attributeValuesKeysFirst = attributeValuesKeys(i)
//           var attributeValuesKeysSecond = attributeValuesKeys(j)
//           
//           
//           var commonValues = localAttributeValues.apply(attributeValuesKeysFirst).
//                                 intersect(localAttributeValues.apply(attributeValuesKeysSecond))
//          if(!commonValues.isEmpty){
//            var combinedValues = (attributeValuesKeysFirst.toSet.union(attributeValuesKeysSecond.toSet)).toList.sorted
//            
//            if(combinedValues.size == attributeValuesKeysFirst.size + 1 && 
//                !reducedAttributeValues.contains(combinedValues.toSet)){
//              reducedAttributeValues += (combinedValues.toSet  -> commonValues)
//            }
//          }
          
           
          
          
//           var combinedValues = attributeValuesKeysFirst.toSet.union(attributeValuesKeysSecond.toSet).toList.sorted
//           if(combinedValues.size == attributeValuesKeysFirst.size + 1 && !containsSameGroupValues(combinedValues.toSet) && 
//                             !reducedAttributeValues.contains(combinedValues.toSet)){
//             reducedAttributeValues += (combinedValues.toSet  ->
//                       localAttributeValues.apply(attributeValuesKeysFirst).intersect(localAttributeValues.apply(attributeValuesKeysSecond)))
//           }
        }
        
      }
      
      localAttributeValues.clear()
      localAttributeValues = reducedAttributeValues.clone()
      reducedAttributeValues.clear()
      
    }
    
//    certainRules.foreach(x => {
//      println(x._1.toString() + " -> " + x._2)
//    })
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