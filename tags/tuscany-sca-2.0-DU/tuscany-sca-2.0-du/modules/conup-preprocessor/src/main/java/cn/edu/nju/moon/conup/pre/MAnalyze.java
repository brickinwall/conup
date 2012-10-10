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
public class MAnalyze {

	/**
	 *瀛樻斁姣忎釜鎺у埗娴佺▼鐨勫叿浣撲俊鎭紝鍗硈i-ai-sj
	 */
	StateMachine stateMachine = new StateMachine();
	
	/**
	 * 瀛樻斁鐘舵�鏈轰俊鎭紝涓昏鏄垎鏋愮▼搴忕殑鎺у埗娴�
	 */
	ControlFlow controlflow = new ControlFlow();
	
	/**
	 * 姣忎釜鐘舵�鐐瑰凡浣跨敤鐨勫澶栬皟鐢�
	 */
	List<String>[] past;
	
	/**
	 * 姣忎釜鐘舵�鐐瑰皢瑕佺殑瀵瑰璋冪敤
	 */
	List<String>[] future;
	
	/**
	 * 瀵�?簬宸插垎鏋愬嚭鐨勮烦杞強鍦ㄦ敼鐐硅鍔犲叆鐨勮烦杞俊鎭�
	 */
	Hashtable<AbstractInsnNode, String> jumpinf = new Hashtable<AbstractInsnNode, String>();
	Hashtable<AbstractInsnNode, String> ejbinf = new Hashtable<AbstractInsnNode, String>();
	Hashtable<String, String> ejball= new Hashtable<String, String>();
	List<State> states = new LinkedList<State>(); 
	List<String> com = new LinkedList<String>();
	
	/**
	 * 鍒嗘瀽鍑哄搴旀瘡涓姸鎬佺殑鎵�互鍙兘鐨勪笅姝ュ姩浣滃強鎵�埌杈剧殑涓嬩釜鐘舵�
	 */
	List<String> next = new LinkedList<String>();
	
