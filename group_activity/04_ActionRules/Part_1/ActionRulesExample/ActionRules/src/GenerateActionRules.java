import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.omg.CORBA.DynAnyPackage.TypeMismatch;

import lers.*;


public class GenerateActionRules {
	
	static Common commonMethods;
	static StringsDAO commonStrings;
	static PerformLERS lersObject;
	
	static Scanner input;
	
	static HashSet<ArrayList<String>> actionRules;
	
	Map<ArrayList<String>, String> certainRules;
	
	double minSupport,minConfidence;
	
	String decisionFrom,decisionTo;
	
	File outputFile;
	FileWriter fileWriter;
	
	public GenerateActionRules(PerformLERS performLERS) throws IOException {
		super();
		this.commonMethods = new Common();
		this.commonStrings = new StringsDAO();
		this.lersObject = performLERS;
		this.actionRules = new HashSet<ArrayList<String>>();
		
		this.certainRules = new HashMap<ArrayList<String>, String>();
		
		outputFile = new File("Output.txt");
		
		if(outputFile.delete())
			outputFile.createNewFile();
		
		fileWriter = new FileWriter(outputFile, true);
	}

//	//Gets decisionFrom value from the user
//	public void getDecisionFromValue() {
//				
//		HashSet<String> decisionValues = lersObject.getDistinctAttributeValues().get(lersObject.getDecisionAttribute());
//		
//		commonMethods.printMessageInNewLine(commonStrings.DECISIONS_AVAILABLE);
//		commonMethods.printHashSet(decisionValues);
//		commonMethods.printMessageInNewLine(commonStrings.CHANGE_FROM);
//	
//		input = new Scanner(System.in);
//		lersObject.setDecisionFrom(input.next());
//		if (decisionValues.contains(lersObject.getDecisionFrom())) {
//			getDecisionToValue(decisionValues);
//		}
//		else{
//			commonMethods.printMessageInNewLine(commonStrings.INVALID_VALUE);
//			getDecisionFromValue();
//		}
//	}
//	
////	Gets decisionTo value from the user
//	private static void getDecisionToValue(HashSet<String> decisionValues) {
//		commonMethods.printMessageInNewLine(commonStrings.DECISIONS_AVAILABLE);
//		
//		commonMethods.printHashSet(decisionValues);
//		commonMethods.printMessageInNewLine(commonStrings.CHANGE_TO);
//		
//		input = new Scanner(System.in);
//		lersObject.setDecisionTo(input.next());
//		if (decisionValues.contains(lersObject.getDecisionTo())) {
//			return;
//		}
//		else{
//			commonMethods.printMessageInNewLine(commonStrings.INVALID_VALUE);
//			getDecisionToValue(decisionValues);
//		}
//		
//	}
	
