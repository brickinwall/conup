package cn.edu.nju.moon.conup.spi.exception;

public class ConupException extends RuntimeException {
	public ConupException() {

	}

	public ConupException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;
}
