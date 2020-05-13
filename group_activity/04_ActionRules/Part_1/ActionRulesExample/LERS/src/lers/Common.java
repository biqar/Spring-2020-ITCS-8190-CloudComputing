package lers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Common {

	StringsDAO accessStrings = new StringsDAO();
	File outputFile;
	FileWriter fileWriter;
	
	public Common() throws IOException{
		outputFile = new File("Output.txt");
		
		if(outputFile.delete())
			outputFile.createNewFile();
		
		fileWriter = new FileWriter(outputFile, true);
	}
	
	public void printMessage(String content){
		System.out.print(content);
	}
	
	public void printMessageInNewLine(String content){
		printNewLine();
		printMessage(content);
	}
	
	public void printNewLine(){
		printMessage(accessStrings.NEW_LINE);
	}
	
	public void printList(List<String> list){
		printNewLine();
		for(String value : list){
			printMessage(value + accessStrings.TAB);
		}
	}
	
	public void printHashSet(HashSet<String> set){
		printNewLine();
		for(String value : set){
			printMessage(value + accessStrings.TAB);
		}
	}
	
	public void printData(ArrayList<ArrayList<String>> data){
		for(ArrayList<String> attrValues : data){
			for(String attrVal : attrValues){
				printMessage(attrVal + accessStrings.TAB);
			}
			printNewLine();
		}
	}
	
	public void printListInList(List<String> attributeNames, Map<ArrayList<String>, Integer> data){
		for(String attrName : attributeNames){
			printMessage(attrName + accessStrings.TAB);
		}
		printNewLine();
		
		for(Map.Entry<ArrayList<String>, Integer> attrValues: data.entrySet()){
			for(String attrVal : attrValues.getKey()){
				printMessage(attrVal + accessStrings.TAB);
			}
			printNewLine();
		}
	}
	
	public void printMapWithSetKeySetValue(Map<HashSet<String>, HashSet<String>> map){
	
		for(Map.Entry<HashSet<String>, HashSet<String>> set : map.entrySet()){
			printMessageInNewLine(set.getKey().toString() + " = " + set.getValue());
		}
	}
	
	public void printSupportAndConfidence(int support, String confidence){
		printMessage("[" + accessStrings.SUPPORT + accessStrings.COLON + support + " , " + 
				accessStrings.CONFIDENCE + accessStrings.COLON + confidence + accessStrings.PERCENTAGE +"]");
	}
	
	public void printLERSRules(String left, String right){
		printMessageInNewLine(left + " -> " + right);
	}
	
	public void printActionRules(String rule,String decision,String left,String right,int support,String oldConfidence,String newConfidence,String utility){
		String finalActionRule = rule + " ==> " + "(" + decision + "," + left + "->" + right +")" + 
				"   [Support:- " + support + " & New Confidence:- " + newConfidence +"% & Old Confidence:- " + oldConfidence + "% & Utility:- " + utility + "%]";
		
		printMessageInNewLine(finalActionRule);
		
		try {
			fileWriter.write(finalActionRule);
			fileWriter.write("\r\n");
			fileWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
//	public void printAAR(String aar){
//		try{
//			
//			writer.append(aar);
//			
//		} catch(FileNotFoundException e){
//			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
}