	//Generates Action Rules
	public void generateActionRules() {
		ArrayList<String> actionFrom;
		ArrayList<String> actionTo;
		ArrayList<String> actions = null;
		
		String rule = commonStrings.EMPTY;
		
//		commonMethods.printMessageInNewLine(lersObject.getCertainRules().toString());
		
		commonMethods.printMessageInNewLine(commonStrings.ACTION_RULES);
		commonMethods.printMessageInNewLine(commonStrings.UNDERLINE);
		
		certainRules = lersObject.getCertainRules();
		decisionFrom = lersObject.getDecisionFrom();
		decisionTo = lersObject.getDecisionTo();
		
		for(Map.Entry<ArrayList<String>, String> certainRules1 : certainRules.entrySet()){
			
			String certainRules1Value = certainRules1.getValue();
			if(certainRules1Value.equals(decisionFrom)){
		
				for(Map.Entry<ArrayList<String>, String> certainRules2 : certainRules.entrySet()){
					ArrayList<String> certainRules1Key = certainRules1.getKey();
					ArrayList<String> certainRules2Key = certainRules2.getKey();
					
					if((certainRules1Key.equals(certainRules2.getKey())) || (!certainRules2.getValue().equals(decisionTo)) || 
							!checkStableAttributes(certainRules1Key,certainRules2Key)){
						continue;
					}
					else{
						String primeAttribute = commonStrings.EMPTY;						
						
						if(checkRulesSubSet(certainRules1Key,certainRules2Key)){//<---------------------- To change
//							commonMethods.printMessageInNewLine(certainRules1Key.toString() + " &&& " + certainRules2Key.toString());
							
							ArrayList<String> checkCertainValues1 = certainRules1Key;
							ArrayList<String> tempList = new ArrayList<String>();
							
							rule = commonStrings.EMPTY;
							actionFrom = new ArrayList<String>();
							actionTo = new ArrayList<String>();
							actions = new ArrayList<String>();
							
							
							for(String value1 : checkCertainValues1){
									
								if(lersObject.getStableAttributes().contains(value1)){
									//if(!checkRule(rule,value1)){//<------------------------------------------Uncomment and delete next line
									if(!actionTo.contains(value1)){
										rule=formRule(rule,value1,value1);
										
										actionFrom.add(value1);
										actionTo.add(value1);
										actions.add(getAction(value1, value1));
									}
									continue;
								}else{
									primeAttribute = lersObject.getAttributeName(value1);
												
									ArrayList<String> checkCertainValues2 = certainRules2.getKey();
									for(String value2 : checkCertainValues2){
										
										if(lersObject.getStableAttributes().contains(value2)) {
											//if(!checkRule(rule,value2)){//<------------------------------------------Uncomment and delete next line
											if(!actionTo.contains(value2)){
												rule=formRule(rule,value2,value2);
												
												actionFrom.add(value2);
												actionTo.add(value2);
												actions.add(getAction(value2, value2));
											}
											 
										}else if(!(lersObject.getAttributeName(value2).equals(primeAttribute))){
											tempList.add(value2);
											//if(!checkRule(rule,value2)){//<------------------------------------------Uncomment and delete next line
//											if(!actionTo.contains(value2)){
//												rule=formRule(rule,"",value2);
//												
//												actionFrom.add("");
//												actionTo.add(value2);
//												actions.add(getAction("", value2));
//											}
										}
										else if(lersObject.getAttributeName(value2).equals(primeAttribute) && !actionTo.contains(value2)){
	//										commonMethods.printMessageInNewLine("Combining " + certainRules1 + " & " + certainRules2);
											
											rule=formRule(rule,value1,value2);
											
											actionFrom.add(value1);
											actionTo.add(value2);
											actions.add(getAction(value1, value2));
										}
										
									}
								}
							}
							
//							commonMethods.printMessageInNewLine("Missed Values " + tempList);
							for(String missedValues:tempList){
								if(!actionTo.contains(missedValues)){
									rule=formRule(rule,"",missedValues);
									
									actionFrom.add("");
									actionTo.add(missedValues);
									actions.add(getAction("", missedValues));
								}
							}
							
												
//							if(rule.indexOf(primeAttribute)!=-1 && !primeAttribute.isEmpty()){
//								commonMethods.printMessageInNewLine("Printing Action Rule...");
							
								printActionRule(actionFrom, actionTo, actions, rule);
								
//								printExtraActionRules(actionFrom,actionTo);
//								ArrayList<String> stableValues = getStableValues(actionTo);
//								ArrayList<String> attributeValues = getAttributeValues(stableValues,decisionFrom);
//								
//								ArrayList<String> toBeAddedAttributes = getNewAttributes(actionFrom,actionTo,stableValues);
//										
//								for(String attributeValue : toBeAddedAttributes){
//									stableValues.add(attributeValue);
//								
//									ArrayList<String> checkList = getAttributeValues(stableValues,lersObject.getDecisionFrom());	
//	
//									if(attributeValues.containsAll(checkList) && !checkList.isEmpty()){
//										String subRule = new String();
//										ArrayList<String> subActionFrom = new ArrayList<String>();
//										ArrayList<String> subActionTo = new ArrayList<String>();
//										ArrayList<String> subActions = new ArrayList<String>();
//										
//										if(lersObject.isStable(lersObject.getAttributeName(attributeValue))){
//											subActionFrom.addAll(actionFrom);
//											subActionFrom.add(attributeValue);
//											
//											subActionTo.addAll(actionTo);
//											subActionTo.add(attributeValue);
//										}
//										else{
//											subActionFrom = getSubActionFrom(actionFrom,actionTo,attributeValue);
//											subActionTo.addAll(actionTo);
//										}
//										subRule = getSubRule(subActionFrom,subActionTo,subActions);
//										
//										printActionRule(subActionFrom, subActionTo, subActions, subRule);
//										
//									}
//									
//									stableValues.remove(stableValues.size()-1);
//								}
								
//							}
						}
					}
				}
					
			}else continue;
		}
		
		
	}

	private void printExtraActionRules(ArrayList<String> actionFrom,
			ArrayList<String> actionTo) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
//				commonMethods.printMessageInNewLine("Printing Extra RUles");
				
				ArrayList<String> stableValues = getStableValues(actionTo);
				ArrayList<String> attributeValues = getAttributeValues(stableValues,decisionFrom,actionFrom);
				
				ArrayList<String> toBeAddedAttributes = getNewAttributes(actionFrom,actionTo,stableValues);
//				commonMethods.printMessageInNewLine("Support of " + actionFrom.toString() + " & " + actionTo.toString() + " are: " + attributeValues);
//				commonMethods.printMessageInNewLine("New attributes of " + actionFrom + " and " + actionTo + " are :" + toBeAddedAttributes);
				ArrayList<String> tempAttributeValues;		
				
