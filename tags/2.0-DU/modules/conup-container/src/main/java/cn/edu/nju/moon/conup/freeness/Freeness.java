package cn.edu.nju.moon.conup.freeness;

public interface Freeness {
	/**
	 * check whether the system is freeness
	 * @return
	 */
	public boolean isFreeness();
	/**
	 * check whether "componentName" is freeness to update
	 * @param componentName
	 * @return
	 */
	public boolean isFreeness(String componentName);
	
	public boolean isFreeness(int count);
}
