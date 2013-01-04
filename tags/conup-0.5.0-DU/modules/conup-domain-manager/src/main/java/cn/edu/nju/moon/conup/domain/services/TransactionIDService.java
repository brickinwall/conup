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
public interface TransactionIDService {
	public String createID();
	public boolean removeID(String id);
}
