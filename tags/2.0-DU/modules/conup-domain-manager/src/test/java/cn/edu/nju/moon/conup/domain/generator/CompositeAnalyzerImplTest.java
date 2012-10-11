package cn.edu.nju.moon.conup.domain.generator;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.domain.generator.CompositeAnalyzerImpl;

public class CompositeAnalyzerImplTest {
	private static CompositeAnalyzerImpl ca = null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ca = new CompositeAnalyzerImpl();
	}

	@Test
	public void testAnalyze() {
		String filePath = "";
		
		String baseUri = new File("").getAbsolutePath();
		baseUri = baseUri.substring(0, baseUri.lastIndexOf(File.separator)) + File.separator +"vc-domain-manager";
		String compositeLocation = baseUri + File.separator + 
				"test" + File.separator +"main" + File.separator + "resources" + File.separator;
		filePath = compositeLocation + "portal.composite";
		System.out.println(filePath);
		ca.analyze(filePath);
		
	}

}
