package cn.edu.nju.moon.conup.txpre;

import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
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
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;

/**
 * 
 * 
 * @author Ping Su<njupsu@gmail.com>
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
	private Hashtable<AbstractInsnNode, List<String>> jumpdstinf = new Hashtable<AbstractInsnNode, List<String>>();
	private Hashtable<AbstractInsnNode, Hashtable<AbstractInsnNode, String>> jumpsrcinf = new Hashtable<AbstractInsnNode, Hashtable<AbstractInsnNode, String>>();
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
	 * Invoke component service firstly. Note: can not use Set, because
	 * methodInsNode don't implement comparable
	 */
	private List<AbstractInsnNode> runinf = new LinkedList<AbstractInsnNode>();
	/**
	 * the end info, including return and throw, etc
	 */
	private List<AbstractInsnNode> endinf = new LinkedList<AbstractInsnNode>();
	
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
	 * Parameter for transfer DDA info, including all states and its nexts
	 */
	private String statesDDA = "";
	private String nextsDDA = "";
	/**
	 * whether the bytecode is analyzed;
	 */
	private int[] isAnalyze;
	private Set<Integer> end = new ConcurrentSkipListSet<Integer>();

	public MethodAnalyzer(List<String> serviceList, String tlm, String tlmDesc) {
		com = serviceList;
		TxlcMgr = tlm;
		TxlcMgrDesc = tlmDesc;
	}

	public MethodAnalyzer() {
	}

	/**
	 * 
	 * @param n
	 *            the number of bytecodes in the program to be analyzed
	 */
	public void initIsAnalyze(int n) {
		isAnalyze = new int[n];
		for (int i = 0; i < n; i++) {
			isAnalyze[i] = 0;
		}
	}

	public int[] getIsAnalyze() {
		return isAnalyze;
	}

	public void printIsAnalyzed(int n) {
		for (int i = 0; i < n; i++) {
//			System.out.println(i + ": " + isAnalyze[i]);
		}
	}

	public StateMachine getStateMachine() {
		return stateMachine;
	}

	public String getStatesDDA() {
		return statesDDA;
	}

	public String getNextsDDA() {
		return nextsDDA;
	}

	public List<AbstractInsnNode> getRunInf() {
		return runinf;
	}

	/**
	 * Set the components or services the method may use.
	 * 
	 * @param c
	 */
	public void setCom(List<String> c) {
		com = c;
	}

	public String getServiceName(String desc) {
		String[] fields = desc.split(File.separator);
		String serviceName = fields[fields.length - 1];
		// System.out.println(desc + " Servic Name is " + serviceName);
		return serviceName;
	}

	public void writeDDA(MethodNode mn, List<String> stateall, List<String> next) {
		statesDDA = "";
		nextsDDA = "";
		for (String s : stateall) {
			if (s.isEmpty()) {
				s = "_E";
			}
			statesDDA = statesDDA + s + ";";
		}
		statesDDA = statesDDA.substring(0, statesDDA.length() - 1);
		for (String s : next) {
			if (s.isEmpty()) {
				s = "_E";
			}
			nextsDDA = nextsDDA + s + ";";
		}
		nextsDDA = nextsDDA.substring(0, nextsDDA.length() - 1);
		LOGGER.fine("statesDDA = " + statesDDA + ", nextsDDA = " + nextsDDA);

	}

	public void printArray(int[] srcNum, int n) {
		for (int i = 0; i < n; i++) {
			System.out.println(i + ":" + srcNum[i]);
		}
	}

	/**
	 * Find the meeting node(have multi-source nodes)
	 * 
	 * @param srcNum
	 *            : number of every node's source nodes in insns
	 * @param insns
	 * @return
	 */
	public List<AbstractInsnNode> getMultiSrcNode(int[] srcNum, InsnList insns) {
		int n = insns.size();
		List<AbstractInsnNode> multiSrcNode = new LinkedList<AbstractInsnNode>();
		for (int i = 0; i < n; i++) {
			if (srcNum[i] > 1) {
				multiSrcNode.add(insns.get(i));
			}
		}
		return multiSrcNode;
	}

	/**
	 * Insert all the trigger information and DDA
	 * 
	 * @param cn
	 * @param mn
	 * @param conupTx
	 *            the transaction or method name to be analyze
	 */
	public void methodTransform(ClassNode cn, MethodNode mn, String conupTx) {
		if (isTransaction(mn, conupTx)) {
			LOGGER.fine("Begin analyze method:" + mn.name);
			InsnList insns = mn.instructions;
			int localNum = mn.localVariables.size();
			if (com.size() == 0) {
				this.transformWhenNoInvocation(cn, insns, localNum);
			} 
			else {
				stateMachine.setStart(0);
				try {
					ExtractControlFlow(cn.name, mn);
				} catch (AnalyzerException ignored) {
				}
				int insnNum = mn.instructions.size();
				initIsAnalyze(insnNum);
				int[] srcNum = new int[insnNum];
				for (int i = 0; i < insnNum; i++) {
					srcNum[i] = 0;
				}
				recognizeState(0, 0, mn.instructions, srcNum);
				correctState();
				List<AbstractInsnNode> multiSrcNode = this.getMultiSrcNode(
						srcNum, insns);
				getFirstRequestService(0, mn.instructions, -1);
				mergeState();
				List<String>[] future = ExtractMetaData();
				setStates(future);
				List<String> stateall = setStateAll();
				List<String> next = setNext();
				writeDDA(mn, stateall, next);

				// insert annotation
				// Iterator<AnnotationNode> iter =
				// mn.visibleAnnotations.iterator();
				insertStartInf(cn, insns, localNum);
				// insert first request service before the method invokation
				if (runinf.size() > 0) {
					for (AbstractInsnNode firstRS : runinf) {
						InsnList setUp = new InsnList();
						setUp.add(new VarInsnNode(ALOAD, localNum));
						// setUp.add(new LdcInsnNode(statesDDA));
						// setUp.add(new LdcInsnNode(nextsDDA));
						setUp.add(new MethodInsnNode(
								INVOKESTATIC,
								"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
								"getInstance",
								"(Ljava/lang/String;)Lcn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager;"));
						setUp.add(new MethodInsnNode(
								INVOKEVIRTUAL,
								"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
								"FirstRequestService", "()V"));
						AbstractInsnNode ilPre = firstRS.getPrevious();
						int op = ilPre.getOpcode();
						// get the parameters of the method invocation
						while ((op <= ALOAD && op >= ILOAD)
								|| (op <= IALOAD && op >= SALOAD)
								|| op == GETSTATIC || op == GETFIELD
								|| (op <= DUP2_X2 && op >= DUP)) {
							ilPre = ilPre.getPrevious();
							op = ilPre.getOpcode();
						}
						insns.insert(ilPre, setUp);
					}
				}
				// insert component-invoked event
				if (cominf.size() > 0) {
					Enumeration<AbstractInsnNode> coms = cominf.keys();
					while (coms.hasMoreElements()) {
						AbstractInsnNode com = coms.nextElement();
						InsnList trig = new InsnList();
						trig.add(new VarInsnNode(ALOAD, localNum));
						// trig.add(new LdcInsnNode(statesDDA));
						// trig.add(new LdcInsnNode(nextsDDA));
						trig.add(new MethodInsnNode(
								INVOKESTATIC,
								"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
								"getInstance",
								"(Ljava/lang/String;)Lcn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager;"));

						trig.add(new LdcInsnNode(cominf.get(com)));
						trig.add(new MethodInsnNode(
								INVOKEVIRTUAL,
								"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
								"trigger", "(Ljava/lang/String;)V"));
						AbstractInsnNode ilPost = com.getNext();
						int op = ilPost.getOpcode();
						// save the results of the method invocation
						while ((op >= ISTORE && op <= ASTORE)
								|| (op >= IASTORE && op <= SASTORE)
								|| op == PUTSTATIC || op == PUTFIELD
								|| op == POP || op == POP2) {
							ilPost = ilPost.getNext();
							op = ilPost.getOpcode();
						}
						insns.insertBefore(ilPost, trig);

					}

				}
				// insert branch event
				if (jumpsrcinf.size() > 0) {
					Enumeration<AbstractInsnNode> srcs = jumpsrcinf.keys();
					while (srcs.hasMoreElements()) {
						AbstractInsnNode src = srcs.nextElement();

						Enumeration<AbstractInsnNode> enuDst = jumpsrcinf.get(
								src).keys();
						while (enuDst.hasMoreElements()) {
							AbstractInsnNode i1dst = enuDst.nextElement();
							String jumpE = jumpsrcinf.get(src).get(i1dst);
							if (multiSrcNode.contains(i1dst)) {
								// it is a meeting note
								if (src instanceof JumpInsnNode) {
									if (((JumpInsnNode) src).label
											.equals(i1dst)) {
										LabelNode nextOne = changeJumpLabel(
												insns, jumpE, localNum,
												(LabelNode) i1dst);
										((JumpInsnNode) src).label = nextOne;
										// System.out.println("--------------"
										// + nextOne + "replaced"
										// + ((JumpInsnNode) src).label);
									} else {
										InsnList trig = new InsnList();
										trig.add(new VarInsnNode(ALOAD,
												localNum));
										// trig.add(new LdcInsnNode(statesDDA));
										// trig.add(new LdcInsnNode(nextsDDA));
										trig.add(new MethodInsnNode(
												INVOKESTATIC,
												"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
												"getInstance",
												"(Ljava/lang/String;)Lcn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager;"));

										trig.add(new LdcInsnNode(jumpE));
										trig.add(new MethodInsnNode(
												INVOKEVIRTUAL,
												"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
												"trigger",
												"(Ljava/lang/String;)V"));
										insns.insert(src, trig);
									}
								} else {
									if (src instanceof TableSwitchInsnNode) {
										LabelNode dflt = ((TableSwitchInsnNode) src).dflt;
										if (dflt.equals(i1dst)) {
											LabelNode nextOne = changeJumpLabel(
													insns, jumpE, localNum,
													dflt);
											((TableSwitchInsnNode) src).dflt = nextOne;
										} else {
											List<LabelNode> allLabel = ((TableSwitchInsnNode) src).labels;
											List<LabelNode> newLabels = new LinkedList<LabelNode>();
											for (LabelNode label : allLabel) {
												if (label.equals(i1dst)) {
													LabelNode nextOne = changeJumpLabel(
															insns, jumpE,
															localNum, label);
													newLabels.add(nextOne);
													LOGGER.fine("TableSwitchInsnNode labels changed!");
												} else {
													newLabels.add(label);
												}
											}
											((TableSwitchInsnNode) src).labels = newLabels;
										}
									} else {
										if (src instanceof LookupSwitchInsnNode) {
											LabelNode dflt = ((LookupSwitchInsnNode) src).dflt;
											if (dflt.equals(i1dst)) {
												jumpE = jumpsrcinf.get(src)
														.get(dflt);
												LabelNode nextOne = changeJumpLabel(
														insns, jumpE, localNum,
														dflt);
												((LookupSwitchInsnNode) src).dflt = nextOne;
											} else {
												List<LabelNode> allLabel = ((TableSwitchInsnNode) src).labels;
												List<LabelNode> newLabels = new LinkedList<LabelNode>();
												for (LabelNode label : allLabel) {
													if (label.equals(i1dst)) {
														jumpE = jumpsrcinf.get(
																src).get(label);
														LabelNode nextOne = changeJumpLabel(
																insns, jumpE,
																localNum, label);
														newLabels.add(nextOne);
													} else {
														newLabels.add(label);
													}
												}
												((LookupSwitchInsnNode) src).labels = newLabels;
											}
										} else {
											LOGGER.fine("Other jump info we have to analyze further!");
										}
									}
								}
							} else {
								// it has only one previous node in CFG
								AbstractInsnNode ilPost = i1dst;
								while (ilPost instanceof LabelNode
										|| ilPost instanceof LineNumberNode) {
									ilPost = ilPost.getNext();
								}
								InsnList trig = new InsnList();
								trig.add(new VarInsnNode(ALOAD, localNum));
								// trig.add(new LdcInsnNode(statesDDA));
								// trig.add(new LdcInsnNode(nextsDDA));
								trig.add(new MethodInsnNode(
										INVOKESTATIC,
										"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
										"getInstance",
										"(Ljava/lang/String;)Lcn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager;"));

								trig.add(new LdcInsnNode(jumpE));
								trig.add(new MethodInsnNode(
										INVOKEVIRTUAL,
										"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
										"trigger", "(Ljava/lang/String;)V"));
								insns.insertBefore(ilPost, trig);
							}
						}
					}
				}
				// insert end event
				if (endinf.size() > 0) {
					for (AbstractInsnNode end : endinf) {
						InsnList trig = new InsnList();
						trig.add(new VarInsnNode(ALOAD, localNum));
						// trig.add(new LdcInsnNode(statesDDA));
						// trig.add(new LdcInsnNode(nextsDDA));
						trig.add(new MethodInsnNode(
								INVOKESTATIC,
								"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
								"getInstance",
								"(Ljava/lang/String;)Lcn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager;"));
						trig.add(new LdcInsnNode(""));
						trig.add(new MethodInsnNode(
								INVOKEVIRTUAL,
								"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
								"trigger", "(Ljava/lang/String;)V"));
						AbstractInsnNode ilPre = end.getPrevious();
						int op = ilPre.getOpcode();

						// get the parameters of the return
						while ((op >= ACONST_NULL && op <= LDC)
								|| (op <= ALOAD && op >= ILOAD)
								|| (op <= IALOAD && op >= SALOAD)
								|| op == GETSTATIC || op == GETFIELD
								|| (op <= DUP2_X2 && op >= DUP)) {
							ilPre = ilPre.getPrevious();
							op = ilPre.getOpcode();
						}
						insns.insert(ilPre, trig);
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
	public void transformWhenNoInvocation(ClassNode cn, InsnList insns, int localNum){
		// insert transaction id
		InsnList trigstart = new InsnList();
		trigstart.add(new VarInsnNode(ALOAD, 0));
		trigstart
				.add(new FieldInsnNode(GETFIELD, cn.name, TxlcMgr, TxlcMgrDesc));
		trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, TxlcMgrDesc.substring(
				1, TxlcMgrDesc.length() - 1), "createID",
				"()Ljava/lang/String;"));
		// System.out.println("TxlcMgrDesc.substring(1,TxlcMgrDesc.length()-1) = "+TxlcMgrDesc.substring(1,TxlcMgrDesc.length()-1));
		trigstart.add(new VarInsnNode(ASTORE, localNum));
		// insert start inf
		trigstart.add(new VarInsnNode(ALOAD, localNum));
		trigstart.add(new LdcInsnNode("_E"));
		// System.out.println("----------------------StatesDDA"+statesDDA);
		trigstart.add(new LdcInsnNode("_E"));
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
		while(i.hasNext()){
			AbstractInsnNode node = i.next();
			if ((node.getOpcode() >= IRETURN && node.getOpcode() <= RETURN)){
				InsnList trig = new InsnList();
				trig.add(new VarInsnNode(ALOAD, localNum));
				// trig.add(new LdcInsnNode(statesDDA));
				// trig.add(new LdcInsnNode(nextsDDA));
				trig.add(new MethodInsnNode(
						INVOKESTATIC,
						"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
						"getInstance",
						"(Ljava/lang/String;)Lcn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager;"));
				trig.add(new LdcInsnNode(""));
				trig.add(new MethodInsnNode(
						INVOKEVIRTUAL,
						"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
						"trigger", "(Ljava/lang/String;)V"));
				AbstractInsnNode ilPre = node.getPrevious();
				int op = ilPre.getOpcode();

				// get the parameters of the return
				while ((op >= ACONST_NULL && op <= LDC)
						|| (op <= ALOAD && op >= ILOAD)
						|| (op <= IALOAD && op >= SALOAD)
						|| op == GETSTATIC || op == GETFIELD
						|| (op <= DUP2_X2 && op >= DUP)) {
					ilPre = ilPre.getPrevious();
					op = ilPre.getOpcode();
				}
				insns.insert(ilPre, trig);
			}
		}
	}
	/**
	 * 
	 * @param cn
	 * @param insns
	 * @param localNum
	 */
	public void insertStartInf(ClassNode cn, InsnList insns, int localNum) {
		// insert transaction id
		InsnList trigstart = new InsnList();
		trigstart.add(new VarInsnNode(ALOAD, 0));
		trigstart.add(new FieldInsnNode(GETFIELD, cn.name, TxlcMgr,
				TxlcMgrDesc));
		trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, TxlcMgrDesc
				.substring(1, TxlcMgrDesc.length() - 1), "createID",
				"()Ljava/lang/String;"));
		// System.out.println("TxlcMgrDesc.substring(1,TxlcMgrDesc.length()-1) = "+TxlcMgrDesc.substring(1,TxlcMgrDesc.length()-1));
		trigstart.add(new VarInsnNode(ASTORE, localNum));
		// insert start inf
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
		trigstart
				.add(new MethodInsnNode(
						INVOKEVIRTUAL,
						"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
						"trigger", "(Ljava/lang/String;)V"));
		insns.insert(insns.getFirst(), trigstart);
	}

	/**
	 * insert branch event while change the branch's dest label
	 * 
	 * @param insns
	 * @param jumpE
	 *            jump event info
	 * @param localNum
	 * @param sourceLabel
	 * @return
	 */
	public LabelNode changeJumpLabel(InsnList insns, String jumpE,
			int localNum, LabelNode sourceLabel) {
		InsnList trig = new InsnList();
		LabelNode nextOne = new LabelNode();
		if (sourceLabel.getPrevious().getOpcode() == GOTO) {
			trig.add(nextOne);
			trig.add(new VarInsnNode(ALOAD, localNum));
			// trig.add(new LdcInsnNode(statesDDA));
			// trig.add(new LdcInsnNode(nextsDDA));
			trig.add(new MethodInsnNode(
					INVOKESTATIC,
					"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
					"getInstance",
					"(Ljava/lang/String;)Lcn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager;"));
			trig.add(new LdcInsnNode(jumpE));
			trig.add(new MethodInsnNode(
					INVOKEVIRTUAL,
					"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
					"trigger", "(Ljava/lang/String;)V"));
			insns.insert(sourceLabel, trig);
		} else {
			LabelNode dstNext = new LabelNode();
			trig.add(new JumpInsnNode(GOTO, dstNext));
			trig.add(nextOne);
			trig.add(new VarInsnNode(ALOAD, localNum));
			// trig.add(new LdcInsnNode(statesDDA));
			// trig.add(new LdcInsnNode(nextsDDA));
			trig.add(new MethodInsnNode(
					INVOKESTATIC,
					"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
					"getInstance",
					"(Ljava/lang/String;)Lcn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager;"));
			trig.add(new LdcInsnNode(jumpE));
			trig.add(new MethodInsnNode(
					INVOKEVIRTUAL,
					"cn/edu/nju/moon/conup/ext/ddm/LocalDynamicDependencesManager",
					"trigger", "(Ljava/lang/String;)V"));
			trig.add(dstNext);
			insns.insert(sourceLabel, trig);
		}
		return nextOne;
	}

	/**
	 * Whether the method has the specified annotation
	 * 
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
//				System.out.println(src+"->"+dst);
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
	 * @param srcNum
	 *            number of source nodes of the src
	 */
	public void recognizeState(int src, int last_state, InsnList insns,
			int[] srcNum) {
		srcNum[src]++;
		if (src == stateMachine.getStart()) {
			stateMachine.addState(src);
			last_state = src;
		}
		if (stateMachine.getStates().contains(src) && (src != last_state)) {

		}

		if (isAnalyze[src] == 0) {
			AbstractInsnNode an = insns.get(src);
			if ((an.getOpcode() >= IRETURN && an.getOpcode() <= RETURN)
					|| an.getOpcode() == ATHROW) {
				stateMachine.addState(src);
				isAnalyze[src] = 1;
				stateMachine.addEvent(new Event(last_state, src, "end"));
				endinf.add(an);
				end.add(src);
			} else {
				if (controlflow.getFlow(src).getDst().size() == 1) {
					if (an instanceof MethodInsnNode) {
						isAnalyze[src] = 1;
						int next = (Integer) controlflow.getFlow(src).getDst()
								.get(0);
						MethodInsnNode method = ((MethodInsnNode) an);
						// System.out.println(an.toString()+"'owner is "+method.owner);
						if (com.contains(method.owner)) {
							// System.out.println(an.toString()+"-------------owner is "+method.owner);
							// String serName = getServiceName(method.owner);
							// String e = "COM." + serName + "." + src;
							String e = "COM." + method.owner + "." + src;						
							cominf.put(insns.get(src), e);
							if (!stateMachine.getStates().contains(src)) {
								stateMachine.addState(src);
							}
							if (src != last_state) {
								stateMachine.addEvent(new Event(last_state,
										src, ""));
							}
							if (!stateMachine.getStates().contains(next)) {
								stateMachine.addState(next);
							}
							stateMachine.addEvent(new Event(src, next, e));

							recognizeState(next, next, insns, srcNum);
						} else {
							recognizeState(next, last_state, insns, srcNum);
						}
					} else {
						if (an.getOpcode() == GOTO) {
							int dst = -1;
							isAnalyze[src] = 1;
							if (!stateMachine.getStates().contains(src)) {
								stateMachine.addState(src);
							}
							if (src != last_state) {
								stateMachine.addEvent(new Event(last_state,
										src, ""));
							}
							dst = (Integer) controlflow.getFlow(src).getDst()
									.get(0);
							if (!stateMachine.getStates().contains(dst)) {
								stateMachine.addState(dst);
							}
							stateMachine.addEvent(new Event(src, dst, ""));
							recognizeState(dst, dst, insns, srcNum);
						} else {
							isAnalyze[src] = 1;
							recognizeState((Integer) controlflow.getFlow(src)
									.getDst().get(0), last_state, insns, srcNum);
						}
					}
				} else {
					int dst = -1;
					isAnalyze[src] = 1;
					// if(runinf.size() == 0){
					// branchNum = controlflow.getDstSize(src);
					// }
					// bytecode not a goto, but a jumpInsnNode
					if (!stateMachine.getStates().contains(src)) {
						stateMachine.addState(src);
					}
					if (src != last_state) {
						stateMachine.addEvent(new Event(last_state, src, ""));
					}
					for (int k = 0; k < controlflow.getDstSize(src); k++) {
						dst = (Integer) controlflow.getDst(src).get(k);
						AbstractInsnNode dstNode = insns.get(dst);
						if (!stateMachine.getStates().contains(dst)) {
							stateMachine.addState(dst);
						}
						stateMachine.addEvent(new Event(src, dst, "branch." + k
								+ "." + src));
						if (jumpsrcinf.containsKey(an)) {
							jumpsrcinf.get(an).put(dstNode,
									"branch." + k + "." + src);
						} else {
							Hashtable<AbstractInsnNode, String> tempdst = new Hashtable<AbstractInsnNode, String>();
							tempdst.put(dstNode, "branch." + k + "." + src);
							jumpsrcinf.put(an, tempdst);
						}
						if (jumpdstinf.containsKey(dstNode)) {
							jumpdstinf.get(dstNode).add(
									"branch." + k + "." + src);
						} else {
							List<String> branchEvent = new LinkedList<String>();
							branchEvent.add("branch." + k + "." + src);
							jumpdstinf.put(dstNode, branchEvent);
						}
						recognizeState(dst, dst, insns, srcNum);
					}
				}
			}
		}
	}
	
	public void correctState(){
		Set<Integer> srcSet = new ConcurrentSkipListSet<Integer>();
		Set<Integer> dstSet = new ConcurrentSkipListSet<Integer>();
		for (int i = 0; i < stateMachine.getEvents().size(); i++) {
			Event event = stateMachine.getEvents().get(i);
			srcSet.add(event.getHead());
			dstSet.add(event.getTail());
		}
		dstSet.removeAll(end);
		dstSet.removeAll(srcSet);
		LOGGER.fine("The set need to corret: " + dstSet);
		if (!dstSet.isEmpty()) {
			for (Integer cor : dstSet) {
				//Should not meet branch node 	
				    LOGGER.fine("we are correcting: " + cor);
					int dstTemp = (Integer) controlflow.getDst(cor).get(0);
					while(!stateMachine.getStates().contains(dstTemp)){
						dstTemp = (Integer) controlflow.getDst(dstTemp).get(0);
					}
					if(stateMachine.getStates().contains(dstTemp)){
						stateMachine.addEvent(new Event(cor, dstTemp, ""));
					}
					else{
						LOGGER.warning("There is some thing wrong in" + cor);
					}
				
			}
		}

	}

	/**
	 * find the first request service node in the method. There may be none, one
	 * or two...
	 * 
	 * @param src
	 * @param insns
	 * @param branch
	 */
	public void getFirstRequestService(int src, InsnList insns, int branch) {
		if (cominf.size() > 0) {
			AbstractInsnNode an = insns.get(src);
			if ((an.getOpcode() >= IRETURN && an.getOpcode() <= RETURN)
					|| an.getOpcode() == ATHROW) {
				return;
			} else {
				if (cominf.containsKey(an)) {
					if (!runinf.contains(an)) {
						runinf.add(an);
					}
					return;
				} else {
					int branchNum = controlflow.getDstSize(src);
					if (branchNum > 1) {
						for (int k = 0; k < branchNum; k++) {
							int dst = (Integer) controlflow.getDst(src).get(k);
							getFirstRequestService(dst, insns, k);
						}
					} else {
						int dst = (Integer) controlflow.getDst(src).get(0);
						getFirstRequestService(dst, insns, branch);
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
			LOGGER.fine(src + "-" + event.getEvent() + "-" + dst);
			// LOGGER.info(event.getEvent().split(".")[0]);

		}
	}

	/**
	 * get future and past components in every state of DDA
	 */
	public List<String>[] ExtractMetaData() {

		int states_count = stateMachine.getStatesCount();
		List<Integer> s = stateMachine.getStates();
		List<Event> e = stateMachine.getEvents();

		for (int i = 0; i < states_count; i++) {
			LOGGER.fine(i + ":" + s.get(i));
		}

		// get the static past that may be used in the future, not used now
		List<String>[] past = new LinkedList[states_count];
		for (int i = 0; i < states_count; i++)
			past[i] = new LinkedList<String>();
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

		List<String>[] future = new LinkedList[states_count];
		for (int i = 0; i < states_count; i++)
			future[i] = new LinkedList<String>();
		changed = true;
		while (changed) {
			changed = false;
			for (int i = e.size() - 1; i >= 0; i--) {
				Event event = (Event) e.get(i);
				int head = event.getHead();
				int tail = event.getTail();
				String port = event.getPort();
				// LOGGER.fine(port);
				int headindex = s.indexOf(head);
				int tailindex = s.indexOf(tail);
				if (port != null && !future[headindex].contains(port)) {
					future[headindex].add(port);
					changed = true;
				}
				if (!stateMachine.getEnd().contains(tail))
					for (int j = 0; j < future[tailindex].size(); j++) {
						if (!future[headindex].contains(future[tailindex]
								.get(j))) {
							future[headindex].add(future[tailindex].get(j));
							changed = true;
						}

					}
			}
		}
		return future;

	}

	/**
	 * put the states content in every state
	 */
	public void setStates(List<String>[] future) {
		int states_count = stateMachine.getStatesCount();
		for (int i = 0; i < states_count; i++) {
			State state = new State(stateMachine.getStates().get(i));
			state.setFuture(future[i]);
			states.add(state);
			LOGGER.fine(i + ":" + future[i]);
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
						if (jumpdstinf.containsValue(event.getEvent())) {
							Enumeration<AbstractInsnNode> eachkey = jumpdstinf
									.keys();
							while (eachkey.hasMoreElements()) {
								AbstractInsnNode key = eachkey.nextElement();
								if (jumpdstinf.get(key).equals(eventinf)) {
									jumpdstinf.remove(key);
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
	public List<String> setStateAll() {
		List<String> stateall = new LinkedList<String>();
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
			stateall.add(sall);
		}
		return stateall;
	}

	/**
	 * set the trigger event and the next state for every state
	 */
	public List<String> setNext() {
		List<String> next = new LinkedList<String>();
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
			LOGGER.fine(i + "next:" + nexts);
		}
		return next;
	}

}
