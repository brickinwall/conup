package cn.edu.nju.moon.conup.pre;




import static org.objectweb.asm.Opcodes.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * 
 * 
 * @author <a href="mailto:njupsu@gmail.com">Su Ping</a>
 */
public class MethodAnalyzer {
	private final static Logger LOGGER = Logger.getLogger(MethodAnalyzer.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	}

	/**
	 *鐎涙ɑ鏂佸В蹇庨嚋閹貉冨煑濞翠胶鈻奸惃鍕徔娴ｆ挷淇婇幁顖ょ礉閸楃i-ai-sj
	 */
	StateMachine stateMachine = new StateMachine();
	
	/**
	 * 鐎涙ɑ鏂侀悩鑸碉拷閺堣桨淇婇幁顖ょ礉娑撴槒顪呴弰顖氬瀻閺嬫劗鈻兼惔蹇曟畱閹貉冨煑濞达拷
	 */
	ControlFlow controlflow = new ControlFlow();
	
	/**
	 * 濮ｅ繋閲滈悩鑸碉拷閻愮懓鍑℃担璺ㄦ暏閻ㄥ嫬顕径鏍殶閻拷
	 */
	List<String>[] past;
	
	/**
	 * 濮ｅ繋閲滈悩鑸碉拷閻愮懓鐨㈢憰浣烘畱鐎电懓顧囩拫鍐暏
	 */
	List<String>[] future;
	
	/**
	 * 鐎碉拷?绨鎻掑瀻閺嬫劕鍤惃鍕儲鏉烆剙寮烽崷銊︽暭閻愮顪呴崝鐘插弳閻ㄥ嫯鐑︽潪顑夸繆閹拷
	 */
	Hashtable<AbstractInsnNode, List<String>> jumpinf = new Hashtable<AbstractInsnNode,List<String>>();
	Hashtable<AbstractInsnNode, String> ejbinf = new Hashtable<AbstractInsnNode, String>();
	Hashtable<String, String> ejball= new Hashtable<String, String>();
	List<AbstractInsnNode> runinf = new LinkedList<AbstractInsnNode>();
	int runNum = 0;
	boolean isBranch = false;
	
	
	List<State> states = new LinkedList<State>(); 
	List<String> com = new LinkedList<String>();
	String statesDDA = "";
	String nextsDDA = "";
	/**
	 * 閸掑棙鐎介崙鍝勵嚠鎼存梹鐦℃稉顏嗗Ц閹胶娈戦幍锟戒簰閸欘垵鍏橀惃鍕瑓濮濄儱濮╂担婊冨挤閹碉拷鍩屾潏鍓ф畱娑撳閲滈悩鑸碉拷
	 */
	List<String> next = new LinkedList<String>();
	
	/**
	 * 閸掑棙鐎介崙铏规畱閹碉拷浜掗悩鑸碉拷娣団剝锟?
	 */
	List<String> stateall = new LinkedList<String>();
	/**
	 * whether the bytecode  is analyzed;
	 */
	private int [] isAnalyze;
	
	/**
	 * 
	 * @param n
	 * the number of bytecodes in the program to be analyzed 
	 */
	private void initIsAnalyze(int n){
		isAnalyze = new int[n];
		for(int i=0;i<n;i++){
			isAnalyze[i] = 0;			
		}
	}
	
	public void setCom(List<String> c){
		com=c;		
	}
	
