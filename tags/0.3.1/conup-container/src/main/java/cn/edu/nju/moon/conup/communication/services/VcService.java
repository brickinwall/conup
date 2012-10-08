package cn.edu.nju.moon.conup.communication.services;

import org.oasisopen.sca.annotation.Remotable;

import cn.edu.nju.moon.conup.def.Arc;

@Deprecated
@Remotable
public interface VcService {
	void createArc(Arc arc);
	void readArc();
//	@Deprecated
//	void update(Arc arc);
	void update(Arc arc, String flag);
	void removeArc(Arc arc);
	
	void updateSetUp();
	void updateCleanUp();

}
