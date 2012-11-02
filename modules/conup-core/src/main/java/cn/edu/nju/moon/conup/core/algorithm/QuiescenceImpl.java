/**
 * 
 */
package cn.edu.nju.moon.conup.core.algorithm;

import java.util.Set;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;


/**
 * @author nju
 *
 */
public class QuiescenceImpl implements Algorithm {

	@Override
	public void analyze(TransactionContext txContext) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 
	 * @param compName
	 * @return
	 */
	public boolean isPassive(String compName){
		return false;
	}

}
