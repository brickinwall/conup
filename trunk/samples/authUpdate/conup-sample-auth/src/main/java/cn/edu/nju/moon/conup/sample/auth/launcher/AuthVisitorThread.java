package cn.edu.nju.moon.conup.sample.auth.launcher;

import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.sample.auth.services.TokenService;


/**
 * @author rgc
 * @version Dec 20, 2012 2:40:00 PM
 */
public class AuthVisitorThread extends Thread {
	
	private Node node;
	private Logger LOGGER = Logger.getLogger(AuthVisitorThread.class.getName());
	public AuthVisitorThread(Node node){
		this.node = node;
	}
	
	public void run(){
		try {
			TokenService tokenService = node.getService(TokenService.class, "AuthComponent/TokenService");
			String execStr = "";
			LOGGER.info("\t" + "" + tokenService.getToken(execStr, "nju,cs"));
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}
}
