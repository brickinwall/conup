package cn.edu.nju.moon.conup.buffer.update;

import org.apache.tuscany.sca.implementation.java.context.ReflectiveInstanceFactory;

import cn.edu.nju.moon.conup.def.ReconfigurationVersion;

public class JavaDispatcherImpl implements Dispatcher {

	@Override
	public boolean dispatchToOldVersion() {
		ReconfigurationVersion reconfigurationVersion = ReconfigurationVersion.getInstance();
		Class oldVersion = reconfigurationVersion.getOldVersion();
		ReflectiveInstanceFactory instanceFactory = reconfigurationVersion.getInstanceFactory();
		try {
			instanceFactory.setCtr(oldVersion.getConstructor());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean dispatchToNewVersion() {
		ReconfigurationVersion reconfigurationVersion = ReconfigurationVersion.getInstance();
		Class newVersion = reconfigurationVersion.getNewVersion();
		ReflectiveInstanceFactory instanceFactory = reconfigurationVersion.getInstanceFactory();
		try {
			instanceFactory.setCtr(newVersion.getConstructor());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return true;
	}

}
