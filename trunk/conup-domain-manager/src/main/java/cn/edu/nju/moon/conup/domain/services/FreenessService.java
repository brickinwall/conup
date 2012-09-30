/**
 * 
 */
package cn.edu.nju.moon.conup.domain.services;

import org.oasisopen.sca.annotation.Remotable;

/**
 * @author nju
 * 
 */
@Remotable
public interface FreenessService {
	public boolean isFreeness(String componentName);
//	public boolean isFreeness(String[] componentNames);
}
