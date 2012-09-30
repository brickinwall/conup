package cn.edu.nju.moon.conup.communication.convention;

public class CompositeConvention {
	
	/** .composite file's extended name */
	public static String CompositeExtension = ".composite";
	
	/** In one domain, an Endpoint's identifier must be unique, which is composed of component name and service name.
	 * 	In our impl, we assume all communication component with same service name, i.e.,VcService, 
	 * 	it means the communication component name must be unique.
	 * 	A communication name is  composed of prefix and suffix, the prefix is same as business component name, 
	 * 	and the following constant is suffix. 
	 *  */
	public static String Component_Name_Suffix = "Comm";
	
	/** XML declaration, generally is a XML document's first line */
	public static String XML_Declaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	/** name space: xmlns */
	public static String NS_XMLNS = "xmlns=\"http://docs.oasis-open.org/ns/opencsa/sca/200912\"";
	
	/** name space: tuscany */
	public static String NS_Tuscany = "xmlns:tuscany=\"http://tuscany.apache.org/xmlns/sca/1.1\"";
	
	/** name space: sca */
	public static String NS_SCA = "xmlns:sca=\"http://docs.oasis-open.org/ns/opencsa/sca/200912\"";
	
	/** name space: target namespace */
	public static String NS_Target_Namespace = "targetNamespace=\"http://" + "version-consistency\"";
	
	public static String Arc_Service_Name = "ArcService";
	
//	public static String Arc_Service_Name = "VcService";
	
	public static String Freeness_Service_Name = "FreenessService";
	
	public static String ONDEMAND_SERVICE = "OndemandService";
	
	public static String UPDATE_SERVICE = "ComponentUpdateService";

}
