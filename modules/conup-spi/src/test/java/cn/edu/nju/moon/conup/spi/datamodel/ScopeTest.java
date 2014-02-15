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
		// auth component
		parentComps.add("ProcComponent");
		parentComps.add("PortalComponent");
		scope.addComponent("AuthComponent", parentComps, subComps);
		// proc component
		parentComps.clear();
		subComps.clear();
		parentComps.add("PortalComponent");
		subComps.add("AuthComponent");
		scope.addComponent("ProcComponent", parentComps, subComps);
		// portal component
		parentComps.clear();
		subComps.clear();
		subComps.add("AuthComponent");
		subComps.add("ProcComponent");
		scope.addComponent("PortalComponent", parentComps, subComps);
		// target component
		targetComps.add("AuthComponent");
		scope.setTarget(targetComps);

		String real = scope.toString();
		System.out.println(real);
		assertTrue(real.contains("ProcComponent<PortalComponent"));
		assertTrue(real.contains("AuthComponent<ProcComponent"));
		assertTrue(real.contains("AuthComponent<PortalComponent"));
		assertTrue(real.contains("ProcComponent>AuthComponent"));
		assertTrue(real.contains(Scope.TARGET_IDENTIFIER
				+ Scope.TARGET_SEPERATOR + "AuthComponent"));
		assertTrue(!real.endsWith(Scope.SCOPE_ENTRY_SEPERATOR));
	}

	@Test
	public void testInverse() {
		String toString = "ProcComponent<PortalComponent#"
				+ "AuthComponent<ProcComponent#"
				+ "AuthComponent<PortalComponent#"
				+ "ProcComponent>AuthComponent#"
				+ "PortalComponent>ProcComponent#"
				+ "PortalComponent>AuthComponent#"
				+ "TARGET_COMP@AuthComponent#"
				+ "SCOPE_FLAG&true";
		Scope scope = Scope.inverse(toString);
		
		// check isSpecified flag
		assertTrue(scope.isSpecifiedScope());
		
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

		// auth component
		expectParent.clear();
		expectSub.clear();
		expectParent.add("ProcComponent");
		expectParent.add("PortalComponent");
		assertEquals(authParent, expectParent);
		assertEquals(authSub, expectSub);

		// proc component
		expectParent.clear();
		expectSub.clear();
		expectParent.add("PortalComponent");
		expectSub.add("AuthComponent");
		assertEquals(procParent, expectParent);
		assertEquals(procSub, expectSub);

		// portal component
		expectParent.clear();
		expectSub.clear();
		expectSub.add("AuthComponent");
		expectSub.add("ProcComponent");
		assertEquals(portalParent, expectParent);
		assertEquals(portalSub, expectSub);

		// target components
		expectTarget.add("AuthComponent");
		assertTrue(targetComps.equals(expectTarget));

		// abnormal cases
		assertNull(Scope.inverse(null));
		assertNull(Scope.inverse("#abc"));
		assertNull(Scope.inverse("#"));
	}

	@Test
	public void testGetRootComp() {
		// TEST CASE 1
		Scope scope = new Scope();
		Set<String> parentComps = new HashSet<String>();
		Set<String> subComps = new HashSet<String>();
		Set<String> targetComps = new HashSet<String>();
		// auth component
		parentComps.add("ProcComponent");
		parentComps.add("PortalComponent");
		scope.addComponent("AuthComponent", parentComps, subComps);
		// proc component
		parentComps.clear();
		subComps.clear();
		parentComps.add("PortalComponent");
		subComps.add("AuthComponent");
		scope.addComponent("ProcComponent", parentComps, subComps);
		// portal component
		parentComps.clear();
		subComps.clear();
		subComps.add("AuthComponent");
		subComps.add("ProcComponent");
		scope.addComponent("PortalComponent", parentComps, subComps);
		// target component
		targetComps.add("AuthComponent");
		scope.setTarget(targetComps);

		Set<String> rootComps = scope.getRootComp("AuthComponent");
		assertTrue(rootComps.size() == 1);
		assertTrue(rootComps.contains("PortalComponent"));

		rootComps = scope.getRootComp("ProcComponent");
		assertTrue(rootComps.size() == 1);
		assertTrue(rootComps.contains("PortalComponent"));

		// TEST CASE 2
		parentComps.clear();
		subComps.clear();
		scope = new Scope();

		// D component
		parentComps.add("C");
		parentComps.add("E");
		scope.addComponent("D", parentComps, subComps);

		// C component
		parentComps.clear();
		subComps.clear();
		parentComps.add("B");
		subComps.add("D");
		scope.addComponent("C", parentComps, subComps);

		// E component
		parentComps.clear();
		subComps.clear();
		parentComps.add("B");
		subComps.add("D");
		scope.addComponent("E", parentComps, subComps);

		// B component
		parentComps.clear();
		subComps.clear();
		subComps.add("C");
		subComps.add("E");
		scope.addComponent("B", parentComps, subComps);

		rootComps = scope.getRootComp("D");
		assertTrue(rootComps.size() == 1);
		assertTrue(rootComps.contains("B"));

		rootComps = scope.getRootComp("C");
		assertTrue(rootComps.size() == 1);
		assertTrue(rootComps.contains("B"));

		rootComps = scope.getRootComp("E");
		assertTrue(rootComps.size() == 1);
		assertTrue(rootComps.contains("B"));

		// TEST CASE 3
		parentComps.clear();
		subComps.clear();
		scope = new Scope();

		// D component
		parentComps.add("C");
		parentComps.add("E");
		scope.addComponent("D", parentComps, subComps);

		// C component
		parentComps.clear();
		subComps.clear();
		parentComps.add("B");
		subComps.add("D");
		scope.addComponent("C", parentComps, subComps);

		// E component
		parentComps.clear();
		subComps.clear();
		parentComps.add("A");
		subComps.add("D");
		scope.addComponent("E", parentComps, subComps);

		// B component
		parentComps.clear();
		subComps.clear();
		subComps.add("C");
		scope.addComponent("B", parentComps, subComps);

		// A component
		parentComps.clear();
		subComps.clear();
		subComps.add("E");
		scope.addComponent("A", parentComps, subComps);
		
		rootComps = scope.getRootComp("D");
		assertTrue(rootComps.size() == 2);
		assertTrue(rootComps.contains("B"));
		assertTrue(rootComps.contains("A"));
		
		rootComps = scope.getRootComp("E");
		assertTrue(rootComps.size() == 1);
		assertTrue(!rootComps.contains("B"));
		assertTrue(rootComps.contains("A"));

		System.out.println(rootComps);
	}
}
