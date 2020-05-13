package org.GraphX

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.graphx.Graph
import org.apache.spark.graphx.Edge

object GraphXShortestPath {
  def main(args : Array[String]){
    
    
    
    
    def dijkstra[VD](g:Graph[VD,Double], origin:Long) = {
      var g2 = g.mapVertices(
        (vid,vd) => (false, if (vid == origin) 0 else Double.MaxValue))
    
      for (i <- 1L to g.vertices.count-1) {
        val currentVertexId =
          g2.vertices.filter(!_._2._1)
            .fold((0L,(false,Double.MaxValue)))((a,b) =>
               if (a._2._2 < b._2._2) a else b)
            ._1
        
        val newDistances = g2.aggregateMessages[Double](
            ctx => if (ctx.srcId == currentVertexId)
                     ctx.sendToDst(ctx.srcAttr._2 + ctx.attr),
            (a,b) => math.min(a,b))
    
        g2 = g2.outerJoinVertices(newDistances)((vid, vd, newSum) =>
          (vd._1 || vid == currentVertexId,
           math.min(vd._2, newSum.getOrElse(Double.MaxValue))))
      }
      
      g.outerJoinVertices(g2.vertices)((vid, vd, dist) =>
        (vd, dist.getOrElse((false,Double.MaxValue))._2))
    }

    
    
    
    val sc = new SparkContext(new SparkConf().setMaster("local")
                                             .setAppName("GraphXName"))
    
//    System.setProperty("hadoop.home.dir","C:\\Hadoop\\hadoop-common-2.2.0-bin-master\\")
    
    var animalIndex : collection.mutable.Map[String,Long] = collection.mutable.Map()
    var animalFile = sc.textFile(args(0)).zipWithIndex()
                       .map{case (name,id) => (id,name)}                         
    var animalVertex = animalFile.collect()
    animalVertex.foreach(vertex => animalIndex += (vertex._2.toLowerCase() -> vertex._1))
    
    var animalEdges = sc.textFile(args(1))
                           .map { x => {
                            var rowSplit = x.split(",")
                            
                            animalIndex.apply(rowSplit(0))
                            Edge(animalIndex.apply(rowSplit(0).toLowerCase()),
                                 animalIndex.apply(rowSplit(1).toLowerCase()),rowSplit(2).toDouble)
                           } }.collect()
                        
   
    
    val vertexRDD: RDD[(Long, String)] = sc.parallelize(animalVertex)
    val edgeRDD : RDD[Edge[Double]] = sc.parallelize(animalEdges)
    
    val graph: Graph[String, Double] = Graph(vertexRDD, edgeRDD)
    
    graph.vertices.collect().foreach(x => println(x._1 + " -> " + x._2))
    dijkstra(graph, 0).vertices.map(_._2).saveAsTextFile(args(2))
    
//    dijkstraShortestPath
    
  }
}