package cn.edu.nju.moon.conup.apppre;

import static org.junit.Assert.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import cn.edu.nju.moon.conup.util.AtLeastOneBranchwithoutService;
import cn.edu.nju.moon.conup.util.GeneratePortalService;
import cn.edu.nju.moon.conup.util.TxWithSwitch;

public class TuscanyProgramAnalyzerTest {
	TuscanyProgramAnalyzer tuscanyProgramAnalyzer = null;
	ClassNode cn = null;
	ClassNode txWithSwitchCn = null;
	ClassNode branchWithoutServiceCn = null;
	
	@Before
	public void setUp() throws Exception {
		tuscanyProgramAnalyzer = new TuscanyProgramAnalyzer();
		GeneratePortalService portalService = new GeneratePortalService();
		cn = portalService.generateBasicClass();
		TxWithSwitch tws = new TxWithSwitch();
		txWithSwitchCn = tws.generateBasicClass();	
		AtLeastOneBranchwithoutService branchWithoutService = new AtLeastOneBranchwithoutService();
		branchWithoutServiceCn = branchWithoutService.generateBasicClass();
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
//		assertEquals(allServices,tuscanyProgramAnalyzer.getAllServices());		
	}
	@Test
	public void TestTxWithSwitch() {
		String conupTx = "Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;";
		String reference = "Lorg/oasisopen/sca/annotation/Reference;";
		if (tuscanyProgramAnalyzer.whetherToAnalyze(txWithSwitchCn, conupTx)) {
			tuscanyProgramAnalyzer.transform(txWithSwitchCn, conupTx, reference);
		}
		else{
			System.out.println("Need not to analyze!");
		}
	}
	@Test
	public void TestConupTx() {
		String conupTx = "Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;";
		String reference = "Lorg/oasisopen/sca/annotation/Reference;";
		if (tuscanyProgramAnalyzer.whetherToAnalyze(txWithSwitchCn, conupTx)) {
			tuscanyProgramAnalyzer.transform(txWithSwitchCn, conupTx, reference);
		}
		else{
			System.out.println("Need not to analyze!");
		}
	}
//	@Test
//	public void TestAnalyzeApplication() {
//		String conupTx = "Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;";
//		String reference = "Lorg/oasisopen/sca/annotation/Reference;";
//		if (tuscanyProgramAnalyzer.whetherToAnalyze(txWithSwitchCn, conupTx)) {
//			tuscanyProgramAnalyzer
//					.transform(txWithSwitchCn, conupTx, reference);
//			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//			txWithSwitchCn.accept(cw);			
//			byte[] b = cw.toByteArray();
//			try {
//				FileOutputStream fout = new FileOutputStream(new File(
//						"/home/PortalServiceImpl.class"));				
//				fout.write(b);
//				fout.close();
//				tuscanyProgramAnalyzer.showClassSource("/home/PortalServiceImpl.class");
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println(e);
//			}
//		} else {
//			System.out.println("Need not to analyze!");
//		}
//	}
	@Test
	public void TestBranchWithoutService() {
		String conupTx = "Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;";
		String reference = "Lorg/oasisopen/sca/annotation/Reference;";
		if (tuscanyProgramAnalyzer.whetherToAnalyze(branchWithoutServiceCn, conupTx)) {
			tuscanyProgramAnalyzer
					.transform(branchWithoutServiceCn, conupTx, reference);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			branchWithoutServiceCn.accept(cw);			
			byte[] b = cw.toByteArray();
			try {
				FileOutputStream fout = new FileOutputStream(new File(
						"src/test/resources/PortalServiceImpl.class"));				
				fout.write(b);
				fout.close();
				tuscanyProgramAnalyzer.showClassSource("src/test/resources/PortalServiceImpl.class");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e);
			}
		} else {
			System.out.println("Need not to analyze!");
		}
	}


}
