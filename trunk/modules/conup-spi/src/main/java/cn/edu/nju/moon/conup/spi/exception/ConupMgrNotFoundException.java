package cn.edu.nju.moon.conup.spi.exception;

/**
 * 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * @version Created time: Jul 28, 2013 11:05:00 PM
 */
public class ConupMgrNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ConupMgrNotFoundException(String message){
		super(message);
	}
	
	public ConupMgrNotFoundException(){
		
	}
}
