
/*
 * @Author: Arunkumar Bagavathi
 * */

package snippet;

import java.awt.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;


public class AssociationActionRules {

	public static class JobMapper extends Mapper<LongWritable, Text, Text, Text>{
				
		boolean falseParameters;
		int lineCount = 0;
		
		String decisionAttribute,decisionFrom,decisionTo;
		
		public static ArrayList<String> attributeNames,stableAttributes,stableAttributeValues,decisionFromTo;
		ArrayList<HashSet<String>> associations;
		
		static Map<ArrayList<String>, Integer> data;
		static Map<String, HashSet<String>> distinctAttributeValues,decisionValues;
		static Map<HashSet<String>, HashSet<String>> attributeValues;
		Map<ArrayList<ArrayList<String>>,ArrayList<String>> actionSets,derivedActionSets;
		Map<String, ArrayList<ArrayList<String>>> singleSet;
		
		public JobMapper(){
			super();
			
			attributeNames = new ArrayList<String>();
			stableAttributes = new ArrayList<String>();
			stableAttributeValues = new ArrayList<String>();
			decisionFromTo = new ArrayList<String>();
			associations = new ArrayList<HashSet<String>>();
			
			data = new HashMap<ArrayList<String>, Integer>();
			distinctAttributeValues = new HashMap<String, HashSet<String>>();
			decisionValues = new HashMap<String, HashSet<String>>();
			attributeValues = new HashMap<HashSet<String>, HashSet<String>>();
			actionSets = new HashMap<ArrayList<ArrayList<String>>, ArrayList<String>>();
			derivedActionSets = new HashMap<ArrayList<ArrayList<String>>, ArrayList<String>>();
			singleSet = new HashMap<String, ArrayList<ArrayList<String>>>();
		}
		
		@Override
		protected void setup(
				Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
						
			@SuppressWarnings("unused")
			Path[] files = context.getLocalCacheFiles();
			
			for (Path path : files) {
				
				//Reading Attribute file from the Distributed Cache
				if (path.getName().equals("attributes.txt")) {
					BufferedReader reader = new BufferedReader(new FileReader(path.toString()));
					String fileLine = reader.readLine();
					
					while(fileLine!=null){
						String[] tokens = fileLine.split("//s");
						attributeNames.add(tokens[0]);
						fileLine = reader.readLine();
					}
				}
				
				//Reading User Parameters file from the Distributed Cache
				else if(path.getName().equals("parameters.txt")){
					BufferedReader reader = new BufferedReader(new FileReader(path.toString()));
					String fileLine = reader.readLine();
					int counter = 0;
					
					while(fileLine!=null){
						counter++;
						
						StringTokenizer tokens = new StringTokenizer(fileLine);
						
						switch(counter){
							case 1:
								while(tokens.hasMoreTokens()){
									String attributeName = tokens.nextToken();
									if(checkInStringList(attributeNames, attributeName)){
										stableAttributes.add(attributeName);
									} else falseParameters = true;
								}
								break;
								
							case 2:
								if(tokens.countTokens() == 1){
									String attributeName = tokens.nextToken();
									if(checkInStringList(attributeNames, attributeName)){
										decisionAttribute = attributeName;
									} else falseParameters = true;
								} else falseParameters = true;
								break;
								
							case 3:
								if(tokens.countTokens() == 2){
									decisionFrom = decisionAttribute + tokens.nextToken();
									decisionTo = decisionAttribute + tokens.nextToken();
									
									decisionFromTo.add(decisionFrom);
									decisionFromTo.add(decisionTo);
								} else falseParameters = true;
								break;
													
						}
						
						fileLine = reader.readLine();
					}
				}
			}
			
			super.setup(context);
		}

		//Check if the value is present in the given list
		private boolean checkInStringList(ArrayList<String> list, String value){
			if(list.contains(value)){
				return true;
			}
			return false;
		}
		
		@Override
		protected void map(LongWritable key, Text inputValue,
				Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
						
			if(falseParameters){
				Text checkText = new Text("Error:");
				HashSet<String> errorMessage = new HashSet<String>();
				errorMessage.add("Invalid Parameters");
				
			}else{
				splitData(inputValue,lineCount);
				
				//Getting stable attribute values
				for(int i = 0;i<stableAttributes.size();i++){
					HashSet<String> distinctStableValues = distinctAttributeValues.get(stableAttributes.get(i));
					
					for (String string : distinctStableValues) {
						if(!stableAttributeValues.contains(string))
							stableAttributeValues.add(string);
						else continue;
					}
					
				}
				
				setDecisionAttributeValues();
			}
			
			
			lineCount++;
			
//			super.map(key, inputValue, context);
		}

