package cn.edu.nju.moon.conup.spi.datamodel;

/**
 * Protocols that are used to exchange messages between distributed components.
 * Temporally, CommProtocols are limited to the following:
 * <ul>
 * 		<li>NEGOTIATION_PROTOCOL is used to exchange configuration between components
 * 		<li>QUIESCENCE_PROTOCOL is used to exchange quiescence algorithm related information
 * 		<li>TRANQUILLITY_PROTOCOL is used to exchange tranquillity algorithm related information
 * 		<li>VERSION_CONSISTENCY_PROTOCOL is used to exchange consistency algorithm related information
 * </ul>
 * 
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class CommProtocol {
	public static final String NEGOTIATION = "NEGOTIATION_PROTOCOL";
	public static final String QUIESCENCE = "QUIESCENCE_PROTOCOL";
	public static final String TRANQUILLITY = "TRANQUILLITY_PROTOCOL";
	public static final String CONSISTENCY = "VERSION_CONSISTENCY_PROTOCOL";
}
