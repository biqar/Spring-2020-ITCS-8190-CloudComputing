
/*
 * @Author: Arunkumar Bagavathi
 * */

package snippet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Main {
	
	static Map<Integer, List<List<String>>> AROutput = new HashMap<Integer, List<List<String>>>();
	static Map<Integer, List<List<String>>> AAROutput = new HashMap<Integer, List<List<String>>>();
	
	static String result = new String();
	
	
	public static void main(final String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		//Association Action Rules Configuration
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
				Configuration associationActionRuleConf = new Configuration();
			    Job associationActionRulesJob;
				try {
					associationActionRuleConf.setInt("Support", Integer.parseInt(args[4]));
				    associationActionRuleConf.setDouble("Confidence", Double.parseDouble(args[5]));
					
					associationActionRulesJob = Job.getInstance(associationActionRuleConf, "Association Action Rules");
					associationActionRulesJob.setJarByClass(AssociationActionRules.class);
				    associationActionRulesJob.setMapperClass(AssociationActionRules.JobMapper.class);
				    associationActionRulesJob.setReducerClass(AssociationActionRules.JobReducer.class);
//				    associationActionRulesJob.setNumReduceTasks(0);
				    
				    associationActionRulesJob.setOutputKeyClass(Text.class);
				    associationActionRulesJob.setOutputValueClass(Text.class);
				    
				    /*
				     * Adding commmon data to all maps to the DistributedCache
				     * These contents are accessed in setup() in the Mapper class
				     */
				    associationActionRulesJob.addCacheFile(new Path(args[0]).toUri());
				    associationActionRulesJob.addCacheFile(new Path(args[2]).toUri());
				    
				    
				    FileInputFormat.addInputPath(associationActionRulesJob, new Path(args[1]));
				    
				    FileOutputFormat.setOutputPath(associationActionRulesJob, new Path(args[3]));
				    associationActionRulesJob.waitForCompletion(true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
				
//			}
//		}){}.start();
		
		//Action Rules Configuration
//		Configuration actionRulesConf = new Configuration();
//		
//		actionRulesConf.setInt("Support", Integer.parseInt(args[4]));
//	    actionRulesConf.setInt("Confidence", Integer.parseInt(args[5]));
//		
//		Job actionRulesJob;
//		actionRulesJob = Job.getInstance(actionRulesConf, "Action Rules");
//				
//		actionRulesJob.setJarByClass(ActionRules.class);
//		actionRulesJob.setMapperClass(ActionRules.JobMapper.class);
//		actionRulesJob.setReducerClass(ActionRules.JobReducer.class);
//						   				    
//		actionRulesJob.setOutputKeyClass(Text.class);
//		actionRulesJob.setOutputValueClass(Text.class);
//				    
////		actionRulesJob.setNumReduceTasks(0);
//		/*
//	     * Adding commmon data to all maps to the DistributedCache
//	     * These contents are accessed in setup() in the Mapper class
//	     */
//		actionRulesJob.addCacheFile(new Path(args[0]).toUri());
//		actionRulesJob.addCacheFile(new Path(args[2]).toUri());
//				    
//		FileInputFormat.addInputPath(actionRulesJob, new Path(args[1]));
//				    
//		FileOutputFormat.setOutputPath(actionRulesJob, new Path(args[3]));
//		actionRulesJob.waitForCompletion(true);
//				    	    
//	   
//		readAndSplitFile(new Path(args[4] + "/part-r-00000").toString(), 1);
	}

	private static void readAndSplitFile(String string, int whichOutput) throws IOException {
		BufferedReader actionRulesOutputReader = new BufferedReader(new FileReader(string));
		 
		String readAROutput = actionRulesOutputReader.readLine();
		 
		int count = 1;
		
		while(readAROutput != null){
			List<String> suppConf = new ArrayList<String>();
			 
			String[] tempList = readAROutput.split("==>");
			List<String> actions = Arrays.asList(tempList[0].split("\\^"));
			Collections.sort(actions);
			 
//			if(result == null){
				result = tempList[1].split("\\[")[0];
//			}
			suppConf.add(tempList[1].split(":- ")[1].split(" ;")[0]);
			suppConf.add(tempList[1].split("Confidence:- ")[1].split(" ;")[0]);
		
			List<List<String>> finalValue = new ArrayList<List<String>>();
			finalValue.add(actions);
			finalValue.add(suppConf);
			
			switch(whichOutput){
				case 1:
					AROutput.put(count++, finalValue);
					break;
					
				case 2:
					AAROutput.put(count++, finalValue);
					break;
			}
			System.out.println(result);
			readAROutput = actionRulesOutputReader.readLine();
		
		}
		
	}
	
}