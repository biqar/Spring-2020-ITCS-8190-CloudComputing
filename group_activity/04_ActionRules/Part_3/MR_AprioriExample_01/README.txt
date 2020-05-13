Download source code here: http://webpages.uncc.edu/aatzache/ITCS6162/Project/StudentPrograms/MR_AprioriExample.zip

To run the Code:

1. Copy the attached ActionRules.jar file to your WinSCP or CyberDuck
2. Template of attributes and parameters file to be given to the code are given in attributes.txt and parameters.txt respectively
3. Create attributes.txt, parameters.txt in the given template for the respective dataset and copy to DSBA-HDFS using:
	hadoop fs -put /users/YOURUNCC_USERNAME/file_name.txt /user/YOURUNCC_USERNAME/


4. Run the .jar file using your terminal or Putty using following command:

	hadoop jar path-to-jar-file snippet.Main path-to-attributes-file path-to-data-file 
	path-to-parameters-file path-to-Output Minimum-Support Minimum-Confidence




