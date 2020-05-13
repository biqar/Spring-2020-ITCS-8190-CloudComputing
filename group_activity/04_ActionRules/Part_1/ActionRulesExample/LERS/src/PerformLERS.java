import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import lers.Common;
import lers.StringsDAO;


public class PerformLERS {
	static Scanner input;
	static Common commonMethods;
	static StringsDAO commonStrings;
	
	public static List<String> attributeNames;
	public static List<String> stableAttributes;
	public static List<String> flexibleAttributes;
	public static ArrayList<ArrayList<String>> duplicateData;
	public static Map<ArrayList<String>,Integer> data;
	
	public static String decisionAttribute,decisionFrom,decisionTo; 
	
	static Map<String, HashSet<String>> distinctAttributeValues;
	static Map<HashSet<String>, HashSet<String>> attributeValues;
	static Map<HashSet<String>, HashSet<String>> reducedAttributeValues;
	static Map<String, HashSet<String>> decisionValues;
	static Map<ArrayList<String>, HashSet<String>> markedValues;
	public static Map<ArrayList<String>,String> certainRules;
	public static Map<ArrayList<String>,HashSet<String>> possibleRules;
	
	public PerformLERS() throws IOException{
		commonMethods = new Common();
		commonStrings = new StringsDAO();
		
		decisionAttribute = new String();
		decisionFrom = new String();
		decisionTo = new String();
		
		attributeNames = new ArrayList<String>();
		stableAttributes = new ArrayList<String>();
		flexibleAttributes = new ArrayList<String>();
//		data = new ArrayList<ArrayList<String>>();
		duplicateData = new ArrayList<ArrayList<String>>();
		
		distinctAttributeValues = new HashMap<String, HashSet<String>>();
		attributeValues = new HashMap<HashSet<String>, HashSet<String>>();
		reducedAttributeValues = new HashMap<HashSet<String>, HashSet<String>>();
		decisionValues = new HashMap<String, HashSet<String>>();
		markedValues = new HashMap<ArrayList<String>, HashSet<String>>();
		certainRules = new HashMap<ArrayList<String>,String>();
		possibleRules = new HashMap<ArrayList<String>,HashSet<String>>();
		data = new HashMap<ArrayList<String>, Integer>();
	}
	
	
	//Required getters and setters	
	
	
	public static Map<String, HashSet<String>> getDecisionValues() {
		return decisionValues;
	}

	public Map<ArrayList<String>, String> getCertainRules() {
		return certainRules;
	}


	public List<String> getStableAttributes() {
		return stableAttributes;
	}

	public static String getDecisionFrom() {
		return decisionFrom;
	}
	
	public static String getDecisionTo() {
		return decisionTo;
	}
	
	public Map<String, HashSet<String>> getDistinctAttributeValues() {
		return distinctAttributeValues;
	}
	
	public String getDecisionAttribute() {
		return decisionAttribute;
	}
	
	public static Map<ArrayList<String>,Integer> getData() {
		return data;
	}
	
	public static void setStableAttributes(List<String> stableAttributes) {
		PerformLERS.stableAttributes = stableAttributes;
	}

	public static void setCertainRules(Map<ArrayList<String>, String> certainRules) {
		PerformLERS.certainRules = certainRules;
	}

	public static void setDecisionFrom(String decisionFrom) {
		PerformLERS.decisionFrom = decisionFrom;
	}

	public static void setDecisionTo(String decisionTo) {
		PerformLERS.decisionTo = decisionTo;
	}

	public static void setDistinctAttributeValues(
			Map<String, HashSet<String>> distinctAttributeValues) {
		PerformLERS.distinctAttributeValues = distinctAttributeValues;
	}

	public static void setDecisionAttribute(String decisionAttribute) {
		PerformLERS.decisionAttribute = decisionAttribute;
	}

//	public static void setData(ArrayList<ArrayList<String>> data) {
//		PerformLERS.data = data;
//	}

	public static void setDecisionValues(Map<String, HashSet<String>> decisionValues) {
		PerformLERS.decisionValues = decisionValues;
	}
	
