package cn.edu.nju.moon.conup.sample.proc2.services;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface VerificationService {
	Boolean verify(String token);
}
