package cn.edu.nju.moon.conup.sample.visitor.services;

import cn.edu.nju.moon.conup.sample.portal.services.VerificationService;

/**
 * @author rgc
 * @version Dec 20, 2012 8:36:40 PM
 */
public class VerifyVisitor extends Thread {

	private VerificationService verify = null;
	
	public VerifyVisitor(VerificationService verify){
		this.verify = verify;
	}
	
	public void run(){
		verify.verify("", "nju,cs,pass");
	}
}
