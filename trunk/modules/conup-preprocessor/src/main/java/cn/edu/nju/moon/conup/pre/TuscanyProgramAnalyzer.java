package cn.edu.nju.moon.conup.pre;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;
import static org.objectweb.asm.Opcodes.*;

/**
 * Analyze the tuscany program
 * 
 * @author Ping Su
 * 
 */
public class TuscanyProgramAnalyzer {

	private final static Logger LOGGER = Logger
			.getLogger(TuscanyProgramAnalyzer.class.getName());

	public static Logger getLogger() {
		return LOGGER;
	}
	@Deprecated
	List<String> coms = new LinkedList<String>();

	List<String> allServices = new LinkedList<String>();
	/**
	 * Every updatable class should be added a field for generating transaction
	 * id.
	 */
	String fieldName = "txLifecycleMgr";
	String fieldDesc = "Lcn/edu/nju/moon/conup/ext/tx/manager/TxLifecycleManager;";

	/**
	 * Whether the given class has @Adaptive and @AEjb
	 * 
	 * @param cn
	 * @return
	 */
	@Deprecated
	public boolean isAnalyze(ClassNode cn) {
		if (cn.visibleAnnotations != null) {
			Iterator<AnnotationNode> i = cn.visibleAnnotations.iterator();
			while (i.hasNext()) {
				AnnotationNode an = i.next();
				if (an.desc.contains("Ljavax/aejb/Adaptive")) {
					// LOGGER.info(an.desc);
					List<FieldNode> fields = (List<FieldNode>) cn.fields;
					for (FieldNode fn : fields) {
						if (fn.visibleAnnotations != null) {
							Iterator<AnnotationNode> fi = fn.visibleAnnotations
									.iterator();
							while (fi.hasNext()) {
								AnnotationNode fa = fi.next();
								// LOGGER.info(fa.desc);
								if (fa.desc.contains("AEjb")
										|| fa.desc.contains("Ejb")) {
									return true;
								}
							}
						}
					}
				}
			}
			return false;

		}
		return false;
	}

	/**
	 * insert field TxLifecycleManager and the method setTxLifecycleManager for
	 * generating transaction id; public void
	 * setTxLifecycleManager(TxLifecycleManager tlm){ txLifecycleMgr = tlm; }
	 * 
	 * @param cn
	 */
	public void addTxLifecycleManager(ClassNode cn) {
		boolean isPresent = true;
		while (isPresent) {
			isPresent = false;
			for (FieldNode fn : (List<FieldNode>) cn.fields) {
				if (fieldName.equals(fn.name)) {
					isPresent = true;
					fieldName = fieldName + "Mgr";
					break;
				}
			}
		}
		cn.fields.add(new FieldNode(ACC_PRIVATE, fieldName, fieldDesc, null,
				null));
		LOGGER.fine("Add field success!");	
	}

