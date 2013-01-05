package cn.edu.nju.moon.conup.spi.exception;

/**
 * An exception represents illegal component status.
 * For example, when a component is in the process of updating, some objects
 * try to change the component status to Valid, an IllegalCompStatusException
 * will be thrown.
 * 
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class IllegalCompStatusException extends RuntimeException {

	private static final long serialVersionUID = 1L;

}
