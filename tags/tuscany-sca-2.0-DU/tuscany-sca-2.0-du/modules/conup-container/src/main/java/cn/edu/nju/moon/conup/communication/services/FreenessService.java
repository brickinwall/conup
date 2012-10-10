package cn.edu.nju.moon.conup.communication.services;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface FreenessService {
	/**
	 * check whether current component is freeness
	 * @return
	 */
//	public boolean isFreeness();
	/**
	 * check whether "componentName" is freeness to update
	 * @param componentName
	 * @return
	 */
	public boolean isFreeness(String componentName);
	/**
	 * ???
	 */
//	public boolean isFreeness(int count);
}
