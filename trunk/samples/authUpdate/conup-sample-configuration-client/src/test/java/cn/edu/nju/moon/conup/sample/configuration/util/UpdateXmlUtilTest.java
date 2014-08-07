package cn.edu.nju.moon.conup.sample.configuration.util;

import static org.junit.Assert.*;

import java.rmi.ConnectException;

import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.sample.configuration.model.TargetComp;

public class UpdateXmlUtilTest {
	UpdateXmlUtil updateXmlUtil = null;
	
	@Before
	public void setUp() throws Exception {
		updateXmlUtil = new UpdateXmlUtil("src/test/resources/TargetComps.xml");
	}

	@Test
	public void testGetTargetComp() {
		try {
			TargetComp targetComp = updateXmlUtil.getTargetComp();
			assertEquals("172.16.154.128", targetComp.getIpAddress());
			assertEquals(18082, targetComp.getPort());
			assertEquals("AuthComponent", targetComp.getTargetCompIdentifier());
			assertEquals("conup-sample-auth", targetComp.getContributionUri());
			assertEquals("auth.composite", targetComp.getCompositeUri());
			assertEquals("cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl", targetComp.getCompImpl());
			assertEquals("/home/conup", targetComp.getBaseDir());
			
		} catch (ConnectException e) {
			e.printStackTrace();
		}
		
	}

	@Test
	public void testGetAllComponents() {
		assertEquals(4, updateXmlUtil.getAllComponents().size());
	}

}
