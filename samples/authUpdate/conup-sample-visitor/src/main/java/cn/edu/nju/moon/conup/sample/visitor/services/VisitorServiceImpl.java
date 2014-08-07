package cn.edu.nju.moon.conup.sample.visitor.services;

import java.util.Random;
import java.util.logging.Logger;

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.comm.api.remote.RemoteConfigTool;
import cn.edu.nju.moon.conup.sample.portal.services.PortalService;
import cn.edu.nju.moon.conup.sample.portal.services.TokenService;
import cn.edu.nju.moon.conup.sample.portal.services.VerificationService;
import cn.edu.nju.moon.conup.spi.datamodel.RemoteConfigContext;

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
				RemoteConfigTool rcs =  new RemoteConfigTool();
				String targetIdentifier = "AuthComponent";
				int port = 18082;
				String baseDir = "/home/nju/deploy/sample/update/old";
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
				RemoteConfigTool rcs =  new RemoteConfigTool();
				String targetIdentifier = "AuthComponent";
				int port = 18082;
				String baseDir = "/home/nju/deploy/sample/update";
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
	
}
