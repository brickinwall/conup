package cn.edu.nju.moon.conup.def;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class OldVersionRootTransation {
	private Set<String> oldRootTxIds = new ConcurrentSkipListSet<String>();
	public Set<String> getOldRootTxIds() {
		return oldRootTxIds;
	}

	public void setOldRootTxIds(Set<String> oldRootTxIds) {
		this.oldRootTxIds = oldRootTxIds;
	}

	private static OldVersionRootTransation oldVersionRootTx = new OldVersionRootTransation();
	
	private OldVersionRootTransation(){
		
	}
	
	public static OldVersionRootTransation getInstance(){
		return oldVersionRootTx;
	}
}
