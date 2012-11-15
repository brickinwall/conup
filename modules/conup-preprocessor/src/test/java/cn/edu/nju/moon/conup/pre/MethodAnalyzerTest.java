package cn.edu.nju.moon.conup.pre;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import cn.edu.nju.moon.conup.preTestClasses.GeneratePortalService;

public class MethodAnalyzerTest {
	
	MethodAnalyzer methodAnalyzer = null;
	ClassNode cn = null;
	MethodNode txmn = null;
	MethodNode getTokenmn = null;
	MethodNode getPromn = null;
	MethodNode setPromn = null;
	String conupTx = "Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;";
	@Before
	public void setUp() throws Exception {
		List<String> allServices = new LinkedList<String>();
		allServices.add("cn/edu/nju/moon/conup/sample/portal/services/TokenService");
		allServices.add("cn/edu/nju/moon/conup/sample/portal/services/ProcService");
		String fieldName = "txLifecycleMgr";
		String fieldDesc = "Lcn/edu/nju/moon/conup/ext/tx/manager/TxLifecycleManager;";		
		methodAnalyzer = new MethodAnalyzer(allServices,fieldName,fieldDesc);		
		GeneratePortalService portalService = new GeneratePortalService();
		cn = portalService.generateBasicClass();
		getPromn = (MethodNode)cn.methods.get(3);
		setPromn = (MethodNode)cn.methods.get(4);
		getTokenmn = (MethodNode)cn.methods.get(5);
		txmn = (MethodNode)cn.methods.get(6);
		
	}

	@Test
	public void TestGetServiceName() {
		assertEquals("TokenService",methodAnalyzer.getServiceName("cn/edu/nju/moon/conup/sample/portal/services/TokenService"));
		assertEquals("ProcService",methodAnalyzer.getServiceName("cn/edu/nju/moon/conup/sample/portal/services/ProcService"));
	}
	
	@Test
	public void TestMethodTransform() {
		int TxlocalVeriableNum = txmn.localVariables.size();
		int getTokenlocalVeriableNum = getTokenmn.localVariables.size();
		int getProlocalVeriableNum = getPromn.localVariables.size();
		int setProlocalVeriableNum = setPromn.localVariables.size();
		methodAnalyzer.methodTransform(cn, txmn, conupTx);
		assertEquals(TxlocalVeriableNum + 1,txmn.localVariables.size());
		methodAnalyzer.methodTransform(cn, getTokenmn, conupTx);
		assertEquals(getTokenlocalVeriableNum,getTokenmn.localVariables.size());
		methodAnalyzer.methodTransform(cn, getPromn, conupTx);
		assertEquals(getProlocalVeriableNum,getPromn.localVariables.size());
		methodAnalyzer.methodTransform(cn, setPromn, conupTx);
		assertEquals(setProlocalVeriableNum,setPromn.localVariables.size());
	}
	
	@Test
	public void TestIsTransaction() {
		assertTrue(methodAnalyzer.isTransaction(txmn, conupTx));
		assertFalse(methodAnalyzer.isTransaction(getTokenmn, conupTx));
		assertFalse(methodAnalyzer.isTransaction(getPromn, conupTx));
		assertFalse(methodAnalyzer.isTransaction(setPromn, conupTx));
	}
	
	@Test
	public void TestRecognizeState() {
		int n = txmn.instructions.size();
		methodAnalyzer.initIsAnalyze(n);		
		for (int i = 0; i < n; i++) {			
			assertEquals(0,methodAnalyzer.getIsAnalyze()[i]);
		}
		try {
			methodAnalyzer.ExtractControlFlow(cn.name, txmn);
		} catch (AnalyzerException ignored) {
		}
		methodAnalyzer.recognizeState(0, 0, txmn.instructions);
		//lastNode is a label or frame node
		for (int i = 0; i < n-1; i++) {			
			assertEquals(1,methodAnalyzer.getIsAnalyze()[i]);
		}		
	}
	@Test
	public void TestExtractMetaData() {
		try {
			methodAnalyzer.ExtractControlFlow(cn.name, txmn);
		} catch (AnalyzerException ignored) {
		}
		methodAnalyzer.initIsAnalyze(txmn.instructions.size());
		methodAnalyzer.recognizeState(0, 0, txmn.instructions);
		methodAnalyzer.mergeState();
		List<String>[] future = methodAnalyzer.ExtractMetaData();
		List<Integer> s = methodAnalyzer.getStateMachine().getStates();
		List<Event> e = methodAnalyzer.getStateMachine().getEvents();
		for (int i = 0; i < e.size(); i++) {
			Event event = (Event) e.get(i);
			int head = event.getHead();
			int tail = event.getTail();
			int headindex = s.indexOf(head);
			int tailindex = s.indexOf(tail);
			for (int j = 0; j < future[tailindex].size(); j++) {
				assertTrue(future[headindex].contains(future[tailindex].get(j)));
			}

		}
	}
	@Test
	public void TestWriteDDA() {
		assertEquals("",methodAnalyzer.getStatesDDA());
		assertEquals("",methodAnalyzer.getNextsDDA());
		try {
			methodAnalyzer.ExtractControlFlow(cn.name, txmn);
		} catch (AnalyzerException ignored) {
		}
		methodAnalyzer.initIsAnalyze(txmn.instructions.size());
		methodAnalyzer.recognizeState(0, 0, txmn.instructions);
		methodAnalyzer.mergeState();
		List<String>[] future = methodAnalyzer.ExtractMetaData();
		methodAnalyzer.setStates(future);
		List<String> stateall = methodAnalyzer.setStateAll();		
		List<String> next = methodAnalyzer.setNext();
		methodAnalyzer.writeDDA(txmn,stateall,next);
		String states = "TokenService,ProcService;ProcService;_E";
		String nexts = "COM.TokenService.23-1;COM.ProcService.35-2;_E";
		assertEquals(states,methodAnalyzer.getStatesDDA());
		assertEquals(nexts,methodAnalyzer.getNextsDDA());
	}
}
