package cn.edu.nju.moon.conup.domain.services;

import java.util.List;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface DBService {
	List<String> dbOperation();
}