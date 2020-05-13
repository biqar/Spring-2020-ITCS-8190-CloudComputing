
/*
 * @Author: Arunkumar Bagavathi
 * */

package snippet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class ActionRules{
	public static class JobMapper extends Mapper<LongWritable, Text, Text, Text>{

		int lineCount = 0;

		boolean falseParameters;
		
		double minSupport,minConfidence;
		
		String decisionAttribute,decisionFrom,decisionTo;
		
		public static ArrayList<String> attributeNames,stableAttributes,stableAttributeValues;
		public static ArrayList<ArrayList<String>> actionRules;
		
		static Map<ArrayList<String>, Integer> data;
		static Map<String, HashSet<String>> distinctAttributeValues,decisionValues;
		static Map<HashSet<String>, HashSet<String>> attributeValues,reducedAttributeValues;
		static Map<ArrayList<String>, HashSet<String>> markedValues,possibleRules;
		static Map<ArrayList<String>,String> certainRules;
		
		public JobMapper() {
			super();
			falseParameters = false;
			
			data = new HashMap<ArrayList<String>, Integer>();
			actionRules = new ArrayList<ArrayList<String>>();
			attributeNames = new ArrayList<String>();
			stableAttributes = new ArrayList<String>();
			stableAttributeValues = new ArrayList<String>();
		
			attributeValues = new HashMap<HashSet<String>, HashSet<String>>();
			reducedAttributeValues = new HashMap<HashSet<String>, HashSet<String>>();
			distinctAttributeValues = new HashMap<String, HashSet<String>>();
			decisionValues = new HashMap<String, HashSet<String>>();
			certainRules = new HashMap<ArrayList<String>, String>();
			markedValues = new HashMap<ArrayList<String>, HashSet<String>>();
			possibleRules = new HashMap<ArrayList<String>, HashSet<String>>();
		}
		
		/*
		 * 
		 * setup() function is to configure all mappers with the common data.
		 * In our case, it is attributes and user parameters.
		 * Since these values has to be given completely to all maps, we are using 
		 * 				them as global values for all maps. 
		 * 
		 */
		
		@Override
		protected void setup(
				Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			
			/*
			 * Stored Cache files are accessed here!!!
			 * The function used here is deprecated. I'm not able to find an alternative
			 * */
			@SuppressWarnings("unused")
			Path[] files = context.getLocalCacheFiles();
			
			for (Path path : files) {
				
				//Reading Attribute file from the Distributed Cache
				if (path.getName().equals("attributes.txt")) {
					BufferedReader reader = new BufferedReader(new FileReader(path.toString()));
					String fileLine = reader.readLine();
					
					while(fileLine!=null){
						String[] tokens = fileLine.split("\t");
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
							case 1://Line 1 in parameters.txt to get the stable attribute names
								while(tokens.hasMoreTokens()){
									String attributeName = tokens.nextToken();
									if(checkInStringList(attributeNames, attributeName)){
										stableAttributes.add(attributeName);
									} else falseParameters = true;
								}
								break;
								
							case 2://Line 2 in parameters.txt to get the decision attribute name
								if(tokens.countTokens() == 1){
									String attributeName = tokens.nextToken();
									if(checkInStringList(attributeNames, attributeName)){
										decisionAttribute = attributeName;
									} else falseParameters = true;
								} else falseParameters = true;
								break;
								
							case 3://Line 3 in parameters.txt to get decisionFrom and decisionTo attribute values
								if(tokens.countTokens() == 2){
									decisionFrom = decisionAttribute + tokens.nextToken();
									decisionTo = decisionAttribute + tokens.nextToken();
								} else falseParameters = true;
								break;
								
							case 4://Line 4 in parameters.txt to get Support and Confidence
//								if(tokens.countTokens() == 2){
//									minSupport = Double.parseDouble(tokens.nextToken());
//									minConfidence = Double.parseDouble(tokens.nextToken());
//								} else falseParameters = true;
//								break;
								
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


		/*
		 * 
		 * map() function reads only the data from the Input file
		 * We used in this way because map() functions runs algorithm for each line of the data
		 * Since LERS and ARoGS can be performed in a group of data, we are using this function 
		 * 				to read data and put them in separate Data Structures
		 * 
		 */
		@Override
		protected void map(
				LongWritable key,
				Text inputValue,
				Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			
			
			if(falseParameters){
				Text checkText = new Text("Error:");
				HashSet<String> errorMessage = new HashSet<String>();
				errorMessage.add("Invalid Parameters");
				
			}else{
				splitData(inputValue,lineCount);
				
			}
			
			
			lineCount++;
//			super.map(key, inputValue, context);
		}

		//Splitting the input data
		private void splitData(Text inputValue, int lineCount) {
			int lineNo = lineCount;
				
			String inputData = inputValue.toString();

			//This line splits only tab separated or comma separated data
			ArrayList<String> lineData = new ArrayList<String>(Arrays.asList(inputData.split("\t|,")));
				
			
			if(!checkEmptyValueInStringArray(lineData)){//Omitting the data line, if it contains empty value
				String key;
				
				lineNo++;
				
				ArrayList<String> tempList = new ArrayList<String>();
				HashSet<String> set;
					
				for (int j=0;j<lineData.size();j++) {
								
					//Creating proper attribute value like Aa1,Bb1 
					//since it would be easy to process them later in the algorithm
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
					//Setting attribute values to the corresponding attribute
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
				
		//Sets Attribute values - similar to lineNumber like x1,x2,x3,...
		private static void setMap(Map<HashSet<String>, HashSet<String>> values,
				HashSet<String> key, int lineNo) {
			HashSet<String> tempSet = new HashSet<String>();
			
			if (values.containsKey(key)) {
				tempSet.addAll(values.get(key));						
			}
			
			tempSet.add("x"+lineNo);
			values.put(key, tempSet);
		}
		
		//Sets Decision attribute values - line numbers for each decision values
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
				attributeValues.remove(newHash);
			}
		}
		
		/*
		 * This function implements both LERS and ARoGS algorithms
		 * */
		@Override
		protected void cleanup(
				final Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {

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
			
			performLERS();
			
//			for (Entry<HashSet<String>, HashSet<String>> certainRule : attributeValues.entrySet()) {
//				context.write(new Text(certainRule.getKey().toString()), new Text(certainRule.getValue().toString()));
//			}
			generateActionRules(context);
			
//			for (Map.Entry<String, HashSet<String>> entry : decisionValues.entrySet()) {
//				context.write(new Text(entry.getKey()), new Text(entry.getValue().toString()));
//			}
			

			super.cleanup(context);
		}
		
		//Finding LERS rules
		private void performLERS() {
			int loopCount;
			for(loopCount = 0;loopCount<5;loopCount++){
//			while(!attributeValues.isEmpty()){

				for (Map.Entry<HashSet<String>, HashSet<String>> set : attributeValues.entrySet()) {
					ArrayList<String> setKey = new ArrayList<String>();
					setKey.addAll(set.getKey());
					
					if (set.getValue().isEmpty()) {
						continue;
					}else{
						for(Map.Entry<String, HashSet<String>> decisionSet : decisionValues.entrySet()){
//							if(decisionSet.getValue().containsAll(set.getValue()) && calculateSupportLERS(setKey,decisionSet.getKey())!=0){
							if(decisionSet.getValue().containsAll(set.getValue())){
								certainRules.put(setKey, decisionSet.getKey());
								markedValues.put(setKey, set.getValue());
								break;
							}
						}
						
					}
							
					if(!markedValues.containsKey(setKey)){
						HashSet<String> possibleRulesSet = new HashSet<String>();
						for(Map.Entry<String, HashSet<String>> decisionSet : decisionValues.entrySet()){
//							if(calculateSupportLERS(setKey,decisionSet.getKey())!=0)
								possibleRulesSet.add(decisionSet.getKey());
						}
								
						if(possibleRulesSet.size()>0)
							possibleRules.put(setKey, possibleRulesSet);
					}
				}
				
				removeMarkedValues();
				
				combinePossibleRules();
			}
			
		}
		
		
		/*
		 * calculateSupportLERS and findLERSSupport calculates support
		 * (i.e) it checks which lines in the data contains the 'value'
		 * 
		 */ 
		static int calculateSupportLERS(ArrayList<String> key, String value) {
			ArrayList<String> tempList = new ArrayList<String>();
			
			for(String val : key){
				if(!val.equals(""))
					tempList.add(val);
			}
					
			if(!value.equals(""))
				tempList.add(value);
		
			return findLERSSupport(tempList);
					
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
		
		//Finds Confidence
		static String calculateConfidenceLERS(ArrayList<String> key,
				String value) {
			int num = calculateSupportLERS(key, value);
			int den = calculateSupportLERS(key, "");
			int confidence = 0;
			
			if(den!=0){
				confidence = (num * 100)/den;
			}
//				else{
//				commonMethods.printMessage("------------->Check this");
//			}
			
			return String.valueOf(confidence);
			
		}
		
		//removeMarkedValues removes duplicate certain rules
		private static void removeMarkedValues() {
			for(Map.Entry<ArrayList<String>, HashSet<String>> markedSet : markedValues.entrySet()){
				attributeValues.remove(new HashSet<String>(markedSet.getKey()));
			}
			
		}
				
		/*
		 * combinePossibleRules combines a possible rule with other supporting possible rules
		 * to form new certain rules
		 */
		private static void combinePossibleRules() {
			Set<ArrayList<String>> keySet = possibleRules.keySet();
			ArrayList<ArrayList<String>> keyList = new ArrayList<ArrayList<String>>();
			keyList.addAll(keySet);
			
			for(int i = 0;i<possibleRules.size();i++){
				for(int j = (i+1);j<possibleRules.size();j++){
					HashSet<String> combinedKeys = new HashSet<String>(keyList.get(i));
					combinedKeys.addAll(new HashSet<String>(keyList.get(j)));
					
					if(!checkSameGroup(combinedKeys)){
						combineAttributeValues(combinedKeys);
					}
				}
			}
								
			removeRedundantValues();
			clearAttributeValues();
			possibleRules.clear();
					
		}
		
		//checkSameGroup checks if the HashSet values fall under same attribute
		public static boolean checkSameGroup(HashSet<String> combinedKeys) {		
			ArrayList<String> combinedKeyAttributes = new ArrayList<String>();
			
			for(Map.Entry<String, HashSet<String>> singleAttribute : distinctAttributeValues.entrySet()){
				for(String key : combinedKeys){
					if(singleAttribute.getValue().contains(key)){
						if(!combinedKeyAttributes.contains(singleAttribute.getKey()))
								combinedKeyAttributes.add(singleAttribute.getKey());
						else return true;
					}
				}
			
			}
			
			return false;
		}
				
		//Performing Intersection of Sets
		private static void combineAttributeValues(HashSet<String> combinedKeys) {
			HashSet<String> combinedValues = new HashSet<String>();
					
			for(Map.Entry<HashSet<String>, HashSet<String>> attributeValue : attributeValues.entrySet()){
				if(combinedKeys.containsAll(attributeValue.getKey())){
					if(combinedValues.isEmpty()){
						combinedValues.addAll(attributeValue.getValue());
					}else{
						combinedValues.retainAll(attributeValue.getValue());
					}
				}
			}
			if(combinedValues.size()!=0)
				reducedAttributeValues.put(combinedKeys, combinedValues);
		}
				
		//Eliminating already marked values
		private static void removeRedundantValues() {
			ArrayList<HashSet<String>> mark = new ArrayList<HashSet<String>>();
			
			for(Map.Entry<HashSet<String>, HashSet<String>> reducedAttributeValue : reducedAttributeValues.entrySet()){
				for(Entry<ArrayList<String>, String> certainRule : certainRules.entrySet()){
					if(reducedAttributeValue.getKey().containsAll(certainRule.getKey())){
						mark.add(reducedAttributeValue.getKey());
						break;
					}
					
				}
				
			}
			
			for (HashSet<String> hashSet : mark) {
				reducedAttributeValues.remove(hashSet);
			}
			
//			HashSet<String> mark = new HashSet<String>();
//			
//			for(Map.Entry<HashSet<String>, HashSet<String>> reducedAttributeValue : reducedAttributeValues.entrySet()){
//				for(Map.Entry<HashSet<String>, HashSet<String>> attributeValue : attributeValues.entrySet()){
//					
//					if(attributeValue.getValue().containsAll(reducedAttributeValue.getValue()) || reducedAttributeValue.getValue().isEmpty()){
//						mark.addAll(reducedAttributeValue.getKey());
//						break;
//					}
//				}
//			}
//				
//			reducedAttributeValues.remove(mark);
			
		}
		
		//Refreshes the map attributeValues  
		private static void clearAttributeValues() {
			 attributeValues.clear();
			 for(Map.Entry<HashSet<String>, HashSet<String>> reducedAttributeValue : reducedAttributeValues.entrySet()){
				 attributeValues.put(reducedAttributeValue.getKey(), reducedAttributeValue.getValue());
			 }
			 reducedAttributeValues.clear();
		}

		//Generates Action Rules
		public ArrayList<String> generateActionRules(Mapper<LongWritable, Text, Text, Text>.Context context) throws IOException, InterruptedException {
			ArrayList<String> actionFrom, actionTo, rules;
			ArrayList<String> actions = null;
		
			String rule = "";
			rules = new ArrayList<String>();
		
			for(Map.Entry<ArrayList<String>, String> certainRules1 : certainRules.entrySet()){
				String certainRules1Value = certainRules1.getValue();
				
				if(certainRules1Value.equals(decisionFrom)){			
					
					for(Map.Entry<ArrayList<String>, String> certainRules2 : certainRules.entrySet()){
						ArrayList<String> certainRules1Key = certainRules1.getKey();
						ArrayList<String> certainRules2Key = certainRules2.getKey();
						
						/*
						 * Iteration proceeds only if certainRules1.value --> certainRules2.value is of the form decisionFrom  -->  decisionTo
						 * certainRules1.key is not a subset of certainRules2.key
						 * certainRules2.value should contain same stable attributes as in certainRules1.value
						 */
						
						if((certainRules1Key.equals(certainRules2Key)) || (!certainRules2.getValue().equals(decisionTo)) || 
								!checkStableAttributes(certainRules1Key,certainRules2Key)){
							continue;
						}
						else{
							String primeAttribute = "";						
							
							if(checkRulesSubSet(certainRules1Key,certainRules2Key)){
								
								ArrayList<String> checkCertainValues1 = certainRules1.getKey();
								ArrayList<String> tempList = new ArrayList<String>();
								
								rule = "";
								actionFrom = new ArrayList<String>();
								actionTo = new ArrayList<String>();
								actions = new ArrayList<String>();
								
								for(String value1 : checkCertainValues1){
									
									if(stableAttributeValues.contains(value1)){
										
										if(!actionTo.contains(value1)){
											rule=formRule(rule,value1,value1);
											
											actionFrom.add(value1);
											actionTo.add(value1);
											actions.add(getAction(value1, value1));
										}
										continue;
									}else{
										primeAttribute = getAttributeName(value1);
													
										ArrayList<String> checkCertainValues2 = certainRules2.getKey();
										for(String value2 : checkCertainValues2){
											
											if(stableAttributeValues.contains(value2)) {
												
												if(!actionTo.contains(value2)){
													rule=formRule(rule,value2,value2);
													
													actionFrom.add(value2);
													actionTo.add(value2);
													actions.add(getAction(value2, value2));
												}
												 
											}else if(!(getAttributeName(value2).equals(primeAttribute))){
												tempList.add(value2);
												
											}
											else if(getAttributeName(value2).equals(primeAttribute) && !actionTo.contains(value2)){
																								
												rule=formRule(rule,value1,value2);
												
												actionFrom.add(value1);
												actionTo.add(value2);
												actions.add(getAction(value1, value2));
											}
											
										}
									}
								}
								
								for(String missedValues:tempList){
									if(!actionTo.contains(missedValues)){
										rule=formRule(rule,"",missedValues);
										
										actionFrom.add("");
										actionTo.add(missedValues);
										actions.add(getAction("", missedValues));
									}
								}
								
								printActionRule(actionFrom, actionTo, actions, rule, context);
																
							}
						}
					}
				}
			}
			
			return rules;
		}
		
		//Checks if key's stable attributes are the subset of key2's stabe attributes 
		private static boolean checkStableAttributes(ArrayList<String> key,
				ArrayList<String> key2) throws IOException, InterruptedException {
			List<String> stableAttributesList1 = new ArrayList<String>();
			List<String> stableAttributesList2 = new ArrayList<String>();
			
			for(String value : key){
				if(stableAttributeValues.contains(value))
					stableAttributesList1.add(value);
			}
			
			for(String value : key2){
				if(stableAttributeValues.contains(value))
					stableAttributesList2.add(value);
			}
			
			if(stableAttributesList2.containsAll(stableAttributesList1))
				return true;
			else return false;
		}
		
		//Checks if certainRules2's flexible attributes contain all certainRules1's flexible attributes
		private boolean checkRulesSubSet(
				ArrayList<String> certainRules1,
				ArrayList<String> certainRules2) {
			ArrayList<String> primeAttributes1 = new ArrayList<String>();
			ArrayList<String> primeAttributes2 = new ArrayList<String>();
			
			for (String string : certainRules1) {
				String attributeName = getAttributeName(string);
				
				if(!isStable(attributeName))
					primeAttributes1.add(attributeName);
			}
			
			for (String string : certainRules2) {
				String attributeName = getAttributeName(string);
				
				if(!isStable(attributeName))
					primeAttributes2.add(attributeName);
			}
			
			if(primeAttributes2.containsAll(primeAttributes1))
				return true;
			else return false;
		}
		
		
		//Returns attribute name of a value
		public static String getAttributeName(String value1) {
			for(Map.Entry<String, HashSet<String>> entryValue : distinctAttributeValues.entrySet()){
				if(entryValue.getValue().contains(value1)){
					return entryValue.getKey();
				}
			}
			return null;
		}
		
		//Creates a rule of format "(x,x1 -> x2) ^ (y,y1 -> y2)"
		private static String formRule(String rule,String value1, String value2) {
			if(!rule.isEmpty())
				rule += "^";
			
			rule += "(" + getAttributeName(value2) + "," + getAction(value1,value2) + ")";
			return rule;
		}
		
		//Forms action of format "left -> right"
		private static String getAction(String left,String right){
			return left + "->" + right;
		}
		
		/*
		 * Prints an action rule and calls ARoGS function(printExtraRules()) if
		 * the rule is not a duplicate 
		 */
		private void printActionRule(final ArrayList<String> actionFrom,final ArrayList<String> actionTo,
				final ArrayList<String> actions,final String rule, final Mapper<LongWritable, Text, Text, Text>.Context context) 
						throws IOException, InterruptedException {
			
			new Thread(new Runnable() {
				
				public void run() {
					final int support = calculateSupportLERS(actionTo,decisionTo);
					
					if(support!=0){
						final String oldConfidence = String.valueOf((Integer.parseInt(calculateConfidenceLERS(actionFrom,decisionFrom)) * 
															Integer.parseInt(calculateConfidenceLERS(actionTo,decisionTo))/100));
						final String newConfidence = calculateConfidenceLERS(actionTo,decisionTo);
						
						
						if(!oldConfidence.equals("0") 
								&& !newConfidence.equals("0")){
							
							if(actions!=null){
													
								Collections.sort(actions);
								if(!actionRules.contains(actions)){
									actionRules.add(actions);
													
									try {
										Text key = new Text(rule + " ==> " + "(" + decisionAttribute + "," + decisionFrom + "->" + decisionTo +")");
										Text value = new Text(support + "," + newConfidence + "," + oldConfidence);
										context.write(key , value);
												
										printExtraRules(actionFrom, actionTo, context);
									} 
									catch (IOException e) {
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
				}
				
				//Performs ARoGS functionality
				private void printExtraRules(final ArrayList<String> actionFrom,final ArrayList<String> actionTo,
						final Mapper<LongWritable, Text, Text, Text>.Context context) throws IOException, InterruptedException{
					
					ArrayList<String> stableValues = getStableValues(actionTo);
					ArrayList<String> attributeValues = getAttributeValues(stableValues,decisionFrom,actionFrom);
							
					ArrayList<String> toBeAddedAttributes = getNewAttributes(actionFrom,actionTo,stableValues);
					ArrayList<String> tempAttributeValues;
									
					for(String attributeValue : toBeAddedAttributes){
						tempAttributeValues = new ArrayList<String>();
						tempAttributeValues.add(attributeValue);
					
						ArrayList<String> checkList = getAttributeValues(stableValues,"",tempAttributeValues);	
						if(attributeValues.containsAll(checkList) && !checkList.isEmpty()){
							String subRule = new String();
							ArrayList<String> subActionFrom = new ArrayList<String>();
							ArrayList<String> subActionTo = new ArrayList<String>();
							ArrayList<String> subActions = new ArrayList<String>();
							
							if(isStable(getAttributeName(attributeValue))){
								subActionFrom.addAll(actionFrom);
								subActionFrom.add(attributeValue);
								
								subActionTo.addAll(actionTo);
								subActionTo.add(attributeValue);
							}
							else{
								subActionFrom = getSubActionFrom(actionFrom,actionTo,attributeValue);
								subActionTo.addAll(actionTo);
							}
							subRule = getSubRule(subActionFrom,subActionTo,subActions);
							
							try {
								printActionRule(subActionFrom, subActionTo, subActions, subRule,context);
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
				
				
			}){}.start();
				
		}
		
		
		private ArrayList<String> getNewAttributes(ArrayList<String> actionFrom,
				ArrayList<String> actionTo,ArrayList<String> stableValues) {
			ArrayList<String> stableAttributes = new ArrayList<String>();
			ArrayList<String> flexibleAttributes = new ArrayList<String>();
			ArrayList<String> newAttributes = new ArrayList<String>();
			
			for (String string : stableValues) {
				stableAttributes.add(getAttributeName(string));
			}
			
			for (String string : actionTo){
				flexibleAttributes.add(getAttributeName(string));
			}
			
			for(Map.Entry<String, HashSet<String>> mapValue : distinctAttributeValues.entrySet()){
				String mapKey = mapValue.getKey();
				HashSet<String> mapValues = mapValue.getValue();
				
				if(mapKey.equals(decisionAttribute) || (stableAttributes.size()!=0 && stableAttributes.contains(mapKey))){
					continue;
				} else if(isStable(mapKey)){
					newAttributes.addAll(mapValues);
				} else if(!flexibleAttributes.isEmpty() && flexibleAttributes.contains(mapKey)){
					
					for(String setValue : mapValues){					
						if(!actionFrom.contains(setValue) && !actionTo.contains(setValue)){
							newAttributes.add(setValue);
						}
					}
				}
			}
			
			return newAttributes;
		}
		
		//Returns stable attribute values from the given ArrayList actionFrom
		private ArrayList<String> getStableValues(ArrayList<String> actionFrom) {
			ArrayList<String> stableValues = (ArrayList<String>) stableAttributeValues;
			ArrayList<String> toBeAdded = new ArrayList<String>();
			
			for(String value : actionFrom){
				if(stableValues.contains(value)){
					toBeAdded.add(value);
				}else{
					continue;
				}
				
			}
			
			return toBeAdded;
		}
		
		//Returns data lines which contains all given stableValues,actionFrom and decisionFrom
		private ArrayList<String> getAttributeValues(ArrayList<String> stableValues,String decisionFrom, ArrayList<String> actionFrom) {
			ArrayList<String> temp = new ArrayList<String>();
			ArrayList<String> attributeValues = new ArrayList<String>();
			int lineCount = 0;
			
			temp.addAll(stableValues);
			
			for(String from : actionFrom){
				if(!from.equals("")){
					temp.add(from);
				}
			}
			
			if(!decisionFrom.equals(""))
				temp.add(decisionFrom);
			
			
			for(Map.Entry<ArrayList<String>, Integer> data : data.entrySet()){
				lineCount++;
				
				if(data.getKey().containsAll(temp)){
					attributeValues.add("x" + lineCount);
				}
			}
			
			return attributeValues;
		}
		
		
		private ArrayList<String> getSubActionFrom(ArrayList<String> actionFrom,
				ArrayList<String> actionTo,String alternateActionFrom) {
			ArrayList<String> finalActionFrom = new ArrayList<String>();
			HashSet<String> checkSameSet;
		
			for (int i = 0;i<actionTo.size();i++) {
				checkSameSet = new HashSet<String>();
				
				checkSameSet.add(alternateActionFrom);
				checkSameSet.add(actionTo.get(i));
				
				if(checkSameGroup(checkSameSet)){
					finalActionFrom.add(alternateActionFrom);
					
				}else{
					if(i<actionFrom.size()){
						finalActionFrom.add(actionFrom.get(i));
					}
				}
				
			}
			
			return finalActionFrom;
		}
		
		private String getSubRule(ArrayList<String> subActionFrom,
				ArrayList<String> subActionTo, ArrayList<String> subActions) {
			String rule = "";
			
			for (int i = 0; i < subActionFrom.size(); i++) {
				rule = formRule(rule, subActionFrom.get(i), subActionTo.get(i));
				subActions.add(getAction(subActionFrom.get(i), subActionTo.get(i)));			
			}
			
			return rule;
		}
		
		//Checks if the given value is stable
		public boolean isStable(String value){
			if(stableAttributeValues.containsAll(distinctAttributeValues.get(value)))
				return true;
			else return false;
		}
		
	}
	
	public static class JobReducer extends Reducer<Text,Text,Text,Text>{
		//Performs Random Forest algorithm
		@Override
		protected void reduce(Text key, Iterable<Text> values,
				Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			
			Configuration conf = context.getConfiguration();
			
			int mapperCount = Integer.parseInt(conf.get("mapred.map.tasks"))/2;
			
			int totalSupport = 0;
			double	totalNewConfidence = 0.0, totalOldConfidence = 0.0;
			
			int sum = 0, minSuppConfMismatch = 0;
			
			for (Text string : values) {
				
				sum += 1;
				
				DecimalFormat df = new DecimalFormat("###.##");
				
				String[] suppConfidence = string.toString().split(",");
				int support = Integer.parseInt(suppConfidence[0]);
				double newConfidence = Double.valueOf(df.format(Double.parseDouble(suppConfidence[1])));
				double oldConfidence = Double.valueOf(df.format(Double.parseDouble(suppConfidence[2])));
				
				int minSupport = conf.getInt("Support",0);
				double minConfidence = conf.getDouble("Confidence",0);
				
				if(support < minSupport || newConfidence < minConfidence || oldConfidence < minConfidence){
					minSuppConfMismatch++;
				}
				
				totalSupport += support;
				totalNewConfidence += newConfidence;
				totalOldConfidence += oldConfidence;
			}
			
			if((minSuppConfMismatch < mapperCount && sum >= mapperCount) || minSuppConfMismatch == 0){				
				context.write(key,new Text("[Support:- " + String.valueOf(totalSupport/sum) + " ; "
												+ "New Confidence:- " + String.valueOf(totalNewConfidence/sum) + "%" + " ; "
												+ "Old Confidence:- " + String.valueOf(totalOldConfidence/sum) + "%]"));
			}
			
			
//			super.reduce(key, values, context);
		}
		
	}
		
}