# Video Case
This document contains the video case assignments of this course. We were asked to watch a couple of videos 
in each assignment and then answer some questions based on the topics discussed in those videos.

## Video Case – 01 (MapReduce)
**Watch following videos:**
* Video 1: https://www.youtube.com/watch?v=n64LnzXQXN0 (provide basic operations of MapReduce)
* Video 2: https://www.youtube.com/watch?v=zgaGeyNgBYE (gives an example problem solved in MapReduce)

**Video Case Questions:**

1. Give some advantages of using MapReduce
<pre>
There are numerous advantages of using MapReduce in wide ranges of applications. 
Among them here is the few notably,

  <b>1. Scalability:</b> Surely this should be the top reason of using MapReduce in any application. 
High scalability comes through the ability of storing and distributing the data and processing 
efforts among lots of servers.

  <b>2. Fastness:</b> MapReduce application can utilize distributed file system which accelerate the 
processing speed of a server, compare with storing the whole data in a single file system.

  <b>3. Cost-effectiveness:</b> MapReduce is a very much cost-effective solution for the applications 
that need to store and process data of exponential growths.

  <b>4. Wide variety of usefulness:</b> Any application that has huge chunk of unstructured data, can 
utilize the MapReduce functionality.
</pre>

2. Describe briefly the steps that the MapReduce follow, after Mapper phases producing their intermediate outputs 
<pre>
MapReduce executes in two steps,

  <b>1. Map step:</b> In this step, unstructured data is been processed and saved to the Hadoop File 
System (HDFS). The main goal is to process the data and create small chunks of relative data.

  <b>2. Reduce step:</b> Reduce phase take the input from the output generated in the map stage. 
Mostly apply the business logic in this step to retrieve more meaningful information.

In the video 2, a MapReduce example demonstrated where in the mapper phase, raw data has been 
broken into chunks and distributed among the mapper nodes. All the mappers run parallelly and 
produced indexed data as the output.
</pre>

3. What happens in the Reducer phase?
<pre>
In the video 2, the Reducer phase receive the indexed data (by store) as input, from the mappers. 
Each store (i.e. NYC, MIAMI, LA, etc.) is been assigned to a single reducer.  Reducers go to the 
mappers and receive the data of the corresponding stores. Once the reducer receives the data, it 
combines each of the small chunk of data (by store) and do the aggregation for them.
</pre>

4. Mention two other applications that could use MapReduce to fasten their processing time
<pre>
As I mentioned earlier, any application that has huge chunk of unstructured data, can utilize the
MapReduce functionality. For example,

  <b>1. Search Engine</b> can use MapReduce to index the search strings

  <b>2. Social Media</b> can use MapReduce to find the key persons in the network
</pre>

## Video Case – 02 (Hadoop HDFS)
**Watch following videos:**
* Video 1: https://youtu.be/cC6sS80sZYQ (introduces you to HDFS)
* Video 2: https://youtu.be/yDV0EE9DCJ0 (describes about Name Node in HDFS)
* Video 3: https://youtu.be/nbOagGnIMiY (provides knowledge about file read and write operations in HDFS)

**Video Case Questions:**
1. Give some description about data nodes and name node
<pre>
  <b>Data Node:</b> In Hadoop echo-system, Data Node is responsible to store the data in the local 
file system. It stores metadata of the data block and serve both the data and metadata requested 
by the client.

  <b>Name Node:</b> In Hadoop echo-system, Name Node manages filesystem namespace as well as 
cluster configuration management. It is considered as the master node and stores the file 
status distributed across the cluster. It maps both the file name to a set of blocks and a 
block to the set of data-nodes where it will be places. It keeps track of the complete file 
system and replications of the file blocks. It works smartly so that it can handle failure 
of a data node (i.e. failure of replica of a file or the whole rack).
</pre>

2. What is the main purpose of secondary name node?
<pre>
  As name node is considered as the master node and responsible for lots of crucial tasks 
