package cn.edu.nju.moon.conup.buffer.update;

import org.apache.tuscany.sca.implementation.java.context.ReflectiveInstanceFactory;

public interface Dispatcher {
	public boolean dispatchToOldVersion();
	public boolean dispatchToNewVersion();
}
