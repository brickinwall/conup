package cn.edu.nju.moon.conup.communication.services;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.data.TransactionRegistry;
import cn.edu.nju.moon.conup.data.TransactionRegistryImpl;

public class TestVcServiceImpl {
	private String hello = null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void test() {
		System.out.println("Before");
//		try {
////			System.in.read();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		System.out.println("After");
	}
	
	@Test
	public void testGetPArcs(){
		TransactionRegistry txRegistry;
		txRegistry = TransactionRegistryImpl.getInstance();
		
	}

}
