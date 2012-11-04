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

	/** arc type can be either future or past. */
	private String type;
	/** root transaction this Arc corresponding to. */
	private String rootTransaction;
	private String sourceComponent;
	private String targetComponent;
	private String sourceService;
	private String targetService;

	public Dependence() {

	}

	public Dependence(String type, String rootTransaction, String sourceComponent,
			String targetComponent, String sourceService, String targetService) {
		this.type = type;
		this.rootTransaction = rootTransaction;
		this.sourceComponent = sourceComponent;
		this.targetComponent = targetComponent;
		this.sourceService = sourceService;
		this.targetService = targetService;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRootTransaction() {
		return rootTransaction;
	}

	@Override
	public boolean equals(Object obj) {
		Dependence dependence = (Dependence) obj;
		if (dependence.getRootTransaction().equals(this.rootTransaction)
				&& dependence.getSourceComponent().equals(this.sourceComponent)
				&& dependence.getTargetComponent().equals(this.targetComponent)
				&& dependence.getType().equals(this.type)) {
			return true;
		} else
			return false;
	}

	@Override
	public int hashCode() {
		int result = Math.abs(this.rootTransaction.hashCode()+ this.sourceComponent.hashCode() + this.targetComponent.hashCode() + this.type.hashCode()) % 997;
		return result;
	}

	public void setRootTransaction(String rootTransaction) {
		this.rootTransaction = rootTransaction;
	}

	public String getSourceComponent() {
		return sourceComponent;
	}

	public void setSourceComponent(String sourceComponent) {
		this.sourceComponent = sourceComponent;
	}

	public String getTargetComponent() {
		return targetComponent;
	}

	public void setTargetComponent(String targetComponent) {
		this.targetComponent = targetComponent;
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
		return "type: " + getType() + " rootTransaction: "
				+ getRootTransaction() + " sourceComponent: "
				+ getSourceComponent() + " targetComponent: "
				+ getTargetComponent() + " sourceService: "
				+ getSourceService() + " targetService: " + getTargetService();
	}

	@Override
	public int compareTo(Dependence anotherArc) {
		String current = this.rootTransaction + this.sourceComponent +
				this.targetComponent + this.type;
		String another = anotherArc.rootTransaction + anotherArc.sourceComponent +
				anotherArc.targetComponent + anotherArc.type;
		return current.compareTo(another);
	}

}
