package com.tuscanyscatours.launcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExecutionRecorderAnalyzer {
	public static final String ACTION_REGEX = "\\[[\\w\\W&&[^:]]{0,}\\]";
	public static final String INCONSISTENCY_VER_REGEX = "\\[\\w\\.Ver_0\\w\\W\\]";
	/** execution records that need to be analyzed */
	private final String records;
	private int totalRecords = 0;
	private int inconsistentRecords = 0;
	
	public ExecutionRecorderAnalyzer(String records){
		this.records = records;
		analyze();
	}
	
	private void analyze(){
		Pattern actionPtn;
		Matcher actionMch;
		actionPtn = Pattern.compile(ACTION_REGEX);
		actionMch = actionPtn.matcher(records);
		while(actionMch.find()){
			totalRecords ++;
			String group = actionMch.group();
//			int start = actionMch.start();
//			int end = actionMch.end();
//			System.out.println(group + "\n\t" + start + "  " + end);
			
			if( (group.contains("Ver_0") && group.contains("Ver_1")) 
				|| (group.contains("Ver_1") && group.contains("Ver_2"))
					){
				inconsistentRecords ++;
			}
		}
	}
	
	public int getInconsistentRecords(){
		return inconsistentRecords;
	}
	
	public int getTotalRecords(){
		return totalRecords;
	}
}
