package cn.edu.nju.moon.conup.sample.portal2.services;

import java.util.List;
import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface PortalService {

//	String getToken(String cred);
//	List<String> process(String token, String data);
	List<String> execute(String userName, String passwd);
	
}
