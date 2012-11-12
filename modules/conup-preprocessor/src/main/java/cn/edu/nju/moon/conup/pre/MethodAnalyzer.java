package cn.edu.nju.moon.conup.pre;

import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;

/**
 * 
 * 
 * @author Ping Su
 */
public class MethodAnalyzer {
	private final static Logger LOGGER = Logger.getLogger(MethodAnalyzer.class
			.getName());

	public static Logger getLogger() {
		return LOGGER;
	}

	/**
	 * DDA infor including states and events, like: si-ei-sj
	 */
	private StateMachine stateMachine = new StateMachine();

	/**
	 * CFG of the method
	 */
	private ControlFlow controlflow = new ControlFlow();
	
	/**
	 * Branch event infor like this:BranchType(if, or while).branchNum.location
	 */
	private Hashtable<AbstractInsnNode, List<String>> jumpinf = new Hashtable<AbstractInsnNode, List<String>>();
	/**
	 * Component-invoked event infor like this:
	 * COM.ComponentName;methodName.location
	 */
	private Hashtable<AbstractInsnNode, String> cominf = new Hashtable<AbstractInsnNode, String>();
	/**
	 * Use for EJB application
	 */
	@Deprecated
	private Hashtable<String, String> ejball = new Hashtable<String, String>();
	/**
	 * Invoke component service firstly
	 */
	private List<AbstractInsnNode> runinf = new LinkedList<AbstractInsnNode>();
	private int runNum = 0;
	private boolean isBranch = false;
	/**
	 * TxLifecycleManager name for creating transaction id.
	 */
	String TxlcMgr = "";
	/**
	 * TxLifecycleManager desc for creating transaction id
	 */
	String TxlcMgrDesc = "";
	/**
	 * All components will use potentially
	 */
	private List<String> com = new LinkedList<String>();
	/**
	 * All State objects in DDA
	 */
	private List<State> states = new LinkedList<State>();
	/**
	 * Past compoments of every state
	 */
	private List<String>[] past;
	/**
	 * Future compoments of every state
	 */
	private List<String>[] future;
	
	/**
	 * Parameter for transfer DDA info, including all states and its nexts
	 */
	private String statesDDA = "";
	private String nextsDDA = "";
	/**
	 * event-nextState in current state
	 */
	private List<String> next = new LinkedList<String>();
	/**
	 * All states info
	 */
	private List<String> stateall = new LinkedList<String>();
	/**
	 * whether the bytecode is analyzed;
	 */
	private int[] isAnalyze;

	public MethodAnalyzer(List<String> serviceList,String tlm,String tlmDesc) {
		com = serviceList;
		TxlcMgr = tlm;
		TxlcMgrDesc = tlmDesc;
	}

	public MethodAnalyzer() {

	}

	/**
	 * 
	 * @param n
	 *     the number of bytecodes in the program to be analyzed
	 */
	private void initIsAnalyze(int n) {
		isAnalyze = new int[n];
		for (int i = 0; i < n; i++) {
			isAnalyze[i] = 0;
		}
	}

	/**
	 * Set the components or services the method may use.
	 * 
	 * @param c
	 */
	public void setCom(List<String> c) {
		com = c;
	}

	private String getServiceName(String desc) {
		String[] fields = desc.split(File.separator);
		String serviceName = fields[fields.length - 1];
//		System.out.println(desc + " Servic Name is " + serviceName);
		return serviceName;
	}

	public void writeState(MethodNode mn) {
		statesDDA = "";
		nextsDDA = "";
		for (String s : stateall) {
			if (s.isEmpty()) {
				s = "_E";
			}
			statesDDA = statesDDA + s + ";";
		}
		statesDDA = statesDDA.subSequence(0, statesDDA.length() - 1).toString();
		// System.out.println("---------state size:"+stateall.size()+"!statesDDA"+statesDDA);
		for (String s : next) {
			if (s.isEmpty()) {
				s = "_E";
			}
			nextsDDA = nextsDDA + s + ";";
		}
		nextsDDA = nextsDDA.subSequence(0, nextsDDA.length() - 1).toString();
		LOGGER.info("statesDDA = " + statesDDA + ", nextsDDA = " + nextsDDA);

	}

