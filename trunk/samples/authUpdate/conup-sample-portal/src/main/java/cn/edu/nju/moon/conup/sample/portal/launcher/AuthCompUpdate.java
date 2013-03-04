package cn.edu.nju.moon.conup.sample.portal.launcher;

import java.lang.reflect.Array;
import java.util.Arrays;

import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;

/**
 * 
 * @Author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class AuthCompUpdate {
//	public static String VER_ZERO = "VER_0";
//	public static String VER_ONE = "VER_1";
//	public static String VER_TWO = "VER_2";
	
	private static void testUpdateVersion2() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier = "AuthComponent";
				int port = 18082;
				String baseDir = "/home/artemis/Tuscany/deploy/auth2";
				String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
				String contributionUri = "conup-sample-auth";
				String compsiteUri = "auth.composite";
				rcs.update("10.0.2.15", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
			}
		});
		
		thread.start();
	}
	
	private static void testUpdateVersion3() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier = "AuthComponent";
				int port = 18082;
				String baseDir = "/home/artemis/Tuscany/deploy/auth3";
				String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
				String contributionUri = "conup-sample-auth";
				String compsiteUri = "auth.composite";
				rcs.update("10.0.2.15", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
			}
		});
		
		thread.start();
	}
	
	
	
	public static void updateAuthCompToVer(CompVersion toVer){
//		CompVersion targetVer = Enum.valueOf(CompVersion.class, toVer);
		switch (toVer) {
		case VER_ONE:
			testUpdateVersion2();
			break;
		case VER_TWO:
			testUpdateVersion3();
			break;
		default:
			System.out.println("Unsupported component verson for update");
			break;
		}
	}
	
	public static void update(String updateComp, String toVer){
		UpdatableComp targetComp = null;
		CompVersion targetVer = null;
		try{
			targetComp = Enum.valueOf(UpdatableComp.class, updateComp);
			targetVer = Enum.valueOf(CompVersion.class, toVer);
		} catch(Exception e){
			System.out.println("No such component for update or unsupported component.");
			return;
		}
		
		switch (targetComp) {
		case AuthComponent:
			updateAuthCompToVer(targetVer);
			break;
		default:
			System.out.println("No such component for update or unsupported component.");
			break;
		}
	}
	
	enum CompVersion{
		VER_ZERO,
		VER_ONE,
		VER_TWO
	}
	
	enum UpdatableComp{
		AuthComponent;
	}
}
