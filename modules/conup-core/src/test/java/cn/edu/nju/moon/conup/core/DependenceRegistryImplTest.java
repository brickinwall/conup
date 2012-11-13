package cn.edu.nju.moon.conup.core;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.core.algorithm.VersionConsistencyImpl;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
/**
 * @author rgc 
 * test commit
 */
public class DependenceRegistryImplTest {
	
	DependenceRegistry dependenceRegistry = null; 
	@Before
	public void setUp() throws Exception {
		dependenceRegistry = new DependenceRegistry();
	}

	@Test
	public void testAddDependence() {
		Dependence dependence = new Dependence();
		dependence.setRootTx(UUID.randomUUID().toString());
		dependence.setSrcCompObjIdentifier("PortalComponent");
		dependence.setTargetCompObjIdentifer("AuthComponent");
		dependence.setType(VersionConsistencyImpl.FUTURE_DEP);
		dependenceRegistry.addDependence(dependence);
		
		assertTrue(dependenceRegistry.contain(dependence));
	}

	@Test
	public void testRemoveDependenceDependence() {
		
		Dependence dependence = new Dependence();
		String rootTx = UUID.randomUUID().toString();
		dependence.setRootTx(rootTx);
		dependence.setSrcCompObjIdentifier("PortalComponent");
		dependence.setTargetCompObjIdentifer("AuthComponent");
		dependence.setType(VersionConsistencyImpl.FUTURE_DEP);
		
		/**
		 * test removeDependence(dependence)
		 */
		dependenceRegistry.addDependence(dependence);
		assertTrue(dependenceRegistry.removeDependence(dependence));
		assertFalse(dependenceRegistry.contain(dependence));
		
		/**
		 * test removeDependence(String type, String rootTx, String srcCompObjIdentifier, String targetCompObjIdentifer) 
		 */
		dependenceRegistry.addDependence(dependence);
		assertTrue(dependenceRegistry.removeDependence(VersionConsistencyImpl.FUTURE_DEP, rootTx, "PortalComponent", "AuthComponent"));
		assertFalse(dependenceRegistry.contain(dependence));
		
	}


	@Test
	public void testGetDependences() {
		assertNotNull(dependenceRegistry.getDependences());
	}

	@Test
	public void testGetDependencesViaType() {
		Dependence d1 = new Dependence();
		String rootTx = UUID.randomUUID().toString();
		d1.setRootTx(rootTx);
		d1.setSrcCompObjIdentifier("PortalComponent");
		d1.setTargetCompObjIdentifer("AuthComponent");
		d1.setType(VersionConsistencyImpl.FUTURE_DEP);
		Dependence d2 = new Dependence();
		d2.setRootTx(rootTx);
		d2.setSrcCompObjIdentifier("PortalComponent");
		d2.setTargetCompObjIdentifer("ProcComponent");
		d2.setType(VersionConsistencyImpl.PAST_DEP);
		Dependence d3 = new Dependence();
		d3.setRootTx(rootTx);
		d3.setSrcCompObjIdentifier("PortalComponent");
		d3.setTargetCompObjIdentifer("AuthComponent");
		d3.setType(VersionConsistencyImpl.PAST_DEP);
		
		dependenceRegistry.addDependence(d1);
		dependenceRegistry.addDependence(d2);
		dependenceRegistry.addDependence(d3);
		
		assertTrue(dependenceRegistry.getDependencesViaType(VersionConsistencyImpl.FUTURE_DEP).contains(d1));
		assertTrue(dependenceRegistry.getDependencesViaType(VersionConsistencyImpl.PAST_DEP).contains(d2));
		assertTrue(dependenceRegistry.getDependencesViaType(VersionConsistencyImpl.PAST_DEP).contains(d3));
		
	}