	/**
	 * insert the trigger information: use notify
	 * 
	 * @param cn
	 * @param mn
	 * @param conupTx
	 *            the transaction or method name to be analyze
	 */
	public void methodTransform(ClassNode cn, MethodNode mn, String conupTx) {
		if (isTransaction(mn, conupTx)) {
			LOGGER.info("Begin analyze method:" + mn.name);
			InsnList insns = mn.instructions;
			stateMachine.setStart(0);
			stateMachine.setEnd(mn.instructions.size());
			try {
				ExtractControlFlow(cn.name, mn);
			} catch (AnalyzerException ignored) {
			}
			initIsAnalyze(mn.instructions.size());
			recognizeState(0, 0, mn.instructions);
			mergeState();
			ExtractMetaData();
			setStates();		
			setStateAll();
			setNext();
			writeState(mn);
			int localNum = mn.localVariables.size();
			// insert annotation
//			Iterator<AnnotationNode> iter = mn.visibleAnnotations.iterator();		
			// insert transaction id
			InsnList trigstart = new InsnList();			
			trigstart.add(new VarInsnNode(ALOAD, 0));
			trigstart.add(new FieldInsnNode(GETFIELD, cn.name, TxlcMgr, TxlcMgrDesc));
			trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, TxlcMgrDesc.substring(1,TxlcMgrDesc.length()-1), "createID", "()Ljava/lang/String;"));
//			System.out.println("TxlcMgrDesc.substring(1,TxlcMgrDesc.length()-1) = "+TxlcMgrDesc.substring(1,TxlcMgrDesc.length()-1));
			trigstart.add(new VarInsnNode(ASTORE, localNum));
//insert start inf
			trigstart.add(new VarInsnNode(ALOAD, localNum));
			trigstart.add(new LdcInsnNode(statesDDA));
			// System.out.println("----------------------StatesDDA"+statesDDA);
			trigstart.add(new LdcInsnNode(nextsDDA));
			trigstart
					.add(new MethodInsnNode(
							INVOKESTATIC,
							"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
							"getInstance",
							"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager;"));
			trigstart.add(new LdcInsnNode("Start"));
			trigstart.add(new MethodInsnNode(INVOKEVIRTUAL,
					"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
					"trigger", "(Ljava/lang/String;)V"));
			insns.insert(insns.getFirst(), trigstart);
			
