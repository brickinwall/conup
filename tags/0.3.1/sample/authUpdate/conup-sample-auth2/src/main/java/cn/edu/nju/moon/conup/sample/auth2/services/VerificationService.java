package cn.edu.nju.moon.conup.sample.auth2.services;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface VerificationService {
	Boolean verify(String token);
}
