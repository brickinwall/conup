package com.tuscanyscatours.launcher;

import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

import com.tuscanyscatours.payment.Payment;

public class LaunchPayment {
	private static Logger LOGGER = Logger.getLogger(LaunchPayment.class.getName());

	/**
	 * @param args
	 * @throws Exception 
	 * @throws ContributionReadException 
	 */
	public static void main(String[] args) throws Exception {
		LOGGER.fine("Starting payment node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		Node node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(LaunchPayment.class);
		node.installContribution(contributionURL);
		node.startComposite("payment-java", "payment.composite");
		LOGGER.fine("payment.composite is ready!");
		
        NodeManager nodeMgr;
        nodeMgr = NodeManager.getInstance();
        nodeMgr.loadConupConf("Payment", "oldVersion");
//        nodeMgr.getDynamicDepManager("Payment").ondemandSetupIsDone();
        CompLifecycleManager.getInstance("Payment").setNode(node);
        CommServerManager.getInstance().start("Payment");
        
        nodeMgr.loadConupConf("CustomerRegistry", "oldVersion");
//      nodeMgr.getDynamicDepManager("CustomerRegistry").ondemandSetupIsDone();
        CompLifecycleManager.getInstance("CustomerRegistry").setNode(node);
        CommServerManager.getInstance().start("CustomerRegistry");
      
        nodeMgr.loadConupConf("EmailGateway", "oldVersion");
//      nodeMgr.getDynamicDepManager("EmailGateway").ondemandSetupIsDone();
        CompLifecycleManager.getInstance("EmailGateway").setNode(node);
        CommServerManager.getInstance().start("EmailGateway");
        
        //launch DepRecorder
        DepRecorder depRecorder;
        depRecorder = DepRecorder.getInstance();
        
        //access
//        accessServices(node);
	}
	
	public static void accessServices(Node node){
		try {
			System.out
					.println("\nTry to access Payment#service-binding(Payment/Payment):");
			Payment payment = node.getService(Payment.class,
					"Payment#service-binding(Payment/Payment)");
			LOGGER.fine(payment.makePaymentMember("c-0", 1000));
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}
}
