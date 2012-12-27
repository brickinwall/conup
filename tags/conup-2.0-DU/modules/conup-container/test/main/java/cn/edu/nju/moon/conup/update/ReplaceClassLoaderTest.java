package cn.edu.nju.moon.conup.update;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.update.ReplaceClassLoader;

public class ReplaceClassLoaderTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void test() {
		Class c = null;
		String baseDir = "/home/nju/classes";
		String classpath = "HelloWorld";
		try {
			ReplaceClassLoader cl = new ReplaceClassLoader(baseDir, new String[]{classpath});
			c = cl.loadClass(classpath);
			System.out.println("c: " + c);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