	public void writeState(MethodNode mn){
		statesDDA = "";
		nextsDDA = "";
		for(String s : stateall){
			if(s.isEmpty())
			{
				s="_E";
			}
			statesDDA = statesDDA + s+";";
		}
		statesDDA = statesDDA.subSequence(0, statesDDA.length()-1).toString();
//		System.out.println("---------state size:"+stateall.size()+"!statesDDA"+statesDDA);		
		for(String s : next){
			if(s.isEmpty())
			{
				s="_E";
			}
			nextsDDA = nextsDDA + s+";";
		}
		nextsDDA = nextsDDA.subSequence(0, nextsDDA.length()-1).toString();
//		System.out.println("---------next size:"+next.size()+"!nextsDDA"+nextsDDA);		
/*		try{
		  File dir = new File("e:\\ttp");
		  dir.mkdir();		 
		  FileWriter fw = new FileWriter("e:\\ttp\\"+mn.name+".txt");
		  BufferedWriter bw = new BufferedWriter(fw);		 
		  bw.write(statew);
		  bw.newLine();
		  bw.write(nextw);
		  bw.flush(); //灏嗘暟鎹洿鏂拌嚦鏂囦欢
		  bw.close();
		  fw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
*/	
	}
	/**
	 * insert the trigger information: use notify
	 * @param cn
	 * @param mn
	 * @param analyzername
	 * the transaction or method name to be analyze
	 */
	public void methodTransform(ClassNode cn,MethodNode mn,String analyzername) {
	

		           if (mn.name.equals(analyzername)||isTransaction(mn, analyzername)) {
		        	   LOGGER.fine("Begin analyze method:" + mn.name);
			            //setEjbs(cn);			
//						LOGGER.fine("begin analyze method:"+mn.name);
						InsnList insns = mn.instructions;
						stateMachine.setStart(0);
						stateMachine.setEnd(mn.instructions.size());
						try {
							ExtractControlFlow(cn.name, mn);
						} catch (AnalyzerException ignored) {
						}
						initIsAnalyze(mn.instructions.size());
						recognize_state2(0, 0, mn.instructions);
						mergeState();
						ExtractMetaData();
						setStates();
						//minimizeState();
						setStateAll();						
						setNext();
						writeState(mn);
						int localNum = mn.localVariables.size();
//insert annotation
						Iterator<AnnotationNode> iter = mn.visibleAnnotations.iterator();
	/*					while (iter.hasNext()) {
							AnnotationNode an = iter.next();
							if (an.desc.equals(analyzername)) {
								List<Object> valuenew = new LinkedList<Object>();
								valuenew.add("name");
								valuenew.add(mn.name);
								valuenew.add("states");
								valuenew.add(stateall);
								valuenew.add("next");
								valuenew.add(next);
								an.values = valuenew;
								System.out.println(an.values);
							}
						}
						
*/
						InsnList trigstart = new InsnList();
//insert mn.name.thread id
						trigstart.add(new TypeInsnNode(NEW, "java/lang/Integer"));									
						trigstart.add(new InsnNode(DUP));
						trigstart.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;"));
						trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I"));					
						trigstart.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/Integer", "<init>", "(I)V"));
						trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Integer", "toString", "()Ljava/lang/String;"));
						trigstart.add(new VarInsnNode(ASTORE, localNum));
						trigstart.add(new TypeInsnNode(NEW, "java/lang/StringBuilder"));									
						trigstart.add(new InsnNode(DUP));
						trigstart.add(new LdcInsnNode(cn.name+";"+mn.name+";"));
						trigstart.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V"));
						trigstart.add(new VarInsnNode(ALOAD, localNum));
						trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));					
						trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));
						trigstart.add(new VarInsnNode(ASTORE, localNum));
						
						
						trigstart.add(new VarInsnNode(ALOAD, localNum));
						trigstart.add(new LdcInsnNode(statesDDA));