	@Test
	public void testGetDependencesViaRootTransaction() {
		Dependence d1 = new Dependence();
		String rootTx1 = UUID.randomUUID().toString();
		d1.setRootTx(rootTx1);
		d1.setSrcCompObjIdentifier("PortalComponent");
		d1.setTargetCompObjIdentifer("AuthComponent");
		d1.setType(VersionConsistencyImpl.FUTURE_DEP);
		Dependence d2 = new Dependence();
		String rootTx2 = UUID.randomUUID().toString();
		d2.setRootTx(rootTx2);
		d2.setSrcCompObjIdentifier("PortalComponent");
		d2.setTargetCompObjIdentifer("ProcComponent");
		d2.setType(VersionConsistencyImpl.PAST_DEP);
		Dependence d3 = new Dependence();
		d3.setRootTx(rootTx2);
		d3.setSrcCompObjIdentifier("PortalComponent");
		d3.setTargetCompObjIdentifer("AuthComponent");
		d3.setType(VersionConsistencyImpl.PAST_DEP);
		
		dependenceRegistry.addDependence(d1);
		dependenceRegistry.addDependence(d2);
		dependenceRegistry.addDependence(d3);
		
		assertTrue(dependenceRegistry.getDependencesViaRootTransaction(rootTx1).contains(d1));
		assertTrue(dependenceRegistry.getDependencesViaRootTransaction(rootTx2).contains(d2));
		assertTrue(dependenceRegistry.getDependencesViaRootTransaction(rootTx2).contains(d3));
		
	}

	@Test
	public void testGetDependencesViaSourceComponent() {
		Dependence d1 = new Dependence();
		String rootTx = UUID.randomUUID().toString();
		d1.setRootTx(rootTx);
		d1.setSrcCompObjIdentifier("PortalComponent");
		d1.setTargetCompObjIdentifer("AuthComponent");
		d1.setType(VersionConsistencyImpl.FUTURE_DEP);
		Dependence d2 = new Dependence();
		d2.setRootTx(rootTx);
		d2.setSrcCompObjIdentifier("PortalComponent");
		d2.setTargetCompObjIdentifer("ProcComponent");
		d2.setType(VersionConsistencyImpl.PAST_DEP);
		Dependence d3 = new Dependence();
		d3.setRootTx(rootTx);
		d3.setSrcCompObjIdentifier("PortalComponent");
		d3.setTargetCompObjIdentifer("AuthComponent");
		d3.setType(VersionConsistencyImpl.PAST_DEP);
		
		dependenceRegistry.addDependence(d1);
		dependenceRegistry.addDependence(d2);
		dependenceRegistry.addDependence(d3);
		
		assertTrue(dependenceRegistry.getDependencesViaSourceComponent("PortalComponent").contains(d1));
		assertTrue(dependenceRegistry.getDependencesViaSourceComponent("PortalComponent").contains(d2));
		assertTrue(dependenceRegistry.getDependencesViaSourceComponent("PortalComponent").contains(d3));
	}

	@Test
	public void testGetDependencesViaTargetComponent() {
		Dependence d1 = new Dependence();
		String rootTx = UUID.randomUUID().toString();
		d1.setRootTx(rootTx);
		d1.setSrcCompObjIdentifier("PortalComponent");
		d1.setTargetCompObjIdentifer("AuthComponent");
		d1.setType(VersionConsistencyImpl.FUTURE_DEP);
		Dependence d2 = new Dependence();
		d2.setRootTx(rootTx);
		d2.setSrcCompObjIdentifier("PortalComponent");
		d2.setTargetCompObjIdentifer("ProcComponent");
		d2.setType(VersionConsistencyImpl.PAST_DEP);
		Dependence d3 = new Dependence();
		d3.setRootTx(rootTx);
		d3.setSrcCompObjIdentifier("PortalComponent");
		d3.setTargetCompObjIdentifer("AuthComponent");
		d3.setType(VersionConsistencyImpl.PAST_DEP);
		
		dependenceRegistry.addDependence(d1);
		dependenceRegistry.addDependence(d2);
		dependenceRegistry.addDependence(d3);
		
		assertTrue(dependenceRegistry.getDependencesViaTargetComponent("AuthComponent").contains(d1));
		assertTrue(dependenceRegistry.getDependencesViaTargetComponent("ProcComponent").contains(d2));
		assertTrue(dependenceRegistry.getDependencesViaTargetComponent("AuthComponent").contains(d3));
	}


	@Test
	public void testContain() {
		Dependence d1 = new Dependence();
		String rootTx = UUID.randomUUID().toString();
		d1.setRootTx(rootTx);
		d1.setSrcCompObjIdentifier("PortalComponent");
		d1.setTargetCompObjIdentifer("AuthComponent");
		d1.setType(VersionConsistencyImpl.FUTURE_DEP);
		
		dependenceRegistry.addDependence(d1);
		assertTrue(dependenceRegistry.getDependencesViaTargetComponent("AuthComponent").contains(d1));
		
	}

}
