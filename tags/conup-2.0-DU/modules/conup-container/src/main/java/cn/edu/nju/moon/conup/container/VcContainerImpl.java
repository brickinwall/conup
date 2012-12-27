package cn.edu.nju.moon.conup.container;

import org.apache.tuscany.sca.Node;

import cn.edu.nju.moon.conup.data.InArcRegistryImpl;
import cn.edu.nju.moon.conup.data.OutArcRegistryImpl;
import cn.edu.nju.moon.conup.data.TransactionRegistryImpl;

public class VcContainerImpl extends VcContainer {
	private static VcContainerImpl vcContainerImpl = new VcContainerImpl();
	private VcContainerImpl(){
//		super.setBusinessNode(....);
//		super.setBusinessNode(...);
		super.setInArcRegistry(InArcRegistryImpl.getInstance());
		super.setOutArcRegistry(OutArcRegistryImpl.getInstance());
		super.setTransactionRegistry(TransactionRegistryImpl.getInstance());
		
	}
	
	private VcContainerImpl(Node businessNode){
		super(businessNode);
		super.setInArcRegistry(InArcRegistryImpl.getInstance());
		super.setOutArcRegistry(OutArcRegistryImpl.getInstance());
		super.setTransactionRegistry(TransactionRegistryImpl.getInstance());
		
	}
	public static VcContainerImpl getInstance() {
		return vcContainerImpl;
	}
	
	
}