				for(String attributeValue : toBeAddedAttributes){
//					stableValues.add(attributeValue);
					tempAttributeValues = new ArrayList<String>();
					tempAttributeValues.add(attributeValue);
				
					ArrayList<String> checkList = getAttributeValues(stableValues,commonStrings.EMPTY,tempAttributeValues);	
//					commonMethods.printMessageInNewLine("Check for " + stableValues + " and " + tempAttributeValues + " : " + checkList +" in " + actionFrom + " and " + actionTo);

					if(attributeValues.containsAll(checkList) && !checkList.isEmpty()){
						String subRule = new String();
						ArrayList<String> subActionFrom = new ArrayList<String>();
						ArrayList<String> subActionTo = new ArrayList<String>();
						ArrayList<String> subActions = new ArrayList<String>();
						
						if(lersObject.isStable(lersObject.getAttributeName(attributeValue))){
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
						
						
						printActionRule(subActionFrom, subActionTo, subActions, subRule);
						
					}
					
//					stableValues.remove(stableValues.size()-1);
				}
				
			}
		}).start();
		
		
		
	}

	private boolean checkRulesSubSet(
			ArrayList<String> certainRules1,
			ArrayList<String> certainRules2) {
		ArrayList<String> primeAttributes1 = new ArrayList<String>();
		ArrayList<String> primeAttributes2 = new ArrayList<String>();
		
		for (String string : certainRules1) {
			String attributeName = lersObject.getAttributeName(string);
			
			if(!lersObject.isStable(attributeName))
				primeAttributes1.add(attributeName);
		}
		
		for (String string : certainRules2) {
			String attributeName = lersObject.getAttributeName(string);
			
			if(!lersObject.isStable(attributeName))
				primeAttributes2.add(attributeName);
		}
		
		if(primeAttributes2.containsAll(primeAttributes1))
			return true;
		else return false;
	}

	private static boolean checkStableAttributes(ArrayList<String> key,
			ArrayList<String> key2) {
		List<String> stableAttributesList1 = new ArrayList<String>();
		List<String> stableAttributesList2 = new ArrayList<String>();
		
		for(String value : key){
			if(lersObject.getStableAttributes().contains(value))
				stableAttributesList1.add(value);
		}
		
		for(String value : key2){
			if(lersObject.getStableAttributes().contains(value))
				stableAttributesList2.add(value);
		}
		
//		if(stableAttributesList1.size() == stableAttributesList2.size()){
//			if(stableAttributesList1.containsAll(stableAttributesList2))
//				return true;
//			else return false;
//		}else if((stableAttributesList1.size()==0 && stableAttributesList2.size()!=0) || (stableAttributesList2.size()==0 && stableAttributesList1.size()!=0)){
//			return true;
//		}
//		else return false;
		
		if(stableAttributesList2.containsAll(stableAttributesList1))
			return true;
		else return false;
	}
	
	private static boolean checkRule(String rule,String value) {
		if(rule.indexOf(value)!=-1)
			return true;
		else return false;
	}
	

	private static String formRule(String rule,String value1, String value2) {
		if(!rule.isEmpty())
			rule += "^";
		
		rule += "(" + lersObject.getAttributeName(value2) + "," + getAction(value1,value2) + ")";
		return rule;
	}
	
	private static String getAction(String left,String right){
		return left + "->" + right;
	}
	
	private ArrayList<String> getStableValues(ArrayList<String> actionFrom) {
		ArrayList<String> stableValues = (ArrayList<String>) lersObject.getStableAttributes();
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
	
	private ArrayList<String> getAttributeValues(ArrayList<String> stableValues,String decisionFrom, ArrayList<String> actionFrom) {
		ArrayList<String> temp = new ArrayList<String>();
		ArrayList<String> attributeValues = new ArrayList<String>();
		int lineCount = 0;
		
		temp.addAll(stableValues);
		
		for(String from : actionFrom){
			if(!from.equals(commonStrings.EMPTY)){
				temp.add(from);
			}
		}
		
		if(!decisionFrom.equals(commonStrings.EMPTY))
			temp.add(decisionFrom);
		
		
		for(Map.Entry<ArrayList<String>, Integer> data : lersObject.getData().entrySet()){
			lineCount++;
			
			if(data.getKey().containsAll(temp)){
				attributeValues.add("x" + lineCount);
			}
		}
		
		return attributeValues;
	}
	
	private ArrayList<String> getNewAttributes(ArrayList<String> actionFrom,
			ArrayList<String> actionTo,ArrayList<String> stableValues) {
		ArrayList<String> stableAttributes = new ArrayList<String>();
		ArrayList<String> flexibleAttributes = new ArrayList<String>();
		ArrayList<String> newAttributes = new ArrayList<String>();
		
		for (String string : stableValues) {
			stableAttributes.add(lersObject.getAttributeName(string));
		}
		
		for (String string : actionTo){
			flexibleAttributes.add(lersObject.getAttributeName(string));
		}
		
		for(Map.Entry<String, HashSet<String>> mapValue : lersObject.getDistinctAttributeValues().entrySet()){
			String mapKey = mapValue.getKey();
			HashSet<String> mapValues = mapValue.getValue();
			
			if(mapKey.equals(lersObject.getDecisionAttribute()) || (stableAttributes.size()!=0 && stableAttributes.contains(mapKey))){
				continue;
			} else if(lersObject.isStable(mapKey)){
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
	
	private void printActionRule(ArrayList<String> actionFrom,ArrayList<String> actionTo,
			ArrayList<String> actions,String rule) {
		
//		final ArrayList<String> tempActionFrom = new ArrayList<String>();
//		final ArrayList<String> tempActionTo = new ArrayList<String>();
//		final ArrayList<String> tempActions = new ArrayList<String>();
//		final String tempRule = new String();
//		
//		tempActionFrom.addAll(actionFrom);
//		tempActionTo.addAll(actionTo);
//		tempActions.addAll(actions);
		
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				int support = lersObject.calculateSupportLERS(actionTo,lersObject.getDecisionTo());//<----------------------Change if algorithm changes
				
				if(support!=0){
					String oldConfidence = String.valueOf((Integer.parseInt(lersObject.calculateConfidenceLERS(actionFrom,lersObject.getDecisionFrom())) * 
														Integer.parseInt(lersObject.calculateConfidenceLERS(actionTo,lersObject.getDecisionTo())))/100);
					String newConfidence = lersObject.calculateConfidenceLERS(actionTo,lersObject.getDecisionTo());
					
					String utility = lersObject.calculateConfidenceLERS(actionFrom,lersObject.getDecisionFrom());

//					commonMethods.printMessageInNewLine(rule + ", Support:" + support + ", Confidence:" + oldConfidence);
					
					if((support >= minSupport && Double.parseDouble(oldConfidence) >= minConfidence ) 
							&& !oldConfidence.equals(commonStrings.ZERO) && !newConfidence.equals(commonStrings.ZERO)){
						
						if(actions!=null){
							
							
//							if(actionRules.size() != 0 && actionRules.containsAll(actions)){
//								for(ArrayList<String> singleAction : actionRules){
//									
//									if(singleAction.containsAll(actions) && actions.containsAll(singleAction)){
//										flag++;
//										break;
//									}
//								}
								
//							}
							
							Collections.sort(actions);
							if(!actionRules.contains(actions)){
								actionRules.add(actions);
								commonMethods.printActionRules(rule,lersObject.getDecisionAttribute(), lersObject.getDecisionFrom(), lersObject.getDecisionTo(),support,oldConfidence,newConfidence,utility);
								
//								commonMethods.printMessageInNewLine("Finding new rules...\n");
								printExtraActionRules(actionFrom, actionTo);
								
							} 
						}						
						
					}
					
				}
				
//				printExtraActionRules(actionFrom,actionTo);
			}
		}){}.start();
		
		
		
	}
	
	private ArrayList<String> getSubActionFrom(ArrayList<String> actionFrom,
			ArrayList<String> actionTo,String alternateActionFrom) {
		ArrayList<String> finalActionFrom = new ArrayList<String>();
		HashSet<String> checkSameSet;
	
		for (int i = 0;i<actionTo.size();i++) {
			checkSameSet = new HashSet<String>();
			
			checkSameSet.add(alternateActionFrom);
			checkSameSet.add(actionTo.get(i));
			
			if(lersObject.checkSameGroup(checkSameSet)){
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
		String rule = commonStrings.EMPTY;
		
		for (int i = 0; i < subActionFrom.size(); i++) {
			rule = formRule(rule, subActionFrom.get(i), subActionTo.get(i));
			subActions.add(getAction(subActionFrom.get(i), subActionTo.get(i)));			
		}
		
		return rule;
	}
	
	public void setMinSupportAndConfidence(){
		commonMethods.printMessageInNewLine("Enter Minimum Support...");
		
		input = new Scanner(System.in);
		
		try{
			minSupport = input.nextDouble();
			
			setConfidence();
		}catch(Exception e){
			commonMethods.printMessageInNewLine("Please enter an integer or decimal values");
			setMinSupportAndConfidence();
		}
	}
	
	private void setConfidence(){
		input = new Scanner(System.in);
		
		commonMethods.printMessageInNewLine("Enter Minimum Confidence Percentage...");
		
		try{
			minConfidence = input.nextDouble();
			
			
		}catch(Exception e){
			commonMethods.printMessageInNewLine("Please enter an integer or decimal percentage values");
			setConfidence();
		}
	}
	
}
