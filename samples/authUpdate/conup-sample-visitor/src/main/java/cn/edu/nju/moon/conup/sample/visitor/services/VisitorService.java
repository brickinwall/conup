package cn.edu.nju.moon.conup.sample.visitor.services;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface VisitorService {
	/** 
	 * 	Via invoking this method, it will start a thread to send continuous requests 
	 * 	to PortalComponent/PortalService 
	 * */
	public void visitPortal(int n);
	
	public void visitToken(int n);
	
	public void visitVerify(int n);
	
	/** 
	 * 	This method is used to send on-demand request to the component you want to update.
	 * 	@param targetComponent the component that you'd like to update
	 * 	@freenessSetup the strategy to achieve freeness, default is "ConcurrentVersion"
	 * 
	 *  */
//	public void ondemand();
//	
	/** 
	 * 	This method is used to send update request to the component you want to update.
	 * 	@param newVersionClassUri the directory of your new version .class files
	 * 	@param className class name with its package, 
	 * 			it should look like "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl"
	 * 	@param contributionName name of your contribution, 
	 * 			it should look like "conup-sample-auth"
	 * 	@param compositeName your .composite file's name, which is under you resources/ directory
	 *  */
//	public void update();
}
