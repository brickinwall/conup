package cn.edu.nju.moon.conup.domain.generator;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.domain.generator.ScopeGeneratorImpl;

public class ScopeGeneratorImplTest {
	private static ScopeGeneratorImpl sg = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sg = new ScopeGeneratorImpl();
	}

	@Test
	public void testGenerate() {
		sg.generate("PortalComponent");
//		sg.generate("AuthComponent");
//		sg.generate("ProcComponent");
//		sg.generate("DBComponent");
	}

}
