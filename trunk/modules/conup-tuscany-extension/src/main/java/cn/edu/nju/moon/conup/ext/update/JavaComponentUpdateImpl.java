/**
 * 
 */
package cn.edu.nju.moon.conup.ext.update;


/**
 * A class for update components implemented in Java POJO.
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class JavaComponentUpdateImpl implements ComponentUpdator {

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.core.update.ComponentUpdate#start(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean start(String baseDir, String classPath,
			String contributionURI, String compositeURI) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.core.update.ComponentUpdate#isUpdated()
	 */
	public boolean isUpdated() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.core.update.ComponentUpdate#cleanUpdate()
	 */
	public boolean cleanUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getImplementationType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean finalizeOld(String compName, String oldVersion,
			String newVersion) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean initNewVersion(String compName, String oldVersion,
			String newVersion) {
		// TODO Auto-generated method stub
		return false;
	}

}
