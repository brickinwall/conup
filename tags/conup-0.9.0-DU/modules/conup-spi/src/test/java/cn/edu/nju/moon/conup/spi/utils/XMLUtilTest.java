package cn.edu.nju.moon.conup.spi.utils;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author rgc
 * @version Dec 23, 2012 5:15:17 PM
 */
public class XMLUtilTest {
	private XMLUtil xmlUtil = null;
	
	@Before
	public void setUp() throws Exception {
		xmlUtil = new XMLUtil("src/test/resources/Conup.xml");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetFreenessStrategy() {
		String strategy = xmlUtil.getFreenessStrategy();
		assertEquals("CONCURRENT_VERSION_FOR_FREENESS", strategy);
	}

	@Test
	public void testGetParents() {
		String compIdentifier = "ProcComponent";
		Set<String> parents = xmlUtil.getParents(compIdentifier);
		
		Set<String> results = new HashSet<String>();
		results.add("PortalComponent");
		
		assertEquals(results, parents);
	}

	@Test
	public void testGetChildren() {
		String compIdentifier = "ProcComponent";
		Set<String> children = xmlUtil.getChildren(compIdentifier);
		
		Set<String> results = new HashSet<String>();
		results.add("AuthComponent");
		results.add("DBComponent");
		
		assertEquals(results, children);
	}

	@Test
	public void testGetAllComponents() {
		Set<String> allComps = xmlUtil.getAllComponents();
		
		Set<String> results = new HashSet<String>();
		results.add("AuthComponent");
		results.add("DBComponent");
		results.add("PortalComponent");
		results.add("ProcComponent");
		
		assertEquals(results, allComps);
	}

}
