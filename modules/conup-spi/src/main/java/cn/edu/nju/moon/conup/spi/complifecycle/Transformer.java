package cn.edu.nju.moon.conup.spi.complifecycle;

/**
 * Because every component may have its own inner status, e.g., opened files, in order to make sure that 
 * the new version component can work fine, a transformer is necessary.
 * 
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public interface Transformer {
	/**
	 * 
	 * @param srcComp an instance of the old version component
	 * @param targetComp and instance of the new version component
	 * @return
	 */
	public boolean transform(Object srcComp, Object targetComp);

}
