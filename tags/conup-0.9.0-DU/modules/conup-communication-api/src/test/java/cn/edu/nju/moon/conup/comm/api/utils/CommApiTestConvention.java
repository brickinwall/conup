package cn.edu.nju.moon.conup.comm.api.utils;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class CommApiTestConvention {
	/** Implementation type for the component */
	public final static String JAVA_POJO_IMPL_TYPE = "JAVA_POJO";
	
	/** represent quiescence algorithm */
	public final static String QUIESCENCE_ALGORITHM = "QUIESCENCE_ALGORITHM";
	
	/** represent tranquillity algorithm */
	public final static String TRANQUILLITY_ALGORITHM = "TRANQUILLITY_ALGORITHM";
	
	/** represent version-consistency algorithm */
	public final static String CONSISTENCY_ALGORITHM = "CONSISTENCY_ALGORITHM";
	
	/** represent blocking strategy */
	public final static String BLOCKING = "BLOCKING_FOR_FREENESS";
	
	/** represent waiting strategy */
	public final static String WAITING = "WAITING_FOR__FREENESS";
	
	/** represent concurrent version strategy */
	public final static String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
	
	/** portal component identifier */
	public final static String PORTAL_COMP = "PortalComponent";
	
	/** process component identifier */
	public final static String PROC_COMP = "ProcComponent";
	
	/** auth component identifier */
	public final static String AUTH_COMP = "AuthComponent";
	
	/** database component identifier */
	public final static String DB_COMP = "DBComponent";
	
	public final static String OLD_VERSION = "OLD_VERSION";
	
	public final static String NEW_VERSION = "NEW_VERSION";
	/** the return value of old version conup-sample-hello-auth */
	public final static String OLD_VERSION_HELLO_AUTH_TOKEN_RESULT = "nju,cs,pass,OLD_VERSION";
	/** the return value of new version conup-sample-hello-auth */
	public final static String NEW_VERSION_HELLO_AUTH_TOKEN_RESULT = "nju,cs,pass,NEW_VERSION";
}
