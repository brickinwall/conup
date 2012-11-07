package cn.edu.nju.moon.conup.spi.datamodel;

/**
 * Arc is an abstract concept for different algorithms.
 * For example, 
 * as to Version_Consistency, it means future/past arcs.
 * as to Quiescence, it means static dependences between components
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 * 
 */
public class Dependence implements Comparable<Dependence>{

	/** different algorithm can define its own dependence type. */
	private String type;
	/** root transaction this dependence corresponding to. */
	private String rootTx;
	private String srcCompObjIdentifier;
	private String targetCompObjIdentifer;
	private String sourceService;
	private String targetService;

	public Dependence() {

	}

	public Dependence(String type, String rootTransaction, String sourceComponent,
			String targetComponent, String sourceService, String targetService) {
		this.type = type;
		this.rootTx = rootTransaction;
		this.srcCompObjIdentifier = sourceComponent;
		this.targetCompObjIdentifer = targetComponent;
		this.sourceService = sourceService;
		this.targetService = targetService;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		Dependence dependence = (Dependence) obj;
		if (dependence.getRootTx().equals(this.rootTx)
				&& dependence.getSrcCompObjIdentifier().equals(this.srcCompObjIdentifier)
				&& dependence.getTargetCompObjIdentifer().equals(this.targetCompObjIdentifer)
				&& dependence.getType().equals(this.type)) {
			return true;
		} else
			return false;
	}

	@Override
	public int hashCode() {
		int result = Math.abs(this.rootTx.hashCode()+ this.srcCompObjIdentifier.hashCode() + this.targetCompObjIdentifer.hashCode() + this.type.hashCode()) % 997;
		return result;
	}

	/**
	 * @return the rootTx
	 */
	public String getRootTx() {
		return rootTx;
	}

	/**
	 * @param rootTx the rootTx to set
	 */
	public void setRootTx(String rootTx) {
		this.rootTx = rootTx;
	}

	/**
	 * @return the srcCompObjIdentifier
	 */
	public String getSrcCompObjIdentifier() {
		return srcCompObjIdentifier;
	}

	/**
	 * @param srcCompObjIdentifier the srcCompObjIdentifier to set
	 */
	public void setSrcCompObjIdentifier(String srcCompObjIdentifier) {
		this.srcCompObjIdentifier = srcCompObjIdentifier;
	}

	/**
	 * @return the targetCompObjIdentifer
	 */
	public String getTargetCompObjIdentifer() {
		return targetCompObjIdentifer;
	}

	/**
	 * @param targetCompObjIdentifer the targetCompObjIdentifer to set
	 */
	public void setTargetCompObjIdentifer(String targetCompObjIdentifer) {
		this.targetCompObjIdentifer = targetCompObjIdentifer;
	}

	public String getSourceService() {
		return sourceService;
	}

	public void setSourceService(String sourceService) {
		this.sourceService = sourceService;
	}

	public String getTargetService() {
		return targetService;
	}

	public void setTargetService(String targetService) {
		this.targetService = targetService;
	}

	@Override
	public String toString() {
		return "type: " + getType() + " rootTx: "
				+ getRootTx() + " srcCompObjIdentifier: "
				+ getSrcCompObjIdentifier() + " targetCompObjIdentifer: "
				+ getTargetCompObjIdentifer() + " sourceService: "
				+ getSourceService() + " targetService: " + getTargetService();
	}

	@Override
	public int compareTo(Dependence anotherArc) {
		String current = this.rootTx + this.srcCompObjIdentifier +
				this.targetCompObjIdentifer + this.type;
		String another = anotherArc.rootTx + anotherArc.srcCompObjIdentifier +
				anotherArc.targetCompObjIdentifer + anotherArc.type;
		return current.compareTo(another);
	}

}
