package cn.edu.nju.moon.conup.ext.utils.experiments.utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExpSetting;

public class ExpXMLUtilTest {

	ExpXMLUtil expXMLUtil = null;

	@Before
	public void setUp() throws Exception {
		expXMLUtil = new ExpXMLUtil("src/test/resources/");
	}

	@Test
	public void testGetExpSetting() {
		ExpSetting expSetting = expXMLUtil.getExpSetting();
		assertTrue(expSetting.getScope() == null);
		// System.out.println(expSetting.getScope());
//		assertEquals(
//				"CurrencyConverter<TravelCatalog#TravelCatalog<Coordination#Coordination>TravelCatalog#TravelCatalog>CurrencyConverter#SCOPE_FLAG&true",
//				expSetting.getScope().toString());
//		assertTrue(expSetting
//				.toString()
//				.equals("indepRun:50 nThreads:100 threadId:50 rqstInterval:50 targetComp:CurrencyConverter ipAddress:114.212.191.22 baseDir:/home/conup"));
	}

}
