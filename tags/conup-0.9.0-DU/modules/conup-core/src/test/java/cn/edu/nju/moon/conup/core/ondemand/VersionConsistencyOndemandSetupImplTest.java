package cn.edu.nju.moon.conup.core.ondemand;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * @author rgc
 * @version Dec 3, 2012 3:38:50 PM
 */
public class VersionConsistencyOndemandSetupImplTest {
	VersionConsistencyOndemandSetupImpl vc = null;
	OndemandSetupHelperImpl osh = null;
	@Before
	public void setUp() throws Exception {
		vc = new VersionConsistencyOndemandSetupImpl();
		
		NodeManager nodeMgr = NodeManager.getInstance();
		nodeMgr.loadConupConf("AuthComponent", "old_version", "src/test/resources/Conup.xml");
		osh = (OndemandSetupHelperImpl) NodeManager.getInstance().getOndemandSetupHelper("AuthComponent");
		vc.setOndemandHelper(osh);
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCalcScope() {
		Scope scope = vc.calcScope();
		Set<String> allCompnents = scope.getAllComponents();
		for (String string : allCompnents) {
			System.out.println(string);
		}
		Set<String> comps = new HashSet<String>();
		comps.add("PortalComponent");
		comps.add("ProcComponent");
		comps.add("AuthComponent");
		assertEquals(comps.size(), allCompnents.size());
		assertTrue(allCompnents.equals(comps));
		Set<String> procSubComps = new HashSet<String>();
		procSubComps.add("AuthComponent");
		assertTrue(scope.getSubComponents("ProcComponent").equals(procSubComps));
	}

}
