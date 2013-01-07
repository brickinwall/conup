package cn.edu.nju.moon.conup.sample.visitor.services;

import java.util.Random;
import java.util.logging.Logger;

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.sample.portal.services.PortalService;
import cn.edu.nju.moon.conup.sample.portal.services.TokenService;
import cn.edu.nju.moon.conup.sample.portal.services.VerificationService;

@Service(VisitorService.class)
public class VisitorServiceImpl implements VisitorService {
	private PortalService portalService;
	private TokenService tokenService;
	private VerificationService verficationService;
	
	public TokenService getTokenService() {
		return tokenService;
	}
	@Reference
	public void setTokenService(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	public VerificationService getVerficationService() {
		return verficationService;
	}
	@Reference
	public void setVerficationService(VerificationService verficationService) {
		this.verficationService = verficationService;
	}

	
//	private ComponentUpdateService componentUpdateService;
//
//	public ComponentUpdateService getComponentUpdateService() {
//		return componentUpdateService;
//	}
//	
//	@Reference
//	public void setComponentUpdateService(
//			ComponentUpdateService componentUpdateService) {
//		this.componentUpdateService = componentUpdateService;
//	}

	public PortalService getPortalService() {
		return portalService;
	}

	@Reference
	public void setPortalService(PortalService portalService) {
		this.portalService = portalService;
	}

	Logger logger = Logger.getLogger(VisitorServiceImpl.class.getName());

	public void visitPortal(int n) {

		for (int i = 0; i < n; i++) {
			
			new PortalVisitor(portalService).start();
			
			Random random;
			random = new Random(System.currentTimeMillis());
			try {
				Thread.sleep(Math.abs(random.nextInt())%10 * 60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void testUpdateToOld() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier = "AuthComponent";
				int port = 18082;
				String baseDir = "/home/nju/deploy/sample/update/old";
				String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
				String contributionUri = "conup-sample-auth";
				String compsiteUri = "auth.composite";
				rcs.update("114.212.84.83", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
			}
		});
		
		thread.start();
	}
	@Override
	public void visitToken(int n) {
		for (int i = 0; i < n; i++) {
			new TokenVisitor(tokenService).start();

			Random random;
			random = new Random(System.currentTimeMillis());
			try {
				Thread.sleep(Math.abs(random.nextInt())%10 * 60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void visitVerify(int n) {
		for (int i = 0; i < n; i++) {
			new VerifyVisitor(verficationService).start();

			Random random;
			random = new Random(System.currentTimeMillis());
			try {
				Thread.sleep(Math.abs(random.nextInt())%10 * 60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void testUpdate() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier = "AuthComponent";
				int port = 18082;
				String baseDir = "/home/nju/deploy/sample/update";
				String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
				String contributionUri = "conup-sample-auth";
				String compsiteUri = "auth.composite";
				rcs.update("114.212.84.83", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
			}
		});
		
		thread.start();
	}
	
//	try {
//		Thread.sleep(1000);
//		new TokenVisitor(tokenService).start();
////		new VerifyVisitor(verficationService).start();
//		
//		Thread.sleep(200);
//	} catch (InterruptedException e) {
//		e.printStackTrace();
//	}
	
	// private class ContinuousVisitor extends Thread {
	//
	// // Logger logger = Logger.getLogger(ContinuousVisitor.class.getName());
	//
	// public ContinuousVisitor() {
	//
	// }
	//
	// public void run() {
	// // try {
	// // for(int i = 0; i < 4; i++){
	// // new ContinuousVisitor().start();
	//
	// logger.info("\t" + "" + portalService.execute("nju", "cs"));
	// // Thread.sleep(3500);
	// // }
	//
	// // while(true){
	// // // PortalService portalService =
	// // node.getService(PortalService.class,
	// // //
	// // "PortalComponent#service-binding(PortalService/PortalService)");
	// // new ContinuousVisitor().start();
	// // logger.info("\t" + "" + portalService.execute("nju", "cs"));
	// // Thread.sleep(3500);
	// // }
	// // } catch (InterruptedException e) {
	// // e.printStackTrace();
	// // }
	// }// END RUN()
	//
	// }

}
