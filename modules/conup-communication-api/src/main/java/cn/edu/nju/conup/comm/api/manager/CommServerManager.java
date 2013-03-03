package cn.edu.nju.conup.comm.api.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.core.assembly.impl.RuntimeEndpointReferenceImpl;
import org.apache.tuscany.sca.impl.NodeImpl;
import org.apache.tuscany.sca.runtime.DomainRegistry;

import cn.edu.nju.moon.conup.comm.api.server.CommServer;
import cn.edu.nju.moon.conup.comm.api.utils.CompCommAddress;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;

/**
 * @author rgc
 * @version Nov 29, 2012 3:55:15 PM
 */
public class CommServerManager {

	private static CommServerManager csm = new CommServerManager();

	/*
	 * key is component identifier value is ip:port
	 */
	private static Map<String, String> compAddresses = new ConcurrentHashMap<String, String>();

	/*
	 * key is ip:port value is key's corresponding CompCommServer
	 */
	private static Map<String, CommServer> commServers = new ConcurrentHashMap<String, CommServer>();

	private CommServerManager() {

	}

	public static CommServerManager getInstance() {
		return csm;
	}

	public CompCommInfo getCommAddress(String componentIdentifier) {
		String address = compAddresses.get(componentIdentifier);
		if(address == null){
			return null;
		}
		
		return new CompCommInfo(componentIdentifier, address.split(":")[0],
				Integer.parseInt(address.split(":")[1]));
	}
	
	

	public CommServer getCommServer(String compIdentifier) {
		String address = compAddresses.get(compIdentifier);
		if(address == null){
			return null;
		}
		String ip =  address.split(":")[0];
		int port = Integer.parseInt(address.split(":")[1]);
		return commServers.get(ip + ":" + port);
	}

	/**
	 * 
	 * @param compIdentifier
	 * @param ip
	 * @param port
	 * @return
	 */
	public boolean start(String compIdentifier) {
		CompCommAddress cca = getInfos(compIdentifier);
		String ip = cca.getIp();
		int port = cca.getPort();
		if (!compAddresses.containsKey(compIdentifier)) {
			compAddresses.put(compIdentifier, ip + ":" + port);
			if (!commServers.containsKey(ip + ":" + port)) {
				CommServer cs = new CommServer();
				commServers.put(ip + ":" + port, cs);
			} else{
				return true;
			}
			return commServers.get(ip + ":" + port).start(ip, port);
		}

		return true;
	}

	/**
	 * 
	 * @param componentIdentifier
	 * @return
	 */
	public boolean stop(String componentIdentifier){
		String address = compAddresses.get(componentIdentifier);
		
		boolean result ;
		compAddresses.remove(componentIdentifier);
		if(compAddresses.containsValue(address)){
			return true;
		} else{
			result = commServers.get(address).stop();
			commServers.remove(address);
		}
		return result;
	}
	
	/**
	 * according the targetCompIdentifier, we get the target component's ip
	 * address and port
	 * 
	 * @param targetCompIdentifier
	 * @return target component's ip address and port: "ip:10.0.2.15,port:18080";
	 */
	public CompCommAddress getInfos(String srcComponentIdentifier, String targetCompIdentifier) {
		CompLifecycleManager compLifecycleMgr = CompLifecycleManager.getInstance(srcComponentIdentifier);

		NodeImpl node = (NodeImpl) compLifecycleMgr.getNode();
		DomainRegistry domainRegistry = node.getDomainRegistry();
		Collection<Endpoint> endpoints = domainRegistry.getEndpoints();

		String ip = null;
		int port = 0;
		for (Endpoint ep : endpoints) {
			if (ep.getComponent().getName().equals(targetCompIdentifier)) {
				String deployURI = ep.getDeployedURI();
				if (deployURI.startsWith("http://")) {
					// ex:deployedUri=http://10.0.2.15:8081/DBComponent/DBService/
					String[] infos = deployURI.split(":");
					ip = infos[1].substring(2);
					port = Integer.parseInt(infos[2].substring(0, infos[2].indexOf("/"))) + 10000;
					break;
				} else {

				}
			}
		}
		CompCommAddress cci = new CompCommAddress(targetCompIdentifier, ip, port);
		return cci;
	}
	
	/**
	 * when start component's communication server
	 * we need to get its ip and port to start it, then this method will be invoked.
	 * @param hostComponentIdentifier
	 * @return
	 */
	public CompCommAddress getInfos(String hostComponentIdentifier) {
		CompLifecycleManager compLifecycleMgr = CompLifecycleManager.getInstance(hostComponentIdentifier);
		
		NodeImpl node = (NodeImpl) compLifecycleMgr.getNode();
		DomainRegistry domainRegistry = node.getDomainRegistry();
		Collection<Endpoint> endpoints = domainRegistry.getEndpoints();
		
		String ip = null;
		int port = 0;
		for (Endpoint ep : endpoints) {
			List<String> compRefName = new ArrayList<String>();
			//TODO confused by the following codes.
			for(ComponentReference ref : ep.getComponent().getReferences()){
				compRefName.add(ref.getName());
			}
			String epServiceName = ep.getService().getName();
			if (ep.getComponent().getName().equals(hostComponentIdentifier) 
				&& !compRefName.contains(epServiceName)) {
				String deployURI = ep.getDeployedURI();
				if (deployURI.startsWith("http://")) {
					// ex:deployedUri=http://10.0.2.15:8081/DBComponent/DBService/
					String[] infos = deployURI.split(":");
					ip = infos[1].substring(2);
					port = Integer.parseInt(infos[2].substring(0, infos[2].indexOf("/"))) + 10000;
					break;
				} else {

				}
			}
		}
		
		CompCommAddress compCommInfo = new CompCommAddress(hostComponentIdentifier, ip, port);
		return compCommInfo;
	}
	
}
