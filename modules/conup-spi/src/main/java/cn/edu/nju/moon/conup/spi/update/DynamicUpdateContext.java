package cn.edu.nju.moon.conup.spi.update;

import java.util.Set;

public interface DynamicUpdateContext {

	public boolean isLoaded();

	public void removeAlgorithmOldRootTx(String oldRootTx);

	public void setLoaded(boolean isLoaded);

	public Class<?> getOldVerClass();

	public void setOldVerClass(Class<?> oldVerClass);

	public Class<?> getNewVerClass();

	public void setNewVerClass(Class<?> newVerClass);

	public Set<String> getAlgorithmOldRootTxs();

	public void setAlgorithmOldRootTxs(Set<String> algorithmOldRootTxs);

	public boolean isOldRootTxsInitiated();

}