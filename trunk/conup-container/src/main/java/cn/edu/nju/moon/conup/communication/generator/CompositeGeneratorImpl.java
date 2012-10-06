package cn.edu.nju.moon.conup.communication.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import cn.edu.nju.moon.conup.communication.convention.CompositeConvention;

/**
 * It's used to generate a .composite file in directory ./src/main/resources
 * 
 * */
public class CompositeGeneratorImpl implements CompositeGenerator {
	private String businessCompositeLocation = null;
	private String commComponentName = null;
	private String compositeLocation = null;
	private String compositeName = null;
	private String compositeUri = null;
	private String ip = null;
	private String namePrefix = null;
	private int port ;
	private String serviceImpl = null;
	private Map<String, String> targetComponentsInfo = null;
//	private static int base = 9091;

	/**
	 * @param namePrefix
	 *            business component name
	 * @param serviceImpl
	 *            communication service's name with its package name
	 * */
	public CompositeGeneratorImpl(String namePrefix, String serviceImpl, String businessCompositeLocation) {
		this.namePrefix = namePrefix;
		this.serviceImpl = serviceImpl;
		this.businessCompositeLocation = businessCompositeLocation;
		commComponentName = this.namePrefix
				+ CompositeConvention.Component_Name_Suffix;
		compositeName = this.namePrefix
				+ CompositeConvention.Component_Name_Suffix
				+ CompositeConvention.CompositeExtension;
	}


	private String addReferences() {
		Map<String, String> buisnessReferenceInfo = getBuisnessRefInfo();
		Iterator iterator = buisnessReferenceInfo.entrySet().iterator();
		StringBuffer addedContent = new StringBuffer();
		
		while(iterator.hasNext()){
			Map.Entry<String, String> entry = (Entry<String, String>) iterator.next();
			String componentName = entry.getKey();
			String businessTargetIpAndPort = entry.getValue();
			String[] tmp = businessTargetIpAndPort.split(":");
			String ip = tmp[0];
			int port = Integer.parseInt(tmp[1]) + 1000;
			
			String lowerComponentName = componentName.substring(0, 1).toLowerCase()
					+ componentName.substring(1);
			addedContent.append("\t<reference name=\"" + lowerComponentName + "OndemandService\">\n");
			addedContent.append("\t\t<tuscany:binding.jsonrpc uri=\"");
			addedContent.append("http://" + ip + ":" + port + "/" + componentName + CompositeConvention.Component_Name_Suffix + "/" + "OndemandService" + "\"/>\n");
			addedContent.append("\t</reference>\n");
			
			addedContent.append("\t<reference name=\"" + lowerComponentName + "ArcService\">\n");
			addedContent.append("\t\t<tuscany:binding.jsonrpc uri=\"");
			addedContent.append("http://" + ip + ":" + port + "/" + componentName + CompositeConvention.Component_Name_Suffix + "/" + "ArcService" + "\"/>\n");
			addedContent.append("\t</reference>\n");
			
		}
		
		return addedContent.toString();
	}

	
	/* delete all .composite files in the given directory. */
	private void deleteCompositeFiles(String dir) {
		File[] files = new File(dir).listFiles();
		for (File file : files) {
			if (!file.isDirectory()
					&& file.getName().toLowerCase()
							.contains(CompositeConvention.CompositeExtension)) {
				file.delete();
			}
		}
	}

