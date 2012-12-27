/**
 * 
 */
package cn.edu.nju.moon.conup.listener;

import java.util.Set;


/**
 * With the executing of a component implementation, some components will never be used,
 * which means that some future/past arcs may need to be changed. In this case, 
 * the component impl will call this interface and pass the latest components information. 
 * 
 * @author nju
 *
 */
public interface ComponentListener {
	public boolean notify(String transactionStatus, String threadID, Set<String> futureC, Set<String> pastC);

}
