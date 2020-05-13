import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import breeze.linalg.DenseVector
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.feature.Normalizer
//import org.apache.spark.SVMMultiClassOVAWithSGD

object svmdriver {
  def main(args: Array[String]): Unit ={
    //val conf = new SparkConf().setAppName("SparkAction").setMaster("local[*]").set("spark.driver.bindAddress","127.0.0.1")
    val conf = new SparkConf().setAppName("SparkAction").setMaster("local").set("spark.executor.heartbeatInterval", "100")
    val sc = new SparkContext(conf)
    
    //Reading the libSVM file
    
    val data = MLUtils.loadLibSVMFile(sc, args(0))
    
    // Split data into training (70%) and test (70%).
    val splits = data.randomSplit(Array(0.7, 0.3), seed = 11L)
    val training = splits(0).cache()
    val test = splits(1)
    
    val t1 = System.nanoTime
    
    val model = SVMMultiClassOVAWithSGD.train(training, 100000, 10, 0.0001)
 
//    
//    // Compute raw scores on the test set.
    val scores = test.map { point =>
      val score = model.predict(point.features)
      ((score),(point.label))
    }
    
    val metrics = new MulticlassMetrics(scores)
//    
//    //Printing confusion matrix
    println("Confusion matrix:")
    println(metrics.confusionMatrix)
//    
     // Getting the accuracy
    val accuracy = metrics.accuracy
    println("Test accuracy = " +accuracy)
//   
//  
    val labels = metrics.labels
    
    //Precision by label
    labels.foreach { l =>
      println(s"Precision($l) = " + metrics.precision(l))
    }

    // Recall by label
    labels.foreach { l =>
      println(s"Recall($l) = " + metrics.recall(l))
    }

    // False positive rate by label
    labels.foreach { l =>
      println(s"FPR($l) = " + metrics.falsePositiveRate(l))
    }

    // F-measure by label
    labels.foreach { l =>
      println(s"F1-Score($l) = " + metrics.fMeasure(l))
    }

    val duration = (System.nanoTime - t1) / 1e9d
    print("Total Runtime in Seconds: " + duration)

  }
  
}