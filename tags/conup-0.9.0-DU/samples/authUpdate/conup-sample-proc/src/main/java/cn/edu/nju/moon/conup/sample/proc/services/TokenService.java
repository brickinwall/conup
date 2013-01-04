package cn.edu.nju.moon.conup.sample.proc.services;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface TokenService {
	String getToken(String exeProc, String cred);
}
