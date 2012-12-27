package cn.edu.nju.moon.conup.sample.portal.services;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface ComponentUpdateService {
//	boolean update();
	
	boolean update(String baseDir, String classPath, String contributionURI, String compositeURI);
}
