package cn.edu.nju.moon.conup.sample.portal.services;

import java.util.List;

import org.oasisopen.sca.annotation.Remotable;
@Remotable
public interface ProcService {
	String process(String exeProc, String token, String data);
}
