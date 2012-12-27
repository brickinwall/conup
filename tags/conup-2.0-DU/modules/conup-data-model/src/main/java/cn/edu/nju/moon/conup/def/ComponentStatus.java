package cn.edu.nju.moon.conup.def;

import java.util.ArrayList;
import java.util.List;

public class ComponentStatus {
	private static ComponentStatus instance = new ComponentStatus();
	
	/** no dynamic update is required */
	public static final String NORMAL = "Normal";
	/** dynamic update is required and is still initiating */
	public static final String ON_DEMAND = "On-demand";
	/** init is done */
	public static final String VALID = "Valid";
	/** achieving freeness via waiting */
	public static final String WAITING = "WaitingForFreeness";
	/** achieving freeness via concurrent version */
	public static final String CONCURRENT = "ConcurrentVersion";
	/** achieving freeness via blocking */
	public static final String BLOCKING = "BlockingForFreeness";
	/** the component is currently free */
	public static final String FREE = "Free";
	/** the component is engaged in dynamic update */
	public static final String UPDATING = "Updating";
	/** the component has already been replaced with a new version */
	public static final String UPDATED = "Updated";
	/** new version of the component is running */
	public static final String ACTIVATED = "Activated";
	
	/**
	 * Achieve Freeness Strategy
	 */
	
	//By Jiang: it seems that the member variable is of no use, remove it temporally
//	private String onDemandSetup = ComponentStatus.NORMAL; 
	
//	private String freenessSetup = ComponentStatus.WAITING;
	private String freenessSetup = ComponentStatus.CONCURRENT;
	
	//currentStatus is supposed to be consistent with defaultStatus
	private String currentStatus = ComponentStatus.NORMAL;
//	private String currentStatus = ComponentStatus.VALID;
//	private String currentStatus = ComponentStatus.FREENESS;
	
	private String componentName = null;
	
//	private Scope scope = null;
	private Scope scope = new Scope();
	
	
	private List<String> status;
	
	private String defaultStatus = VALID;
	
	public String getDefaultStatus() {
		return defaultStatus;
	}

	public void setDefaultStatus(String defaultStatus) {
		this.defaultStatus = defaultStatus;
	}

	private ComponentStatus(){
		
	}
	
	public static ComponentStatus getInstance(){
		return instance;
	}
	
	//By Jiang: it seems that this method is of no use, remove it temporally
//	public void init(String componentName, String onDemandSetup, String freenessSetup){
//		this.componentName = componentName;
//		this.onDemandSetup = onDemandSetup;
//		this.freenessSetup = freenessSetup;
//		
//		currentStatus = onDemandSetup;
//	}
	
	/** return next component status */
	public String getNext(){
		initStatus();
		int cur = status.indexOf(currentStatus);
		int next = ((cur+1)>=status.size()) ? 0 : cur+1;
		currentStatus = status.get(next);
		return currentStatus;
	}
	
	private void initStatus(){
		status = new ArrayList<String>();
		
		status.add(ComponentStatus.NORMAL);
		status.add(ComponentStatus.ON_DEMAND);
		status.add(ComponentStatus.VALID);
		status.add(freenessSetup);
		status.add(ComponentStatus.FREE);
		status.add(ComponentStatus.UPDATING);
		status.add(ComponentStatus.UPDATED);
		status.add(ComponentStatus.ACTIVATED);
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	//By Jiang: it seems that this method is of no use, remove it temporally
//	public String getOnDemandSetup() {
//		return onDemandSetup;
//	}
//	//By Jiang: it seems that this method is of no use, remove it temporally
//	public void setOnDemandSetup(String onDemandSetup) {
//		if(this.currentStatus == ComponentStatus.NORMAL 
//			&& onDemandSetup.equals(ComponentStatus.ON_DEMAND)){
//			this.onDemandSetup = onDemandSetup;
//			this.currentStatus = onDemandSetup;
//		} else{
//			System.out.println("Error: illegal setup. Current component status=" + currentStatus +
//					", but your onDemandSetup=" + onDemandSetup);
//		}
//		
//	}

	public String getFreenessSetup() {
		return freenessSetup;
	}

	public void setFreenessSetup(String freenessSetup) {
		this.freenessSetup = freenessSetup;
	}

	public String getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	
	
}
