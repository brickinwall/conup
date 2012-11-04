/**
 * 
 */
package cn.edu.nju.moon.conup.core.freeness;

import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;

/**
 * Implementation of concurrent version strategy for achieving freeness
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class ConcurrentVersionStrategy implements FreenessStrategy {

	@Override
	public void manage(String rootTxID, String rootComp, String parentComp,
			String curTxID, String hostComp) {
		// TODO Auto-generated method stub
		
	}

}
