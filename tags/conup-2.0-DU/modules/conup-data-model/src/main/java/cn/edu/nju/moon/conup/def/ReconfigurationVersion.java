package cn.edu.nju.moon.conup.def;

import org.apache.tuscany.sca.implementation.java.context.ReflectiveInstanceFactory;

/** For dynamic update only, maintain old and new version classes */
public class ReconfigurationVersion {
	private static ReconfigurationVersion reconfigurationVersion = new ReconfigurationVersion();
	private Class oldVersion;
	private Class newVersion;
	private ReflectiveInstanceFactory instanceFactory;
	private boolean isLoaded = false;
	
	
	public boolean isLoaded() {
		return isLoaded;
	}

	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	public ReflectiveInstanceFactory getInstanceFactory() {
		return instanceFactory;
	}

	public void setInstanceFactory(ReflectiveInstanceFactory instanceFactory) {
		this.instanceFactory = instanceFactory;
	}

	public Class getOldVersion() {
		return oldVersion;
	}

	public void setOldVersion(Class oldVersion) {
		this.oldVersion = oldVersion;
	}

	public Class getNewVersion() {
		return newVersion;
	}

	public void setNewVersion(Class newVersion) {
		this.newVersion = newVersion;
	}

	public static ReconfigurationVersion getInstance(){
		return reconfigurationVersion;
	}
	
	private ReconfigurationVersion(){
		
	}
	
	/** when a dynamic update is done, it's recommended to invoke the method to reset variables   */
	public boolean reset(){
		oldVersion = null;
		newVersion = null;
		isLoaded = false;
		instanceFactory = null;
		return true;
	}
}
