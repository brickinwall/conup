package cn.edu.nju.moon.conup.domain.def;

public class StaticEdge {
	private String srcComponent;
	private String targetComponent;

	public StaticEdge(String srcComponent, String targetComponent) {
		super();
		this.srcComponent = srcComponent;
		this.targetComponent = targetComponent;
	}
	
	public StaticEdge(){
		
	}
	public String getSrcComponent() {
		return srcComponent;
	}

	public void setSrcComponent(String srcComponent) {
		this.srcComponent = srcComponent;
	}

	public String getTargetComponent() {
		return targetComponent;
	}

	public void setTargetComponent(String targetComponent) {
		this.targetComponent = targetComponent;
	}
}
