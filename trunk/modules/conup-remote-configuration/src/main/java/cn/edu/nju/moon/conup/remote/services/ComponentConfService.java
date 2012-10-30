package cn.edu.nju.moon.conup.remote.services;

import java.util.List;
import java.util.Set;

/**
 * A interface for querying component status
 * @author Jiang Wang
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
	 * 	@param compName component name
	 *  
	 *  */
	public String getCurrentStatus(String compName);
	
	/** 
	 *  component's default status
	 *  @param compName component name
	 * */
	public String getDefaultStatus(String compName);
	
	/** 
	 *  component's freenessSetup strategy 
	 *  @param compName component name
	 * */
	public String getFreenessSetup(String compName);
	
	/** 
	 *  all statuses defined for the component 
	 *  @param compName component name
	 * */
	public List<String> getAllStatuses(String compName);
	
	/** 
	 *  ForTest: started composite in the current node 
	 * */
	public Set<String> getStartedCompositeUri();
}