//						System.out.println("----------------------StatesDDA"+statesDDA);
						trigstart.add(new LdcInsnNode(nextsDDA));
						trigstart.add(new MethodInsnNode(INVOKESTATIC, "cn/edu/nju/moon/conup/pre/DynamicDependency", "getInstance", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcn/edu/nju/moon/conup/pre/DynamicDependency;"));									
//						trigstart.add(new VarInsnNode(ALOAD, 0));
//						trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;"));
						trigstart.add(new VarInsnNode(ALOAD, localNum));
						trigstart.add(new LdcInsnNode("Start"));						
						trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, "cn/edu/nju/moon/conup/pre/DynamicDependency", "trigger", "(Ljava/lang/String;Ljava/lang/String;)V"));
			
						insns.insert(insns.getFirst(),trigstart);
					    Iterator<AbstractInsnNode> i = insns.iterator();
						int src = 0;
						while (i.hasNext()) {
							AbstractInsnNode i1 = i.next();
//							System.out.println(((AbstractInsnNode)i1).toString());					
							if (i1 instanceof MethodInsnNode) {	
								//COM
							if(runNum > 0){
									if(runinf.contains(i1)){										
										InsnList setUp = new InsnList();
										setUp.add(new VarInsnNode(ALOAD, localNum));										
										setUp.add(new LdcInsnNode(statesDDA));
										setUp.add(new LdcInsnNode(nextsDDA));
										setUp.add(new MethodInsnNode(INVOKESTATIC, "cn/edu/nju/moon/conup/pre/DynamicDependency", "getInstance", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcn/edu/nju/moon/conup/pre/DynamicDependency;"));
										setUp.add(new MethodInsnNode(INVOKEVIRTUAL, "cn/edu/nju/moon/conup/pre/DynamicDependency", "notifyRun", "()V"));
										AbstractInsnNode ilPre = i1.getPrevious();
										int op=ilPre.getOpcode();
										while (op == ALOAD||op == LLOAD||op == FLOAD||op == DLOAD||op == ILOAD) {
											ilPre = ilPre.getPrevious();
											op = ilPre.getOpcode();
										} 
										insns.insert(ilPre, setUp);
									}
								}

								if (ejbinf.containsKey(i1)) {
									InsnList trig = new InsnList();
									trig.add(new VarInsnNode(ALOAD, localNum));
									trig.add(new LdcInsnNode(statesDDA));
									trig.add(new LdcInsnNode(nextsDDA));
							        trig.add(new MethodInsnNode(INVOKESTATIC, "cn/edu/nju/moon/conup/pre/DynamicDependency", "getInstance", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcn/edu/nju/moon/conup/pre/DynamicDependency;"));									
			//						trig.add(new VarInsnNode(ALOAD, 0));
			//						trig.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;"));
							        trig.add(new VarInsnNode(ALOAD, localNum));
									trig.add(new LdcInsnNode(ejbinf.get(i1)));
									trig.add(new MethodInsnNode(INVOKEVIRTUAL, "cn/edu/nju/moon/conup/pre/DynamicDependency", "trigger", "(Ljava/lang/String;Ljava/lang/String;)V"));
									
									int op=i1.getNext().getOpcode();
									if (op == ISTORE||op == LSTORE||op == FSTORE||op == DSTORE||op == ASTORE) {
										insns.insert(i1.getNext(), trig);
									} else {
										insns.insert(i1, trig);
									}
								}

							} else {
								if (jumpinf.containsKey(i1)) {
									List<String> jumpEventInf = jumpinf.get(i1);
									for(String jumpE : jumpEventInf){
									InsnList trig = new InsnList();
									trig.add(new VarInsnNode(ALOAD, localNum));
									trig.add(new LdcInsnNode(statesDDA));
									trig.add(new LdcInsnNode(nextsDDA));
							        trig.add(new MethodInsnNode(INVOKESTATIC, "cn/edu/nju/moon/conup/pre/DynamicDependency", "getInstance", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcn/edu/nju/moon/conup/pre/DynamicDependency;"));									
						//			trig.add(new VarInsnNode(ALOAD, 0));
						//			trig.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;"));
							        trig.add(new VarInsnNode(ALOAD, localNum));
									trig.add(new LdcInsnNode(jumpE));
									trig.add(new MethodInsnNode(INVOKEVIRTUAL, "cn/edu/nju/moon/conup/pre/DynamicDependency", "trigger", "(Ljava/lang/String;Ljava/lang/String;)V"));
									
									if(i1 instanceof LabelNode){
										if(i1.getNext() instanceof LineNumberNode)
											insns.insert(i1.getNext(), trig);
										else
											insns.insert(i1, trig);
									}										
									else
										insns.insert(i1.getPrevious(), trig);
									}
								} else {
									if ((i1.getOpcode() >= IRETURN && i1.getOpcode() <= RETURN)|| i1.getOpcode()==ATHROW) {
										InsnList trig = new InsnList();
										trig.add(new VarInsnNode(ALOAD, localNum));
										trig.add(new LdcInsnNode(statesDDA));
										trig.add(new LdcInsnNode(nextsDDA));
								        trig.add(new MethodInsnNode(INVOKESTATIC, "cn/edu/nju/moon/conup/pre/DynamicDependency", "getInstance", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcn/edu/nju/moon/conup/pre/DynamicDependency;"));									
				//						trig.add(new VarInsnNode(ALOAD, 0));
				//						trig.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;"));
								        trig.add(new VarInsnNode(ALOAD, localNum));
										trig.add(new LdcInsnNode(""));
										trig.add(new MethodInsnNode(INVOKEVIRTUAL, "cn/edu/nju/moon/conup/pre/DynamicDependency", "trigger", "(Ljava/lang/String;Ljava/lang/String;)V"));
										insns.insert(i1.getPrevious(), trig);
									}
								}

							}					

						}
						LabelNode last = ((LocalVariableNode)mn.localVariables.get(0)).end;
						LabelNode start = ((LocalVariableNode)mn.localVariables.get(0)).start;;
						mn.localVariables.add(new LocalVariableNode("threadID", "Ljava/lang/String;", null, start, last, localNum));
						mn.maxLocals=mn.maxLocals+1;
						
				}

	}
	
	public void methodTransformForExperiment(ClassNode cn,MethodNode mn,String analyzername) {
		
        if (mn.name.equals(analyzername)||isTransaction(mn, analyzername)) {
	            //setEjbs(cn);			
				System.out.println("begin analyze method:"+mn.name);
				InsnList insns = mn.instructions;
				stateMachine.setStart(0);
				stateMachine.setEnd(mn.instructions.size());
				try {
					ExtractControlFlow(cn.name, mn);
				} catch (AnalyzerException ignored) {
				}
				initIsAnalyze(mn.instructions.size());
				recognize_state2(0, 0, mn.instructions);
				mergeState();
				ExtractMetaData();
				setStates();
//              minimizeState();
				setStateAll();						
				setNext();
				writeState(mn);
				

				InsnList trigstart = new InsnList();
				trigstart.add(new LdcInsnNode(mn.name));
				trigstart.add(new MethodInsnNode(INVOKESTATIC, "cn/edu/nju/DynamicDependency", "getInstance", "(Ljava/lang/String;)Lcn/edu/nju/DynamicDependency;"));									
				trigstart.add(new LdcInsnNode(mn.name));
				trigstart.add(new LdcInsnNode("Start"));						
				trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, "cn/edu/nju/DynamicDependency", "trigger", "(Ljava/lang/String;Ljava/lang/String;)V"));
				insns.insert(insns.getFirst(),trigstart);
			    Iterator<AbstractInsnNode> i = insns.iterator();
				int src = 0;
				while (i.hasNext()) {
					AbstractInsnNode i1 = i.next();
//					System.out.println(((AbstractInsnNode)i1).toString());					
					if (i1 instanceof MethodInsnNode) {	
						//EJB
						if (ejbinf.containsKey(i1)) {
							InsnList trig = new InsnList();
							trig.add(new LdcInsnNode(mn.name));
					        trig.add(new MethodInsnNode(INVOKESTATIC, "cn/edu/nju/DynamicDependency", "getInstance", "(Ljava/lang/String;)Lcn/edu/nju/DynamicDependency;"));									
					        trig.add(new LdcInsnNode(mn.name));
							trig.add(new LdcInsnNode(ejbinf.get(i1)));
							trig.add(new MethodInsnNode(INVOKEVIRTUAL, "cn/edu/nju/DynamicDependency", "trigger", "(Ljava/lang/String;Ljava/lang/String;)V"));

							int op=i1.getNext().getOpcode();
							if (op == ISTORE||op == LSTORE||op == FSTORE||op == DSTORE||op == ASTORE) {
								insns.insert(i1.getNext(), trig);
							} else {
								insns.insert(i1, trig);
							}
		}

					} else {
						if (jumpinf.containsKey(i1)) {
							List<String> jumpEventInf = jumpinf.get(i1);
							for(String jumpE : jumpEventInf){
							InsnList trig = new InsnList();
							trig.add(new LdcInsnNode(mn.name));
					        trig.add(new MethodInsnNode(INVOKESTATIC, "cn/edu/nju/DynamicDependency", "getInstance", "(Ljava/lang/String;)Lcn/edu/nju/DynamicDependency;"));									
					        trig.add(new LdcInsnNode(mn.name));
							trig.add(new LdcInsnNode(jumpE));
							trig.add(new MethodInsnNode(INVOKEVIRTUAL, "cn/edu/nju/DynamicDependency", "trigger", "(Ljava/lang/String;Ljava/lang/String;)V"));

							if(i1 instanceof LabelNode){
								if(i1.getNext() instanceof LineNumberNode)
									insns.insert(i1.getNext(), trig);
								else
									insns.insert(i1, trig);
							}										
							else
								insns.insert(i1.getPrevious(), trig);
							}
						} else {
							if ((i1.getOpcode() >= IRETURN && i1.getOpcode() <= RETURN)|| i1.getOpcode()==ATHROW) {
								InsnList trig = new InsnList();
								trig.add(new LdcInsnNode(mn.name));
						        trig.add(new MethodInsnNode(INVOKESTATIC, "cn/edu/nju/DynamicDependency", "getInstance", "(Ljava/lang/String;)Lcn/edu/nju/DynamicDependency;"));									
						        trig.add(new LdcInsnNode(mn.name));
								trig.add(new LdcInsnNode(""));
								trig.add(new MethodInsnNode(INVOKEVIRTUAL, "cn/edu/nju/DynamicDependency", "trigger", "(Ljava/lang/String;Ljava/lang/String;)V"));
								insns.insert(i1.getPrevious(), trig);
							}
						}

					}

				}	
		}

}
	
	/**
	 * 閸掋倖鏌囩拠銉︽煙濞夋洘妲搁崥锔芥Ц闂囷拷顪呴崚鍡橈拷?閻ㄥ嫭鏌熷▔锟?	 * @param mn
	 * @param annotationDesc
	 * @return
	 */
	public boolean isTransaction(MethodNode mn, String annotationDesc) {

		if (mn.visibleAnnotations != null) {
			Iterator<AnnotationNode> i = mn.visibleAnnotations.iterator();
			while (i.hasNext()) {
				AnnotationNode an = i.next();
				if (annotationDesc.equals(an.desc)) {
					return true;
				}
			}
		}
		return false;
	}
	

	
	/**
	 * find all EJB ports in the given class
	 * @param cn
	 */
	public void setEjbs(ClassNode cn) {

		for (FieldNode fn : (List<FieldNode>) cn.fields) {
			if (fn.visibleAnnotations != null) {
				Iterator<AnnotationNode> fi = fn.visibleAnnotations.iterator();
				while (fi.hasNext()) {
					AnnotationNode fa = fi.next();
					//System.out.println(fa.values);
					if (fa.desc.contains("AEjb") || fa.desc.contains("Ejb")) {
						//String ejb = fa.values.get(1).toString().split("/")[0];
						String ejb = fn.desc.substring(1, fn.desc.length()-1);;						
						//System.out.println(ejb+"-"+fn.name);
						ejball.put(ejb, (String)fn.name);
						//System.out.println(ejb+"-"+fn.value);
					}
				}
			}
		}
	}

	// get control flow of an method
	public void ExtractControlFlow(String owner, final MethodNode mn)
			throws AnalyzerException {
		Analyzer a = new Analyzer(new BasicInterpreter()) {

			@Override
			protected void newControlFlowEdge(int src, int dst) {

				controlflow.addFlow(src, dst);
				if (src > dst) {
					controlflow.getFlow(src).setIsWhile(true);
				}
			}
		};
		a.analyze(owner, mn);
	}
	/**
	 * whether the given service is a component inovoked event
	 * @param service
	 * @return component/service
	 */
	public String isCom(String service){
		for(String s:com){
			if(s.split("/")[1].equals(service)){
				return s;
			}
		}
		return null;
	}

	/**
	 * recognize all the state of the method for tuscany application
	 * @param src
	 * @param last_state
	 * @param insns
	 */
	public void recognize_state2(int src, int last_state, InsnList insns) {

			if(src == stateMachine.getStart()){
				stateMachine.addState(src);
				last_state = src;
			}
			if(stateMachine.getStates().contains(src) && (src != last_state)){				
				
			}
			
			if (isAnalyze[src]==0) {				
				AbstractInsnNode an = insns.get(src);
				if (an instanceof MethodInsnNode) {
					isAnalyze[src]=1;
					String [] owners = ((MethodInsnNode) an).owner.split("/");
					String owner = owners[owners.length-1];
					int next =(Integer)controlflow.getFlow(src).dst.get(0);
//					System.out.println(owner);	
//					System.out.println(isCom(owner));
					if (isCom(owner)!= null) {
						if(runNum == 0||(runNum == 1 && isBranch)){
							runinf.add(an);
							runNum = runNum + 1;
						}						
						String e = "COM." + isCom(owner) + "."	+src;
						ejbinf.put(insns.get(src), e);
						if(!stateMachine.getStates().contains(src)){
							stateMachine.addState(src);													
						}
						if(src!=last_state){
							stateMachine.addEvent(new Event(last_state, src, ""));
						}					
						if(!stateMachine.getStates().contains(next)){
								stateMachine.addState(next);								
							}						
						stateMachine.addEvent(new Event(src, next, e));							
						
						recognize_state2(next,next,insns);								
					}
					else
					{
						recognize_state2(next, last_state, insns);
					}
				} 
				else {
					if (an instanceof JumpInsnNode) {
						int dst = -1;
						isAnalyze[src]=1;
						if (an.getOpcode() == GOTO) {
							if(!stateMachine.getStates().contains(src)){
								stateMachine.addState(src);															
							}
							if(src!=last_state){
								stateMachine.addEvent(new Event(last_state, src, ""));
							}
							dst = (Integer) controlflow.getFlow(src).dst.get(0);							
							if(!stateMachine.getStates().contains(dst)){
								stateMachine.addState(dst);														
							}							
							stateMachine.addEvent(new Event(src, dst, ""));							
							recognize_state2(dst, dst, insns);
						} 
						else {
							if(!stateMachine.getStates().contains(src)){
								stateMachine.addState(src);
								}
							if(src!=last_state){
								stateMachine.addEvent(new Event(last_state, src, ""));
							}
							if (controlflow.getFlow(src).isWhile) {
								if(runNum == 0){
									isBranch = true;
								}
								
								for (int k = 0; k < controlflow.getDstSize(src); k++) {									
									dst = (Integer) controlflow.getDst(src).get(k);
									AbstractInsnNode dstNode = insns.get(dst);
									if(!stateMachine.getStates().contains(dst)){
										stateMachine.addState(dst);
									}
									if (src < dst) {										
										stateMachine.addEvent(new Event(src,dst, "while.F."+src));										
										if(jumpinf.containsKey(dstNode)){
											jumpinf.get(dstNode).add("while.F."+src);
										}
										else{
											List<String> branchEvent = new LinkedList<String>();
											branchEvent.add("while.F."+src);
											jumpinf.put(dstNode, branchEvent);
										}									
										recognize_state2(dst, dst, insns);
									} else {										
										stateMachine.addEvent(new Event(src,dst, "while.T."+src));
										if(jumpinf.containsKey(dstNode)){
											jumpinf.get(dstNode).add("while.T."+src);
										}
										else{
											List<String> branchEvent = new LinkedList<String>();
											branchEvent.add("while.T."+src);
											jumpinf.put(dstNode, branchEvent);
										}		
//										jumpinf.put(insns.get(dst),"while.T."+src);
										recognize_state2(dst, dst, insns);
									}
								}
							
							} else {
								if(runNum == 0){
									isBranch = true;
								}
								for (int k = 0; k < controlflow.getDstSize(src); k++) {
									dst = (Integer) controlflow.getDst(src).get(k);
									AbstractInsnNode dstNode = insns.get(dst);
									if(!stateMachine.getStates().contains(dst)){
										stateMachine.addState(dst);
										}
									stateMachine.addEvent(new Event(src, dst,"if." + k+"."+ src));
									if(jumpinf.containsKey(dstNode)){
										jumpinf.get(dstNode).add("if." + k+"."+ src);
									}
									else{
										List<String> branchEvent = new LinkedList<String>();
										branchEvent.add("if." + k+"."+ src);
										jumpinf.put(dstNode, branchEvent);
									}
//									jumpinf.put(insns.get(dst), "if." + k+"."+ src);
									recognize_state2(dst, dst, insns);
								}								
							}
						}
					} else {						
						if((an.getOpcode()>=IRETURN && an.getOpcode()<=RETURN)||an.getOpcode()==ATHROW){
							stateMachine.addState(src);
							isAnalyze[src]=1;
							stateMachine.addEvent(new Event(last_state, src, "end"));
						}
						else{
						isAnalyze[src]=1;
						recognize_state2((Integer) controlflow.getFlow(src).dst.get(0), last_state, insns);
						}
						}
				}
			}
		
	}
	/**
	 * recognize all the state of the method for EJB application
	 * @param src
	 * @param last_state
	 * @param insns
	 */
/*	public void recognize_state(int src, int last_state, InsnList insns) {
		//System.out.println(src);
//		if (src < stateMachine.getEnd()) {
			if(src == stateMachine.getStart()){
				stateMachine.addState(src);
				recognize_state((Integer) controlflow.getFlow(src).dst.get(0), last_state, insns);
			}
			else
			{
			if (stateMachine.getStates().contains(src)) {
				stateMachine.addEvent(new Event(last_state, src, ""));
			} 
			else {
				AbstractInsnNode an = insns.get(src);
				if (an instanceof MethodInsnNode) {
					String owner = ((MethodInsnNode) an).owner;
					//System.out.println(owner);					
					if (ejball.containsKey(owner)) {
						//System.out.println(true);
						String e = "COM." + ejball.get(owner) + "."	+src;
						stateMachine.addState(src);
						stateMachine.addEvent(new Event(last_state, src, e));
						ejbinf.put(insns.get(src), e);
						last_state = src;
						recognize_state((Integer) controlflow.getFlow(src).dst.get(0), last_state, insns);
					}
					else
					{
						recognize_state((Integer) controlflow.getFlow(src).dst.get(0), last_state, insns);
					}
				} 
				else {
					if (an instanceof JumpInsnNode) {

						int dst = -1;
						if (an.getOpcode() == GOTO) {
							dst = (Integer) controlflow.getFlow(src).dst.get(0);
							recognize_state((Integer) controlflow.getFlow(dst).dst.get(0), last_state, insns);
						} 
						else {
							stateMachine.addState(src);
							stateMachine
									.addEvent(new Event(last_state, src, ""));
							if (controlflow.getFlow(src).isWhile) {
								for (int k = 0; k < controlflow.getDstSize(src); k++) {
									dst = (Integer) controlflow.getDst(src)
											.get(k);
									if (src < dst) {
										stateMachine.addState(dst);
										stateMachine.addEvent(new Event(src,
												dst, "while.F."+src));
										jumpinf.put(insns.get(dst), "while.F."+src);
										recognize_state((Integer) controlflow.getFlow(dst).dst.get(0), dst, insns);
									} else {
										stateMachine.addState(dst);
										stateMachine.addEvent(new Event(src,
												dst, "while.T."+src));
										jumpinf.put(insns.get(dst),"while.T."+src);
										recognize_state((Integer) controlflow.getFlow(dst).dst.get(0), dst, insns);
									}
								}
							} else {
								for (int k = 0; k < controlflow.getDstSize(src); k++) {
									dst = (Integer) controlflow.getDst(src)
											.get(k);
									stateMachine.addState(dst);
									stateMachine.addEvent(new Event(src, dst,
											"if." + k+"."+ src));
									jumpinf.put(insns.get(dst), "if." + k+"."+ src);
									recognize_state((Integer) controlflow.getFlow(dst).dst.get(0), dst, insns);
								}
							}
						}
					} else {
						
						if((an.getOpcode()>=IRETURN && an.getOpcode()<=RETURN)||an.getOpcode()==ATHROW){
							stateMachine.addState(src);
							stateMachine.addEvent(new Event(last_state, src, "end"));
						}
						else{
						recognize_state((Integer) controlflow.getFlow(src).dst.get(0), last_state, insns);
						}
						}
				}
			}
		}
			
	}
*/
	// merge the state event is empty or the end
	public void mergeState() {
//		stateMachine.getStates().add(0, stateMachine.getStart());
		boolean change = true;
		while(change){
			change = false;
			for (int i = 0; i < stateMachine.getEvents().size(); i++) {
				Event event = stateMachine.getEvents().get(i);
			if (event.getEvent().isEmpty() || event.getEvent().contains("end")) {
				int src = event.getHead();
				int dst = event.getTail();
				stateMachine.mergeStates(src, dst);
				stateMachine.getEvents().remove(i);
				change = true;
				}
			}
		}
	}


	public void showEvent() {
		
		for (int i = 0; i < stateMachine.getEvents().size(); i++) {
			Event event = stateMachine.getEvents().get(i);
			int src = event.getHead();
			int dst = event.getTail();
			LOGGER.fine(src + "-" + event.getEvent() + "-" + dst);
			// LOGGER.fine(event.getEvent().split(".")[0]);

		}
	}

	
	/**
	 * set future and past for every state
	 */
	public void ExtractMetaData() {
	
		int states_count = stateMachine.getStatesCount();

		List[] state = new LinkedList[states_count];
		for (int i = 0; i < states_count; i++)
			state[i] = new LinkedList();

		List s = stateMachine.getStates();

		for (int i = 0; i < states_count; i++) {
			LOGGER.fine(i + ":" + s.get(i));

		}

		List<Event> e = stateMachine.getEvents();

		past = new LinkedList[states_count];
		for (int i = 0; i < states_count; i++)
			past[i] = new LinkedList();
		future = new LinkedList[states_count];
		for (int i = 0; i < states_count; i++)
			future[i] = new LinkedList();

		boolean changed = true;
		while (changed) {
			changed = false;
			for (int i = 0; i < e.size(); i++) {
				Event event = (Event) e.get(i);
				int head = event.getHead();
				int tail = event.getTail();
				String port = event.getPort();

				int headindex = s.indexOf(head);
				int tailindex = s.indexOf(tail);
				if (port != null && !past[tailindex].contains(port)) {
					past[tailindex].add(port);
					changed = true;
				}
				if (head != stateMachine.getStart() && headindex != -1)
					for (int j = 0; j < past[headindex].size(); j++) {
						if (!past[tailindex].contains(past[headindex].get(j))) {
							past[tailindex].add(past[headindex].get(j));
							changed = true;
						}
					}
			}
		}
		
		changed = true;
		while (changed) {
			changed = false;
			for (int i = e.size() - 1; i >= 0; i--) {
				Event event = (Event) e.get(i);
				int head = event.getHead();
				int tail = event.getTail();
				String port = event.getPort();
				LOGGER.fine(port);
				int headindex = s.indexOf(head);
				int tailindex = s.indexOf(tail);
				if (port != null && !future[headindex].contains(port)) {
					future[headindex].add(port);
					changed = true;
				}
				if (tail != stateMachine.getEnd())
					for (int j = 0; j < future[tailindex].size(); j++) {
						if (!future[headindex].contains(future[tailindex]
								.get(j))) {
							future[headindex].add(future[tailindex].get(j));
							changed = true;
						}

					}
			}
		}
		

	}
	public void setStates(){
		
		int states_count = stateMachine.getStatesCount();
		for(int i=0; i<states_count; i++){
			State state = new State(stateMachine.getStates().get(i));
/*			for(int j=0; j<future[i].size(); j++){
				state.addFuture(future[i].get(j));
			}
			for(int j=0; j<past[i].size(); j++){
				state.addPast(past[i].get(j));
			}*/
			state.setFuture(future[i]);
			state.setPast(past[i]);
			states.add(state);
			LOGGER.fine(i+":"+future[i]+";"+past[i]);
		}
	}
	
	/**
	 * whether two states are equal:the elements are equal ignore the order
	 * @param si
	 * @param sj
	 * @return
	 */
	public boolean equalState(State si, State sj){
		
		List<String> futurei = si.future;
		List<String> futurej = sj.future;
		List<String> pasti = si.past;
		List<String> pastj = sj.past;
		int ifl = futurei.size();
		int jfl = futurej.size();
		int ipl = pasti.size();
		int jpl = pastj.size();
		
		if(ifl==jfl && ipl==jpl)
		{
			for(int k=0;k<ifl;k++){
				if(!(futurej.contains(futurei.get(k))))
				{
					return false;
				}
			}
			for(int k=0;k<ipl;k++){
				if(!(pastj.contains(pasti.get(k))))
				{
					return false;
				}
			}
			return true;
			
		}
		return false;
	}
	public State getState(int i){
		if (states != null) {
			Iterator<State> s = states.iterator();
			while (s.hasNext()) {
				State state = s.next();
				if (state.getLoc()==i) {
					return state;
				}
			}
			return null;
		}
		return null;
		
	}
	/**
	 *Test units
	 * @param cn
	 * @param mn
	 */
	public void test(ClassNode cn,MethodNode mn){
		if (isTransaction(mn, "Ljavax/aejb/Transaction;")) {
			setEjbs(cn);
						System.out.println("begin analyze method"+mn.name);
						InsnList insns = mn.instructions;
						stateMachine.setStart(0);
						stateMachine.setEnd(mn.instructions.size());
						try {
							ExtractControlFlow(cn.name, mn);
						} catch (AnalyzerException ignored) {
						}
						recognize_state2(0, 0, mn.instructions);
						mergeState();
						ExtractMetaData();
						setStates();
//						minimizeState();
						}
		
	}
	/**
	 * merge the same state 
	 */
	
	public void minimizeState(){
		List<Event> e = stateMachine.getEvents();
		//List<Integer> state = stateMachine.getStates();
		int iter=0;		
		while(iter < e.size()){
			Event event = e.get(iter);					
			int headindex = event.getHead();
			int tailindex = event.getTail();
			if(headindex == tailindex){
				stateMachine.getEvents().remove(event);
			}
			else{
			State head = getState(headindex);
			State tail = getState(tailindex);	
			if(equalState(head,tail)){
				System.out.println(headindex+"=="+tailindex+"true");
				stateMachine.getEvents().remove(event);
				stateMachine.mergeStates(headindex, tailindex);				
				states.remove(head);
				String eventinf = event.getEvent();
				if(ejbinf.containsValue(eventinf)){
					Enumeration<AbstractInsnNode> eachkey=ejbinf.keys();
					while(eachkey.hasMoreElements()){
						AbstractInsnNode key = eachkey.nextElement();
						if(ejbinf.get(key).equals(eventinf)){
							ejbinf.remove(key);							
						}
					}
				}
				else
				{
					if(jumpinf.containsValue(event.getEvent())){
						Enumeration<AbstractInsnNode> eachkey=jumpinf.keys();
						while(eachkey.hasMoreElements()){
							AbstractInsnNode key = eachkey.nextElement();
							if(jumpinf.get(key).equals(eventinf)){
								jumpinf.remove(key);							
							}
						}
					}
				}
			 }
			else{
				iter++;
			}
			}
			
		}
		
	}

	/**
	 * set the state information in  transaction annotation
	 * no use
	 */
	public void setStateAll(){
		for (int i = 0; i < states.size(); i++) {
			State s = states.get(i);
			List<String> fu = s.getFuture();
			List<String> pa = s.getPast();
			int fulen = fu.size();
			int palen = pa.size();
			int k = 0, p = 0;
			String sall = "";
			if (fulen > 0) {
				for (k = 0; k < fulen - 1; k++) {
					sall = sall + fu.get(k) + ",";
				}
				sall = sall + fu.get(k);
			}
			/*sall = sall + ";";
			if (palen > 0) {
				for (p = 0; p < palen - 1; p++) {
					sall = sall + pa.get(p) + ",";
				}
				sall = sall + pa.get(p);
			}*/
//			System.out.println(i + ":" + sall);
			stateall.add(sall);
		}
	}
	
	/**
	 * set the next state and the trigger event for every state
	 */
	public void setNext() {
	
		List<Event> event = stateMachine.getEvents();
		//List state = stateMachine.getStates();
		for (int i = 0; i < states.size(); i++) {
			String nexts = "";
			int sn = states.get(i).loc;
			int j;
			for (j = 0; j < event.size(); j++) {
				if (sn == event.get(j).getHead()) {
					State tail = getState(event.get(j).getTail());
					nexts = nexts + event.get(j).getEvent() + "-"
							+ states.indexOf(tail) + ",";
				}
			}
			if (nexts.length() == 0) {				
				next.add(nexts);
			}
			else
				next.add((String)nexts.subSequence(0, nexts.length()-1));			
			
			LOGGER.fine(i + "next:" + nexts);
			
		}
	}

	
	

}
