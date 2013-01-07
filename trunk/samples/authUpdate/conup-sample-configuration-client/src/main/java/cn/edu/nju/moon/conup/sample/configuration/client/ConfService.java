package cn.edu.nju.moon.conup.sample.configuration.client;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface ConfService {
	/** 
	 * 	This method is used to send on-demand request to the component you want to update.
	 *  */
	public void ondemand();
//	
	/** 
	 * 	This method is used to send update request to the component you want to update.
	 *  @param targetComponent's name
	 * 	@param newVersionClassUri the directory of your new version .class files
	 *  */
	public void update(String compIdentifier, String baseDir);
}
