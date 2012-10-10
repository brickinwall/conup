package cn.edu.nju.moon.conup.pre;

import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;

import cn.edu.nju.moon.conup.pre.ProgramAnalyzer;

/**
 * @author <a href="mailto:njupsu@gmail.com">Su Ping</a>
 */
public class ProgramAnalyzer {


	List<String> coms = new LinkedList<String>();

	/**
	 * the given class whether has @Adaptive and @AEjb
	 * 
	 * @param cn
	 * @return
	 */
	public boolean isAnalyze(ClassNode cn) {

		if (cn.visibleAnnotations != null) {
			Iterator<AnnotationNode> i = cn.visibleAnnotations.iterator();
			while (i.hasNext()) {
				AnnotationNode an = i.next();
				if (an.desc.contains("Ljavax/aejb/Adaptive")) {
					// System.out.println(an.desc);
					for (FieldNode fn : (List<FieldNode>) cn.fields) {
						if (fn.visibleAnnotations != null) {
							Iterator<AnnotationNode> fi = fn.visibleAnnotations
									.iterator();
							while (fi.hasNext()) {
								AnnotationNode fa = fi.next();
								// System.out.println(fa.desc);
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
	 * 
	 * @param cn
	 *            the class to analyze and transform
	 * 
	 */
	public void transform(ClassNode cn, String analyzername) {
		System.out.println("Begin analyzing class:" + cn.name);
		for (MethodNode mn : (List<MethodNode>) cn.methods) {
			MethodAnalyzer methodtransform = new MethodAnalyzer();
			// methodtransform.setCom(findComponents("D:\\program files\\vc-policy-proc-node\\target\\classes\\proc.composite"));
			methodtransform.setCom(coms);
			methodtransform.methodTransform(cn, mn, analyzername);
			// methodtransform.methodTransformForExperiment(cn, mn,
			// analyzername);
			// methodtransform.test(cn, mn);
			/*
			 * MAnalyze methodtransform = new MAnalyze();
			 * methodtransform.methodTransform(cn, mn);
			 */

		}

	}

	/**
	 * search the configure file ".composite" for the components. Every
	 * component is like "component/service"
	 * 
	 * @param fileName
	 */
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
					System.out.println("com"
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
		// return com;

	}

	/**
	 * find all the components the transaction may use possibly
	 * 
	 * @param tempFile
	 */
	public void findAllCom(File tempFile) {
		if (tempFile.isDirectory()) {
			File file[] = tempFile.listFiles();
			for (int i = 0; i < file.length; i++) {
				System.out.println("Analyze file:" + file[i].getName());
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
			final String analyzername) {
		return new ClassNode() {
			@Override
			public void visitEnd() {
				transform(this, analyzername);
				this.accept(cvr);
			}
		};
	}

	/**
	 * find and analyze class files recursively
	 * 
	 * @param tempFile
	 */
	protected void begin_analyze(File tempFile, String analyzername) {
		if (tempFile.isDirectory()) {
			File file[] = tempFile.listFiles();
			for (int i = 0; i < file.length; i++) {
				System.out.println("Analyze file:" + file[i].getName());
				begin_analyze(file[i], analyzername);
			}
		} else {
			try {
				if (tempFile.getName().endsWith(".class")) {
					System.out.println(tempFile.getName());
					FileInputStream input = new FileInputStream(
							tempFile.getAbsolutePath());
					ClassReader cr = new ClassReader(input);
					ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
					ClassVisitor cv = getClassAdapter(cw, analyzername);
					cr.accept(cv, 0);

					byte[] b = cw.toByteArray();

					FileOutputStream fout = new FileOutputStream(new File(
							tempFile.getAbsolutePath()));
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
		begin_analyze(tempFile, "Lcn/edu/nju/moon/conup/def/VcTransaction;");
	}
	/**
	 * analyze jar files : .war, .ear, .jar, .zip
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
		begin_analyze(tempFile, "Lcn/edu/nju/moon/conup/def/VcTransaction;");
		
		File destJar = new File(output);
		try {
			temp2jar.jarDir(tempFile, destJar);
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
	public void analyzeApplication(String sourcePath,String tempPath,String targetPath){			
		String[] names = sourcePath.split("/");
		String fileName = names[names.length-1];
		if(fileName.endsWith(".jar")||fileName.endsWith(".war")||fileName.endsWith(".ear")||fileName.endsWith(".zip")){
			String tempLocation = tempPath+fileName.substring(0, fileName.length()-4);
			String outputLocation =	targetPath+fileName;
			analyzeJar(sourcePath, tempLocation, outputLocation);
		}
		else
		{
			analyzeSource(sourcePath);
		}
	}
	/**
	 * target application overwrites the source one.
	 * @param sourcePath
	 * @param tempPath
	 */
	public void analyzeApplication(String sourcePath,String tempPath){
		String[] names = sourcePath.split("/");
		String fileName = names[names.length-1];
		if(fileName.endsWith(".jar")||fileName.endsWith(".war")||fileName.endsWith(".ear")||fileName.endsWith(".zip")){
			String tempLocation = tempPath+fileName.substring(0, fileName.length()-4);			
			analyzeJar(sourcePath, tempLocation, sourcePath);
		}
		else
		{
			analyzeSource(sourcePath);
		}
	}

	public static void main(String args[]) {
		try {
			ProgramAnalyzer analyse = new ProgramAnalyzer();
//			analyse.analyzeApplication("/home/analyzed/conup-sample-portal.jar", "/home/temp/", "/home/analyzed/");
			analyse.analyzeApplication("/home/analyzed/conup-sample-auth.jar", "/home/temp/");
			// test a source application
//			 String projectPath ="/home/nju/workspace/conup-sample-db/target/classes/";
//			 String projectPath ="/home/nju/workspace/conup-sample-auth/target/classes/";
//			 String projectPath ="/home/nju/workspace/conup-sample-proc/target/classes/";
//			 String projectPath ="/home/nju/workspace/conup-sample-portal/target/classes/";
//			 ProgramAnalyzer analyse=new ProgramAnalyzer();
//			 analyse.analyzeSource(projectPath);
/*			ProgramAnalyzer analyse = new ProgramAnalyzer();
			String[] classesToBeAnalysed = new String[] {
					"/home/nju/workspace/conup-sample-db/target/classes/",
					"/home/nju/workspace/conup-sample-auth/target/classes/",
					"/home/nju/workspace/conup-sample-proc/target/classes/",
					"/home/nju/workspace/conup-sample-portal/target/classes/" };

			for (int i = 0; i < classesToBeAnalysed.length; i++) {
				analyse.analyzeSource(classesToBeAnalysed[i]);
			}
*/
			
/*			// test jar,war,ear
			ProgramAnalyzer analyse = new ProgramAnalyzer();
			String jarLocation = "/home/nju/conup-sample-auth.jar";
			String tempLocation = "/home/temp/conup-sample-auth";
			String outputLocation =	"/home/analyzed/conup-sample-auth.jar";
			analyse.analyzeJar(jarLocation, tempLocation, outputLocation);
*/
			
			
/*			// Test all!
			ProgramAnalyzer analyse = new ProgramAnalyzer();
			System.out.println("Please input the application path to be analyze:(For example : /home/nju/workspace/conup-sample-auth/target/classes/)");
			InputStreamReader is = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(is);			
//			String classesToBeAnalyse = br.readLine();
			String classesToBeAnalyse ="/home/nju/conup-sample-auth.jar";
			String[] names = classesToBeAnalyse.split("/");
			String fileName = names[names.length-1];
			if(fileName.endsWith(".jar")||fileName.endsWith(".war")||fileName.endsWith(".ear")||fileName.endsWith(".zip")){
				System.out.println("It is a .jar,.war or an .ear,please input the unjared file path and the dest file path:(For example: /home/analyzed/ /home/temp/)");
//				String infos = br.readLine();
				String infos = "/home/temp/ /home/analyzed/";
				String tempLocation = infos.split(" ")[0]+fileName.substring(0, fileName.length()-4);
				String outputLocation =	infos.split(" ")[1]+fileName;
				analyse.analyzeJar(classesToBeAnalyse, tempLocation, outputLocation);
			}
			else
			{
				analyse.analyzeSource(classesToBeAnalyse);
			}
*/			
			// test a source application
			/*
			 * ProgramAnalyzer analyse=new ProgramAnalyzer(); String projectPath
			 * ="/home/nju/workspace/vc-policy-db-node/target/classes/"; //
			 * String input = args[0]; String input = projectPath; File tempFile
			 * = new File(input); tempFile.mkdir();
			 * analyse.findAllCom(tempFile); analyse.begin_analyze(tempFile,
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
			 * findAllCom(tempFile); analyse.begin_analyze(tempFile);
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
