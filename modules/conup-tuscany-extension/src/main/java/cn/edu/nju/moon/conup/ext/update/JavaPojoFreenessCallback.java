package cn.edu.nju.moon.conup.ext.update;

import org.apache.tuscany.sca.implementation.java.context.ReflectiveInstanceFactory;

import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.spi.helper.FreenessCallback;

/**
 * For now, we've got three freeness strategies:
 * <ul>
 * 		<li>Waiting
 * 		<li>Blocking
 * 		<li>Concurrent version
 * </ul>
 * 
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
@Deprecated
public class JavaPojoFreenessCallback implements FreenessCallback {

	@Override
	public void toNewVersionComp(String compIdentifer) {
		ReflectiveInstanceFactory instanceFactory;
		CompLifecycleManagerImpl compLcMgr;
		Class<?> compClass;
		
		compLcMgr = (CompLifecycleManagerImpl) CompLifecycleManagerImpl.getInstance(compIdentifer);
//		instanceFactory = compLcMgr.getInstanceFactory();
//		instanceFactory = compLcMgr.getCompUpdator();
//		compClass = compLcMgr.getUpdateCtx().getNewVerClass();
//		try {
//			instanceFactory.setCtr(compClass.getConstructor());
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void toOldVersionComp(String compIdentifer) {
		ReflectiveInstanceFactory instanceFactory;
		CompLifecycleManagerImpl compLcMgr;
		Class<?> compClass;
		
		compLcMgr = (CompLifecycleManagerImpl) CompLifecycleManagerImpl.getInstance(compIdentifer);
//		instanceFactory = compLcMgr.getInstanceFactory();
//		compClass = compLcMgr.getUpdateCtx().getOldVerClass();
//		try {
//			instanceFactory.setCtr(compClass.getConstructor());
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		}		
	}
	
	@Override
	public String getCompImplType(){
		return JavaCompUpdatorImpl.IMPL_TYPE;
	}

}