(i.e. data replication, management of data node failure etc.), a single name node will 
become the single point of failure for the whole system. That’s why we need secondary 
name node to assist the name node to release the resources (i.e. main memory). Secondary 
name node doesn’t function like name node. Rather, it’s main purpose is to combine the 
name space image and edit logs so that the main memory of name node will not fills up by 
the ever increasing edit logs. It also creates the checkpoint of namespace image and edit 
log merge together and write it to a file so that name node can release the main memory 
till the checkpoint.
</pre>

3. What are all the steps followed by HDFS for write operation?
<pre>
  <b>1.</b> Client communicate with the name node with a write request
  <b>2.</b> Name node perform various check (i.e. if the file exists or not, client has 
  the correct permission or not for the activity)
  <b>3.</b> If the check phase pass, the name node will respond the client with the list 
  of data nodes where the data will be copied
  <b>4.</b> The client then connects to the first data node and ask it to make a pipeline 
  for the subsequent data nodes
  <b>5.</b> Data nodes will send the acknowledgement to the client as they successfully 
  copy the blocks
  <b>6.</b> Steps 3-5 will be repeated until all the chunks of the file has been written 
  to the HDFS.
  <b>7.</b> After that, the client will send a completion message to the name node
  <b>8.</b> In case of data node failure, the data is written on the remaining other data 
  nodes. Name node notice the under replication and arrange for the replication. Same steps 
  will be followed for the case of multiple node failure. The data is necessary to be written 
  to at least one node and the under replicated chunks will be taken care by the name node. 
</pre>

4. Explain the steps that Hadoop follows for reading the data during a data node failure.
<pre>
  Bandwidth is the key concern for the read operation. There is a distance concept in HDFS 
based on the bandwidth. 2 data blocks referred from the same data node have 0 distance. 
If the blocks stored on the same rack but different data node, then the distance is counted 
as 2. The distance is considered as 4 and 6 in the case of different racks and different data 
center respectively. Now come to the read operation steps

  <b>1.</b> HDFS client sends a read request to the name node.
  <b>2.</b> In response, the name node provides a list of data nodes containing the first 
  few blocks. Name node provide the data node list with the increasing distance of the data 
  nodes.
  <b>3.</b> Then the client will send the first data nodes and read the blocks one by one.
  <b>4.</b> In case of a data node failure,
    <b>a.</b> If the data is corrupted, the client will forward the read request to the next 
    data nodes.
    <b>b.</b> If it is a node failure case, the HDFS client will keep this information to 
    remove the further appearance of that data node in the next responses it gets from the 
    name node (for other blocks).
</pre>

## Video Case – 03 (CloudTools Pig-Hive-HBase)

**Watch following videos:**
* Video 1: https://youtu.be/rxnXHlaSohM (basic knowledge about Pig)
* Video 2: https://youtu.be/uY7Rr7ru9E4 (basic knowledge about Hive)
* Video 3: https://youtu.be/kN01ELCAsn8 (basic knowledge about HBase)

**Video Case Questions:**
1. Briefly explain the execution steps followed by Pig.
<pre>
  <b>•</b> Load the input data from HDFS
    <b>-</b> A = LOAD ‘datafile.txt’;
  <b>•</b> Run the program through a set of transformation, which under the cover translated to a 
  set of map and reduce tasks
  <b>•</b> Dump/store the results while execution done
    <b>-</b> DUMP C;
    <b>-</b> STORE C into ‘Results’;
</pre>

2. What is the purpose of Hive? Mention some of the advantages of Hive.
<pre>
  Purpose of Hive: Though the Pig is quite powerful and easy to use, the downside is it has 
a learning curve to be master on that. It’s easier than writing MapReduce program, but it’s 
not easy as like SQL is. Hive comes to fill this gap. Hive is a Hadoop runtime component 
that allows anyone already mastered in SQL to utilize the Hadoop platform without an additional 
learning curve.

Some major advantages of using Hive is:
  <b>•</b> As mentioned earlier, it is easy to use Hive due to it’s similarity with SQL.
  <b>•</b> It is possible run the Hive queries in a few different ways (i.e. command line interface 
named HiveShell, JDBC/ODBC driver, Hive Thrift Client etc), which brings elasticity for the 
developers who want to utilize Hadoop for their application.
  <b>•</b> The Hive Thrift client can be used with applications written in C++, Java, PHP, Python, 
