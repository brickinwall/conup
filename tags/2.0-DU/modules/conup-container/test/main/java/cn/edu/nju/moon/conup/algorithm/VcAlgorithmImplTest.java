package cn.edu.nju.moon.conup.algorithm;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.algorithm.VcAlgorithm;
import cn.edu.nju.moon.conup.algorithm.VcAlgorithmImpl;
import cn.edu.nju.moon.conup.container.VcContainer;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.def.InterceptorCache;
import cn.edu.nju.moon.conup.def.TransactionDependency;

public class VcAlgorithmImplTest {
	
	private static VcAlgorithm vcAlgorithm;
	private static VcContainer vcContainer;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		vcContainer = VcContainerImpl.getInstance();
		vcAlgorithm = new VcAlgorithmImpl(vcContainer);
	}

	@Test
	public void testAnalyze() {
		String threadID = getThreadID();
		InterceptorCache ic = vcContainer.getInterceptorCache();
		TransactionDependency dependency = new TransactionDependency();
//		dependency.setCurrentTx(null);
//		dependency.setHostComponent(hostComponent);
//		dependency.setParentTx(parentTx);
//		dependency.setParentComponent(parentComponent);
//		dependency.setRootTx(rootTx);
//		dependency.setRootComponent(rootComponent);
		//in interceptor we set up the dependency
		dependency.setCurrentTx(null);
		dependency.setHostComponent("PortalComponent");
		dependency.setParentTx(null);
		dependency.setParentComponent(null);
		dependency.setRootTx(null);
		dependency.setRootComponent(null);
		
		//in ComponentListener, we analyze the root, current, and parent tx informations
		String currentTransaction = dependency.getCurrentTx();
		String parentTransaction = dependency.getParentTx();
		String rootTransaction = dependency.getRootTx();
		String hostComponent = dependency.getHostComponent();
		String rootComponent = dependency.getRootComponent();
		String parentComponent = dependency.getParentComponent();
		
		if(rootTransaction==null && parentTransaction==null 
				&& currentTransaction==null && hostComponent!=null){
			//current transaction is root
//			isRoot = true;
//			currentTransaction = createTransactionID();
			currentTransaction = "112229398880";
			rootTransaction = currentTransaction;
			parentTransaction = currentTransaction;
			//update interceptor cache dependency
			dependency.setCurrentTx(currentTransaction);
			dependency.setParentTx(parentTransaction);
			dependency.setRootTx(rootTransaction);
			rootComponent = hostComponent;
			parentComponent = hostComponent;
			dependency.setHostComponent(hostComponent);
			dependency.setRootComponent(rootComponent);
			dependency.setParentComponent(parentComponent);
		} 
		
		ic.setCache(threadID, dependency);
		Set<String> futureC = new HashSet<String>();
		futureC.add("AuthComponent");
		futureC.add("ProcComponent");
		Set<String> pastC = new HashSet<String>();
		
		vcAlgorithm.analyze("start", threadID, futureC, pastC);
		
//		fail("Not yet implemented");
	}
	
	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}

}
