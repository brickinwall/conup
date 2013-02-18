package cn.edu.nju.moon.conup.sample.visitor.services;

import java.util.logging.Logger;

import cn.edu.nju.moon.conup.sample.portal.services.TokenService;

/**
 * @author rgc
 * @version Dec 20, 2012 8:34:27 PM
 */
public class TokenVisitor extends Thread {
	private static Logger LOGGER = Logger.getLogger(TokenVisitor.class.getName());

	private TokenService tokenService = null;
	
	public TokenVisitor(TokenService tokenService){
		this.tokenService = tokenService;
	}
	
	public void run(){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LOGGER.fine(tokenService.getToken("", "nju,cs"));
	}
}