Ruby etc. This means, developers from a diversified domain can utilize the power of Hadoop 
in their application with a very small change.
</pre>

3. Give some similarities of architectures of HBase and HDFS and MapReduce.
<pre>
  HBase follows master-slave architecture. Just like HDFS which has a NameNode and SlaveNode, 
and MapReduce that has job tracker and task tracker slaves, HBase is built on similar concepts. 
In HBase, the master node manages the cluster and resolves servers store portion of the tables 
and perform the work on the data. Similar to HDFS’s concern to the availability of NameNode, 
HBase is also sensitive to the loss of it’s master node.
</pre>

## Video Case – 04 (Spark)
**Watch following videos:**
* Video 1: https://youtu.be/PiJGa26OHFM
* Video 2: https://youtu.be/eMGjuK-Pk9g

**Video Case Questions:**
1. What is Spark?
<pre>
  Apache Spark is an open-source, highly fault tolerant data analytics and cluster computing 
framework which runs on top of HDFS. In contrast with Hadoop MapReduce, Apache Spark is not 
tied to two-stage of MapReduce paradigm. For certain applications, it performs 100X faster 
than Hadoop MapReduce. Spark provides primitives for in-memory cluster computing, meaning, 
it allows user programs to load data into a cluster’s memory, so that, they could be queried 
repeatedly. This makes Apache Spark well suited for machine learning algorithm.
</pre>

2. What are all the layers or packages that come along with Spark? And what they are used for?
<pre>
Apache Spark packages,
  <b>•</b> Spark SQL: SQL interface, similar to Hive SQL used in Apache Hadoop ecosystem.
  <b>•</b> Spark Streaming: Allows processing of live data stream.
  <b>•</b> MLlib: Machine learning part like Mahout in Apache Hadoop.
  <b>•</b> GraphX: Allows graph processing and parallel execution on graph.
</pre>

3. Why does the Spark runs faster than Hadoop?
<pre>
  Apache Spark is faster than Hadoop because Spark does all the processing in-memory whereas the 
Hadoop MapReduce is not doing like that.

  Hadoop MapReduce persists the full dataset to HDFS after running each job, which is very 
expensive. Spark overcome this by utilizing the technique of operation pipeline, where it can 
feed the output of one operation into another one without writing it back to a persistent storage. 
As Spark relies on in-memory processing, it can utilize the in-memory caching as well.
</pre>

## Video Case – 05 (Finding Similar Items)
**Watch following videos:**
* Video 1: https://youtu.be/wrkVnwaKTjo (sample example for Jaccard coefficient and its limitations)
* Video 2: https://youtu.be/ubqGFxHeg7Q (sample example for Cosine similarity)

**Video Case Questions:**
```
D1: The sky is blue
D2: The sun in bright
Query: The sun in the sky is bright
```

1. Find Jaccard coefficient for the above documents (D1 and D2) for the query Q
<pre>
  Intersection: The.sky.is
  Union: The.sun.in.sky.is.bright.blue
  J(Q, D1) = 3/7

  Intersection: The.sun.in.bright
  Union: The.sun.in.the.sky.is.bright
  J(Q, D2) = 4/6
</pre>

2. What is the advantage of using cosine similarity over Jaccard coefficient?
<pre>
  Jaccard coefficient does not consider term frequency. Also, compared with cosine similarity, 
Jaccard coefficient provide a less sophisticated way of normalizing for length.
</pre>

3. Where do you think, these measures can be used?
<pre>
  Jaccard coefficient and cosine similarity can be used in applications for information retrieval, 
biologic taxonomy, gene feature mapping, plagiarism check, etc.
</pre>

## Video Case – 06 (PageRank)

**Watch following videos:**
* Video 1: https://youtu.be/A4-yw07Ku1s

**Video Case Questions:**
1. What is PageRank?
```
PageRank is an algorithm used by google search engine to rank the webpages in thir search results.
```
2. What are all factors we need to consider for calculating a web page’s PageRank score?
<pre>
  <b>1.</b> The page-rank score of pages that have outbound link to a specific page
  <b>2.</b> The number of outbound links of those pages
</pre>

