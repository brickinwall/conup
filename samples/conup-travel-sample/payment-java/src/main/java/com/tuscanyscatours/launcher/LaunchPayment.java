package com.tuscanyscatours.launcher;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.monitor.ValidationException;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import com.tuscanyscatours.payment.Payment;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class LaunchPayment {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws ContributionReadException 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Starting payment node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		Node node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(LaunchPayment.class);
		node.installContribution(contributionURL);
		node.startComposite("payment-java", "payment.composite");
		System.out.println("payment.composite is ready!");
		
        NodeManager nodeMgr;
        nodeMgr = NodeManager.getInstance();
        nodeMgr.loadConupConf("Payment", "oldVersion");
        CompLifecycleManager.getInstance("Payment").setNode(node);
        CommServerManager.getInstance().start("Payment");
        
        nodeMgr.loadConupConf("CustomerRegistry", "oldVersion");
        CompLifecycleManager.getInstance("CustomerRegistry").setNode(node);
        CommServerManager.getInstance().start("CustomerRegistry");
      
        nodeMgr.loadConupConf("EmailGateway", "oldVersion");
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
			System.out.println(payment.makePaymentMember("c-0", 1000));
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}
}
