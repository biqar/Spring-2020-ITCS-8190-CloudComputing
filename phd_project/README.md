# PhD Project Description

Due to the inadequate number of PhD students in the class, we submitted the "Part 0" only as an individual work.

Part 0 
-----------
* PhD Project Presentation Topic: Vector space classification
* Read from book 4: Introduction to Information Retrieval ([Chapter 14](https://webpages.uncc.edu/aatzache/ITCS6190/Textbooks/IR/IR_14_Vector_Space_Classification_Chapter14.pdf))
* Submitted:
  1. [Presentation slides](https://github.com/biqar/Spring-2020-ITCS-8190-CloudComputing/blob/master/phd_project/ITCS%206190_8190%20(phd%20project).pptx)
  2. [Recorded slideshow of the presentation](https://drive.google.com/file/d/1itrN7QvkKRgPYGZ-klaxglFhqDk_9YIl/view?usp=sharing)

Part 1
-----------

MapReduce program - worth 20 points ( out of 100 )

- Obtain PatentData XML files on the go to: dsba-hadoop.uncc.edu

```
# single XML file
$ cd /projects/class/itcs-6190/

# all the XML files
$ cd /projects/class/itcs-6100/patentData2000_2015/patGrants2005_2015h1_ipgs/

# to UnZip the file
$ unzip ipg100727.zip
```

- Parse the PatentData XML files to create data in a table ( relational ) format . MapReduce program should take as an input several XML files at once ( not just one ) .

- For example from the following XML tags :
```
<patent_name> new_chip </patenet_name>
<patenet_class> electronics </patent_Class>
<year_granted> 2015 </year_granted>
```
create a comma separated text file , containing one patenet per line , and each tag is a column ( AttributeName ) :
```
patenet_name	patent_class	year
----- 		--------	-----
new_chip	electronics	2015
monitor		electronics	2014
electric_car	vehicles	2012
```
- The XML file contains a text filed with description of the patent. Use your WordCount program to extract the 3 most frequent words ( words with 3 highest counts in the text ) , and create 3 additional attributes containint these words :

```
patenet_name	patent_class	year	word1	word2	word3
----- 		--------	-----	-----	-----	-----
new_chip	electronics	2015	chip	intel	design
monitor		electronics	2014	display	apple	27"
electric_car	vehicles	2012	drive	google	battery
```

- Save the data file as PatentData.txt   each patenet on a new line , each value separated by comma ' , ' and NO attribute names . Save the attribute names in a separate file called  PatentDataAttributes.txt   each attribute on a  new line .

- Upload your PatentData.txt and PatentDataAttributes.txt  to Canvas.

Part 2
-------

HIVE program	-	worth 20 points ( out of 100 )

- Load PatentData.txt file ( you created in Step. 1 ) into a HIVE table
- Run SQL queries on the data to obtain:
```
2.1. all Patents from year 2015
2.2. all Patents owned by the company ' Apple '
2.3. all patents in class electronics, that contain the frequent word ' Samsung ' , AND ' Apple '
```
- Save the output of each query in a separate text file . Upload your output files to Canvas .


Part 3
-------

SPARK program	-	worth 20 points ( out of 100 )

- Use the PatentData.txt file ( from Step. 1 ) with the Spark MLib ( Machine Learning Library )

- Create the following :
```
3.1. Decision Tree , use the attribute PatentClass as a decision attribute
3.2. Classification , use the attribute word1 ( most frequent word ) as a decision attribute
3.3. Clustering : - use K-MEANS clustering , create 12 clusters ; - use hierarchical clustering ( number of clusters automatic ) 
```
- Save the output for DecisionTree, Classification, and Clustering . Upload your output files to Canvas .
 
Part 4
-------

PRESENTATION - worth 20 points (out of 100)

- bring candy / sweets ( ex. chocolates ( sneakers , mars , M&M's , etc.) , cookies , cupcakes , doughnuts )
- present your project to the class on the assigned presentation date 
- create 7 PowerPoint slides total for the group
- each student presents 1 slide , and speaks maximum 1 to 2 minutes
- each student should ask 1 question to the class about what is shown on his/her slide
- if a student answers the question correctly - go and give them the candy / sweets .
- one student runs the 3 programs above - MapReduce , Hive , and Spark MLib in front of the class , and explains the code , and what the results of the programs are
- find a YouTube video related to your presentation subject , video should be 2 - 3 minutes maximum ( otherwise stop the video at the 3rd minute ) . one student should show the video to the class .
- all students must attend the class on the presentation date . if a student is not present on the presentation date , the student will receive a grade of  0  for the project 
- all source code ( Part 3 ) , PowerPoint slides file , and YouTube video link  are due on Canvas  3  days before the presentation date .


Part 5
-------

Research Paper Report	-	worth 20 points ( out of 100 )

- Write a research paper showing the results of your project .
- The paper should contain the following sections :
```
5.1. Abstract
5.2. Introduction
5.3. Related Work
5.4. Method
5.5. Experiment and Results
5.6. Conclusion
5.7. References
```
- Use the following papers as an example . However, you cannot copy any parts of the example papers . Your writing should be original .
```
MR – Random Forest Algorithm for Distributed Action Rules Discovery
http://webpages.uncc.edu/aatzache/Papers/IEEE_Formatted_MRRandomForestDistributedActionRules.pdf

MR - Apriori Count Distribution Algorithm for Parallel Action Rules Discovery
http://webpages.uncc.edu/aatzache/Papers/IEEE_Formatted_MRCountDistributionAprioriActionRules.pdf
```
- Format the paper in IEEE format ( 2 columns , times new roman font , etc. ). Use the following template :
```
IEEE Formatting Template
http://webpages.uncc.edu/aatzache/ITCS6190/Project/IEEE_Format_Instructions8.5x11x2.doc
```
- References should be formatted the same as in the example papers above . References are shown in the order in which they are cited . In other words, the first paper which is cited in the text should be numbered [1], the second paper cited should be numbered [2], and so on . Use the following guidelines for formatting your references . Do not use  Wikipedia , or web links for references . All references should be citing a published research paper - a conference paper , a journal paper , a textbook , or another published book .
```
https://owl.english.purdue.edu/owl/resource/560/06/
```
- Upload your Paper as a Word document in IEEE format to Canvas .