	/**
	 * generate composite file
	 * 
	 * @return composite file's location
	 * */
	public String generate() {
		setCommIpAndPort();
		StringBuffer buffer = new StringBuffer();
		buffer.append(CompositeConvention.XML_Declaration + "\n");
		buffer.append("<composite ");
		buffer.append(CompositeConvention.NS_XMLNS + " \n\t");
		buffer.append(CompositeConvention.NS_Tuscany + " \n\t");
		buffer.append(CompositeConvention.NS_SCA + " \n\t");
		buffer.append(CompositeConvention.NS_Target_Namespace + " \n\t");
		buffer.append("name=" + "\"" + compositeName + "\"");
		buffer.append(" >" + "\n");
		buffer.append("<component name=\"" + commComponentName + "\">" + "\n");
		buffer.append("\t<implementation.java class=\"" + serviceImpl + "\" />"
				+ "\n");
		buffer.append("\t<service name=\""
				+ CompositeConvention.Arc_Service_Name + "\">" + "\n");
		buffer.append("\t\t<tuscany:binding.jsonrpc uri=\"http://" + this.ip//getIP()
//				+ ":" + getPort() + "/" + commComponentName + "/"
				+ ":" + this.port + "/" + commComponentName + "/"
				+ CompositeConvention.Arc_Service_Name + "\"/>" + "\n");
		buffer.append("\t</service>" + "\n");

		buffer.append("\t<service name=\""
				+ CompositeConvention.Freeness_Service_Name + "\">" + "\n");
		buffer.append("\t\t<tuscany:binding.jsonrpc uri=\"http://" + this.ip//getIP()
//				+ ":" + getPort() + "/" + commComponentName + "/"
				+ ":" + this.port + "/" + commComponentName + "/"
				+ CompositeConvention.Freeness_Service_Name + "\"/>" + "\n");
		buffer.append("\t</service>" + "\n");

		buffer.append("\t<service name=\"" + CompositeConvention.ONDEMAND_SERVICE + "\">" + "\n");
		buffer.append("\t\t<tuscany:binding.jsonrpc uri=\"http://" + this.ip + ":" + this.port + "/" + //getIP() + ":" + getPort() + "/" +
				commComponentName + "/" + CompositeConvention.ONDEMAND_SERVICE + "\"/>" + "\n");
		buffer.append("\t</service>" + "\n");
		
		buffer.append("\t<service name=\"" + CompositeConvention.UPDATE_SERVICE + "\">" + "\n");
		buffer.append("\t\t<tuscany:binding.jsonrpc uri=\"http://" + this.ip + ":" + this.port + "/" + //getIP() + ":" + getPort() + "/" +
				commComponentName + "/" + CompositeConvention.UPDATE_SERVICE + "\"/>" + "\n");
		buffer.append("\t</service>" + "\n");
		
		buffer.append("\t<service name=\"" + CompositeConvention.COMPONENT_CONF_SERVICE + "\">" + "\n");
		buffer.append("\t\t<tuscany:binding.jsonrpc uri=\"http://" + this.ip + ":" + this.port + "/" + //getIP() + ":" + getPort() + "/" +
				commComponentName + "/" + CompositeConvention.COMPONENT_CONF_SERVICE + "\"/>" + "\n");
		buffer.append("\t</service>" + "\n");
		
		//add reference
		buffer.append(addReferences());
		
		buffer.append("</component>" + "\n");
		buffer.append("</composite>" + "\n");

		String baseUri = new File("").getAbsolutePath();
		baseUri = baseUri.substring(0, baseUri.lastIndexOf(File.separator))
				+ File.separator + "conup-container";
		// compositeLocation = baseUri + File.separator + "src" + File.separator
		// +"main" + File.separator + "resources" + File.separator;
		compositeLocation = baseUri + File.separator + "target" + File.separator
				+ "classes" + File.separator;
		compositeUri = compositeLocation + compositeName;

		// deleteCompositeFiles(compositeLocation);
		File file;
			file = new File(compositeUri);
		try {
			if(file.exists()){
				file.delete();
			}
			file.createNewFile();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(buffer.toString());
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return compositeUri;
	}

	/**
	 * get target component's binding uri info
	 * @return
	 */
	private Map<String,String> getBuisnessRefInfo(){
		
//		Map<String, String> refs = new ConcurrentHashMap<String, String>();
		
		targetComponentsInfo = new ConcurrentHashMap<String, String>();
		try {
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(businessCompositeLocation);
			Element root = doc.getRootElement();
			Namespace ns = Namespace.getNamespace("http://docs.oasis-open.org/ns/opencsa/sca/200912");
			Element component = root.getChild("component", ns);
			List references = component.getChildren("reference", ns);
			Iterator iterator = references.iterator();
			while (iterator.hasNext()) {
				Element reference = (Element) iterator.next();
				List binding = reference.getChildren();
				Iterator bindingIterator = binding.iterator();
				String bindingUri = null;
				while (bindingIterator.hasNext()) {
					Element specificBinding = (Element) bindingIterator.next();
					bindingUri = specificBinding.getAttributeValue("uri");
					break;
				}
				String ipAndPort = processBindingUri(bindingUri);
				String targetComponent = getTargetFromBinding(bindingUri);
//				refs.put(targetComponent, ipAndPort);
				targetComponentsInfo.put(targetComponent, ipAndPort);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		return refs;
		return targetComponentsInfo;
	}
	
//	private String getIpAndPort(String componentName, String serviceName){
//		String baseUri = new File("").getAbsolutePath();
//		baseUri = baseUri.substring(0, baseUri.lastIndexOf(File.separator))
//				+ File.separator + "vc-communication";
//		compositeLocation = baseUri + File.separator + "target" + File.separator
//				+ "classes" + File.separator;
//		compositeUri = compositeLocation + componentName + CompositeConvention.Component_Name_Suffix
//				+ CompositeConvention.CompositeExtension;
//		try {
//			SAXBuilder sb = new SAXBuilder();
//			Document doc = sb.build(compositeUri);
//			Element root = doc.getRootElement();
//			Namespace ns = Namespace.getNamespace("http://docs.oasis-open.org/ns/opencsa/sca/200912");
//			Element component = root.getChild("component", ns);
//			List services = component.getChildren("service", ns);
//			Iterator iterator = services.iterator();
//			while (iterator.hasNext()) {
//				Element service = (Element) iterator.next();
//				String name = service.getAttributeValue("name");
//				if(name.equals(serviceName)){
//					List binding = service.getChildren();
//					Iterator bindingIterator = binding.iterator();
//					while (bindingIterator.hasNext()) {
//						Element specificBinding = (Element) bindingIterator.next();
//						String bindingUri = specificBinding.getAttributeValue("uri");
//						return bindingUri;
//					}
//					
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
	@Override
	public String getCompositeName() {
		return compositeName;
	}

	public Map<String, String> getTargetComponentsInfo() {
		return targetComponentsInfo;
	}

	
	private String getTargetFromBinding(String bindingUri) {
		int lastIndex = bindingUri.lastIndexOf("/");
		String subStr = bindingUri.substring(0, lastIndex);
		lastIndex = subStr.lastIndexOf("/");
		String targetComponentName = subStr.substring(lastIndex + 1, subStr.length());
		return targetComponentName;
	}
	
	private String processBindingUri(String bindingUri){
		int index = bindingUri.indexOf("http://");
		String subStr = bindingUri.substring(index + 7);
		int slashIndex = subStr.indexOf("/");
		String ipAndPort = subStr.substring(0, slashIndex);
		return ipAndPort;									//xx.xx.xx.xx:xx
	}
	
	
	/* get IP address of the host machine */
//	private static String getIP() {
//		return "10.0.2.15";
//		// String ip = null;
//		// try {
//		// InetAddress address = null;
//		// Enumeration<NetworkInterface> networkInterfaces;
//		// networkInterfaces = NetworkInterface.getNetworkInterfaces();
//		// while(networkInterfaces.hasMoreElements()){
//		// NetworkInterface tmp =
//		// (NetworkInterface)networkInterfaces.nextElement();
//		// address = (InetAddress)tmp.getInetAddresses().nextElement();
//		// if(!address.isSiteLocalAddress() && !address.isLoopbackAddress() &&
//		// address.getHostAddress().indexOf(":")==-1)
//		// return address.getHostAddress();
//		// else
//		// address = null;
//		// }
//		// } catch (SocketException e) {
//		// e.printStackTrace();
//		// }
//		// return ip;
//	}

//	private int getPort() {
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}
//		int base = 9090;
//		int up = 65535;
//		int port = 0;
//		ServerSocket s = null;
//		for (int i = base; i < up; i++) {
//			try {
//				s = new ServerSocket(i);
//				if (s.isBound()) {
//					port = i;
////					base++;
//				}
//			} catch (IOException e) {
//				// e.printStackTrace();
//			} finally {
//				if (s != null) {
//					try {
//						s.close();
//					} catch (IOException e) {
//					}
//				}
//			}// finally
//			if (port != 0)
//				break;
//		}
//
//		// for(int i=port; i< 65535; i++){
//		// socket = new Socket();
//		// port = socket.getPort();
//		// try {
//		// socket.close();
//		// } catch (IOException e) {
//		// e.printStackTrace();
//		// }
//		// }
//
//		return port;
//	}

	private void setCommIpAndPort(){
		try {
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(businessCompositeLocation);
			Element root = doc.getRootElement();
			Namespace ns = Namespace.getNamespace("http://docs.oasis-open.org/ns/opencsa/sca/200912");
			Element component = root.getChild("component", ns);
			List services = component.getChildren("service", ns);
			Iterator iterator = services.iterator();
			while (iterator.hasNext()) {
				Element service = (Element) iterator.next();
				// String name = service.getAttributeValue("name");
				// if(name.equals(serviceName)){
				List binding = service.getChildren();
				Iterator bindingIterator = binding.iterator();
				String bindingUri = null;
				while (bindingIterator.hasNext()) {
					Element specificBinding = (Element) bindingIterator.next();
					bindingUri = specificBinding.getAttributeValue("uri");
					break;
				}
				String ipAndPort = processBindingUri(bindingUri);
				String[] tmp = ipAndPort.split(":");
				this.ip = tmp[0];
				this.port = Integer.parseInt(tmp[1]) + 1000;
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

}
