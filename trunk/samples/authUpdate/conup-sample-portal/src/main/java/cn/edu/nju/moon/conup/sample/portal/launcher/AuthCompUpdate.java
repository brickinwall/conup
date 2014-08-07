package cn.edu.nju.moon.conup.sample.portal.launcher;

import java.util.HashSet;
import java.util.Set;

import cn.edu.nju.moon.conup.comm.api.remote.RemoteConfigTool;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.RemoteConfigContext;

/**
 * 
 * @Author JiangWang<jiang.wang88@gmail.com>
 * 
 */
public class AuthCompUpdate {
	// public static String VER_ZERO = "VER_0";
	// public static String VER_ONE = "VER_1";
	// public static String VER_TWO = "VER_2";

	private static void testUpdateVersion2() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				RemoteConfigTool rcs = new RemoteConfigTool();
				String targetIdentifier = "AuthComponent";
				int port = 18082;
				String baseDir = "/home/artemis/Tuscany/deploy/auth2";
				String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
				String contributionUri = "conup-sample-auth";
				String compsiteUri = "auth.composite";
				String ip = "172.16.154.128";
				String protocol = "CONSISTENCY";
				RemoteConfigContext rcc = new RemoteConfigContext(ip, port, targetIdentifier, protocol, baseDir, classFilePath, contributionUri, null, compsiteUri);
				rcs.update(rcc);
			}
		});

		thread.start();
	}


	private static void testUpdateVersion2WithScope() {
		final Scope scope = new Scope();
		Set<String> parentComps = new HashSet<String>();
		Set<String> subComps = new HashSet<String>();
		Set<String> targetComps = new HashSet<String>();
		// auth component
		parentComps.add("ProcComponent");
		scope.addComponent("AuthComponent", parentComps, subComps);
		// proc component
		parentComps.clear();
		subComps.clear();
		subComps.add("AuthComponent");
		scope.addComponent("ProcComponent", parentComps, subComps);
		// target component
		targetComps.add("AuthComponent");
		scope.setTarget(targetComps);
		scope.setSpecifiedScope(true);

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				RemoteConfigTool rcs = new RemoteConfigTool();
				String targetIdentifier = "AuthComponent";
				int port = 18082;
				String baseDir = "/home/artemis/Tuscany/deploy/auth2";
				String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
				String contributionUri = "conup-sample-auth";
				String compsiteUri = "auth.composite";
				String ip = "172.16.154.128";
				String protocol = "CONSISTENCY";
				RemoteConfigContext rcc = new RemoteConfigContext(ip, port, targetIdentifier, protocol, baseDir, classFilePath, contributionUri, null, compsiteUri);
				rcs.update(rcc);

			}
		});

		thread.start();
	}

	private static void testUpdateVersion3WithScope() {
		final Scope scope = new Scope();
		Set<String> parentComps = new HashSet<String>();
		Set<String> subComps = new HashSet<String>();
		Set<String> targetComps = new HashSet<String>();
		// auth component
		parentComps.add("ProcComponent");
		scope.addComponent("AuthComponent", parentComps, subComps);
		// proc component
		parentComps.clear();
		subComps.clear();
		subComps.add("AuthComponent");
		scope.addComponent("ProcComponent", parentComps, subComps);
		// target component
		targetComps.add("AuthComponent");
		scope.setTarget(targetComps);
		scope.setSpecifiedScope(true);

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				RemoteConfigTool rcs = new RemoteConfigTool();
				String targetIdentifier = "AuthComponent";
				int port = 18082;
				String baseDir = "/home/artemis/Tuscany/deploy/auth3";
				String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
				String contributionUri = "conup-sample-auth";
				String compsiteUri = "auth.composite";
				
				String ip = "172.16.154.128";
				String protocol = "CONSISTENCY";
				RemoteConfigContext rcc = new RemoteConfigContext(ip, port, targetIdentifier, protocol, baseDir, classFilePath, contributionUri, null, compsiteUri);
				rcs.update(rcc);

			}
		});

		thread.start();
	}
	
	private static void testUpdateVersion3() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				RemoteConfigTool rcs = new RemoteConfigTool();
				String targetIdentifier = "AuthComponent";
				int port = 18082;
				String baseDir = "/home/artemis/Tuscany/deploy/auth3";
				String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
				String contributionUri = "conup-sample-auth";
				String compsiteUri = "auth.composite";
				
				String ip = "172.16.154.128";
				String protocol = "CONSISTENCY";
				RemoteConfigContext rcc = new RemoteConfigContext(ip, port, targetIdentifier, protocol, baseDir, classFilePath, contributionUri, null, compsiteUri);
				rcs.update(rcc);
			}
		});

		thread.start();
	}

	public static void updateAuthCompToVer(CompVersion toVer, boolean scopeFlag) {
		// CompVersion targetVer = Enum.valueOf(CompVersion.class, toVer);
		if(!scopeFlag){
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
		} else {
			switch (toVer) {
			case VER_ONE:
				testUpdateVersion2WithScope();
				break;
			case VER_TWO:
				testUpdateVersion3WithScope();
				break;
			default:
				System.out.println("Unsupported component verson for update");
				break;
			}
		}
	}

	public static void update(String updateComp, String toVer, boolean scopeFlag) {
		UpdatableComp targetComp = null;
		CompVersion targetVer = null;
		try {
			targetComp = Enum.valueOf(UpdatableComp.class, updateComp);
			targetVer = Enum.valueOf(CompVersion.class, toVer);
		} catch (Exception e) {
			System.out
					.println("No such component for update or unsupported component.");
			return;
		}

		switch (targetComp) {
		case AuthComponent:
			updateAuthCompToVer(targetVer, scopeFlag);
			break;
		default:
			System.out
					.println("No such component for update or unsupported component.");
			break;
		}
	}

	public static void ondemand(String updateComp) {
		UpdatableComp targetComp = null;
		try {
			targetComp = Enum.valueOf(UpdatableComp.class, updateComp);
		} catch (Exception e) {
			System.out
					.println("No such component for update or unsupported component.");
			return;
		}

		switch (targetComp) {
		case AuthComponent:
			sendOndemandToAuthComp();
			break;
		default:
			System.out
					.println("No such component for update or unsupported component.");
			break;
		}
	}

	private static void sendOndemandToAuthComp() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				RemoteConfigTool rcs = new RemoteConfigTool();
				String targetIdentifier = "AuthComponent";
				int port = 18082;

				rcs.ondemand("172.16.154.128", port, targetIdentifier, "CONSISTENCY", null);
			}
		});

		thread.start();
	}

	enum CompVersion {
		VER_ZERO, VER_ONE, VER_TWO
	}

	enum UpdatableComp {
		AuthComponent;
	}
}
