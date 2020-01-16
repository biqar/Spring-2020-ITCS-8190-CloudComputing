# Cloud Computing for Data Analysis (Group 4 Activity)
#### Group members
    * Shweta Mahadev Gondi
    * Akshara Gone
    * Deepak Gorle
    * Abhinav Gupta
    * Shreeya Sanjay Gupta
    * Sahithi Priya Gutta
    * Raj Ingle
    * Raqib Islam
    * Jinraj Jain
    * Kriti Jain
#### Work to do

* Presentation Subject: Boolean Retrieval | Term Vocabulary and Posting Lists | Web Search Basics
    * Chapter 1. from Book 4. InformationRetrieval
    * Chapter 2. from Book 4. InformationRetrieval
    * Chapter 19. from Book 4. Information Retrieval 
* implement: PageRank program  as described in: http://webpages.uncc.edu/aatzache/ITCS6190/Exercises/05_Exercise_PageRank.txt

## Exercise 05 - PageRank

Download a .zip file from the link and unzip the downloaded file: http://webpages.uncc.edu/aatzache/ITCS6190/Exercises/05_Exercise_PageRank.zip

### PART - 1:
Steps to execute a simple java program for PageRank:
1. Read the PageRankAlgorithm.pdf, PageRankLogicDiagram.pdf, and PageRankPseudoCode.pdf for PageRank, which comes with 05_Exercise_PageRank.zip
2. Open your Eclipse
3. Create a simple java project
4. Copy the attached PageRank.java in 'Simple Java Code' folder that comes from 05_Exercise_PageRank.zip into your java project
5. Run PageRank.java
6. When it asks for number of iterations and nodes, give:
```
5
5
```
7. When it asks for the node links, give:
```
0
1
0
0
1
1
0
1
1
0
1
0
0
0
1
0
1
0
0
1
0
0
1
1
0
```
8. Copy the output into a SimpleJavaPageRankOutput.txt file

### PART - 2: (IN CASE DSBA-HADOOP CLUSTER IS NOT WORKING, SKIP THIS AND DO PART - 3 INSTEAD)
Steps to execute PageRank in Hadoop code:
1. Open WinSCP (in Windows) or Cyberduck (in Macintosh)
2. Open Putty (in Windows) or Terminal (in Macintosh) 
3. Copy PageRank.jar and input-pages.txt from 'Hadoop Code' folder that comes with 05_Exercise_PageRank.zip to WinSCP or Cyberduck
	(Description about the data is provided in the 'Data and Code Description' file inside the .zip file)
	(The code is well documented. MapReduce code can be found in the 'MR_PageRank' folder inside the .zip folder)
4. Copy input-pages.txt to HDFS using
```
hadoop fs -put /users/your_UNCCName/input-pages.txt /user/your_UNCCName/ 
```
5. Run the .jar file like:
```
hadoop jar /users/your_UNCCName/PageRank.jar Driver /user/your_UNCCName/input-pages.txt /user/your_UNCCName/PageRankOutput 6
```
6. Copy output file using:
```
hadoop fs -get /user/your_UNCCName/PageRankOutput/part-r-00000 /users/your_UNCCName/PageRankOutput.txt
```
7. Copy the PageRankOutput.txt from WinSCP or CyberDuck to your local machine

Submit the SimpleJavaPageRankOutput.txt and PageRankOutput.txt in Canvas.

### PART - 3: (STEPS TO DO PART - 2 USING AWS)
Steps to execute PageRank in Hadoop code:
1. Log-in to the AWS account
2. Create a Hadoop cluster with required number of m4.large instances
3. Create a data bucket in AWS S3 to store all data files
4. Upload PageRank.jar and input-pages.txt from 'Hadoop Code' folder that comes with 05_Exercise_PageRank.zip to the data bucket in S3
	(Description about the data is provided in the 'Data and Code Description' file inside the .zip file)
	(The code is well documented. MapReduce code can be found in the 'MR_PageRank' folder inside the .zip folder)

5. Log-in to the master node using Putty(Windows) or SSH(Linux or MAC)
6. Download PageRank.jar to master node using:
```
aws s3 cp s3://BUCKET_NAME/PageRank.jar .
```
7. Run the .jar file like:
```
hadoop jar ./PageRank.jar Driver s3://BUCKET_NAME/input-pages.txt /user/PageRankOutput 6
```
8. Copy the output to the master node using:
```
hadoop fs -get /user/PageRankOutput/part-r-00000 .
```
9. Copy the output to AWS S3 using:
```
aws s3 cp ./part-r-00000 s3://BUCKET_NAME/PageRankOutput.txt
```
8. Download PageRanOutput.txt from S3

Submit the Command window text, SimpleJavaPageRankOutput.txt and PageRankOutput.txt in Canvas.