			Iterator<AbstractInsnNode> i = insns.iterator();
			while (i.hasNext()) {
				AbstractInsnNode i1 = i.next();
				// System.out.println(((AbstractInsnNode)i1).toString());
				if (i1 instanceof MethodInsnNode) {
					// COM
					if (runNum > 0) {
						if (runinf.contains(i1)) {
							InsnList setUp = new InsnList();
							setUp.add(new VarInsnNode(ALOAD, localNum));
							setUp.add(new LdcInsnNode(statesDDA));
							setUp.add(new LdcInsnNode(nextsDDA));
							setUp.add(new MethodInsnNode(
									INVOKESTATIC,
									"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
									"getInstance",
									"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager;"));
							setUp.add(new MethodInsnNode(
									INVOKEVIRTUAL,
									"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
									"FirstRequestService", "()V"));
							AbstractInsnNode ilPre = i1.getPrevious();
							int op = ilPre.getOpcode();
							while (op == ALOAD || op == LLOAD || op == FLOAD
									|| op == DLOAD || op == ILOAD) {
								ilPre = ilPre.getPrevious();
								op = ilPre.getOpcode();
							}
							insns.insert(ilPre, setUp);
						}
					}

					if (cominf.containsKey(i1)) {
						InsnList trig = new InsnList();
						trig.add(new VarInsnNode(ALOAD, localNum));
						trig.add(new LdcInsnNode(statesDDA));
						trig.add(new LdcInsnNode(nextsDDA));
						trig.add(new MethodInsnNode(
								INVOKESTATIC,
								"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
								"getInstance",
								"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager;"));
	
						trig.add(new LdcInsnNode(cominf.get(i1)));
						trig.add(new MethodInsnNode(
								INVOKEVIRTUAL,
								"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
								"trigger", "(Ljava/lang/String;)V"));

						int op = i1.getNext().getOpcode();
						if (op == ISTORE || op == LSTORE || op == FSTORE
								|| op == DSTORE || op == ASTORE) {
							insns.insert(i1.getNext(), trig);
						} else {
							insns.insert(i1, trig);
						}
					}

				} else {
					if (jumpinf.containsKey(i1)) {
						List<String> jumpEventInf = jumpinf.get(i1);
						for (String jumpE : jumpEventInf) {
							InsnList trig = new InsnList();
							trig.add(new VarInsnNode(ALOAD, localNum));
							trig.add(new LdcInsnNode(statesDDA));
							trig.add(new LdcInsnNode(nextsDDA));
							trig.add(new MethodInsnNode(
									INVOKESTATIC,
									"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
									"getInstance",
									"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager;"));
			
							trig.add(new LdcInsnNode(jumpE));
							trig.add(new MethodInsnNode(
									INVOKEVIRTUAL,
									"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
									"trigger", "(Ljava/lang/String;)V"));

							if (i1 instanceof LabelNode) {
								if (i1.getNext() instanceof LineNumberNode)
									insns.insert(i1.getNext(), trig);
								else
									insns.insert(i1, trig);
							} else
								insns.insert(i1.getPrevious(), trig);
						}
					} else {
						if ((i1.getOpcode() >= IRETURN && i1.getOpcode() <= RETURN)
								|| i1.getOpcode() == ATHROW) {
							InsnList trig = new InsnList();
							trig.add(new VarInsnNode(ALOAD, localNum));
							trig.add(new LdcInsnNode(statesDDA));
							trig.add(new LdcInsnNode(nextsDDA));
							trig.add(new MethodInsnNode(
									INVOKESTATIC,
									"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
									"getInstance",
									"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager;"));
							trig.add(new LdcInsnNode(""));
							trig.add(new MethodInsnNode(
									INVOKEVIRTUAL,
									"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
									"trigger", "(Ljava/lang/String;)V"));
							insns.insert(i1.getPrevious(), trig);
						}
					}

				}

			}
			LabelNode last = ((LocalVariableNode) mn.localVariables.get(0)).end;
			LabelNode start = ((LocalVariableNode) mn.localVariables.get(0)).start;			
			mn.localVariables.add(new LocalVariableNode("transactionID",
					"Ljava/lang/String;", null, start, last, localNum));
			mn.maxLocals = mn.maxLocals + 1;

		}

	}

	/**
	 * Whether the method has the specified annotation
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
	 * 
	 * @param cn
	 */
	@Deprecated
	public void setEjbs(ClassNode cn) {

		for (FieldNode fn : (List<FieldNode>) cn.fields) {
			if (fn.visibleAnnotations != null) {
				Iterator<AnnotationNode> fi = fn.visibleAnnotations.iterator();
				while (fi.hasNext()) {
					AnnotationNode fa = fi.next();
					// System.out.println(fa.values);
					if (fa.desc.contains("AEjb") || fa.desc.contains("Ejb")) {
						// String ejb =
						// fa.values.get(1).toString().split("/")[0];
						String ejb = fn.desc.substring(1, fn.desc.length() - 1);
						;
						// System.out.println(ejb+"-"+fn.name);
						ejball.put(ejb, (String) fn.name);
						// System.out.println(ejb+"-"+fn.value);
					}
				}
			}
		}
	}

	/**
	 * Extract CFG of the method
	 * 
	 * @param owner
	 * @param mn
	 * @throws AnalyzerException
	 */
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
	 * 
	 * @param service
	 * @return component/service
	 */
	@Deprecated
	public String isCom(String service) {
		for (String s : com) {
			if (s.split("/")[1].equals(service)) {
				return s;
			}
		}
		return null;
	}

	/**
	 * recognize all the state of the method for tuscany application
	 * 
	 * @param src
	 * @param last_state
	 * @param insns
	 */
	public void recognizeState(int src, int last_state, InsnList insns) {

		if (src == stateMachine.getStart()) {
			stateMachine.addState(src);
			last_state = src;
		}
		if (stateMachine.getStates().contains(src) && (src != last_state)) {

		}

		if (isAnalyze[src] == 0) {
			AbstractInsnNode an = insns.get(src);
			if (an instanceof MethodInsnNode) {
				isAnalyze[src] = 1;
				int next = (Integer) controlflow.getFlow(src).getDst().get(0);
				MethodInsnNode method = ((MethodInsnNode) an);
//				System.out.println(an.toString()+"'owner is "+method.owner);
				if (com.contains(method.owner)) {
					String serName = getServiceName(method.owner);
					if (runNum == 0 || (runNum == 1 && isBranch)) {
						runinf.add(an);
						runNum = runNum + 1;
					}
					String e = "COM." + serName + "." + src;
					cominf.put(insns.get(src), e);
					if (!stateMachine.getStates().contains(src)) {
						stateMachine.addState(src);
					}
					if (src != last_state) {
						stateMachine.addEvent(new Event(last_state, src, ""));
					}
					if (!stateMachine.getStates().contains(next)) {
						stateMachine.addState(next);
					}
					stateMachine.addEvent(new Event(src, next, e));

					recognizeState(next, next, insns);
				} else {
					recognizeState(next, last_state, insns);
				}
			} else {
				if (an instanceof JumpInsnNode) {
					int dst = -1;
					isAnalyze[src] = 1;
					if (an.getOpcode() == GOTO) {
						if (!stateMachine.getStates().contains(src)) {
							stateMachine.addState(src);
						}
						if (src != last_state) {
							stateMachine
									.addEvent(new Event(last_state, src, ""));
						}
						dst = (Integer) controlflow.getFlow(src).getDst()
								.get(0);
						if (!stateMachine.getStates().contains(dst)) {
							stateMachine.addState(dst);
						}
						stateMachine.addEvent(new Event(src, dst, ""));
						recognizeState(dst, dst, insns);
					} else {
						if (!stateMachine.getStates().contains(src)) {
							stateMachine.addState(src);
						}
						if (src != last_state) {
							stateMachine
									.addEvent(new Event(last_state, src, ""));
						}
						if (controlflow.getFlow(src).getIsWhile()) {
							if (runNum == 0) {
								isBranch = true;
							}

							for (int k = 0; k < controlflow.getDstSize(src); k++) {
								dst = (Integer) controlflow.getDst(src).get(k);
								AbstractInsnNode dstNode = insns.get(dst);
								if (!stateMachine.getStates().contains(dst)) {
									stateMachine.addState(dst);
								}
								if (src < dst) {
									stateMachine.addEvent(new Event(src, dst,
											"while.F." + src));
									if (jumpinf.containsKey(dstNode)) {
										jumpinf.get(dstNode).add(
												"while.F." + src);
									} else {
										List<String> branchEvent = new LinkedList<String>();
										branchEvent.add("while.F." + src);
										jumpinf.put(dstNode, branchEvent);
									}
									recognizeState(dst, dst, insns);
								} else {
									stateMachine.addEvent(new Event(src, dst,
											"while.T." + src));
									if (jumpinf.containsKey(dstNode)) {
										jumpinf.get(dstNode).add(
												"while.T." + src);
									} else {
										List<String> branchEvent = new LinkedList<String>();
										branchEvent.add("while.T." + src);
										jumpinf.put(dstNode, branchEvent);
									}
									// jumpinf.put(insns.get(dst),"while.T."+src);
									recognizeState(dst, dst, insns);
								}
							}

						} else {
							if (runNum == 0) {
								isBranch = true;
							}
							for (int k = 0; k < controlflow.getDstSize(src); k++) {
								dst = (Integer) controlflow.getDst(src).get(k);
								AbstractInsnNode dstNode = insns.get(dst);
								if (!stateMachine.getStates().contains(dst)) {
									stateMachine.addState(dst);
								}
								stateMachine.addEvent(new Event(src, dst, "if."
										+ k + "." + src));
								if (jumpinf.containsKey(dstNode)) {
									jumpinf.get(dstNode).add(
											"if." + k + "." + src);
								} else {
									List<String> branchEvent = new LinkedList<String>();
									branchEvent.add("if." + k + "." + src);
									jumpinf.put(dstNode, branchEvent);
								}
								// jumpinf.put(insns.get(dst), "if." + k+"."+
								// src);
								recognizeState(dst, dst, insns);
							}
						}
					}
				} else {
					if ((an.getOpcode() >= IRETURN && an.getOpcode() <= RETURN)
							|| an.getOpcode() == ATHROW) {
						stateMachine.addState(src);
						isAnalyze[src] = 1;
						stateMachine
								.addEvent(new Event(last_state, src, "end"));
					} else {
						isAnalyze[src] = 1;
						recognizeState((Integer) controlflow.getFlow(src)
								.getDst().get(0), last_state, insns);
					}
				}
			}
		}

	}

	/**
	 * merge the state event is empty or the end
	 */
	public void mergeState() {
		// stateMachine.getStates().add(0, stateMachine.getStart());
		boolean change = true;
		while (change) {
			change = false;
			for (int i = 0; i < stateMachine.getEvents().size(); i++) {
				Event event = stateMachine.getEvents().get(i);
				if (event.getEvent().isEmpty()
						|| event.getEvent().contains("end")) {
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
			LOGGER.info(src + "-" + event.getEvent() + "-" + dst);
			// LOGGER.info(event.getEvent().split(".")[0]);

		}
	}

	/**
	 * get future and past components in every state of DDA
	 */
	public void ExtractMetaData() {

		int states_count = stateMachine.getStatesCount();

		List[] state = new LinkedList[states_count];
		for (int i = 0; i < states_count; i++)
			state[i] = new LinkedList();

		List<Integer> s = stateMachine.getStates();

		for (int i = 0; i < states_count; i++) {
			LOGGER.info(i + ":" + s.get(i));

		}

		List<Event> e = stateMachine.getEvents();

		past = new LinkedList[states_count];
		for (int i = 0; i < states_count; i++)
			past[i] = new LinkedList<String>();
		future = new LinkedList[states_count];
		for (int i = 0; i < states_count; i++)
			future[i] = new LinkedList<String>();

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
/**
 * put the states content in every state
 */
	public void setStates() {
		int states_count = stateMachine.getStatesCount();
		for (int i = 0; i < states_count; i++) {
			State state = new State(stateMachine.getStates().get(i));
			/*
			 * for(int j=0; j<future[i].size(); j++){
			 * state.addFuture(future[i].get(j)); } for(int j=0;
			 * j<past[i].size(); j++){ state.addPast(past[i].get(j)); }
			 */
			state.setFuture(future[i]);
			state.setPast(past[i]);
			states.add(state);
			LOGGER.info(i + ":" + future[i] + ";" + past[i]);
		}
	}

	/**
	 * whether two states are equal:the elements are equal ignore the order
	 * 
	 * @param si
	 * @param sj
	 * @return
	 */
	@Deprecated
	public boolean equalState(State si, State sj) {

		List<String> futurei = si.future;
		List<String> futurej = sj.future;
		List<String> pasti = si.past;
		List<String> pastj = sj.past;
		int ifl = futurei.size();
		int jfl = futurej.size();
		int ipl = pasti.size();
		int jpl = pastj.size();

		if (ifl == jfl && ipl == jpl) {
			for (int k = 0; k < ifl; k++) {
				if (!(futurej.contains(futurei.get(k)))) {
					return false;
				}
			}
			for (int k = 0; k < ipl; k++) {
				if (!(pastj.contains(pasti.get(k)))) {
					return false;
				}
			}
			return true;

		}
		return false;
	}

	public State getState(int i) {
		if (states != null) {
			Iterator<State> s = states.iterator();
			while (s.hasNext()) {
				State state = s.next();
				if (state.getLoc() == i) {
					return state;
				}
			}
			return null;
		}
		return null;

	}

	/**
	 * merge the same state
	 */
	@Deprecated
	public void minimizeState() {
		List<Event> e = stateMachine.getEvents();
		// List<Integer> state = stateMachine.getStates();
		int iter = 0;
		while (iter < e.size()) {
			Event event = e.get(iter);
			int headindex = event.getHead();
			int tailindex = event.getTail();
			if (headindex == tailindex) {
				stateMachine.getEvents().remove(event);
			} else {
				State head = getState(headindex);
				State tail = getState(tailindex);
				if (equalState(head, tail)) {
					System.out.println(headindex + "==" + tailindex + "true");
					stateMachine.getEvents().remove(event);
					stateMachine.mergeStates(headindex, tailindex);
					states.remove(head);
					String eventinf = event.getEvent();
					if (cominf.containsValue(eventinf)) {
						Enumeration<AbstractInsnNode> eachkey = cominf.keys();
						while (eachkey.hasMoreElements()) {
							AbstractInsnNode key = eachkey.nextElement();
							if (cominf.get(key).equals(eventinf)) {
								cominf.remove(key);
							}
						}
					} else {
						if (jumpinf.containsValue(event.getEvent())) {
							Enumeration<AbstractInsnNode> eachkey = jumpinf
									.keys();
							while (eachkey.hasMoreElements()) {
								AbstractInsnNode key = eachkey.nextElement();
								if (jumpinf.get(key).equals(eventinf)) {
									jumpinf.remove(key);
								}
							}
						}
					}
				} else {
					iter++;
				}
			}

		}

	}

	/**
	 * set the state information in transaction annotation or parameter to use
	 */
	public void setStateAll() {
		for (int i = 0; i < states.size(); i++) {
			State s = states.get(i);
			List<String> fu = s.getFuture();
			int fulen = fu.size();
			int k = 0;
			String sall = "";
			if (fulen > 0) {
				for (k = 0; k < fulen - 1; k++) {
					sall = sall + fu.get(k) + ",";
				}
				sall = sall + fu.get(k);
			}
			/*
			 * List<String> pa = s.getPast(); int palen = pa.size(); int p = 0;
			 * sall = sall + ";"; if (palen > 0) { for (p = 0; p < palen - 1;
			 * p++) { sall = sall + pa.get(p) + ","; } sall = sall + pa.get(p);
			 * }
			 */
			// System.out.println(i + ":" + sall);
			stateall.add(sall);
		}
	}

	/**
	 * set the trigger event and the next state for every state
	 */
	public void setNext() {
		List<Event> event = stateMachine.getEvents();
		// List state = stateMachine.getStates();
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
			} else
				next.add((String) nexts.subSequence(0, nexts.length() - 1));
			LOGGER.info(i + "next:" + nexts);
		}
	}

}
