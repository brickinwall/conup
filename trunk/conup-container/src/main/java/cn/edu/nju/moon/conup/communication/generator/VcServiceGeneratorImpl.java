package cn.edu.nju.moon.conup.communication.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.communication.services.ArcService;
import cn.edu.nju.moon.conup.communication.services.ComponentUpdateService;
import cn.edu.nju.moon.conup.communication.services.FreenessService;
import cn.edu.nju.moon.conup.communication.services.OndemandService;
import cn.edu.nju.moon.conup.data.InArcRegistryImpl;
import cn.edu.nju.moon.conup.def.Arc;

public class VcServiceGeneratorImpl implements VcServiceGenerator {
	private static String CLASS_NAME = "VcServiceImpl";
	private static String CLASS_FILE = null;
	private String componentName;
//	private String qualifier;
	private String fileDir;
	private String fileLocation;
	private CompositeGeneratorImpl compositeGenerator;

//	public VcServiceGeneratorImpl(String componentName, String qualifier) {
//		super();
//		this.componentName = componentName;
//		this.qualifier = qualifier;
//		this.CLASS_FILE = componentName + this.CLASS_NAME + ".java";
//	}

	public VcServiceGeneratorImpl(String componentName, CompositeGenerator compositeGenerator) {
		super();
		this.componentName = componentName;
		this.compositeGenerator = (CompositeGeneratorImpl) compositeGenerator;
		this.CLASS_FILE = componentName + this.CLASS_NAME + ".java";
	}
	
	@Override
	public boolean generate() {
		createJavaFile();
		
		compileClass();
		
		return true;
	}
	