	/**
	 * 
	 * @param cn
	 * @param conupTx
	 *            default
	 * @return
	 */
	public boolean whetherToAnalyze(ClassNode cn, String conupTx) {
		for (MethodNode mn : (List<MethodNode>) cn.methods) {
			if (mn.visibleAnnotations != null) {
				Iterator<AnnotationNode> i = mn.visibleAnnotations.iterator();
				while (i.hasNext()) {
					AnnotationNode an = i.next();
					if (conupTx.equals(an.desc)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * for every class,if it needs to be analyzed, firstly find all the services it will use; and then add 
	 * the TxLifeManager field; at last, analyze every method.
	 * @param cn
	 * @param conupTx
	 * @param reference
	 */
	public void transform(ClassNode cn, String conupTx,String reference) {
		if (whetherToAnalyze(cn, conupTx)) {
			findAllServices(cn,reference);
			addTxLifecycleManager(cn);
			for (MethodNode mn : (List<MethodNode>) cn.methods) {
				MethodAnalyzer methodtransform = new MethodAnalyzer(allServices,fieldName,fieldDesc);
//				methodtransform.setCom(allServices);
				methodtransform.methodTransform(cn, mn, conupTx);

			}
		}
	}
	/**
	 * Every possible component/service has a method with annotation @reference 
	 * @param cn
	 */
	public void findAllServices(ClassNode cn,String reference) {
		for (MethodNode mn : (List<MethodNode>) cn.methods) {
			if (mn.visibleAnnotations != null) {
				Iterator<AnnotationNode> i = mn.visibleAnnotations.iterator();
				while (i.hasNext()) {
					AnnotationNode an = i.next();
					if (reference.equals(an.desc)) {
						InsnList insns = mn.instructions;
						Iterator<AbstractInsnNode> aInsn = insns.iterator();						
						while (aInsn.hasNext()) {
							AbstractInsnNode insNode = aInsn.next();
							if(insNode instanceof FieldInsnNode){
								FieldInsnNode fieldNode = ((FieldInsnNode) insNode);							
								if(fieldNode.getOpcode() == PUTFIELD && isThisPossibleUsedService(cn,fieldNode.owner,fieldNode.name,fieldNode.desc)){																		
									String serviceName = fieldNode.desc;
									//get the real class information
									while(serviceName.startsWith("[")){
										serviceName = serviceName.substring(1);										
									}
									if(serviceName.startsWith("L")){
										serviceName = serviceName.substring(1);
									}									
									if(serviceName.endsWith(";")){
										serviceName = serviceName.substring(0, serviceName.length()-1);
									}	
									if(!allServices.contains(serviceName)){
										LOGGER.info("All possible Services to be used:"+serviceName);
										allServices.add(serviceName);
									}
								}								
							}
						}
					}
				}
			}
		}
	}
	/**
	 * Whether the field is a service
	 * @param cn
	 * @param fieldName
	 * @param fieldDesc
	 * @param owner
	 * @return
	 */

	public boolean isThisPossibleUsedService(ClassNode cn, String owner, String fieldName,
			String fieldDesc) {
		for (FieldNode fn : (List<FieldNode>) cn.fields) {
			if (fieldName.equals(fn.name) && fieldDesc.equals(fn.desc)
					&& owner.equals(cn.name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * search the configure file ".composite" for the components. Every
	 * component is like "component/service"
	 * 
	 * @param fileName
	 */
	@Deprecated
	public void findComponents(String fileName) {
		// List<String> com = new LinkedList<String>();
		File file = new File(fileName);
		BufferedReader reader = null;
		try {

			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;

			while ((tempString = reader.readLine()) != null) {
				if (tempString.contains("<tuscany:binding.jsonrpc")) {
					coms.add(tempString.split("/")[3]
							+ "/"
							+ tempString.split("/")[4].substring(0,
									tempString.split("/")[4].length() - 2));
					LOGGER.info("com"
							+ tempString.split("/")[3]
							+ "/"
							+ tempString.split("/")[4].substring(0,
									tempString.split("/")[4].length() - 2));
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
	}

	/**
	 * find all the components the transaction may use possibly
	 * 
	 * @param tempFile
	 */
	@Deprecated
	public void findAllCom(File tempFile) {
		if (tempFile.isDirectory()) {
			File file[] = tempFile.listFiles();
			for (int i = 0; i < file.length; i++) {
				LOGGER.info("Analyze file:" + file[i].getName());
				findAllCom(file[i]);
			}
		} else {
			try {
				if (tempFile.getName().endsWith(".composite")) {
					findComponents(tempFile.getAbsolutePath());
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	public ClassVisitor getClassAdapter(final ClassVisitor cvr,
			final String conupTx,final String reference) {
		return new ClassNode() {
			@Override
			public void visitEnd() {
				transform(this, conupTx,reference);
				this.accept(cvr);
			}
		};
	}

	/**
	 * find and analyze class files recursively
	 * 
	 * @param tempFile
	 */
	protected void beginAnalyze(File tempFile, String analyzername,String reference) {
		if (tempFile.isDirectory()) {
			File file[] = tempFile.listFiles();
			for (int i = 0; i < file.length; i++) {
				// LOGGER.info("Analyze file:" + file[i].getName());
				beginAnalyze(file[i], analyzername,reference);
			}
		} else {
			try {
				if (tempFile.getName().endsWith(".class")) {					
					LOGGER.info("Analyze file:" + tempFile.getName());
					FileInputStream input = new FileInputStream(
							tempFile.getAbsolutePath());
					ClassReader cr = new ClassReader(input);
					ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
					ClassVisitor cv = getClassAdapter(cw, analyzername,reference);
					cr.accept(cv, 0);
					byte[] b = cw.toByteArray();
					FileOutputStream fout = new FileOutputStream(new File(
							tempFile.getAbsolutePath()));
//					FileOutputStream fout = new FileOutputStream(new File("/home/PortalServiceImpl.class"));
					fout.write(b);
					fout.close();
				}
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	/**
	 * 鏄剧ず�?楄妭鐮佹枃浠�? * @param file
	 */
	public static void showClassSource(String file) {
		FileInputStream is;
		ClassReader cr;
		try {
			is = new FileInputStream(file);
			cr = new ClassReader(is);
			TraceClassVisitor trace = new TraceClassVisitor(new PrintWriter(
					System.out));
			cr.accept(trace, ClassReader.EXPAND_FRAMES);
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param projectLocation
	 */
	public void analyzeSource(String projectLocation) {
		String input = projectLocation;
		File tempFile = new File(input);
		tempFile.mkdir();
		findAllCom(tempFile);
		beginAnalyze(tempFile, "Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;","Lorg/oasisopen/sca/annotation/Reference;");
	}

	/**
	 * analyze jar files : .war, .ear, .jar, .zip
	 * 
	 * @param jarLocation
	 * @param tempLocation
	 * @param outputLocation
	 */
	public void analyzeJar(String jarLocation, String tempLocation,
			String outputLocation) {
		UnjarTool jar2temp = new UnjarTool();
		JarTool temp2jar = new JarTool();

		String input = jarLocation;
		String temp = tempLocation;
		String output = outputLocation;
		File tempFile = new File(temp);
		tempFile.mkdir();
		jar2temp.unjar(input, temp);

		findAllCom(tempFile);
		beginAnalyze(tempFile,"Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;","Lorg/oasisopen/sca/annotation/Reference;");

		File destJar = new File(output);
		try {
			temp2jar.jarDir(tempFile, destJar);
			jar2temp.clean(tempLocation);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param sourcePath
	 * @param tempPath
	 * @param targetPath
	 */
	public void analyzeApplication(String sourcePath, String tempPath,
			String targetPath) {
		String[] names = sourcePath.split("/");
		String fileName = names[names.length - 1];
		if (fileName.endsWith(".jar") || fileName.endsWith(".war")
				|| fileName.endsWith(".ear") || fileName.endsWith(".zip")) {
			String tempLocation = tempPath
					+ fileName.substring(0, fileName.length() - 4);
			String outputLocation = targetPath + fileName;
			analyzeJar(sourcePath, tempLocation, outputLocation);
		} else {
			analyzeSource(sourcePath);
		}
	}

	/**
	 * target application overwrites the source one.
	 * 
	 * @param sourcePath
	 * @param tempPath
	 */
	public void analyzeApplication(String sourcePath, String tempPath) {
		String[] names = sourcePath.split("/");
		String fileName = names[names.length - 1];
		if (fileName.endsWith(".jar") || fileName.endsWith(".war")
				|| fileName.endsWith(".ear") || fileName.endsWith(".zip")) {
			String tempLocation = tempPath
					+ fileName.substring(0, fileName.length() - 4);
			analyzeJar(sourcePath, tempLocation, sourcePath);
		} else {
			analyzeSource(sourcePath);
		}
	}

	public static void main(String args[]) {
		try {
			/*
			 * File file = new File(""); String absolutePath =
			 * file.getAbsolutePath(); System.out.print(absolutePath);
			 */
			TuscanyProgramAnalyzer analyse = new TuscanyProgramAnalyzer();
			// analyse.analyzeApplication("/home/analyzed/conup-sample-portal.jar",
			// "/home/temp/", "/home/analyzed/");
			// analyse.analyzeApplication("/home/analyzed/conup-sample-auth.jar",
			// "/home/temp/");
			// test a source application
			String projectPath = "/home/nju/PortalServiceImpl.class";
			// String projectPath
			// ="/home/nju/2.0-DU/samples/authUpdate/conup-sample-auth/target/classes/";
			// String projectPath
			// ="/home/nju/2.0-DU/samples/authUpdate/conup-sample-db/target/classes/";
			// String projectPath
			// ="/home/nju/2.0-DU/samples/authUpdate/conup-sample-proc/target/classes/";
			// String projectPath
			// ="/home/nju/2.0-DU/samples/authUpdate/conup-sample-portal/target/classes/";
			// ProgramAnalyzer analyse=new ProgramAnalyzer();
			analyse.analyzeSource(projectPath);
			// TuscanyProgramAnalyzer analyse = new TuscanyProgramAnalyzer();
			// String[] classesToBeAnalysed = new String[] {
			// "/home/nju/2.0-DU/samples/authUpdate/conup-sample-db/target/classes/",
			// "/home/nju/2.0-DU/samples/authUpdate/conup-sample-auth/target/classes/",
			// "/home/nju/2.0-DU/samples/authUpdate/conup-sample-proc/target/classes/",
			// "/home/nju/2.0-DU/samples/authUpdate/conup-sample-portal/target/classes/"
			// };
			//
			// for (int i = 0; i < classesToBeAnalysed.length; i++) {
			// analyse.analyzeSource(classesToBeAnalysed[i]);
			// }

			// test jar,war,ear
			/*
			 * ProgramAnalyzer analyse = new ProgramAnalyzer(); String
			 * jarLocation = "/home/nju/conup-sample-auth.jar"; String
			 * tempLocation = "/home/temp/conup-sample-auth"; String
			 * outputLocation = "/home/analyzed/conup-sample-auth.jar";
			 * analyse.analyzeJar(jarLocation, tempLocation, outputLocation);
			 */

			/*
			 * // Test all! ProgramAnalyzer analyse = new ProgramAnalyzer();
			 * LOGGER.info(
			 * "Please input the application path to be analyze:(For example : /home/nju/workspace/conup-sample-auth/target/classes/)"
			 * ); InputStreamReader is = new InputStreamReader(System.in);
			 * BufferedReader br = new BufferedReader(is); // String
			 * classesToBeAnalyse = br.readLine(); String classesToBeAnalyse
			 * ="/home/nju/conup-sample-auth.jar"; String[] names =
			 * classesToBeAnalyse.split("/"); String fileName =
			 * names[names.length-1];
			 * if(fileName.endsWith(".jar")||fileName.endsWith
			 * (".war")||fileName.endsWith(".ear")||fileName.endsWith(".zip")){
			 * LOGGER.info(
			 * "It is a .jar,.war or an .ear,please input the unjared file path and the dest file path:(For example: /home/analyzed/ /home/temp/)"
			 * ); // String infos = br.readLine(); String infos =
			 * "/home/temp/ /home/analyzed/"; String tempLocation =
			 * infos.split(" ")[0]+fileName.substring(0, fileName.length()-4);
			 * String outputLocation = infos.split(" ")[1]+fileName;
			 * analyse.analyzeJar(classesToBeAnalyse, tempLocation,
			 * outputLocation); } else {
			 * analyse.analyzeSource(classesToBeAnalyse); }
			 */
			// test a source application
			/*
			 * ProgramAnalyzer analyse=new ProgramAnalyzer(); String projectPath
			 * ="/home/nju/workspace/vc-policy-db-node/target/classes/"; //
			 * String input = args[0]; String input = projectPath; File tempFile
			 * = new File(input); tempFile.mkdir();
			 * analyse.findAllCom(tempFile); analyse.beginAnalyze(tempFile,
			 * "Lcn/edu/nju/moon/vc/def/VcTransaction;");
			 */
			/*
			 * // test jar,war,ear ProgramAnalyzer analyse=new
			 * ProgramAnalyzer(); UnjarTool jar2temp = new UnjarTool(); JarTool
			 * temp2jar = new JarTool();
			 * 
			 * String input = args[0]; String temp = args[1]; String output =
			 * args[2]; // String input =
			 * "D:\\program files\\kitchensink-jsp\\target\\jboss-as-kitchensink-jsp.war"
			 * ; // String temp = "e:\\temp"; // String output =
			 * "D:\\program files\\jboss-as-7.1.1.Final\\standalone\\deployments\\jboss-as-kitchensink-jsp.war"
			 * ; File tempFile = new File(temp); tempFile.mkdir(); File destJar
			 * = new File(output); jar2temp.unjar(input, temp);
			 * findAllCom(tempFile); analyse.beginAnalyze(tempFile);
			 * temp2jar.jarDir(tempFile, destJar);
			 */
			// for debug
			// test a class file
			// String
			// filename1="/home/nju/workspace/vc-policy-portal-node/target/classes/cn/edu/nju/moon/vc/portal/services/PortalServiceImpl.class";
			// String
			// filename1="D:\\program files\\vc-policy-proc-node\\target\\classes\\services\\ProcServiceImpl.class";
			// String
			// filename1="D:\\program files\\vc-policy-portal-node\\target\\classes\\launch\\ServiceAccessing.class";
			// String
			// filename1="C:\\Users\\SuPing\\workspace\\asmtest\\bin\\Ttest.class";
			// String filename1="E:\\ServiceAccessing.class";
			// FileInputStream in = new FileInputStream(filename1);
			// //showClassSource("D:\\program files\\vc-policy-portal-node\\target\\classes\\launch\\ServiceAccessing.class");
			// ProgramAnalyzer t = new ProgramAnalyzer();
			// t.findComponents("/home/nju/workspace/vc-policy-portal-node/target/classes/portal.composite");
			// ClassReader cr = new ClassReader(in);
			// ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			// ClassVisitor cv =
			// t.getClassAdapter(cw,"Lcn/edu/nju/moon/vc/def/VcTransaction;");
			// cr.accept(cv, 0);
			// byte[] b = cw.toByteArray();
			//
			// String [] classname = filename1.split("/");
			// String real_classname = classname[classname.length-1];
			// File dir = new File("/home/analyzed");
			// dir.mkdir();
			// File out = new File("/home/analyzed/" + real_classname);
			// FileOutputStream fout = new FileOutputStream(out);
			// fout.write(b);
			// fout.close();
			// String showclass =
			// "/home/nju/workspace/vc-policy-portal-node/target/classes/cn/edu/nju/moon/vc/portal/services/PortalServiceImpl.class";
			// showClassSource(showclass);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
