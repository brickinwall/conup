package cn.edu.nju.moon.conup.sample.visitor.services;

import cn.edu.nju.moon.conup.sample.portal.services.TokenService;

/**
 * @author rgc
 * @version Dec 20, 2012 8:34:27 PM
 */
public class TokenVisitor extends Thread {

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
		System.out.println(tokenService.getToken("", "nju,cs"));
	}
}
