package cn.edu.nju.moon.conup.freeness;

import java.util.Set;

import cn.edu.nju.moon.conup.container.VcContainer;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.data.ArcRegistry;
import cn.edu.nju.moon.conup.def.Arc;

public class FreenessImpl implements Freeness{
	private static Freeness freeness = new FreenessImpl();
	private VcContainer vcContainer = VcContainerImpl.getInstance();
	private ArcRegistry inArcRegistry = vcContainer.getInArcRegistry();
	private FreenessImpl(){
		
	}
	
	public static Freeness getInstance(){
		return freeness;
	}

	@Override
	public boolean isFreeness(String componentName) {
		Set<Arc> inArcs = inArcRegistry.getArcsViaTargetComponent(componentName);
		boolean hasFutureArc = false;
		boolean hasPastArc = false;
		for(Arc arc : inArcs){
			if(arc.getType().equals("future"))
				hasFutureArc = true;
			else
				hasPastArc = true;
		}
//		if current component hava both future and past arc 
//		then it is not free
		if(hasFutureArc && hasPastArc){
			return false;
		}
		return true;
	}

	@Override
	public boolean isFreeness(int count) {
		// TODO Auto-generated method stub
		return false;
	}
	
//check for the system's freeness
	@Override
	public boolean isFreeness() {
		
		return false;
	}

}
