package cn.edu.nju.moon.conup.spi.exception;

public class ConupMgrNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ConupMgrNotFoundException(String message){
		super(message);
	}
	
	public ConupMgrNotFoundException(){
		
	}
}
