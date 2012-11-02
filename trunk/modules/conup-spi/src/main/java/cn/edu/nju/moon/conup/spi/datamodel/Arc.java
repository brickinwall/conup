package cn.edu.nju.moon.conup.spi.datamodel;

/**
 * Arc is an abstract concept for different algorithms.
 * For example, 
 * as to Version_Consistency, it means future/past arcs.
 * as to Quiescence, it means static dependences between components
 * 
 * @author nju
 * 
 */
public class Arc implements Comparable<Arc>{
	/** arc type is "future" */
	public final static String FUTURE = "FUTURE";
	/** arc type is "past" */
	public final static String PAST = "PAST";
	/** arc type is static dependent */
	public final static String STATIC_DEP = "STATIC_DEP";

	/** arc type can be either future or past. */
	private String type;
	/** root transaction this Arc corresponding to. */
	private String rootTransaction;
	private String sourceComponent;
	private String targetComponent;
	private String sourceService;
	private String targetService;

	public Arc() {

	}

	public Arc(String type, String rootTransaction, String sourceComponent,
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
		Arc arc = (Arc) obj;
		if (arc.getRootTransaction().equals(this.rootTransaction)
				&& arc.getSourceComponent().equals(this.sourceComponent)
				&& arc.getTargetComponent().equals(this.targetComponent)
				&& arc.getType().equals(this.type)) {
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
	public int compareTo(Arc anotherArc) {
		String current = this.rootTransaction + this.sourceComponent +
				this.targetComponent + this.type;
		String another = anotherArc.rootTransaction + anotherArc.sourceComponent +
				anotherArc.targetComponent + anotherArc.type;
		return current.compareTo(another);
	}

}