	/**
	 * 鍒嗘瀽鍑虹殑鎵�互鐘舵�淇℃�?
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
		String statew="";
		String nextw="";
		for(String s : stateall){
			statew = statew + s+";";
		}
		statew.subSequence(0, statew.length()-1);
		for(String s : next){
			nextw = nextw + s+";";
		}
		nextw.subSequence(0, nextw.length()-1);
		try{
		  File dir = new File("e:\\ttp");
		  dir.mkdir();		 
		  FileWriter fw = new FileWriter("e:\\ttp\\"+mn.name+".txt");
		  BufferedWriter bw = new BufferedWriter(fw);		 
		  bw.write(statew);
		  bw.newLine();
		  bw.write(nextw);
		  bw.flush(); //将数据更新至文件
		  bw.close();
		  fw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * 瀛楄妭鐮佷腑鎻掑叆transaction銆佽Е鍙憈rigger绛変俊鎭�?
	 * @param cn
	 * @param mn
	 */
	public void methodTransform(ClassNode cn,MethodNode mn) {
		
		if (mn.name.equals("Tranquillity")) {
			setEjbs(cn);
			System.out.println("begin analyze method:"+mn.name);
			InsnList insns = mn.instructions;
			stateMachine.setStart(0);
			stateMachine.setEnd(mn.instructions.size());
			try {
				ExtractControlFlow(cn.name, mn);
			} catch (AnalyzerException ignored) {
			}
			initIsAnalyze(mn.instructions.size());
			recognize_state(0, 0, mn.instructions);
			mergeState();
			ExtractMetaData();
			setStates();
			//minimizeState();
			setStateAll();						
			setNext();
			writeState(mn);

						
						InsnList trigstart = new InsnList();
						trigstart.add(new MethodInsnNode(INVOKESTATIC, "launch/ComponentListenerImpl", "getInstance", "()Llaunch/ComponentListenerImpl;"));									
						trigstart.add(new VarInsnNode(ALOAD, 0));
						trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;"));
						trigstart.add(new LdcInsnNode("Start"));
						trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, "launch/ComponentListenerImpl", "notify", "(Ljava/lang/String;Ljava/lang/String;)Z"));
						trigstart.add(new InsnNode(POP));
						insns.insert(insns.getFirst(),trigstart);
					    Iterator<AbstractInsnNode> i = insns.iterator();
						int src = 0;
						while (i.hasNext()) {
							AbstractInsnNode i1 = i.next();
//							System.out.println(((AbstractInsnNode)i1).toString());					
							if (i1 instanceof MethodInsnNode) {	
								//EJB
								if (ejbinf.containsKey(i1)) {
									InsnList trig = new InsnList();
							        trig.add(new MethodInsnNode(INVOKESTATIC, "launch/ComponentListenerImpl", "getInstance", "()Llaunch/ComponentListenerImpl;"));									
			//						trig.add(new VarInsnNode(ALOAD, 0));
			//						trig.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;"));
							        trig.add(new LdcInsnNode(mn.name));
									trig.add(new LdcInsnNode(ejbinf.get(i1)));
									trig.add(new MethodInsnNode(INVOKEVIRTUAL, "launch/ComponentListenerImpl", "notify", "(Ljava/lang/String;Ljava/lang/String;)Z"));
									trig.add(new InsnNode(POP));
									int op=i1.getNext().getOpcode();
									if (op == ISTORE||op == LSTORE||op == FSTORE||op == DSTORE||op == ASTORE) {
										insns.insert(i1.getNext(), trig);
									} else {
										insns.insert(i1, trig);
									}
				}

							} else {
								if (jumpinf.containsKey(i1)) {
									InsnList trig = new InsnList();
							        trig.add(new MethodInsnNode(INVOKESTATIC, "launch/ComponentListenerImpl", "getInstance", "()Llaunch/ComponentListenerImpl;"));									
						//			trig.add(new VarInsnNode(ALOAD, 0));
						//			trig.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;"));
							        trig.add(new LdcInsnNode(mn.name));
									trig.add(new LdcInsnNode(jumpinf.get(i1)));
									trig.add(new MethodInsnNode(INVOKEVIRTUAL, "launch/ComponentListenerImpl", "notify", "(Ljava/lang/String;Ljava/lang/String;)Z"));
									trig.add(new InsnNode(POP));
									if(i1 instanceof LabelNode){
										if(i1.getNext() instanceof LineNumberNode)
											insns.insert(i1.getNext(), trig);
										else
											insns.insert(i1, trig);
									}										
									else
										insns.insert(i1.getPrevious(), trig);
								} else {
									if ((i1.getOpcode() >= IRETURN && i1.getOpcode() <= RETURN)|| i1.getOpcode()==ATHROW) {
										InsnList trig = new InsnList();
								        trig.add(new MethodInsnNode(INVOKESTATIC, "launch/ComponentListenerImpl", "getInstance", "()Llaunch/ComponentListenerImpl;"));									
				//						trig.add(new VarInsnNode(ALOAD, 0));
				//						trig.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;"));
								        trig.add(new LdcInsnNode(mn.name));
										trig.add(new InsnNode(ACONST_NULL));
										trig.add(new MethodInsnNode(INVOKEVIRTUAL, "launch/ComponentListenerImpl", "notify", "(Ljava/lang/String;Ljava/lang/String;)Z"));
										insns.insert(i1.getPrevious(), trig);
									}
								}

							}

						}	
				}

	}

	
	/**
	 * 鍒ゆ柇璇ユ柟娉曟槸鍚︽槸闇�鍒嗘�?鐨勬柟娉�?
	 * @param mn
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
	public String isCom(String service){
		for(String s:com){
			if(s.split("/")[1].equals(service)){
				return s;
			}
		}
		return null;
	}

	/**
	 * recognize all the state of the method
	 * @param src
	 * @param last_state
	 * @param insns
	 */
	public void recognize_state2(int src, int last_state, InsnList insns) {
//		System.out.println(src);
//		if (src < stateMachine.getEnd()) {
			if(src == stateMachine.getStart()){
				stateMachine.addState(src);
				last_state = src;
//				recognize_state2((Integer) controlflow.getFlow(src).dst.get(0), last_state, insns);
			}
			if(stateMachine.getStates().contains(src) && (src != last_state)){				
					stateMachine.addEvent(new Event(last_state, src, ""));
				/*
				else
				{
					recognize_state2((Integer) controlflow.getFlow(src).dst.get(0), src, insns);
				}*/
				
			}
			
			if (isAnalyze[src]==0) {				
				AbstractInsnNode an = insns.get(src);
				if (an instanceof MethodInsnNode) {
					isAnalyze[src]=1;
					String owner = ((MethodInsnNode) an).owner.split("/")[1];
					int next =(Integer)controlflow.getFlow(src).dst.get(0);
					//System.out.println(owner);					
					if (isCom(owner)!= null) {
						//System.out.println(true);
						String e = "Ejb." + isCom(owner) + "."	+src;
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
								for (int k = 0; k < controlflow.getDstSize(src); k++) {
									dst = (Integer) controlflow.getDst(src).get(k);
									if(!stateMachine.getStates().contains(dst)){
										stateMachine.addState(dst);
									}
									if (src < dst) {										
										stateMachine.addEvent(new Event(src,dst, "while.F."+src));
										jumpinf.put(insns.get(dst), "while.F."+src);
//										isAnalyze[dst]=1;
										recognize_state2(dst, dst, insns);
									} else {										
										stateMachine.addEvent(new Event(src,dst, "while.T."+src));
										jumpinf.put(insns.get(dst),"while.T."+src);
//										isAnalyze[dst]=1;
										recognize_state2(dst, dst, insns);
									}
								}
							} else {
								for (int k = 0; k < controlflow.getDstSize(src); k++) {
									dst = (Integer) controlflow.getDst(src).get(k);
									if(!stateMachine.getStates().contains(dst)){
										stateMachine.addState(dst);
										}	
//									stateMachine.addState(dst);
									stateMachine.addEvent(new Event(src, dst,"if." + k+"."+ src));
									jumpinf.put(insns.get(dst), "if." + k+"."+ src);
//									isAnalyze[dst]=1;
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
	 * recognize all the state of the method
	 * @param src
	 * @param last_state
	 * @param insns
	 */
	public void recognize_state(int src, int last_state, InsnList insns) {
		//System.out.println(src);
//		if (src < stateMachine.getEnd()) {
		if(src == stateMachine.getStart()){
			stateMachine.addState(src);
			last_state = src;
//			recognize_state2((Integer) controlflow.getFlow(src).dst.get(0), last_state, insns);
		}
		if(stateMachine.getStates().contains(src) && (src != last_state)){				
				stateMachine.addEvent(new Event(last_state, src, ""));
			/*
			else
			{
				recognize_state2((Integer) controlflow.getFlow(src).dst.get(0), src, insns);
			}*/
			
		}
		
		if (isAnalyze[src]==0) {				
			AbstractInsnNode an = insns.get(src);
			if (an instanceof MethodInsnNode) {
				isAnalyze[src]=1;
				String owner = ((MethodInsnNode) an).owner;
				int next =(Integer)controlflow.getFlow(src).dst.get(0);
				//System.out.println(owner);					
				if (ejball.containsKey(owner)) {
					//System.out.println(true);
					String e = "Ejb." + ejball.get(owner) + "."	+src;
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
					
					recognize_state(next,next,insns);								
				}
				else
				{
					recognize_state(next, last_state, insns);
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
						recognize_state(dst, dst, insns);
					} 
					else {
						if(!stateMachine.getStates().contains(src)){
							stateMachine.addState(src);
							}
						if(src!=last_state){
							stateMachine.addEvent(new Event(last_state, src, ""));
						}
						if (controlflow.getFlow(src).isWhile) {
							for (int k = 0; k < controlflow.getDstSize(src); k++) {
								dst = (Integer) controlflow.getDst(src).get(k);
								if(!stateMachine.getStates().contains(dst)){
									stateMachine.addState(dst);
								}
								if (src < dst) {										
									stateMachine.addEvent(new Event(src,dst, "while.F."+src));
									jumpinf.put(insns.get(dst), "while.F."+src);
//									isAnalyze[dst]=1;
									recognize_state(dst, dst, insns);
								} else {										
									stateMachine.addEvent(new Event(src,dst, "while.T."+src));
									jumpinf.put(insns.get(dst),"while.T."+src);
//									isAnalyze[dst]=1;
									recognize_state(dst, dst, insns);
								}
							}
						} else {
							for (int k = 0; k < controlflow.getDstSize(src); k++) {
								dst = (Integer) controlflow.getDst(src).get(k);
								if(!stateMachine.getStates().contains(dst)){
									stateMachine.addState(dst);
									}	
//								stateMachine.addState(dst);
								stateMachine.addEvent(new Event(src, dst,"if." + k+"."+ src));
								jumpinf.put(insns.get(dst), "if." + k+"."+ src);
//								isAnalyze[dst]=1;
								recognize_state(dst, dst, insns);
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
					recognize_state((Integer) controlflow.getFlow(src).dst.get(0), last_state, insns);
					}
					}
			}
		}
	
 }
			
	// 鍚堝苟鐘舵�
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

	// 鏄剧ず鎵�湁鐘舵�鍙樺寲
	public void showEvent() {
		for (int i = 0; i < stateMachine.getEvents().size(); i++) {
			Event event = stateMachine.getEvents().get(i);
			int src = event.getHead();
			int dst = event.getTail();
			System.out.println(src + "-" + event.getEvent() + "-" + dst);
			// System.out.println(event.getEvent().split(".")[0]);

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
		// 杈撳嚭鎵�湁鐨勭姸鎬佷俊鎭�
		for (int i = 0; i < states_count; i++) {
			System.out.println(i + ":" + s.get(i));

		}
		// ///////////////////
		List<Event> e = stateMachine.getEvents();
		// 涓や釜鏁扮粍鍒嗗埆�?樻斁姣忎釜鐘舵�鐨刦uture鍜宲ast
		past = new LinkedList[states_count];
		for (int i = 0; i < states_count; i++)
			past[i] = new LinkedList();
		future = new LinkedList[states_count];
		for (int i = 0; i < states_count; i++)
			future[i] = new LinkedList();

		// 棣栧厛浠庡墠鍒板悗閬嶅巻浜嬩欢鍒楄�?锛屼负姣忎竴涓姸鎬佹坊鍔爌ast锛屾�璺槸锛氬浜�?-a1-s1杩欎竴浜嬩欢锛宻1鐨刾ast=a0+s0鐨刾ast
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
		// 鐒跺悗浠庡悗鍒板墠閬嶅巻浜嬩欢鍒楄�?锛屼负姣忎竴涓姸鎬佹坊鍔爁uture锛屾�璺被浼间箣鍓�?
		changed = true;
		while (changed) {
			changed = false;
			for (int i = e.size() - 1; i >= 0; i--) {
				Event event = (Event) e.get(i);
				int head = event.getHead();
				int tail = event.getTail();
				String port = event.getPort();
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
			for(int j=0; j<future[i].size(); j++){
				state.addFuture(future[i].get(j));
			}
			for(int j=0; j<past[i].size(); j++){
				state.addPast(past[i].get(j));
			}
			states.add(state);
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
			System.out.println(i + ":" + sall);
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
			
			System.out.println(i + "next:" + nexts);
			
		}
	}
	
	List<String> real_past=new LinkedList<String>();
	/**
	 * get the real past after the given trigger action dynamically
	 * @param action
	 */
	public State trigger(String action){
		Event e = stateMachine.getEvent(action);
		State s = getState(e.getTail());
		if(action.contains("Ejb")){
			String port = action.split("\\.")[1];		
			if(!real_past.contains(port))
			{
				real_past.add(port);
				s.setPast(real_past);				
			}
			
		}
		else
			s.setPast(real_past);
		return s;
	}
	
	

}
