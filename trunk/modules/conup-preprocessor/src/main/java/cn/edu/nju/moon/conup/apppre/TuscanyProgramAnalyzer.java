package cn.edu.nju.moon.conup.apppre;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;

import cn.edu.nju.moon.conup.txpre.MethodAnalyzer;
import static org.objectweb.asm.Opcodes.*;

/**
 * Analyze the tuscany program
 * 
 * @author Ping Su<njupsu@gmail.com>
 * 
 */
public class TuscanyProgramAnalyzer {

	private final static Logger LOGGER = Logger
			.getLogger(TuscanyProgramAnalyzer.class.getName());

	public static Logger getLogger() {
		return LOGGER;
	}
	
//	List<String> allServices = null;
	/**
	 * Every updatable class should be added a field for generating transaction
	 * id.
	 */
	String fieldName = "_txLifecycleMgr";
	String fieldDesc = "Lcn/edu/nju/moon/conup/spi/tx/TxLifecycleManager;";
	
//	public List<String> getAllServices(){
//		return allServices;
//	}
	
	/**
	 * insert field TxLifecycleManager and the method setTxLifecycleManager for
	 * generating transaction id; public void
	 * setTxLifecycleManager(TxLifecycleManager tlm){ txLifecycleMgr = tlm; }
	 * 
	 * @param cn
	 */
	@SuppressWarnings("unchecked")
	public boolean addTxLifecycleManager(ClassNode cn) {
		for (FieldNode fn : (List<FieldNode>) cn.fields) {
			if (fieldName.equals(fn.name)) {
				LOGGER.info("There has already a _txLifecycleMgr in the "
						+ cn.name);
				return false;
			}
		}
		cn.fields.add(new FieldNode(ACC_PRIVATE, fieldName, fieldDesc, null,
				null));
		LOGGER.fine("Add field _txLifecycleMgr success!");
		return true;
	}

	/**
	 * 
	 * @param cn
	 * @param conupTx
	 *            default
	 * @return
	 */
	@SuppressWarnings("unchecked")
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
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void transform(ClassNode cn, String conupTx,String reference) {
		List<String> allServices = findAllServices(cn, reference);
		if (addTxLifecycleManager(cn)) {
			for (MethodNode mn : (List<MethodNode>) cn.methods) {
				MethodAnalyzer methodtransform = new MethodAnalyzer(
						allServices, fieldName, fieldDesc);
				methodtransform.methodTransform(cn, mn, conupTx);
			}
		} else {
			LOGGER.info("The insert didn't work because the injection of _txLifecycleMgr fails!");
		}

	}
	/**
	 * Every possible component/service has a method with annotation @reference 
	 * @param cn
	 */
	@SuppressWarnings("unchecked")
	public List<String> findAllServices(ClassNode cn,String reference) {
		List<String> allServices = new LinkedList<String>();
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
										LOGGER.fine("All possible Services to be used:"+serviceName);
										allServices.add(serviceName);
									}
								}								
							}
						}
					}
				}
			}
		}
		return allServices;
	}
	/**
	 * Whether the field is a service
	 * @param cn
	 * @param fieldName
	 * @param fieldDesc
	 * @param owner
	 * @return
	 */
	@SuppressWarnings("unchecked")
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
	 * find and analyze class files recursively
	 * 
	 * @param tempFile
	 */
	protected void beginAnalyze(File tempFile, String conupTx,String reference) {
		if (tempFile.isDirectory()) {
			File file[] = tempFile.listFiles();
			for (int i = 0; i < file.length; i++) {
				beginAnalyze(file[i], conupTx,reference);
			}
		} else {
			try {
				if (tempFile.getName().endsWith(".class")) {					
					LOGGER.fine("Analyze file:" + tempFile.getName());
					FileInputStream input = new FileInputStream(
							tempFile.getAbsolutePath());
					ClassReader cr = new ClassReader(input);
					ClassNode cn = new ClassNode();
					cr.accept(cn, 0);
					if (whetherToAnalyze(cn, conupTx)) {
						LOGGER.fine("Need analyze file:" + tempFile.getName());
						transform(cn,conupTx,reference);
						ClassWriter cw = new TranClassWriter(ClassWriter.COMPUTE_FRAMES);	
						cn.accept(cw);
						byte[] b = cw.toByteArray();
						FileOutputStream fout = new FileOutputStream(new File(
								tempFile.getAbsolutePath()));
//						FileOutputStream fout = new FileOutputStream(new File("/home/PortalServiceImpl.class"));
						fout.write(b);
						fout.close();
					}
					else{
						LOGGER.fine("Don't need analyze file:" + tempFile.getName());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	/**
	 * @param file 
	 * file path
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
	 * analyze a uncompressed file
	 * @param projectLocation
	 */
	public void analyzeSource(String projectLocation) {
		String input = projectLocation;
		File tempFile = new File(input);
		tempFile.mkdir();
		beginAnalyze(tempFile, "Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;","Lorg/oasisopen/sca/annotation/Reference;");
	}

	/**
	 * analyze compressed files : .war, .ear, .jar, .zip
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
	 * analyze an tuscany application, whether a file or a compressed file. If it's a compressed one,we will uncompress
	 * it in the tempPath, and put the analyzed one in the target path.
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
			TuscanyProgramAnalyzer analyse = new TuscanyProgramAnalyzer();			
			
			List<String> targetProjs = new ArrayList<String>();
			targetProjs.add("conup-travel-sample/fullapp-bespoketrip");
			targetProjs.add("conup-travel-sample/fullapp-coordination");
			targetProjs.add("conup-travel-sample/fullapp-currency");
			targetProjs.add("conup-travel-sample/fullapp-packagedtrip");
			targetProjs.add("conup-travel-sample/fullapp-shoppingcart");
			targetProjs.add("conup-travel-sample/fullapp-bank");
			targetProjs.add("conup-travel-sample/payment-java");
			
			targetProjs.add("authUpdate/conup-sample-auth");
			targetProjs.add("authUpdate/conup-sample-db");
			targetProjs.add("authUpdate/conup-sample-portal");
			targetProjs.add("authUpdate/conup-sample-proc");
			
			String baseDir = "/home/artemis/Documents/conup/samples/";
			for(String projLoc : targetProjs){
				projLoc = baseDir + projLoc + "/target/classes";
				analyse.analyzeApplication(projLoc, "");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
class TranClassWriter extends ClassWriter {

	public TranClassWriter(final int flags) {
	        super(flags);	       
	    }
	@Override
	 protected String getCommonSuperClass(final String type1, final String type2)
	 {
		Class<?> c, d;
		try {
			c = Class.forName(type1.replace('/', '.'));
			d = Class.forName(type2.replace('/', '.'));
		} catch (Exception e) {
			System.out.println("Exception, Type: " + type1 + "," + type2);
			System.out.println("Exception, "+e.toString());
			return "java/lang/Object";
		}
		if (c.isAssignableFrom(d)) {
			return type1;
		}
		if (d.isAssignableFrom(c)) {
			return type2;
		}
		if (c.isInterface() || d.isInterface()) {
			return "java/lang/Object";
		} else {
			do {
				c = c.getSuperclass();
			} while (!c.isAssignableFrom(d));
			return c.getName().replace('.', '/');
		}
	}
}
