package cn.edu.nju.moon.conup.ext.test;

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
		
		authComp = new ComponentObject(BufferTestConvention.AUTH_COMP, 
				BufferTestConvention.OLD_VERSION, 
				BufferTestConvention.CONSISTENCY_ALGORITHM, 
				BufferTestConvention.CONCURRENT_VERSION, 
				null, null, 
				BufferTestConvention.JAVA_POJO_IMPL_TYPE);
		
		
		portalComp = new ComponentObject(BufferTestConvention.PORTAL_COMP, 
				BufferTestConvention.OLD_VERSION, 
				BufferTestConvention.CONSISTENCY_ALGORITHM, 
				BufferTestConvention.CONCURRENT_VERSION, 
				null, null, 
				BufferTestConvention.JAVA_POJO_IMPL_TYPE);
		
		procComp = new ComponentObject(BufferTestConvention.PROC_COMP, 
				BufferTestConvention.OLD_VERSION, 
				BufferTestConvention.CONSISTENCY_ALGORITHM, 
				BufferTestConvention.CONCURRENT_VERSION, 
				null, null, 
				BufferTestConvention.JAVA_POJO_IMPL_TYPE);
		
		dbComp = new ComponentObject(BufferTestConvention.DB_COMP, 
				BufferTestConvention.OLD_VERSION, 
				BufferTestConvention.CONSISTENCY_ALGORITHM, 
				BufferTestConvention.CONCURRENT_VERSION, 
				null, null, 
				BufferTestConvention.JAVA_POJO_IMPL_TYPE);
		
		nodeMgr.addComponentObject(BufferTestConvention.AUTH_COMP, authComp);
		nodeMgr.addComponentObject(BufferTestConvention.PORTAL_COMP, portalComp);
		nodeMgr.addComponentObject(BufferTestConvention.PROC_COMP, procComp);
		nodeMgr.addComponentObject(BufferTestConvention.DB_COMP, dbComp);
		
		
		
	}
}
