package cn.edu.nju.moon.conup.update;

import org.apache.tuscany.sca.runtime.RuntimeComponent;

public interface DynamicUpdate {
	public boolean update(RuntimeComponent runtimeComponent, String baseDir, String filePath, String contributionURI, String compositeURI);
}