	public static Map<HashSet<String>, HashSet<String>> getAttributeValues() {
		return attributeValues;
	}


	public static void setAttributeValues(
			Map<HashSet<String>, HashSet<String>> attributeValues) {
		PerformLERS.attributeValues = attributeValues;
	}


	public void readAndPrintFiles() {
		readAttributes();
		readData();
		
//		ArrayList<String> checkList = new ArrayList<String>();
//		checkList.add("A2");
//		checkList.add("B1");
//		checkList.add("C1");
//		checkList.add("D1");
		
//		for(Map.Entry<ArrayList<String>, Integer> tempData : data.entrySet()){
//			commonMethods.printMessageInNewLine("Checking Data - " + data.containsKey(checkList));
//		}
		
//		commonMethods.printListInList(attributeNames,data);
//		commonMethods.printList(attributeNames);
	}
	
	//Reading attributes file
	private static void readAttributes() {
		try {
			input = new Scanner(new File(commonStrings.ATTRIBUTES_FILE_PATH));
			
			while (input.hasNext()) {
				String attributeName = input.next(); 
				
				attributeNames.add(attributeName);	
//				distinctAttributeValues.put(attributeName, new HashSet<String>());
			}
			
		} catch (FileNotFoundException e) {
			commonMethods.printMessage(commonStrings.FILE_NOT_FOUND);
			e.printStackTrace();
		}
			
	}
	
