package com.tuscanyscatours.launcher;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import com.tuscanyscatours.payment.Payment;
import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitorImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxLifecycleManagerImpl;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class PaymentLauncher {
	private static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	/**
	 * @param args
	 * @throws Exception 
	 * @throws ContributionReadException 
	 */
	public static void main(String[] args) throws Exception {
		
		LOGGER.setLevel(Level.OFF);
		
		LOGGER.fine("Starting payment node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		Node node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(PaymentLauncher.class);
		node.installContribution(contributionURL);
		node.startComposite("payment-java", "payment.composite");
		LOGGER.fine("payment.composite is ready!");
		
        NodeManager nodeMgr;
        nodeMgr = NodeManager.getInstance();
//        nodeMgr.loadConupConf("Payment", "oldVersion");
//        CompLifecycleManager.getInstance("Payment").setNode(node);
//        CommServerManager.getInstance().start("Payment");
        
        nodeMgr.loadConupConf("Payment", "oldVersion");
		ComponentObject paymentCompObj = nodeMgr.getComponentObject("Payment");
		CompLifecycleManagerImpl paymentCompLifecycleManager = new CompLifecycleManagerImpl(paymentCompObj);
		paymentCompLifecycleManager.setNode(node);
		nodeMgr.setCompLifecycleManager("Payment", paymentCompLifecycleManager);
		TxDepMonitor paymentTxDepMonitor = new TxDepMonitorImpl(paymentCompObj);
		nodeMgr.setTxDepMonitor("Payment", paymentTxDepMonitor);
		TxLifecycleManager paymentTxLifecycleMgr = new TxLifecycleManagerImpl(paymentCompObj);
		nodeMgr.setTxLifecycleManager("Payment", paymentTxLifecycleMgr);
		CommServerManager.getInstance().start("Payment");
        
//        nodeMgr.loadConupConf("CustomerRegistry", "oldVersion");
//        CompLifecycleManager.getInstance("CustomerRegistry").setNode(node);
//        CommServerManager.getInstance().start("CustomerRegistry");
        
        nodeMgr.loadConupConf("CustomerRegistry", "oldVersion");
		ComponentObject customerRegistryCompObj = nodeMgr.getComponentObject("CustomerRegistry");
		CompLifecycleManagerImpl customerRegistryCompLifecycleManager = new CompLifecycleManagerImpl(customerRegistryCompObj);
		customerRegistryCompLifecycleManager.setNode(node);
		nodeMgr.setCompLifecycleManager("CustomerRegistry", customerRegistryCompLifecycleManager);
		TxDepMonitor customerRegistryTxDepMonitor = new TxDepMonitorImpl(customerRegistryCompObj);
		nodeMgr.setTxDepMonitor("CustomerRegistry", customerRegistryTxDepMonitor);
		TxLifecycleManager customerRegistryTxLifecycleMgr = new TxLifecycleManagerImpl(customerRegistryCompObj);
		nodeMgr.setTxLifecycleManager("CustomerRegistry", customerRegistryTxLifecycleMgr);
		CommServerManager.getInstance().start("CustomerRegistry");
        
//        nodeMgr.loadConupConf("EmailGateway", "oldVersion");
//        CompLifecycleManager.getInstance("EmailGateway").setNode(node);
//        CommServerManager.getInstance().start("EmailGateway");
        
        nodeMgr.loadConupConf("EmailGateway", "oldVersion");
		ComponentObject emailGatewayCompObj = nodeMgr.getComponentObject("EmailGateway");
		CompLifecycleManagerImpl emailGatewayCompLifecycleManager = new CompLifecycleManagerImpl(emailGatewayCompObj);
		emailGatewayCompLifecycleManager.setNode(node);
		nodeMgr.setCompLifecycleManager("EmailGateway", emailGatewayCompLifecycleManager);
		TxDepMonitor emailGatewayTxDepMonitor = new TxDepMonitorImpl(emailGatewayCompObj);
		nodeMgr.setTxDepMonitor("EmailGateway", emailGatewayTxDepMonitor);
		TxLifecycleManager emailGatewayTxLifecycleMgr = new TxLifecycleManagerImpl(emailGatewayCompObj);
		nodeMgr.setTxLifecycleManager("EmailGateway", emailGatewayTxLifecycleMgr);
		CommServerManager.getInstance().start("EmailGateway");
        
//        nodeMgr.loadConupConf("CreditCardPayment", "oldVersion");
//        CompLifecycleManager.getInstance("CreditCardPayment").setNode(node);
//        CommServerManager.getInstance().start("CreditCardPayment");
        
        nodeMgr.loadConupConf("CreditCardPayment", "oldVersion");
		ComponentObject creditCardPaymentCompObj = nodeMgr.getComponentObject("CreditCardPayment");
		CompLifecycleManagerImpl creditCardPaymentCompLifecycleManager = new CompLifecycleManagerImpl(creditCardPaymentCompObj);
		creditCardPaymentCompLifecycleManager.setNode(node);
		nodeMgr.setCompLifecycleManager("CreditCardPayment", creditCardPaymentCompLifecycleManager);
		TxDepMonitor creditCardPaymentTxDepMonitor = new TxDepMonitorImpl(creditCardPaymentCompObj);
		nodeMgr.setTxDepMonitor("CreditCardPayment", creditCardPaymentTxDepMonitor);
		TxLifecycleManager creditCardPaymentTxLifecycleMgr = new TxLifecycleManagerImpl(creditCardPaymentCompObj);
		nodeMgr.setTxLifecycleManager("CreditCardPayment", creditCardPaymentTxLifecycleMgr);
		CommServerManager.getInstance().start("CreditCardPayment");
        
//        nodeMgr.getDynamicDepManager("Payment").ondemandSetting();
//		nodeMgr.getDynamicDepManager("CustomerRegistry").ondemandSetting();
//		nodeMgr.getDynamicDepManager("EmailGateway").ondemandSetting();
//		nodeMgr.getDynamicDepManager("CreditCardPayment").ondemandSetting();
//		
//		nodeMgr.getDynamicDepManager("Payment").ondemandSetupIsDone();
//		nodeMgr.getDynamicDepManager("CustomerRegistry").ondemandSetupIsDone();
//		nodeMgr.getDynamicDepManager("EmailGateway").ondemandSetupIsDone();
//		nodeMgr.getDynamicDepManager("CreditCardPayment").ondemandSetupIsDone();

        
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
			Payment payment = node.getService(Payment.class, "Payment/Payment");
			LOGGER.info(payment.makePaymentMember("c-0", 1000));
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}
}