		//Splitting the input data
		private void splitData(Text inputValue, int lineCount) {
			int lineNo = lineCount;
					
			String inputData = inputValue.toString();
			
			ArrayList<String> lineData = new ArrayList<String>(Arrays.asList(inputData.split("\t|,")));
				
			if(!checkEmptyValueInStringArray(lineData)){
				String key;
				
				lineNo++;
				
				ArrayList<String> tempList = new ArrayList<String>();
				HashSet<String> set;
							
				for (int j=0;j<lineData.size();j++) {
								
					//Creating proper attribute value like Aa1,Bb1
					String currentAttributeValue = lineData.get(j);
					String attributeName = attributeNames.get(j);
					key = attributeName + currentAttributeValue;
						
					tempList.add(key);
								
					if (distinctAttributeValues.containsKey(attributeName)) {
						set = distinctAttributeValues.get(attributeName);
						
					}else{
						set = new HashSet<String>();
					}
							
					set.add(key);
					
					distinctAttributeValues.put(attributeName, set);
				}
							
				if(!data.containsKey(tempList)){
					data.put(tempList,1);
				
					for(String listKey : tempList){
						HashSet<String> mapKey = new HashSet<String>();
						mapKey.add(listKey);
						setMap(attributeValues,mapKey,lineNo);
					}
				}
				else
					data.put(tempList, data.get(tempList) + 1);
			} 
								
		}
		
		//Checks if string array contains empty value
		private static boolean checkEmptyValueInStringArray(ArrayList<String> lineData) {
			return lineData.contains("") || lineData.contains("?");
		}
				
		//Sets Attribute values
		private static void setMap(Map<HashSet<String>, HashSet<String>> values,
				HashSet<String> key, int lineNo) {
			HashSet<String> tempSet = new HashSet<String>();
			
			if (values.containsKey(key)) {
				tempSet.addAll(values.get(key));						
			}
			
			tempSet.add("x"+lineNo);
			values.put(key, tempSet);
		}
				
		//Sets Decision attribute values
		private void setDecisionAttributeValues() {
			HashSet<String> distinctDecisionValues = distinctAttributeValues.get(decisionAttribute);
			for(String value : distinctDecisionValues){
				HashSet<String> newHash = new HashSet<String>();
				HashSet<String> finalHash = new HashSet<String>();
				newHash.add(value);
				
				if(decisionValues.containsKey(value)){
					finalHash.addAll(decisionValues.get(value));
				}
				if(attributeValues.containsKey(newHash))
					finalHash.addAll(attributeValues.get(newHash));
				
				decisionValues.put(value, finalHash);
			}
		}
				
		@Override
		protected void cleanup(
				Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			fillAttributeValues();
			
			while(!actionSets.isEmpty()){
				combineFrequentActions(context);
			}
			
			super.cleanup(context);
		}
		
		private void fillAttributeValues() {
			ArrayList<String> processedStableAttributes = new ArrayList<String>();
			
			for(Map.Entry<HashSet<String>, HashSet<String>> set : attributeValues.entrySet()){
				int support = set.getValue().size();
//				if(support >= minSupport){
					ArrayList<ArrayList<String>> outerSet = new ArrayList<ArrayList<String>>();
					ArrayList<String> innerSet = new ArrayList<String>();
					ArrayList<String> attributeNames= new ArrayList<String>();
					
					String primeAttribute = null;
					
					for(String element : set.getKey()){
						innerSet.add(element);
						innerSet.add(element);
						outerSet.add(innerSet);
						
						primeAttribute = getAttributeName(element);
						attributeNames.add(primeAttribute);
						attributeNames.add(String.valueOf(support));
					}
					
					
					if(primeAttribute != null){
						actionSets.put(outerSet, attributeNames);
						
						ArrayList<ArrayList<String>> forValue = new ArrayList<ArrayList<String>>();
						if(singleSet.get(primeAttribute) != null){
							forValue = singleSet.get(primeAttribute);
						}
						forValue.add(innerSet);
						
						singleSet.put(primeAttribute, forValue);
					
					
//						String subPrimeAttribute = primeAttribute;
						if(!isStable(primeAttribute) && !processedStableAttributes.contains(primeAttribute)){
							ArrayList<String> distinctAttributeValuesAAR = new ArrayList<String>();
							
							distinctAttributeValuesAAR.addAll(distinctAttributeValues.get(primeAttribute));
							
							//Combining all flexible attribute values (e.g) b1->b2
							for(int i=0;i<distinctAttributeValuesAAR.size();i++){
								for(int j=0;j<distinctAttributeValuesAAR.size();j++){
									
									if(i!=j){
										HashSet<String> left = new HashSet<String>(Arrays.asList(distinctAttributeValuesAAR.get(i)));
										HashSet<String> right = new HashSet<String>(Arrays.asList(distinctAttributeValuesAAR.get(j)));
										
										support = Math.min(attributeValues.get(left).size()
												, attributeValues.get(right).size());
										
										
//										if(support >= minSupport){
											processedStableAttributes.add(primeAttribute);
											
											outerSet = new ArrayList<ArrayList<String>>();
											innerSet = new ArrayList<String>();
																			
											innerSet.addAll(left);
											innerSet.addAll(right);
											
											outerSet.add(innerSet);
											attributeNames.set(1, String.valueOf(support));
											
											actionSets.put(outerSet, attributeNames);
											
											forValue = singleSet.get(primeAttribute);
											forValue.add(innerSet);
											
											singleSet.put(primeAttribute, forValue);
											
//										}
									}
								}
							}
						}
					}
//				}
			}
		}
		