	//Reading data file
	private static void readData() {
		try {
			input = new Scanner(new File(commonStrings.DATA_FILE_PATH));
			int lineNo = 0;
//			
//			String splittingCharacter = getSplittingCharacter(input);
//			
//			input = new Scanner(new File(commonStrings.DATA_FILE_PATH));
			
			while(input.hasNextLine()){
//				String[] lineData = input.nextLine().split(splittingCharacter);
//				String[] lineData = input.nextLine().split("\t|,");
				ArrayList<String> lineData = new ArrayList<String>(Arrays.asList(input.nextLine().split("\t|,")));
				
				if(!checkEmptyValueInStringArray(lineData)){
					String key;
					
					lineNo++;
					ArrayList<String> tempList = new ArrayList<String>();
					HashSet<String> set;
					
//					for (int i=0;i<lineData.length;i++) {
					for (int i=0;i<lineData.size();i++) {
						String currentAttributeValue = lineData.get(i);
						String attributeName = attributeNames.get(i);
						key = attributeName + currentAttributeValue;
						
						tempList.add(key);
	
						
						
						if (distinctAttributeValues.containsKey(attributeName)) {
							set = distinctAttributeValues.get(attributeName);
							set.add(key);
							
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
//						duplicateData.add(tempList);
					
				}else{
					continue;
				}
			}
			
			
		} catch (FileNotFoundException e) {
			commonMethods.printMessage(commonStrings.FILE_NOT_FOUND);
			e.printStackTrace();
		}
	}
	

	//Checks if string array contains empty value
//	private static boolean checkEmptyValueInStringArray(String[] lineData) {
	private static boolean checkEmptyValueInStringArray(ArrayList<String> lineData) {
		return lineData.contains(commonStrings.EMPTY) || lineData.contains("?");
	}
	
	//Sets Attribute values
	private static void setMap(Map<HashSet<String>, HashSet<String>> values,
			HashSet<String> key, int lineNo) {
		HashSet<String> tempSet;
		
		if (values.containsKey(key)) {
			tempSet = values.get(key);						
		}else{
			tempSet = new HashSet<String>();
		}
		
		tempSet.add("x"+lineNo);
		values.put(key, tempSet);
	}

	
	public int getUserChoice(String choices){
		commonMethods.printMessageInNewLine(choices);
		
		input = new Scanner(System.in);
		int choice = input.nextInt();
		
		return choice;
	}
	
	public void setStableAttributes(int which) {

		commonMethods.printMessageInNewLine(commonStrings.GIVE_CHOICE);
		
		input = new Scanner(System.in);
		int choice = input.nextInt();
		
		if(choice==1){
			commonMethods.printMessageInNewLine(commonStrings.ATTRIBUTES_AVAILABLE);
			commonMethods.printMessageInNewLine(commonStrings.UNDERLINE);
			commonMethods.printList(attributeNames);
			commonMethods.printMessageInNewLine(commonStrings.ENTER_STABLE_ATTR);
			
			String userStableAttribute = input.next();
			if(checkValid(attributeNames,userStableAttribute)){
				stableAttributes.addAll(distinctAttributeValues.get(userStableAttribute));
				attributeNames.remove(userStableAttribute);
			}else{
				commonMethods.printMessageInNewLine(commonStrings.INVALID_VALUE);
			}
			commonMethods.printMessageInNewLine("Stable Attributes " + stableAttributes);
			setStableAttributes(which);
		}
		
		else if(choice==2){
			setDecisionAttribute(attributeNames,which);
		}
		
		else{
			commonMethods.printMessageInNewLine(commonStrings.INVALID_VALUE);
			setStableAttributes(which);
		}
	}
	
	//Check if list contains an attribute
	private static boolean checkValid(List<String> attributes,String userStableAttribute) {
		if(attributes.contains(userStableAttribute))
			return true;
		else return false;
	}
	
	//Queries user about the decision attribute
	private static void setDecisionAttribute(List<String> attributes,int which) {
		commonMethods.printMessageInNewLine("Data size " + attributeValues.size());
		
		commonMethods.printMessageInNewLine(commonStrings.ATTRIBUTES_AVAILABLE);
		commonMethods.printMessageInNewLine(commonStrings.UNDERLINE);
		commonMethods.printList(attributes);
		commonMethods.printMessageInNewLine(commonStrings.ENTER_DECISION_ATTR);
		
		input = new Scanner(System.in);
		decisionAttribute = input.next();
		
		if (checkValid(attributes,decisionAttribute)) {
			attributes.remove(decisionAttribute);
			flexibleAttributes = attributes;
			
			HashSet<String> decisionValues = distinctAttributeValues.get(decisionAttribute);
			
			if(which==1)
				removeDecisionValueFromAttributes(decisionValues);
			
			getDecisionFromValue();
		}else{
			commonMethods.printMessageInNewLine(commonStrings.INVALID_VALUE);
			setDecisionAttribute(attributes,which);
		}
		
	}
	
	private static void removeDecisionValueFromAttributes(HashSet<String> decisionValues) {
		for(String value : decisionValues){
			HashSet<String> newHash = new HashSet<String>();
			newHash.add(value);
			PerformLERS.decisionValues.put(value, attributeValues.get(newHash));
			attributeValues.remove(newHash);
		}
	}
	
	//Gets decisionFrom value from the user
	public static void getDecisionFromValue() {
						
		HashSet<String> decisionValues = distinctAttributeValues.get(decisionAttribute);
			
		commonMethods.printMessageInNewLine(commonStrings.DECISIONS_AVAILABLE);
		commonMethods.printHashSet(decisionValues);
		commonMethods.printMessageInNewLine(commonStrings.CHANGE_FROM);
			
		input = new Scanner(System.in);
		setDecisionFrom(input.next());
		if (decisionValues.contains(getDecisionFrom())) {
			getDecisionToValue(decisionValues);
		}
		else{
			commonMethods.printMessageInNewLine(commonStrings.INVALID_VALUE);
			getDecisionFromValue();
		}
	}
			
//	Gets decisionTo value from the user
	private static void getDecisionToValue(HashSet<String> decisionValues) {
		commonMethods.printMessageInNewLine(commonStrings.DECISIONS_AVAILABLE);
			
		commonMethods.printHashSet(decisionValues);
		commonMethods.printMessageInNewLine(commonStrings.CHANGE_TO);
			
		input = new Scanner(System.in);
		setDecisionTo(input.next());
		if (decisionValues.contains(getDecisionTo())) {
			
			return;
		}
		else{
			commonMethods.printMessageInNewLine(commonStrings.INVALID_VALUE);
			getDecisionToValue(decisionValues);
		}
			
	}
	
	//Finding Certain and Possible rules
	public void findRules() {
		int loopCount = 0;
		
		while(!attributeValues.isEmpty()){
		
//		for(int i = 0; i<3; i++){
			for (Map.Entry<HashSet<String>, HashSet<String>> set : attributeValues.entrySet()) {			
				
				ArrayList<String> setKey = new ArrayList<String>();
				setKey.addAll(set.getKey());
				
				HashSet<String> setValue = set.getValue();
				
				if (!setValue.isEmpty()) {

					for(Map.Entry<String, HashSet<String>> decisionSet : decisionValues.entrySet()){
//						if(decisionSet.getValue().containsAll(set.getValue()) && calculateSupportLERS(setKey,decisionSet.getKey())!=0){
						
						String decisionSetKey = decisionSet.getKey();
						if((decisionSetKey.equals(decisionFrom) || decisionSetKey.equals(decisionTo)) && decisionSet.getValue().containsAll(setValue)){
							certainRules.put(setKey, decisionSet.getKey());
							markedValues.put(setKey, setValue);
							break;
						}
					}
				}
				
				if(!markedValues.containsKey(setKey)){
					HashSet<String> possibleRulesSet = new HashSet<String>();
					for(Map.Entry<String, HashSet<String>> decisionSet : decisionValues.entrySet()){
//						if(calculateSupportLERS(setKey,decisionSet.getKey())!=0)
						
						String decisionSetKey = decisionSet.getKey();
						
						if((decisionSetKey.equals(decisionFrom) || decisionSetKey.equals(decisionTo)))
							possibleRulesSet.add(decisionSet.getKey());
					}
					
					if(possibleRulesSet.size()>0)
						possibleRules.put(setKey, possibleRulesSet);
				}	
				
			}
			
			removeMarkedValues();
			

			new Thread(new Runnable() {
				
				@Override
				public void run() {
//					commonMethods.printNewLine();
//					printCertainRulesMap(certainRules);
//					printPossibleRulesMap(possibleRules);
					
				}
			}).start();
						
//			commonMethods.printMessageInNewLine("Loop " + ++loopCount + " Attribute Values count:" + attributeValues.size());
			combinePossibleRules();
		}
	}
	
	
	//Removing marked sets
	private static void removeMarkedValues() {
		for(Map.Entry<ArrayList<String>, HashSet<String>> markedSet : markedValues.entrySet()){
			attributeValues.remove(new HashSet<String>(markedSet.getKey()));
		}
		
	}
	
	//Printing Certain Rules
	private static void printCertainRulesMap(Map<ArrayList<String>, String> value) {
		commonMethods.printMessageInNewLine(commonStrings.CERTAIN_RULES + commonStrings.COLON);
		commonMethods.printMessageInNewLine(commonStrings.UNDERLINE);
		for(Map.Entry<ArrayList<String>,String> set : value.entrySet()){
			int support = calculateSupportLERS(set.getKey(),set.getValue());
			String confidence = calculateConfidenceLERS(set.getKey(),set.getValue());
			
			commonMethods.printLERSRules(set.getKey().toString(), set.getValue());
			commonMethods.printSupportAndConfidence(support, confidence);			
		}
		commonMethods.printNewLine();
	}
	
	//Printing Possible Rules
	private static void printPossibleRulesMap(Map<ArrayList<String>, HashSet<String>> value) {
		
		if(!value.isEmpty()){
			commonMethods.printMessageInNewLine(commonStrings.POSSIBLE_RULES + commonStrings.COLON);
			commonMethods.printMessageInNewLine(commonStrings.UNDERLINE);
			for(Map.Entry<ArrayList<String>,HashSet<String>> set : value.entrySet()){
				for(String possibleValue:set.getValue()){
					int support = calculateSupportLERS(set.getKey(),possibleValue);
					String confidence = calculateConfidenceLERS(set.getKey(),possibleValue);
					
					commonMethods.printLERSRules(set.getKey().toString(), possibleValue);
					commonMethods.printSupportAndConfidence(support, confidence);
				}

			}
			commonMethods.printNewLine();
		}
	}

	//Finding Support
	public static int findLERSSupport(ArrayList<String> tempList) {
		int count = 0;
		
//		commonMethods.printMessageInNewLine(tempList.toString() + " is - " + data.containsKey(tempList));
//		if(data.containsKey(tempList))
//			count = data.get(tempList);
		
		for(Map.Entry<ArrayList<String>, Integer> entry : data.entrySet()){
			if(entry.getKey().containsAll(tempList)){
				count += entry.getValue();
			}
		}
		
//		count = Collections.frequency(data , tempList);
//		count += Collections.frequency(duplicateData , tempList);
//		for(ArrayList<String> data : data){	
//			if(data.containsAll(tempList))
//				count++;
//		}
//		
//		if(duplicateData.containsAll(tempList)){
//			for(ArrayList<String> data : duplicateData){	
//				if(data.containsAll(tempList))
//					count++;
//			}
//		}
		
		return count;
	}
	
	static int calculateSupportLERS(ArrayList<String> key, String value) {
		ArrayList<String> tempList = new ArrayList<String>();
		
		for(String val : key){
			if(!val.equals(commonStrings.EMPTY))
				tempList.add(val);
		}
		
		if(!value.equals(commonStrings.EMPTY))
			tempList.add(value);
	
		return findLERSSupport(tempList);
		
	}

	//Finds Confidence
	static String calculateConfidenceLERS(ArrayList<String> key,
			String value) {
		int num = calculateSupportLERS(key, value);
		int den = calculateSupportLERS(key, commonStrings.EMPTY);
		int confidence = 0;
		
		if(den!=0){
			confidence = (num / den) * 100;
		}
//			else{
//			commonMethods.printMessage("------------->Check this");
//		}
		
		return String.valueOf(confidence);
		
	}
	
	
	//Combining Possible Rules
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
	
	//Checks if the values belong to the same attribute
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
//				commonMethods.printMessage("Combining " + combinedValues.toString() + " & " + attributeValue.toString());
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
	
	private static void removeRedundantValues() {
		HashSet<String> mark = new HashSet<String>();
		
		for(Map.Entry<HashSet<String>, HashSet<String>> reducedAttributeValue : reducedAttributeValues.entrySet()){
			for(Map.Entry<HashSet<String>, HashSet<String>> attributeValue : attributeValues.entrySet()){
				
				if(attributeValue.getValue().containsAll(reducedAttributeValue.getValue()) || reducedAttributeValue.getValue().isEmpty()){
					mark.addAll(reducedAttributeValue.getKey());
				}
			}
		}
		
		reducedAttributeValues.remove(mark);
		
		
	}
	
	private static void clearAttributeValues() {
		 attributeValues.clear();
		 for(Map.Entry<HashSet<String>, HashSet<String>> reducedAttributeValue : reducedAttributeValues.entrySet()){
			 attributeValues.put(reducedAttributeValue.getKey(), reducedAttributeValue.getValue());
		 }
		 reducedAttributeValues.clear();
	}
	
	public boolean isStable(String value){
		if(stableAttributes.containsAll(distinctAttributeValues.get(value)))
			return true;
		else return false;
	}
	
	public static String getAttributeName(String value1) {
		for(Map.Entry<String, HashSet<String>> entryValue : distinctAttributeValues.entrySet()){
			if(entryValue.getValue().contains(value1)){
				return entryValue.getKey();
			}
		}
		return null;
	}
	
	public String calculateAARSSupport(ArrayList<String> actionFrom, ArrayList<String> actionTo, 
			String decisionFrom, String decisionTo,
			double minSupp, double minConf){
		String supportConfidence = new String();
		
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
		
//		System.out.println(leftRuleSupport+","+leftSupport);
		if(confidence >= minConf && support>=minSupp)
			supportConfidence = "   [Support:- " + support +" and Confidence:- " + confidence +"%]";
		
		return supportConfidence;
	}
}
