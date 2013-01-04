package cn.edu.nju.moon.conup.domain.services;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface NotifyService {
	boolean notifyInterceptor();
}
