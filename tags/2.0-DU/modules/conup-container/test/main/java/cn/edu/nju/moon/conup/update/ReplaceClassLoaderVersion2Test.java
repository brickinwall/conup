package cn.edu.nju.moon.conup.update;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.update.ReplaceClassLoaderVersion2;

public class ReplaceClassLoaderVersion2Test {

	static ReplaceClassLoaderVersion2 rc = null;
	static String basedir = "/home/nju/classes";
	static String classns = "HelloWorld";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		rc = new ReplaceClassLoaderVersion2(basedir, classns);
	}
	
	public static void main(String[] args) {
		Class c = null;
		try {
			rc = new ReplaceClassLoaderVersion2(basedir, classns);
			c = rc.loadClass(classns);
			System.out.println("c: " + c);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() {
		Class c = null;
//		String baseDir = "/home/nju/classes";
//		String classpath = "HelloWorld";
		try {
//			ReplaceClassLoader cl = new ReplaceClassLoader(baseDir, new String[]{classpath});
			c = rc.loadClass(classns);
			Object obj = c.newInstance();
			Method method = c.getMethod("hello", new Class[]{String.class});
			
			System.out.println("hello(nju,cs):  " + method.invoke(obj, "rgc"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 
	}

}
