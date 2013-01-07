package cn.edu.nju.moon.conup.spi.test;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * For many test cases, they all need to initiate the NodeManager, such as
 * add component objects. This class is for that.
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class InitNodeManager {
	public void initCompObjects(NodeManager nodeMgr){
		ComponentObject authComp;
		ComponentObject portalComp;
		ComponentObject procComp;
		ComponentObject dbComp;
		
		authComp = new ComponentObject(SpiTestConvention.AUTH_COMP, 
				SpiTestConvention.OLD_VERSION, 
				SpiTestConvention.CONSISTENCY_ALGORITHM, 
				SpiTestConvention.CONCURRENT_VERSION, 
				null, null,
				SpiTestConvention.JAVA_POJO_IMPL_TYPE);
		
		
		portalComp = new ComponentObject(SpiTestConvention.PORTAL_COMP, 
				SpiTestConvention.OLD_VERSION, 
				SpiTestConvention.CONSISTENCY_ALGORITHM, 
				SpiTestConvention.CONCURRENT_VERSION, 
				null, null,
				SpiTestConvention.JAVA_POJO_IMPL_TYPE);
		
		procComp = new ComponentObject(SpiTestConvention.PROC_COMP, 
				SpiTestConvention.OLD_VERSION, 
				SpiTestConvention.CONSISTENCY_ALGORITHM, 
				SpiTestConvention.CONCURRENT_VERSION, 
				null, null,
				SpiTestConvention.JAVA_POJO_IMPL_TYPE);
		
		dbComp = new ComponentObject(SpiTestConvention.DB_COMP, 
				SpiTestConvention.OLD_VERSION, 
				SpiTestConvention.CONSISTENCY_ALGORITHM, 
				SpiTestConvention.CONCURRENT_VERSION, 
				null, null,
				SpiTestConvention.JAVA_POJO_IMPL_TYPE);
		
		nodeMgr.addComponentObject(SpiTestConvention.AUTH_COMP, authComp);
		nodeMgr.addComponentObject(SpiTestConvention.PORTAL_COMP, portalComp);
		nodeMgr.addComponentObject(SpiTestConvention.PROC_COMP, procComp);
		nodeMgr.addComponentObject(SpiTestConvention.DB_COMP, dbComp);
		
		
		
	}
}