	private void createJavaFile(){
		String baseUri = new File("").getAbsolutePath();
		baseUri = baseUri.substring(0, baseUri.lastIndexOf(File.separator))
				+ File.separator + "conup-container";
		fileDir = baseUri + File.separator + "src" + File.separator + "main"
				+ File.separator + "java" + File.separator + "cn"
				+ File.separator + "edu" + File.separator + "nju"
				+ File.separator + "moon" + File.separator + "conup"
				+ File.separator + "communication" + File.separator
				+ "services" + File.separator;
//		String fileDir = baseUri + File.separator + "target" + File.separator
//				+ "classes" + File.separator;
		fileLocation = fileDir +CLASS_FILE;
		File vcFile = new File(fileLocation);
		if (vcFile.exists()) {
			vcFile.delete();
		}
		FileWriter fw = null;
		try {
			vcFile.createNewFile();
			fw = new FileWriter(vcFile);
			String fileContent = generateContent(fileDir + "VcServiceImplTemplate.java");
			fw.write(fileContent);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(fw != null){
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private String generateContent(String fileName) {
		File file = new File(fileName);
		BufferedReader reader = null;
		StringBuffer newJavaContent = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			newJavaContent = new StringBuffer();
			int line = 1;
			while ((tempString = reader.readLine()) != null) {
//				System.out.println("line " + line + ": " + tempString);
				if (tempString.contains("public class VcServiceImplTemplate implements ArcService, FreenessService, OndemandService,ComponentUpdateService {")) {
					tempString = tempString.replace("VcServiceImplTemplate", componentName + "VcServiceImpl");
					newJavaContent.append(tempString + "\n");
					newJavaContent.append(addReferences());
				}else if(tempString.contains("VcServiceImplTemplate")){
					tempString = tempString.replace("VcServiceImplTemplate", componentName + "VcServiceImpl");
					newJavaContent.append(tempString + "\n");
				}else if(tempString.contains("VcServiceImpl")){
					tempString = tempString.replace("VcServiceImpl", componentName + "VcServiceImpl");
					newJavaContent.append(tempString + "\n");
				}else{
					newJavaContent.append(tempString + "\n");
				}
				line++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return newJavaContent.toString();
	}

	private String addReferences() {
		StringBuffer addedContent = new StringBuffer();
		Map<String, String> targetComponents = compositeGenerator.getTargetComponentsInfo();
		Iterator iterator = targetComponents.keySet().iterator();
		while(iterator.hasNext()){
			String componentName = (String) iterator.next();
			componentName = componentName.substring(0, 1).toLowerCase()
					+ componentName.substring(1);
			addedContent.append("\tprivate OndemandService " + componentName
					+ "OndemandService;\n");
			addedContent.append("\tprivate ArcService " + componentName + "ArcService;\n");
			
		}
		
//		DomainConfig domainConfigTool = new DomainConfigImpl();
//		List<String> destNodes = domainConfigTool.getDestNodes(componentName);
//		if(destNodes == null){
//			return null;
//		}
//		Iterator destIterator = destNodes.iterator();
//		
////		StringBuffer addedContent = new StringBuffer();
//
//		while (destIterator.hasNext()) {
//			String componentName = (String) destIterator.next();
//			componentName = componentName.substring(0, 1).toLowerCase()
//					+ componentName.substring(1);
////			System.out.println(componentName);
//			addedContent.append("\tprivate OndemandService " + componentName
//					+ "OndemandService;\n");
//			addedContent.append("\tprivate ArcService " + componentName + "ArcService;\n");
//		}
		addedContent.append("\n");
		iterator = targetComponents.keySet().iterator();
		// generate getter setter method
		while (iterator.hasNext()) {
			String componentName = (String) iterator.next();
			//if the field's second char is Upper case, the getter and setter method name = get/set + field name
			if(Character.isUpperCase(componentName.charAt(1))){
				String lowCaseComponentName = componentName.substring(0, 1).toLowerCase() + componentName.substring(1);
				addedContent.append("\tpublic OndemandService get" + lowCaseComponentName + "OndemandService(){\n");
				addedContent.append("\t\treturn " + lowCaseComponentName + "OndemandService;\n");
				addedContent.append("\t}\n");
				addedContent.append("\t@Reference\n");
				addedContent.append("\tpublic void set" + lowCaseComponentName + "OndemandService(OndemandService ondemandService){\n");
				addedContent.append("\t\t this." + lowCaseComponentName + "OndemandService = ondemandService;\n");
				addedContent.append("\t}\n\n");
				
				addedContent.append("\tpublic ArcService get" + lowCaseComponentName + "ArcService(){\n");
				addedContent.append("\t\treturn " + lowCaseComponentName + "ArcService;\n");
				addedContent.append("\t}\n");
				addedContent.append("\t@Reference\n");
				addedContent.append("\tpublic void set" + lowCaseComponentName + "ArcService(ArcService arcService){\n");
				addedContent.append("\t\t this." + lowCaseComponentName + "ArcService = arcService;\n");
				addedContent.append("\t}\n\n");
			}else{
				addedContent.append("\tpublic OndemandService get" + componentName + "OndemandService(){\n");
				String lowCaseComponentName = componentName.substring(0, 1).toLowerCase() + componentName.substring(1);
				addedContent.append("\t\treturn " + lowCaseComponentName + "OndemandService;\n");
				addedContent.append("\t}\n");
				addedContent.append("\t@Reference\n");
				addedContent.append("\tpublic void set" + componentName + "OndemandService(OndemandService ondemandService){\n");
				addedContent.append("\t\t this." + lowCaseComponentName + "OndemandService = ondemandService;\n");
				addedContent.append("\t}\n\n");
				
				addedContent.append("\tpublic ArcService get" + componentName + "ArcService(){\n");
				addedContent.append("\t\treturn " + lowCaseComponentName + "ArcService;\n");
				addedContent.append("\t}\n");
				addedContent.append("\t@Reference\n");
				addedContent.append("\tpublic void set" + componentName + "ArcService(ArcService arcService){\n");
				addedContent.append("\t\t this." + lowCaseComponentName + "ArcService = arcService;\n");
				addedContent.append("\t}\n\n");
			}
			
		}

//		addedContent.append("}");
		return addedContent.toString();
	}

	private void compileClass() {
		String baseUri = new File("").getAbsolutePath();
		baseUri = baseUri.substring(0, baseUri.lastIndexOf(File.separator))
				+ File.separator + "conup-container";
		String srcLocation = baseUri + File.separator + "src" + File.separator + "main"
				+ File.separator + "java" + File.separator + "cn"
				+ File.separator + "edu" + File.separator + "nju"
				+ File.separator + "moon" + File.separator + "conup"
				+ File.separator + "communication" + File.separator
				+ "services" + File.separator + CLASS_FILE;
		String classLocation = baseUri + File.separator + "target" + File.separator
				+ "classes" + File.separator;
	        String[] source = { "-d", classLocation, new String(srcLocation) };
	        try{
	        	System.out.println("javac out:"
	        			+ com.sun.tools.javac.Main.compile(source));
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
	}

}