		public static String getAttributeName(String value1) {
			for(Map.Entry<String, HashSet<String>> entryValue : distinctAttributeValues.entrySet()){
				if(entryValue.getValue().contains(value1)){
					return entryValue.getKey();
				}
			}
			return null;
		}
		
		public boolean isStable(String value){
			if(stableAttributeValues.containsAll(distinctAttributeValues.get(value)))
				return true;
			else return false;
		}
		
		
		private void combineFrequentActions(Mapper<LongWritable, Text, Text, Text>.Context context) {
	
			int minSupport = context.getConfiguration().getInt("Support",0);
			
			derivedActionSets = new HashMap<ArrayList<ArrayList<String>>, ArrayList<String>>();
			for (Map.Entry<ArrayList<ArrayList<String>>, ArrayList<String>> mainSet : actionSets.entrySet()) {
				ArrayList<ArrayList<String>> mainKey = mainSet.getKey();
				ArrayList<String> mainValue = mainSet.getValue();

				for(Map.Entry<String, ArrayList<ArrayList<String>>> attrSet : singleSet.entrySet()){
					String attrKey = attrSet.getKey();
						
					if(((mainValue.contains(decisionAttribute) && mainKey.contains(decisionFromTo)) || 
							(attrKey.equals(decisionAttribute) && attrSet.getValue().contains(decisionFromTo)))
							&& !mainValue.contains(attrKey)){
						
						for(ArrayList<String> extraSet : attrSet.getValue()){
							ArrayList<String> toCheckIn = new ArrayList<String>();
							ArrayList<String> toCheckOut = new ArrayList<String>(); 			
							ArrayList<String> newValue = new ArrayList<String>();
							ArrayList<ArrayList<String>> newKey = new ArrayList<ArrayList<String>>();
								
							newKey.addAll(mainKey);
							newKey.add(extraSet);
									
							newValue.addAll(mainValue.subList(0, mainValue.size()-1));
							newValue.add(attrKey);
									
							//To handle duplicates
							HashSet<String> newAssociation = new HashSet<String>();
							for (ArrayList<String> checkMultipleValues : newKey) {
								String toAddIntoAssociations = "";
								
								String left = checkMultipleValues.get(0);
								String right = checkMultipleValues.get(1);
									
								toCheckIn.add(left);
								toAddIntoAssociations += left + " -> ";
									
								toCheckOut.add(right);
								toAddIntoAssociations += right;
									
								newAssociation.add(toAddIntoAssociations);
							}
							
							ArrayList<String> sortedList = new ArrayList<String>(newAssociation);
							Collections.sort(sortedList);
							newAssociation.clear();
							newAssociation.addAll(sortedList);
							
							if(!associations.contains(newAssociation)){
								
								int support = Math.min(findLERSSupport(toCheckIn), findLERSSupport(toCheckOut));
								newValue.add(String.valueOf(support));
								
								
								
								if(support >= minSupport/4){
									derivedActionSets.put(newKey, newValue);
									printFrequentActions(newKey,newValue,context);
									associations.add(newAssociation);
								}
								
							}
						}
					}
				}
			}

			actionSets.clear();
			actionSets.putAll(derivedActionSets);
			derivedActionSets.clear();
			
		}
		
