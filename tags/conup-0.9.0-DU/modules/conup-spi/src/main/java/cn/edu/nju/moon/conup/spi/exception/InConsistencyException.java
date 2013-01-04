package cn.edu.nju.moon.conup.spi.exception;

/**
 * This class was used in test three algorithm's correctness
 * @author rgc
 * @version Dec 11, 2012 4:55:41 PM
 */
public class InConsistencyException extends RuntimeException {

	public InConsistencyException() {

	}

	public InConsistencyException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;

}
