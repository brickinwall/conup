package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Set;

/**
 * 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * @version Created time: Jul 28, 2013 11:04:36 PM
 */
public class TxDep {
	/** components that will never be used. */
	private Set<String> pastComponents;
	/** components that will be used later. */
	private Set<String> futureComponents;

	public TxDep() {

	}

	public TxDep(Set<String> futureComponents, Set<String> pastComponents) {
		this.futureComponents = futureComponents;
		this.pastComponents = pastComponents;
	}

	public Set<String> getPastComponents() {
		return pastComponents;
	}

	public void setPastComponents(Set<String> pastComponents) {
		this.pastComponents = pastComponents;
	}

	public Set<String> getFutureComponents() {
		return futureComponents;
	}

	public void setFutureComponents(Set<String> futureComponents) {
		this.futureComponents = futureComponents;
	}

	@Override
	public String toString() {
		StringBuffer txDep = new StringBuffer();
		txDep.append("FutureComponents:");
		for(String str : futureComponents){
			txDep.append(str).append(",");
		}
		
		txDep.append("\nPastComponents:");
		for(String str: pastComponents){
			txDep.append(str).append(",");
		}
		return txDep.toString();
	}
}
