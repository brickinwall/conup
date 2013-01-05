package cn.edu.nju.moon.conup.remote.services;

import java.util.List;
import java.util.Set;

/**
 * A interface for querying component status
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface ComponentConfService {
	/** 
	 * @return components names that hosted by the node
	 * 
	 * */
	public List<String> getComponentNames();
	
	/** 
	 *  component's current status
	 * 	@param compIdentifier component object identifier
	 *  
	 *  */
	public String getCurrentStatus(String compIdentifier);
	
	/** 
	 *  component's default status
	 *  @param compIdentifier component object identifier
	 * */
	public String getDefaultStatus(String compIdentifier);
	
	/** 
	 *  component's freenessSetup strategy 
	 *  @param compIdentifier component object identifier
	 * */
	public String getFreenessSetup(String compIdentifier);
	
	/** 
	 *  all statuses defined for the component 
	 *  @param compIdentifier component object identifier
	 * */
	public List<String> getAllStatuses(String compIdentifier);
	
	/** 
	 *  ForTest: started composite in the current node 
	 * */
	public Set<String> getStartedCompositeUri();
}
