package cn.edu.nju.moon.conup.spi.exception;

/**
 * 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * @version Created time: Jul 28, 2013 11:04:52 PM
 */
public class ConupException extends RuntimeException {
	public ConupException() {

	}

	public ConupException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;
}
