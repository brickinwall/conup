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
import cn.edu.nju.moon.conup.comm.api.server.ServerIoHandler;
import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitorImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxLifecycleManagerImpl;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
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
        nodeMgr.loadConupConf("Payment", "oldVersion");
		ComponentObject paymentCompObj = nodeMgr.getComponentObject("Payment");
		CompLifecycleManagerImpl paymentCompLifecycleManager = new CompLifecycleManagerImpl(paymentCompObj);
		nodeMgr.setTuscanyNode(node);
		nodeMgr.setCompLifecycleManager("Payment", paymentCompLifecycleManager);
		TxLifecycleManager paymentTxLifecycleMgr = new TxLifecycleManagerImpl(paymentCompObj);
		nodeMgr.setTxLifecycleManager("Payment", paymentTxLifecycleMgr);
		TxDepMonitor paymentTxDepMonitor = new TxDepMonitorImpl(paymentCompObj);
		nodeMgr.setTxDepMonitor("Payment", paymentTxDepMonitor);
		
		DynamicDepManager paymentDepMgr = NodeManager.getInstance().getDynamicDepManager(paymentCompObj.getIdentifier());
		paymentDepMgr.setTxLifecycleMgr(paymentTxLifecycleMgr);
		paymentDepMgr.setCompLifeCycleMgr(paymentCompLifecycleManager);
		
		nodeMgr.getOndemandSetupHelper("Payment");
		UpdateManager paymentUpdateMgr = nodeMgr.getUpdateManageer("Payment");
		CommServerManager.getInstance().start("Payment");
		ServerIoHandler paymentServerIoHandler = CommServerManager.getInstance().getCommServer("Payment").getServerIOHandler();
		paymentServerIoHandler.registerUpdateManager(paymentUpdateMgr);
        
        nodeMgr.loadConupConf("CustomerRegistry", "oldVersion");
		ComponentObject customerRegistryCompObj = nodeMgr.getComponentObject("CustomerRegistry");
		CompLifecycleManagerImpl customerRegistryCompLifecycleManager = new CompLifecycleManagerImpl(customerRegistryCompObj);
		nodeMgr.setCompLifecycleManager("CustomerRegistry", customerRegistryCompLifecycleManager);
		TxLifecycleManager customerRegistryTxLifecycleMgr = new TxLifecycleManagerImpl(customerRegistryCompObj);
		nodeMgr.setTxLifecycleManager("CustomerRegistry", customerRegistryTxLifecycleMgr);
		TxDepMonitor customerRegistryTxDepMonitor = new TxDepMonitorImpl(customerRegistryCompObj);
		nodeMgr.setTxDepMonitor("CustomerRegistry", customerRegistryTxDepMonitor);
		
		DynamicDepManager customerRegistryDepMgr = NodeManager.getInstance().getDynamicDepManager(customerRegistryCompObj.getIdentifier());
		customerRegistryDepMgr.setTxLifecycleMgr(customerRegistryTxLifecycleMgr);
		customerRegistryDepMgr.setCompLifeCycleMgr(customerRegistryCompLifecycleManager);
		
		nodeMgr.getOndemandSetupHelper("CustomerRegistry");
		UpdateManager customerRegistryUpdateMgr = nodeMgr.getUpdateManageer("CustomerRegistry");
		CommServerManager.getInstance().start("CustomerRegistry");
		ServerIoHandler customerRegistryServerIoHandler = CommServerManager.getInstance().getCommServer("CustomerRegistry").getServerIOHandler();
		customerRegistryServerIoHandler.registerUpdateManager(customerRegistryUpdateMgr);
        
        nodeMgr.loadConupConf("EmailGateway", "oldVersion");
		ComponentObject emailGatewayCompObj = nodeMgr.getComponentObject("EmailGateway");
		CompLifecycleManagerImpl emailGatewayCompLifecycleManager = new CompLifecycleManagerImpl(emailGatewayCompObj);
		nodeMgr.setCompLifecycleManager("EmailGateway", emailGatewayCompLifecycleManager);
		TxLifecycleManager emailGatewayTxLifecycleMgr = new TxLifecycleManagerImpl(emailGatewayCompObj);
		nodeMgr.setTxLifecycleManager("EmailGateway", emailGatewayTxLifecycleMgr);
		TxDepMonitor emailGatewayTxDepMonitor = new TxDepMonitorImpl(emailGatewayCompObj);
		nodeMgr.setTxDepMonitor("EmailGateway", emailGatewayTxDepMonitor);
		
		DynamicDepManager emailGatewayDepMgr = NodeManager.getInstance().getDynamicDepManager(emailGatewayCompObj.getIdentifier());
		emailGatewayDepMgr.setTxLifecycleMgr(emailGatewayTxLifecycleMgr);
		emailGatewayDepMgr.setCompLifeCycleMgr(emailGatewayCompLifecycleManager);
		
		nodeMgr.getOndemandSetupHelper("EmailGateway");
		UpdateManager emailGatewayUpdateMgr = nodeMgr.getUpdateManageer("EmailGateway");
		CommServerManager.getInstance().start("EmailGateway");
		ServerIoHandler emailGatewayServerIoHandler = CommServerManager.getInstance().getCommServer("EmailGateway").getServerIOHandler();
		emailGatewayServerIoHandler.registerUpdateManager(emailGatewayUpdateMgr);
        
        nodeMgr.loadConupConf("CreditCardPayment", "oldVersion");
		ComponentObject creditCardPaymentCompObj = nodeMgr.getComponentObject("CreditCardPayment");
		CompLifecycleManagerImpl creditCardPaymentCompLifecycleManager = new CompLifecycleManagerImpl(creditCardPaymentCompObj);
		nodeMgr.setCompLifecycleManager("CreditCardPayment", creditCardPaymentCompLifecycleManager);
		TxLifecycleManager creditCardPaymentTxLifecycleMgr = new TxLifecycleManagerImpl(creditCardPaymentCompObj);
		nodeMgr.setTxLifecycleManager("CreditCardPayment", creditCardPaymentTxLifecycleMgr);
		TxDepMonitor creditCardPaymentTxDepMonitor = new TxDepMonitorImpl(creditCardPaymentCompObj);
		nodeMgr.setTxDepMonitor("CreditCardPayment", creditCardPaymentTxDepMonitor);
		
		DynamicDepManager creditCardPaymentDepMgr = NodeManager.getInstance().getDynamicDepManager(creditCardPaymentCompObj.getIdentifier());
		creditCardPaymentDepMgr.setTxLifecycleMgr(creditCardPaymentTxLifecycleMgr);
		creditCardPaymentDepMgr.setCompLifeCycleMgr(creditCardPaymentCompLifecycleManager);
		
		nodeMgr.getOndemandSetupHelper("CreditCardPayment");
		UpdateManager creditCardPaymentUpdateMgr = nodeMgr.getUpdateManageer("CreditCardPayment");
		CommServerManager.getInstance().start("CreditCardPayment");
		ServerIoHandler creditCardPaymentServerIoHandler = CommServerManager.getInstance().getCommServer("CreditCardPayment").getServerIOHandler();
		creditCardPaymentServerIoHandler.registerUpdateManager(creditCardPaymentUpdateMgr);
        
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
			LOGGER.fine(payment.makePaymentMember("c-0", 1000));
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}
}
