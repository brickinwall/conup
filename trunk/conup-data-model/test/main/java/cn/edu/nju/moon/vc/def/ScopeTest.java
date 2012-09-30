package cn.edu.nju.moon.vc.def;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class ScopeTest {
	private static Scope scope = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		scope = new Scope();
		String graphName = "graph.xml";
		String baseUri = new File("").getAbsolutePath();
		baseUri = baseUri.substring(0, baseUri.lastIndexOf(File.separator))
				+ File.separator + "vc-domain-manager";
		String graphLocation = baseUri + File.separator + "src"
				+ File.separator + "main" + File.separator + "resources"
				+ File.separator;
		String graphUri = graphLocation + graphName;
		SAXBuilder sb = new SAXBuilder();
		Document doc = sb.build(graphUri);
		Element root = doc.getRootElement();
		scope.setScope(root);
	}

	@Test
	public void testGetParentComponents() {
		Set<String> parentComponents = scope.getParentComponents("AuthComponent");
		System.out.println(parentComponents);
	}

	@Test
	public void testGetSubComponents() {
		Set<String> subComponents = scope.getSubComponents("PortalComponent");
		System.out.println(subComponents);
	}

	@Test
	public void testContains() {
		System.out.println(scope.contains("PortalComponent"));
	}

	@Test
	public void testGetAllComponents() {
		Set<String> allComponents = scope.getAllComponents();
		System.out.println(allComponents);
	}

}
