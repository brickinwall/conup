package cn.edu.nju.moon.conup.sample.portal.services;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface VerificationService {
	String verify(String exeProc, String token);
}
