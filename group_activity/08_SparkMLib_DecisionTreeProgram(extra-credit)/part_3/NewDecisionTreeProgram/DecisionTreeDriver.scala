package org.ML

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

object DecisionTreeDriver {
  
  case class Car (buying : String, maint : String, doors : String, persons : String, lug_boot : String, safety : String, carClass : String)
  
  def main(args : Array[String]): Unit = {
    
    System.setProperty("hadoop.home.dir", "C:\\Hadoop\\hadoop-common-2.2.0-bin-master\\")
    
    //Parsing the input data
    def parseData(str:String) : Car = {
      var line = str.split(",")
      
      if(line.length == 7)
        Car(line(0), line(1), line(2), line(3), line(4), line(5), line(6))
      else Car("None", "None", "None", "None", "None", "None", "None")
    }
    
    
    //Setting up Spark configurations
    val conf = new SparkConf().setAppName("SparkAction").setMaster("local")
    val sc = new SparkContext(conf)
    
    //Reading an input data file
    val inputDataRDD = sc.textFile(args(0))
    
    val parsedInputRDD = inputDataRDD.map(parseData).cache()
    val validParsedInputRDD = parsedInputRDD.filter(line => !line.carClass.equals("None"))
    
    //Converting Strings to Double
    var buyingMap : Map[String,Int] = Map()
    var index1 = 0
    validParsedInputRDD.map(car => car.buying).distinct.collect().foreach(x => { buyingMap += (x -> index1); index1 += 1 })
    
    var maintMap : Map[String,Int] = Map()
    var index2 = 0
    validParsedInputRDD.map(car => car.maint).distinct.collect().foreach(x => { maintMap += (x -> index2); index2 += 1 })
    
    var doorsMap : Map[String,Int] = Map()
    var index3 = 0
    validParsedInputRDD.map(car => car.doors).distinct.collect().foreach(x => { doorsMap += (x -> index3); index3 += 1 })
    
    var personsMap : Map[String,Int] = Map()
    var index4 = 0
    validParsedInputRDD.map(car => car.persons).distinct.collect().foreach(x => { personsMap += (x -> index4); index4 += 1 })
    
    var lugMap : Map[String,Int] = Map()
    var index5 = 0
    validParsedInputRDD.map(car => car.lug_boot).distinct.collect().foreach(x => { lugMap += (x -> index5); index5 += 1 })
    
    var safetyMap : Map[String,Int] = Map()
    var index6 = 0
    validParsedInputRDD.map(car => car.safety).distinct.collect().foreach(x => { safetyMap += (x -> index6); index6 += 1 })
    
    var classMap : Map[String,Int] = Map()
    var index7 = 0
    validParsedInputRDD.map(car => car.carClass).distinct.collect().foreach(x => { classMap += (x -> index7); index7 += 1 })
    
    
    
    //Getting final data for Decision tree
    val dataPrep = validParsedInputRDD.map(car => {
      val carClass = classMap(car.carClass)
      val buying = buyingMap(car.buying)
      val maint = maintMap(car.maint)
      val doors = doorsMap(car.doors)
      val persons = personsMap(car.persons)
      val lugBoot = lugMap(car.lug_boot)
      val safety = safetyMap(car.safety)
      
      Array(carClass.toDouble,buying.toDouble,maint.toDouble,doors.toDouble,persons.toDouble,lugBoot.toDouble,safety.toDouble)
    })
    
    
    //Creating labeled points from the data
    val dataLabels = dataPrep.map(dataLine => {
        var vectorData = Vectors.dense(dataLine.apply(1), dataLine.apply(2), dataLine.apply(3), dataLine.apply(4), dataLine.apply(5), dataLine.apply(6))
        LabeledPoint(dataLine.apply(0),vectorData)
    })
    
    
    // Load and parse the data file.
//    val data = MLUtils.loadLibSVMFile(sc, args(0))
    // Split the data into training and test sets (30% held out for testing)
    val splits = dataLabels.randomSplit(Array(0.7, 0.3))
    val (trainingData, testData) = (splits(0), splits(1))
    
    // Train a DecisionTree model.
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val numClasses = 4
    val categoricalFeaturesInfo = Map[Int, Int]()
    val impurity = "gini"
    val maxDepth = 5
    val maxBins = 32
    
    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo, impurity, maxDepth, maxBins)
    
    // Evaluate model on test instances and compute test error
    val labelAndPreds = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    val testErr = labelAndPreds.filter(r => r._1 != r._2).count().toDouble / testData.count()
    println("Test Error = " + testErr)
    println("Learned classification tree model:\n" + model.toDebugString)
    
//    // Save and load model
//    model.save(sc, "target/tmp/myDecisionTreeClassificationModel")
//    val sameModel = DecisionTreeModel.load(sc, args(1))
  }
}