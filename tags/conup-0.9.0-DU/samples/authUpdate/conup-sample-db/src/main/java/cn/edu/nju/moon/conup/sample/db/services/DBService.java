package cn.edu.nju.moon.conup.sample.db.services;

import java.util.List;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface DBService {
	String dbOperation(String exeProc);
}