3. When does a PageRank of a web page goes high?
<pre>
  <b>•</b> When a web page has an inbound link from a page with high page-rank score
  <b>•</b> When a web page has so many inbound links
</pre>

## Video Case – 07 (Association Rules Market Basket Analysis)
**Watch following videos:**
* Video 1: https://youtu.be/GqwrAJPP4mk
* Video 2: https://www.youtube.com/watch?v=TcUlzuQ27iQ

**Video Case Questions:**
1. What is Market Basket Analysis?
<pre>
  Finding customer buying habits by associations and correlations between the different items 
that customers place in their “shopping basket” is called Market Basket Analysis.

  For example, given a database of customer transactions, where each transaction is a set of 
items, Market Basket Analysis is the process of find groups of items which are frequently 
purchased together.
</pre>

2. Mention some disadvantages of Apriori algorithm for longer transactions.
<pre>
  For large transactions, execution time is more as wasted in producing candidates every-time. 
Besides this, it also needs more search space and higher computational cost.
</pre>

3. In what other domains Market Basket Analysis can be applied?
<pre>
Market Basket Analysis can be applied fraud detection, recommendation engine, product placement, etc.
</pre>

## Video Case – 08 (Mining Actionable Patterns)
**Watch following videos:**
* Video 1: https://youtu.be/3ZdhhuqypDM

**Video Case Questions:**
1. What are differences between Classification Rules and Action Rules?
<pre>
  Classification rule tells us the condition on which it will go to one of the classes. Action 
rules represent actionable patterns in the given data and produces actions that needs to undertake 
to achieve desired action goal.
</pre>

2. How the attribute set in a dataset should be divided for Action Rules?
<pre>
For action rules the attributes of the data are split in three parts,
  1. Stable attribute
  2. Flexible attribute
  3. Decision attribute
</pre>

3. In what other application action rules can be applied?
<pre>
Other applications where action rules can be applied:
  • Finance
  • Education
  • Recommendation System
</pre>

## Video Case – 09 (Decision Trees)
**Watch following videos:**
* Video 1: https://youtu.be/RG4FYHfAQJQ

**Video Case Questions:**
1. What are the purposes of decision trees?
<pre>
  Decision trees are algorithms that organizations use to make best possible decision for a 
predefined objective which is profitable to them.
</pre>

2. What are some disadvantages that you see in decision trees when built for larger datasets?
<pre>
The disadvantages of decision trees for large datasets:
  • Computational in-efficiency
  • Large number of variables and choosing right variables in right order
  • Split the variables in the most optimal value
  • Choosing variables in a way so that over fitting can be avoided
</pre>

3. Mention some other applications of decision trees.
<pre>
  • Recommendation system
  • Sentiment Analysis
  • Fraud detection
</pre>

## Video Case – 10 (Text Classification)
**Watch following videos:**
* Video 1: https://youtu.be/Czueje0eVzY
* Video 2: https://youtu.be/tzfOsLeVoPI

**Video Case Questions:**
1. What is text mining?
<pre>
  Text mining is a process to filter large number of documents and extracts the relevant 
information from there according to the user’s need. Text mining is different than search 
tools as it not only finds the parts of speech of the sentences, but also find the actionable 
patterns that help find more useful information. It enables the users to see emerging trends 
and patterns that is impossible to do if the user had to read all the contents yourself. 
This results in new insights which helps answer queries that is not possible using a search 
engine.
</pre>

2. How text mining tools works on the given text for extracting information?
<pre>
The text mining tool extracts by learning how to find information within each article. It 
examines complex structures documents containing unique language, abbreviations, codes, 
and symbols. This process then ends up with a long list of extracted words and sentences. 
The text mining tool also understands how the words related to one another and can analyze 
the results. This is how text mining tool works in extracting information.
</pre>

3. What are some applications of text mining?
<pre>
There are several areas where text mining can put it’s influences, such as –
  1. Virtual assistants (Siri, Google Assistant, etc.)
  2. Sentiment analysis
  3. Document summarization
  4. Text categorization in specific domain (i.e. filtering spam emails)
  5. Fraud detection
  6. Customer care service (intelligent chat bot)
</pre>
