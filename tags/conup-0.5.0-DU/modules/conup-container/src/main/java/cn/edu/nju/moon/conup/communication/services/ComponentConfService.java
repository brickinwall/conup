package cn.edu.nju.moon.conup.communication.services;

import java.util.List;
import java.util.Set;

import org.oasisopen.sca.annotation.Remotable;


@Remotable
public interface ComponentConfService {
	/** host component name */
	public String getComponentName();
	/** component's current status */
	public String getCurrentStatus();
	/** component's default status*/
	public String getDefaultStatus();
	/** component's freenessSetup strategy */
	public String getFreenessSetup();
	/** all statuses defined for the component */
	public List<String> getAllStatuses();
	
	/** ForTest: started composite in the current node */
	public Set<String> getStartedCompositeUri();
}
