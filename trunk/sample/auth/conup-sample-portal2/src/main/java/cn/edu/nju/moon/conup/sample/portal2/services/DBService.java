package cn.edu.nju.moon.conup.sample.portal2.services;

import java.util.List;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface DBService {
	List<String> dbOperation();
}