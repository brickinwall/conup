package cn.edu.nju.moon.conup.spi.datamodel;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class ScopeTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testToString() {
		Scope scope = new Scope();
		Set<String> parentComps = new HashSet<String>();
		Set<String> subComps = new HashSet<String>();
		Set<String> targetComps = new HashSet<String>();
		//auth component
		parentComps.add("ProcComponent");
		parentComps.add("PortalComponent");
		scope.addComponent("AuthComponent", parentComps, subComps);
		//proc component
		parentComps.clear();
		subComps.clear();
		parentComps.add("PortalComponent");
		subComps.add("AuthComponent");
		scope.addComponent("ProcComponent", parentComps, subComps);
		//portao component
		parentComps.clear();
		subComps.clear();
		subComps.add("AuthComponent");
		subComps.add("ProcComponent");
		scope.addComponent("PortalComponent", parentComps, subComps);
		//target component
		targetComps.add("AuthComponent");
		scope.setTarget(targetComps);
		
		String real = scope.toString();
		assertTrue(real.contains("ProcComponent<PortalComponent"));
		assertTrue(real.contains("AuthComponent<ProcComponent"));
		assertTrue(real.contains("AuthComponent<PortalComponent"));
		assertTrue(real.contains("ProcComponent>AuthComponent"));
		assertTrue(real.contains(Scope.TARGET_IDENTIFIER + Scope.TARGET_SEPERATOR + "AuthComponent"));
		assertTrue(!real.endsWith(Scope.SCOPE_ENTRY_SEPERATOR));
	}
	
	@Test
	public void testInverse(){
		String toString = "ProcComponent<PortalComponent#" +
						  "AuthComponent<ProcComponent#" +
						  "AuthComponent<PortalComponent#" +
						  "ProcComponent>AuthComponent#" +
						  "PortalComponent>ProcComponent#" +
						  "PortalComponent>AuthComponent#" + 
						  "TARGET_COMP@AuthComponent";
		Scope scope = Scope.inverse(toString);
		Set<String> authParent = scope.getParentComponents("AuthComponent");
		Set<String> authSub = scope.getSubComponents("AuthComponent");
		Set<String> procParent = scope.getParentComponents("ProcComponent");
		Set<String> procSub = scope.getSubComponents("ProcComponent");
		Set<String> portalParent = scope.getParentComponents("PortalComponent");
		Set<String> portalSub = scope.getSubComponents("PortalComponent");
		Set<String> targetComps = scope.getTargetComponents();
		
		Set<String> expectParent = new HashSet<String>();
		Set<String> expectSub = new HashSet<String>();
		Set<String> expectTarget = new HashSet<String>();
		
		//auth component
		expectParent.clear();
		expectSub.clear();
		expectParent.add("ProcComponent");
		expectParent.add("PortalComponent");
		assertEquals(authParent, expectParent);
		assertEquals(authSub, expectSub);
		
		//proc component
		expectParent.clear();
		expectSub.clear();
		expectParent.add("PortalComponent");
		expectSub.add("AuthComponent");
		assertEquals(procParent, expectParent);
		assertEquals(procSub, expectSub);
		
		//portal component
		expectParent.clear();
		expectSub.clear();
		expectSub.add("AuthComponent");
		expectSub.add("ProcComponent");
		assertEquals(portalParent, expectParent);
		assertEquals(portalSub, expectSub);
		
		//target components
		expectTarget.add("AuthComponent");
		assertTrue(targetComps.equals(expectTarget));
		
		
		//abnormal cases
		assertNull(Scope.inverse(null));
		assertNull(Scope.inverse("#abc"));
		assertNull(Scope.inverse("#"));
	}
	
	@Test
	public void testInverse2(){
//		String toString = "Payment>CurrencyConverter#TARGET_COMP@CurrencyConverter";
		
		Set<String> parentComps = new HashSet<String>();
		parentComps.add("Payment");
		
		String hostComp = "CurrencyConverter";
		for (String parent : parentComps) {
			Scope scope = new Scope();
			Set<String> parentSet = new HashSet<String>();
			Set<String> subSet = new HashSet<String>();
			Set<String> targetSet = new HashSet<String>();
			targetSet.add(hostComp);
			subSet.add(hostComp);
			scope.addComponent(parent, parentSet, subSet);
			
			parentSet.clear();
			subSet.clear();
			parentSet.add(parent);
			scope.addComponent(hostComp, parentSet, subSet);
			scope.setTarget(targetSet);
			
//			Scope scope = Scope.inverse(toString);
			System.out.println("targetComp: " + scope.getTargetComponents());
			System.out.println("CurrencyConverter's parents: " + scope.getParentComponents("CurrencyConverter"));
			System.out.println("Payment's child: " + scope.getSubComponents("Payment"));
		}
		
	}
	
}
