package cn.edu.nju.moon.conup.pre;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import cn.edu.nju.moon.conup.preTestClasses.GeneratePortalService;

public class TuscanyProgramAnalyzerTest {
	TuscanyProgramAnalyzer tuscanyProgramAnalyzer = null;
	ClassNode cn = null;
	
	@Before
	public void setUp() throws Exception {
		tuscanyProgramAnalyzer = new TuscanyProgramAnalyzer();
		GeneratePortalService portalService = new GeneratePortalService();
		cn = portalService.generateBasicClass();	
		
	}

	@Test
	public void TestaddTxLifecycleManager() {
		int cnFieldNum = cn.fields.size();
		tuscanyProgramAnalyzer.addTxLifecycleManager(cn);
		assertEquals(cnFieldNum + 1,cn.fields.size());
	}
	@Test
	public void TestWhetherToAnalyze() {
		String conupTx = "Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;";
		assertTrue(tuscanyProgramAnalyzer.whetherToAnalyze(cn,conupTx));
	}
	@Test
	public void TestFindAllServices() {
		String reference = "Lorg/oasisopen/sca/annotation/Reference;";
		List<String> allServices = new LinkedList<String>();
		allServices.add("cn/edu/nju/moon/conup/sample/portal/services/TokenService");
		allServices.add("cn/edu/nju/moon/conup/sample/portal/services/ProcService");
		tuscanyProgramAnalyzer.findAllServices(cn, reference);
		assertEquals(allServices,tuscanyProgramAnalyzer.getAllServices());
		
	}

}
