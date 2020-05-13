package org.GraphX

import org.apache.spark.graphx._
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD

object GraphXBasics {
  def main(args : Array[String]){
    
    
    val sc = new SparkContext(new SparkConf().setMaster("local")
                                             .setAppName("GraphXName"))
    
    
    //Creating Graph Vertices
    val vertexArray = Array(
      (1L, ("Alice", 28)),
      (2L, ("Bob", 27)),
      (3L, ("Charlie", 65)),
      (4L, ("David", 42)),
      (5L, ("Ed", 55)),
      (6L, ("Fran", 50))
      )
      
    //Creating Graph Edges
    val edgeArray = Array(
      Edge(2L, 1L, 7),
      Edge(2L, 4L, 2),
      Edge(3L, 2L, 4),
      Edge(3L, 6L, 3),
      Edge(4L, 1L, 1),
      Edge(5L, 2L, 2),
      Edge(5L, 3L, 8),
      Edge(5L, 6L, 3)
      )
    
      
    //Creating Vertices and Edges RDD
    val vertexRDD: RDD[(Long, (String, Int))] = sc.parallelize(vertexArray)
    val edgeRDD: RDD[Edge[Int]] = sc.parallelize(edgeArray)
    
    
    
    //Creating a graph with defined vertices and edges
    //Graph takes the form of: (Person Name, Age) as vertex and EdgeWeight - count of number of times that a person likes another person
    val graph: Graph[(String, Int), Int] = Graph(vertexRDD, edgeRDD)
    
    
    
    //Filtering the vertices whose age is greater than 30
    println("Showing persons whose age is greater than 30")
    var f = graph.vertices.filter { case (id, (name, age)) => age > 30 }
//    .collect.foreach {
//      case (id, (name, age)) => println(s"$name is $age")
//    }
    f.saveAsTextFile(args(0) + "1")
    
    
    //Printing graph triplets - Showing which vertex is connected to which vertex
    println("Showing which person likes which person")
//    for (triplet <- graph.triplets.collect) {
//      println(s"${triplet.srcAttr._1} likes ${triplet.dstAttr._1}")
//    }
    graph.triplets.saveAsTextFile(args(0)+"2")
    
    //Filtering triplets - Showing which person likes aother person more than 5 times
    println("Showing which person loves another person")
//    for (triplet <- graph.triplets.filter(t => t.attr > 5).collect) {
//      println(s"${triplet.srcAttr._1} loves ${triplet.dstAttr._1}")
//    }
    graph.triplets.filter(t => t.attr > 5).saveAsTextFile(args(0)+"3")
    
    
    
    // Define a class to more clearly model the user property
    case class User(name: String, age: Int, inDeg: Int, outDeg: Int)
    
    // Create a user Graph 
    val initialUserGraph: Graph[User, Int] = graph.mapVertices{ case (id, (name, age)) => User(name, age, 0, 0) }
    
    // Performing join operations
    val userGraph = initialUserGraph.outerJoinVertices(initialUserGraph.inDegrees) {
      case (id, u, inDegOpt) => User(u.name, u.age, inDegOpt.getOrElse(0), u.outDeg)
    }.outerJoinVertices(initialUserGraph.outDegrees) {
      case (id, u, outDegOpt) => User(u.name, u.age, u.inDeg, outDegOpt.getOrElse(0))
    }
    
    println("Printing which person is liked by how many people:")
//    for ((id, property) <- userGraph.vertices.collect) {
//      println(s"${property.name} and is liked by ${property.inDeg} people.")
//    }
    userGraph.vertices.saveAsTextFile(args(0)+"4")
    
    
    
    println("\nPrinting the persons who are liked by the same number of people they like")
    var g = userGraph.vertices.filter {
      case (id, u) => u.inDeg == u.outDeg
    }
//    .collect.foreach {
//      case (id, property) => println(property.name)
//    }
    g.saveAsTextFile(args(0)+"5")
    
    
    
    //Finding oldest follower for each person
    val oldestFollower: VertexRDD[(String, Int)] = userGraph.aggregateMessages[(String, Int)](
      // For each edge send a message to the destination vertex with the attribute of the source vertex
      edge => Iterator((edge.dstId, (edge.srcAttr.name, edge.srcAttr.age))),
      // To combine messages take the message for the older follower
      (a, b) => if (a._2 > b._2) a else b
      )
    
    userGraph.vertices.leftJoin(oldestFollower) { (id, user, optOldestFollower) =>
      optOldestFollower match {
        case None => s"${user.name} does not have any followers."
        case Some((name, age)) => s"${name} is the oldest follower of ${user.name}."
      }
    }.saveAsTextFile(args(0) + "6")
//    .collect.foreach { case (id, str) => println(str) }
    
    
    
    
    
    //Finding average age of followers of each person
    val averageAge: VertexRDD[Double] = userGraph.aggregateMessages[(Int, Double)](
      // map function returns a tuple of (1, Age)
      edge => Iterator((edge.dstId, (1, edge.srcAttr.age.toDouble))),
      // reduce function combines (sumOfFollowers, sumOfAge)
      (a, b) => ((a._1 + b._1), (a._2 + b._2))
      ).mapValues((id, p) => p._2 / p._1)
    
    // Display the results
    userGraph.vertices.leftJoin(averageAge) { (id, user, optAverageAge) =>
      optAverageAge match {
        case None => s"${user.name} does not have any followers."
        case Some(avgAge) => s"The average age of ${user.name}\'s followers is $avgAge."
      }
    }.saveAsTextFile(args(0) + "7")
    
  }    
}