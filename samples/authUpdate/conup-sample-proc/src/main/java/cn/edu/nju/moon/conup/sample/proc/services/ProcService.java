package cn.edu.nju.moon.conup.sample.proc.services;

import org.oasisopen.sca.annotation.Remotable;
@Remotable
public interface ProcService {
	String process(String exeProc, String token, String data);
}