		private static int findLERSSupport(ArrayList<String> tempList) {
			int count = 0;
				
			for(Map.Entry<ArrayList<String>, Integer> entry : data.entrySet()){
				if(entry.getKey().containsAll(tempList)){
					count += entry.getValue();
				}
			}
				
			return count;
		}
		
		
		private void printFrequentActions(final ArrayList<ArrayList<String>> key,
				final ArrayList<String> value, final Mapper<LongWritable, Text, Text, Text>.Context context) {
		
			new Thread(new Runnable() {
				
				public void run() {					
					
					if(value.contains(decisionAttribute)){
						
						String rule = "", decision = "", decisionFrom="", decisionTo = "";
						int count = 0;
						ArrayList<String> actionFrom = new ArrayList<String>();
						ArrayList<String> actionTo = new ArrayList<String>(); 
						
						
						for (ArrayList<String> list : key) {
							if(value.get(count).equals(decisionAttribute)){
								decisionFrom = list.get(0);
								decisionTo = list.get(1);
								decision = "(" + value.get(count) + "," + decisionFrom + "->" + decisionTo + ")";
							}
							else{
								if(!rule.equals("")){
									rule += "^";
								}
								
								rule += "(" + value.get(count) + "," + list.get(0) + "->" + list.get(1) + ")";
					
								actionFrom.add(list.get(0));
								actionTo.add(list.get(1));
							}
					
							count++;
						}
					
						
						if(!rule.equals("") && !stableAttributeValues.containsAll(actionFrom)){
							rule += " ==> " + decision;
							
							final String finalRule = rule;
							final String finalDecisionFrom = decisionFrom;
							final String finalDecisionTo = decisionTo;
	
							String suppConf = calculateAARSSupport(actionFrom, actionTo, finalDecisionFrom, finalDecisionTo);
									
									
							if(!suppConf.equals("")){
								try {
									Text mapKey = new Text(finalRule);
									Text mapValue = new Text(suppConf);
											
									context.write(mapKey, mapValue);

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
						}
					}
				}
			}){}.start();
			
		}
		
		
		public String calculateAARSSupport(ArrayList<String> actionFrom, ArrayList<String> actionTo, 
				String decisionFrom, String decisionTo){
			String supportConfidence = "";
			
			ArrayList<String> leftRule = new ArrayList<String>();
			ArrayList<String> rightRule = new ArrayList<String>();
			
			leftRule.addAll(actionFrom);
			leftRule.add(decisionFrom);
			rightRule.addAll(actionTo);
			rightRule.add(decisionTo);
			
			double leftRuleSupport = findLERSSupport(leftRule);
			double rightRuleSupport = findLERSSupport(rightRule);
			double leftSupport = findLERSSupport(actionFrom);
			double rightSupport = findLERSSupport(actionTo);
			
			double support = Math.min(leftRuleSupport, rightRuleSupport);
			double confidence = (leftRuleSupport/leftSupport) * (rightRuleSupport/rightSupport) * 100;
			
			if(confidence > 0 && support > 0)
				supportConfidence = support+","+confidence;
			
			return supportConfidence;
		}
		
	}
	
	public static class JobReducer extends Reducer<Text,Text,Text,Text>{

		@Override
		protected void reduce(Text key, Iterable<Text> values,
				Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
			
			int mapperCount = Integer.parseInt(conf.get("mapred.map.tasks"));
			
			int totalSupport = 0, totalConfidence = 0;
			
			int sum = 0, minSuppConfMismatch = 0;
			
			for (Text string : values) {

				sum += 1;
				
				DecimalFormat df = new DecimalFormat("###.##");
				
				String[] suppConfidence = string.toString().split(",");
				double support = Double.parseDouble(suppConfidence[0]);
				double confidence = Double.valueOf(df.format(Double.parseDouble(suppConfidence[1])));
				
				int minSupport = conf.getInt("Support",0);
				double minConfidence = conf.getDouble("Confidence",0);
				
				if(support < minSupport || confidence < minConfidence){
					minSuppConfMismatch++;
				}
				
				totalSupport += support;
				totalConfidence += confidence;
			}
			
			if((minSuppConfMismatch < mapperCount/2 && sum >= mapperCount) || minSuppConfMismatch == 0){				
				context.write(key,new Text("[Support:- " + String.valueOf(totalSupport/sum) + " ; "
												+ "Confidence:- " + String.valueOf(totalConfidence/sum) + "%]"));
			}
						
//			super.reduce(key, values, context);
		}
	}
}
