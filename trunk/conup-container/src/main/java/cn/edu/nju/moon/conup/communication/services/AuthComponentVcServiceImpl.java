package cn.edu.nju.moon.conup.communication.services;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Queue;
import java.util.Set;

//import org.apache.commons.httpclient.HostConfiguration;
import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.impl.DeployedComposite;
import org.apache.tuscany.sca.impl.NodeImpl;
import org.apache.tuscany.sca.implementation.java.JavaConstructorImpl;
import org.apache.tuscany.sca.implementation.java.context.ReflectiveInstanceFactory;
import org.apache.tuscany.sca.implementation.java.context.ReflectiveInstanceWrapper;
import org.apache.tuscany.sca.implementation.java.impl.JavaImplementationImpl;
import org.apache.tuscany.sca.implementation.java.invocation.JavaComponentContextProvider;
import org.apache.tuscany.sca.implementation.java.invocation.JavaImplementationProvider;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.oasisopen.sca.NoSuchServiceException;
import org.oasisopen.sca.annotation.Reference;

import cn.edu.nju.moon.conup.algorithm.VcAlgorithmImpl;
import cn.edu.nju.moon.conup.communication.convention.CompositeConvention;
import cn.edu.nju.moon.conup.communication.launcher.LaunchCommunication;
import cn.edu.nju.moon.conup.communication.services.OndemandService;
import cn.edu.nju.moon.conup.container.VcContainer;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.container.contribution.ContributionResolver;
import cn.edu.nju.moon.conup.container.contribution.DirectoryContributionResolver;
import cn.edu.nju.moon.conup.container.contribution.JarContributionResolver;
import cn.edu.nju.moon.conup.data.ArcRegistry;
import cn.edu.nju.moon.conup.data.InArcRegistryImpl;
import cn.edu.nju.moon.conup.data.MessageQueue;
import cn.edu.nju.moon.conup.data.OndemandThreadBuffer;
import cn.edu.nju.moon.conup.data.OutArcRegistryImpl;
import cn.edu.nju.moon.conup.data.TransactionRegistry;
import cn.edu.nju.moon.conup.data.TransactionRegistryImpl;
import cn.edu.nju.moon.conup.def.Arc;
import cn.edu.nju.moon.conup.def.ComponentStatus;
import cn.edu.nju.moon.conup.def.InterceptorCache;
import cn.edu.nju.moon.conup.def.InterceptorCacheImpl;
import cn.edu.nju.moon.conup.def.OldVersionRootTransation;
import cn.edu.nju.moon.conup.def.ReconfigurationVersion;
import cn.edu.nju.moon.conup.def.Scope;
import cn.edu.nju.moon.conup.def.SubTransaction;
import cn.edu.nju.moon.conup.def.TransactionDependency;
import cn.edu.nju.moon.conup.def.TransactionSnapshot;
import cn.edu.nju.moon.conup.domain.services.StaticConfigService;
import cn.edu.nju.moon.conup.domain.services.TransactionIDService;
import cn.edu.nju.moon.conup.printer.container.ContainerPrinter;
import cn.edu.nju.moon.conup.update.DynamicUpdate;
import cn.edu.nju.moon.conup.update.JavaDynamicUpdateImpl;
import cn.edu.nju.moon.conup.update.ReplaceClassLoader;

import org.oasisopen.sca.annotation.*;

@Service({ArcService.class,FreenessService.class,OndemandService.class, ComponentUpdateService.class})
public class AuthComponentVcServiceImpl implements ArcService, FreenessService, OndemandService,ComponentUpdateService {


	private CompositeResolver compositeResolver = new CompositeResolver();
	private static Map<String, Boolean> OndemandRequestStatus = new HashMap<String, Boolean>();
	private static Map<String, Boolean> ConfirmOndemandStatus = new HashMap<String, Boolean>();
	
	private final static Logger LOGGER = Logger.getLogger(AuthComponentVcServiceImpl.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	}
	
	@Override
	public void createArc(Arc arc) {
		InArcRegistryImpl.getInstance().addArc(arc);
	}

	@Override
	public void readArc() {

	}

	@Override
	public void update(Arc arc, String flag) {
		LOGGER.info("****AuthComponentVcServiceImpl.update(...) Arc: source= " + arc.getSourceComponent() + ", target=" + arc.getTargetComponent() + 
				"flag: " + flag);
//		System.out.println("AuthComponentVcServiceImpl.update(...) Arc: source=" + arc.getSourceComponent() + ", target=" + arc.getTargetComponent());
		if (flag.equals("parent")) {
			if (arc.getType().equals("future")) {
				OutArcRegistryImpl.getInstance().update(arc);
			}
		}

		if (flag.equals("sub")) {
			// System.out.println("notify sub...");
			if (arc.getType().equals("future")) {
				InArcRegistryImpl.getInstance().update(arc);
			}
		}
		
//		ContainerPrinter containerPrinter = new ContainerPrinter();
//		containerPrinter.printInArcRegistry(InArcRegistryImpl.getInstance());
//		containerPrinter.printOutArcRegistry(OutArcRegistryImpl.getInstance());
	}

//	@Override
	public void removeArc(Arc arc) {
		String hostCompName = compositeResolver.getHostComponentName();
		if(arc.getSourceComponent().equals(hostCompName)){
			OutArcRegistryImpl.getInstance().removeArc(arc);
		}else{
			InArcRegistryImpl.getInstance().removeArc(arc);
		}
	}

	@Override
	public boolean isFreeness(String componentName) {
		Set<Arc> inArcs = InArcRegistryImpl.getInstance().getArcs();
		Set<String> rootTransactionIds = new HashSet<String>();
		Iterator iterator = inArcs.iterator();
		while(iterator.hasNext()){
			Arc arc = (Arc) iterator.next();
			if(!rootTransactionIds.contains(arc.getRootTransaction()))
				rootTransactionIds.add(arc.getRootTransaction());
		}
		iterator = rootTransactionIds.iterator();
		while(iterator.hasNext()){
			Set<Arc> belongToSameRootID = InArcRegistryImpl.getInstance().getArcsViaRootTransaction((String)iterator.next());
			boolean hasFuture = false;
			boolean hasPast = false;
			for(Arc arc : belongToSameRootID){
				if(arc.getTargetComponent().equals(componentName) && arc.getType().equals("future"))
					hasFuture = true;
				if(arc.getTargetComponent().equals(componentName) && arc.getType().equals("past"))
					hasPast = true;
			}
			if(hasFuture && hasPast){
				return false;
			}
		}
		return true;
	}

	/**
	 * a setup(...) method is supposed to:
	 * 1) createArc(arc)
	 * 2) update the scope in ComponentStatus
	 * 3) invoke its sub-component recursively
	 */
	@Override
	public boolean setUp(Arc arc, Scope scope) {
		String setUpInfos = new String();
		setUpInfos += "****Set up: root = " + arc.getRootTransaction() + "\n";
//		LOGGER.info("****Set up: root = " + arc.getRootTransaction());
//		System.out.println("Set up: root = " + arc.getRootTransaction());
		String root = arc.getRootTransaction();
		String hostComponent = null;
		Set<String> targetRef = new HashSet<String>();
		
		hostComponent = compositeResolver.getHostComponentName();
		
		//add arc to InArcRegistry
		if(!arc.getSourceComponent().equals(arc.getTargetComponent())){
			InArcRegistryImpl.getInstance().addArc(arc);
		}
		
		if(scope == null)
			targetRef.addAll(compositeResolver.getReferenceComponents());
		else
			targetRef.addAll(scope.getSubComponents(hostComponent));
		
		setUpInfos += "****" + hostComponent + "'s targetRef:\n";
//		LOGGER.info("****" + hostComponent + "'s targetRef:");
//		System.out.println(hostComponent + "'s targetRef:");
		for(String component : targetRef){
			setUpInfos += "\t" + component +"\n";
//			System.out.println("\t" + component);
		}
		
		Class<AuthComponentVcServiceImpl> vcServiceImpl = AuthComponentVcServiceImpl.class;
		Field [] arcServiceFields = vcServiceImpl.getDeclaredFields();
		for(String subComponent : targetRef){
			for(Field field : arcServiceFields){
//				System.out.println("field.getType().getName()=" + field.getType().getName());
//				System.out.println("ArcService.class.getName()=" + ArcService.class.getName());
//				System.out.println("field.getName()=" + field.getName());
				if(field.getType().getName().equals(ArcService.class.getName())
					&& field.getName().toLowerCase().startsWith(subComponent.toLowerCase())){
					Arc futureArc = new Arc(Arc.FUTURE, root, hostComponent, subComponent, null, null);
//					System.out.println("Arc: host=" + hostComponent + ", sub=" + subComponent);
					setUpInfos += "Arc: host=" + hostComponent + ", sub=" + subComponent +"\n";
					ArcRegistry outArcRegistry = OutArcRegistryImpl.getInstance();
					outArcRegistry.addArc(futureArc);
					try {
						ArcService arcService = (ArcService)field.get(this);
						arcService.setUp(futureArc, scope);
//						System.out.println("this=" + this + ", arcService=" + arcService);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}//END FOR
		
		LOGGER.info(setUpInfos);
		
		LOGGER.info("**** >>in AuthComponentVcServiceImpl.setUp(...) print informations start:");
		ContainerPrinter containerPrinter = new ContainerPrinter();
		containerPrinter.printInArcRegistry(InArcRegistryImpl.getInstance());
		containerPrinter.printOutArcRegistry(OutArcRegistryImpl.getInstance());
		containerPrinter.printTransactionRegistry(TransactionRegistryImpl.getInstance());
		LOGGER.info("**** >>in AuthComponentVcServiceImpl.setUp(...) print informations end. " +
				"END OF setUp(...) for " + hostComponent);
		return true;
	}

//	@Override
	public boolean cleanUp(String rootID, Scope scope) {
//		LOGGER.info("**** Clean up: root = " + rootID);
//		System.out.println("Clean up: root = " + rootID);
		String removedArcsInfos = new String();
		
		//remove arc whose root is rootID from inArcRegistry
		ArcRegistry inArcRegistry = InArcRegistryImpl.getInstance();
		Iterator<Arc> inIterator = inArcRegistry.getArcs().iterator();
		while(inIterator.hasNext()){
			Arc arc = (Arc)inIterator.next();
			if(arc.getRootTransaction().equals(rootID)){
				String tmp = "removed InArc " + arc.toString() + "\n";
				removedArcsInfos += tmp;
//				System.out.println("removed InArc " + arc.toString());
				inIterator.remove();
			}
		}
		
		//remove arc whose root is rootID from outArcRegistry
		ArcRegistry outArcRegistry = OutArcRegistryImpl.getInstance();
		Iterator<Arc> outIterator = outArcRegistry.getArcs().iterator();
		while(outIterator.hasNext()){
			Arc arc = (Arc)outIterator.next();
			if(arc.getRootTransaction().equals(rootID)){
				String tmp = "removed OutArc " + arc.toString() + "\n";
				removedArcsInfos += tmp;
//				System.out.println("removed OutArc " + arc.toString());
				outIterator.remove();
			}
		}
		
		LOGGER.info("**** Clean up: root = " + rootID
				+ "\n" + removedArcsInfos);
		
		//remove root tx id from OldVersionRootTransation
		//OldVersionRootTransation is used to identify those root txs which are using the old version obj
		OldVersionRootTransation.getInstance().getOldRootTxIds().remove(rootID);
		if(OldVersionRootTransation.getInstance().getOldRootTxIds().isEmpty()
				&& ComponentStatus.getInstance().getCurrentStatus().equals(ComponentStatus.UPDATING)){
			ComponentStatus.getInstance().getNext();				// updating----------> updated
			LOGGER.info("**** cleanup:CurrentComponentStatus: " + ComponentStatus.getInstance().getCurrentStatus() + " : which is supposed to be UPDATED.");
//			System.out.println("cleanup:CurrentComponentStatus: " + ComponentStatus.getInstance().getCurrentStatus() + "which is supposed to be UPDATED.");
			ReconfigurationVersion rcfgVersion = ReconfigurationVersion.getInstance();
			rcfgVersion.setOldVersion(rcfgVersion.getNewVersion());
			try {
				rcfgVersion.getInstanceFactory().setCtr(rcfgVersion.getNewVersion().getConstructor());
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			ComponentStatus.getInstance().getNext();					// updated--------> activated
			LOGGER.info("**** cleanup:CurrentComponentStatus: " + ComponentStatus.getInstance().getCurrentStatus() + "which is supposed to be ACTIVATED.");
//			System.out.println("cleanup:CurrentComponentStatus: " + ComponentStatus.getInstance().getCurrentStatus() + "which is supposed to be ACTIVATED.");
			//TODO ACTIVATED------>NORMAL OR VALID
		}
		
		
		//remove transaction dependencies from TransactionRegistry
		//destroy transaction ID
		TransactionRegistry txRegistry;
		Iterator<Entry<String, TransactionDependency>> txIterator;
		txRegistry = TransactionRegistryImpl.getInstance();
		txIterator = txRegistry.getDependencies().entrySet().iterator();
		TransactionDependency dependency;
		String currentTx = null;
		Entry<String, TransactionDependency> entry;
		String cleanUpTxRegistry = new String();
		cleanUpTxRegistry += "**** Start clean up tx Registry, txRegistry.size()=" + txRegistry.getDependencies().size();
//		LOGGER.info("**** txRegistry.size()=" + txRegistry.getDependencies().size());
//		System.out.println("txRegistry.size()=" + txRegistry.getDependencies().size());
		while(txIterator.hasNext()){
			entry = txIterator.next();
			currentTx = entry.getKey();
			removeTransactionID(currentTx);
			dependency = entry.getValue();
			if(dependency.getRootTx().equals(rootID)){
				cleanUpTxRegistry += "\n\tremoved entry from txRegistry " + currentTx + ", " + dependency;
				cleanUpTxRegistry += "\n\ttxRegistry remainiing size()=" + txRegistry.getDependencies().size();
//				System.out.println("removed entry from txRegistry " + 
//						currentTx + ", " + dependency);
//				System.out.println("txRegistry remainiing size()=" + txRegistry.getDependencies().size());
				txIterator.remove();
			}
		}//END WHILE
		LOGGER.info(cleanUpTxRegistry);
		//notify sub components to clean up
		Class<AuthComponentVcServiceImpl> vcServiceImpl = AuthComponentVcServiceImpl.class;
		Field [] arcServiceFields = vcServiceImpl.getDeclaredFields();
		for (Field field : arcServiceFields) {
			if (field.getType().getName().equals(ArcService.class.getName())) {
				try {
					ArcService arcService = (ArcService)field.get(this);
					arcService.cleanUp(rootID, scope);
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}//END FOR
		
		LOGGER.info("**** >>in AuthComponentVcServiceImpl.cleanUp(...) print informations start:");
		ContainerPrinter containerPrinter = new ContainerPrinter();
//		System.out.println("InArcRegistry when cleanup:");
		containerPrinter.printInArcRegistry(inArcRegistry);
//		System.out.println("OutArcRegistry when cleanup:");
		containerPrinter.printOutArcRegistry(outArcRegistry);
//		System.out.println("TransactionRegistry when cleanup:");
		containerPrinter.printTransactionRegistry(txRegistry);
		LOGGER.info("**** >>in AuthComponentVcServiceImpl.cleanUp(...) print informations end");
//		System.out.println("//END OF cleanUp(...)");
		
		return true;
	}

	/** 
	 * A reqOndemandSetup(...) is sent by current(host) component's sub-component
	 * If currentComponent.equals(requestSourceComponent), it means this is a 
	 * request from domain manager
	 * @param currentComponent 
	 * @param current component's sub-component
	 * 
	 *  */
//	@Override
	public boolean reqOndemandSetup(String currentComponent,
			String requestSourceComponent,  Scope scope, String freenessSetup) {
		LOGGER.info("**** in AuthComponentVcServiceImpl.reqOndemandSetup(...):"+
			"\t" + "currentComponent=" + currentComponent +
			"\t" + "requestSourceComponent=" + requestSourceComponent);
//		System.out.println("reqOndemandSetup(...)");
//		System.out.println("\t" + "currentComponent=" + currentComponent);
//		System.out.println("\t" + "requestSourceComponent=" + requestSourceComponent);
		
		ComponentStatus componentStatus;
		String hostComponent = null;
		Set<String> targetRef;
		Set<String> parentComponents;
		
		componentStatus = ComponentStatus.getInstance();
		hostComponent = compositeResolver.getHostComponentName();
		targetRef = new HashSet<String>();
		
		String currentStatus = componentStatus.getCurrentStatus();
		if( !currentStatus.equals(ComponentStatus.NORMAL) ){
			LOGGER.info("**** duplicated reqOndemandSetup, " + "because current status is " + currentStatus);
//			System.out.println("duplicated reqOndemandSetup, " +
//					"because current status is " + currentStatus);
			return true;
		}
		
		//save scope and freenessSetup in ComponentStatus
		componentStatus.setScope(scope);
		componentStatus.setFreenessSetup(freenessSetup);
		
		//calculate target(sub) components
		if(scope == null)
			targetRef.addAll(compositeResolver.getReferenceComponents());
		else
			targetRef.addAll(scope.getSubComponents(hostComponent));
		
		//calculate parent components
		parentComponents = getParentComponents(hostComponent, scope);
		
		//init OndemandRequestStatus
		for(String subComponent : targetRef){
			if(!OndemandRequestStatus.containsKey(subComponent))
				OndemandRequestStatus.put(subComponent, false);
		}
		
		//init ConfirmOndemandStatus
		for(String component : parentComponents){
			if(!ConfirmOndemandStatus.containsKey(component))
				ConfirmOndemandStatus.put(component, false);
		}
		
		//FOR TEST
		String targetRefs = new String();
		for(String component : targetRef){
			targetRefs += "\n\t" + component;
		}
		LOGGER.info("**** " + hostComponent + "'s targetRef:"
				+ targetRefs);
		String parentComps = new String();
		for(String component : parentComponents){
			parentComps += "\n\t" + component;
		}
		LOGGER.info("**** " + hostComponent + "'s parents:"
				+ parentComps);
		
		//wait for other reqOndemandSetup(...)
//		if(!currentComponent.equals(requestSourceComponent)){
			receivedReqOndemandSetup(requestSourceComponent, hostComponent, parentComponents);
//		}

		return true;
	}
	
//	@Override
	public boolean confirmOndemandSetup(String parentComponent, 
			String currentComponent) {
		LOGGER.info("**** " + "confirmOndemandSetup(...) from " + parentComponent);
//		System.out.println("confirmOndemandSetup(...) from " + parentComponent);
		ComponentStatus componentStatus = ComponentStatus.getInstance();
		if (componentStatus.getCurrentStatus().equals(ComponentStatus.VALID)){
			LOGGER.info("**** component status is " + componentStatus.getCurrentStatus() + ", and return");
//			System.out.println("component status is " + componentStatus.getCurrentStatus() + ", and return");
			return true;
		}
		
		// update current component's ConfirmOndemandStatus
		if (ConfirmOndemandStatus.containsKey(parentComponent))
			ConfirmOndemandStatus.put(parentComponent, true);
		else
			LOGGER.info("Illegal status while confirmOndemandSetup(...)");
//			System.out.println("Illegal status while confirmOndemandSetup(...)");
		
		//print ConfirmOndemandStatus
		LOGGER.fine("ConfirmOndemandStatus:");
//		System.out.println("ConfirmOndemandStatus:");
		for(Entry entry : ConfirmOndemandStatus.entrySet()){
			System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
		}
		
		//isConfirmedAll?
		boolean isConfirmedAll = true;
		for (Entry entry : ConfirmOndemandStatus.entrySet()) {
			isConfirmedAll = isConfirmedAll && (Boolean) entry.getValue();
		}
		if(isConfirmedAll){
			//change current componentStatus to 'valid'
			LOGGER.info("confirmOndemandSetup(...) from " + parentComponent + 
					"Confirmed All, before changing mode to valid, component status is" + 
					componentStatus.getCurrentStatus());
//			System.out.println("confirmOndemandSetup(...) from " + parentComponent);
//			System.out.println("Confirmed All");
//			System.out.println("before changing mode to valid, component status is " + componentStatus.getCurrentStatus());
			if (componentStatus.getCurrentStatus().equals(ComponentStatus.ON_DEMAND)){
				componentStatus.getNext();
				LOGGER.info("after changing, component status is " + componentStatus.getCurrentStatus());
//				System.out.println("after changing, component status is " + componentStatus.getCurrentStatus());
			} else
				LOGGER.info("Invalid component status found while confirmOndemandSetup(...)");
//				System.out.println("Invalid component status found while confirmOndemandSetup(...)");
			//send confirmOndemandSetup(...)
			sendConfirmOndemandSetup(currentComponent);
		}
		
		return true;
	}

	public boolean onDemandSetUp() {
		
		
		System.out.println("onDemandSetUp()");
		
		ArcRegistry inArcRegistry = InArcRegistryImpl.getInstance();
		ArcRegistry outArcRegistry = OutArcRegistryImpl.getInstance();
//		InterceptorCache cache = InterceptorCacheImpl.getInstance();
		TransactionRegistry txRegistry = TransactionRegistryImpl.getInstance();
		Set<Arc> fArcs = new HashSet<Arc>();
		Set<Arc> pArcs = new HashSet<Arc>();
		Set<Arc> sArcs = new HashSet<Arc>();
		String hostComponent = compositeResolver.getHostComponentName();
		
//		Iterator<Entry<String, TransactionDependency>> iterator = 
//				cache.getDependencies().iterator();
		Iterator<Entry<String, TransactionDependency>> iterator = 
				txRegistry.getDependencies().entrySet().iterator();
		while(iterator.hasNext()){
			TransactionDependency dependency = 
					(TransactionDependency)iterator.next().getValue();
			String root = dependency.getRootTx();
			String current = dependency.getCurrentTx();
			
			//in this case, it means current entry in interceptorCache is invalid
			if(root == null || current == null){
				LOGGER.warning("Invalid data found while onDemandSetUp");
//				System.out.println("Invalid data found while onDemandSetUp");
				continue;
			}
			
			Arc lfe = new Arc();
			lfe.setType(Arc.FUTURE);
			lfe.setRootTransaction(root);
			lfe.setSourceComponent(hostComponent);
			lfe.setTargetComponent(hostComponent);
			lfe.setSourceService(null);
			lfe.setTargetService(null);
			
			Arc lpe = new Arc();
			lpe.setType(Arc.PAST);
			lpe.setRootTransaction(root);
			lpe.setSourceComponent(hostComponent);
			lpe.setTargetComponent(hostComponent);
			lpe.setSourceService(null);
			lpe.setTargetService(null);
			
			inArcRegistry.addArc(lfe);
			inArcRegistry.addArc(lpe);
			outArcRegistry.addArc(lfe);
			outArcRegistry.addArc(lpe);
			
			//if current transaction is not a root transaction
			if(!root.equals(current)){
				System.out.println("current tx is " + current + ",and root is " + root);
				continue;
			}
			
			//if current is root 
			System.out.println(current + " is a root tx");
			
			fArcs = getFArcs(hostComponent, root);
			System.out.println("fArcs:");
			Class<AuthComponentVcServiceImpl> vcServiceImpl = AuthComponentVcServiceImpl.class;
			Field [] arcServiceFields = vcServiceImpl.getDeclaredFields();
			String subComponent;
			for(Arc arc : fArcs){
				arc.setType(Arc.FUTURE);
				arc.setRootTransaction(root);
				System.out.println("\t" + arc.toString());
				if(!outArcRegistry.contain(arc)){
					//add arc to OutArcRegistry
					outArcRegistry.addArc(arc);
					//notifyFutureOndemand
					for(Field field : arcServiceFields){
						subComponent = arc.getTargetComponent();
						if(field.getType().getName().equals(OndemandService.class.getName())
							&& field.getName().toLowerCase().startsWith(subComponent.toLowerCase())){
							System.out.println("\t" + "to notify future ondemand " + arc.toString());
							try {
								OndemandService ondemandService = (OndemandService)field.get(this);
								ondemandService.notifyFutureOndemand(arc);
							} catch (SecurityException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}//END IF
					}//END FOR
				}
			}//END FOR
			
			pArcs = getPArcs(hostComponent, root);
			System.out.println("pArcs:");
			for(Arc arc : pArcs){
				arc.setType(Arc.PAST);
				arc.setRootTransaction(root);
				System.out.println("\t" + arc.toString());
				if(!outArcRegistry.contain(arc)){
					//add arc to OutArcRegistry
					outArcRegistry.addArc(arc);
					//notifyPastOndemand
					for(Field field : arcServiceFields){
						subComponent = arc.getTargetComponent();
						if(field.getType().getName().equals(OndemandService.class.getName())
							&& field.getName().toLowerCase().startsWith(subComponent.toLowerCase())){
							System.out.println("\t" + "to notify past ondemand " + arc.toString());
							try {
								OndemandService ondemandService = (OndemandService)field.get(this);
								ondemandService.notifyPastOndemand(arc);
							} catch (SecurityException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}//END IF
					}//END FOR
				}
			}//END FOR
			
			sArcs = getSArcs(hostComponent, root);
			System.out.println("sArcs:");
			for(Arc arc : sArcs){
				arc.setRootTransaction(root);
				arc.setType(Arc.FUTURE);
				if(!fArcs.contains(arc)){
					System.out.println("\t" + "!fArcs.contains(arc)" + arc.toString());
					//notifySubFutureOndemand
					for(Field field : arcServiceFields){
						subComponent = arc.getTargetComponent();
						if(field.getType().getName().equals(OndemandService.class.getName())
							&& field.getName().toLowerCase().startsWith(subComponent.toLowerCase())){
							System.out.println("\t" + "to notify sub future ondemand " + arc.toString());
							try {
								OndemandService ondemandService = (OndemandService)field.get(this);
								ondemandService.notifySubFutureOndemand(arc);
							} catch (SecurityException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}//END IF
					}//END FOR
				}
				arc.setType(Arc.PAST);
				if(!pArcs.contains(arc)){
					System.out.println("\t" + "!pArcs.contains(arc)" + arc.toString());
					//notifySubPastOndemand
					for(Field field : arcServiceFields){
						subComponent = arc.getTargetComponent();
						if(field.getType().getName().equals(OndemandService.class.getName())
							&& field.getName().toLowerCase().startsWith(subComponent.toLowerCase())){
							System.out.println("\t" + "to notify sub past ondemand " + arc.toString());
							try {
								OndemandService ondemandService = (OndemandService)field.get(this);
								ondemandService.notifySubPastOndemand(arc);
							} catch (SecurityException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}//END IF
					}//END FOR
				}
			}//ENF FOR
			
		}//END WHILE
		
		LOGGER.info("\n**** >>in AuthComponentVcServiceImpl.onDemandSetUp(...) print informations start:");
		ContainerPrinter containerPrinter = new ContainerPrinter();
//		System.out.println("InArcRegistry after onDemandSetUp():");
		containerPrinter.printInArcRegistry(InArcRegistryImpl.getInstance());
//		System.out.println("OutArcRegistry after onDemandSetUp():");
		containerPrinter.printOutArcRegistry(OutArcRegistryImpl.getInstance());
//		System.out.println("TransactionRegistry after onDemandSetUp():");
		containerPrinter.printTransactionRegistry(TransactionRegistryImpl.getInstance());
		LOGGER.info("\n**** <<in AuthComponentVcServiceImpl.onDemandSetUp(...) print informations end.");
		
		return true;
	}

//	@Override
	public boolean notifyFutureOndemand(Arc arc) {
		System.out.println("notifyFutureOndemand(Arc arc) with " + arc.toString());
		ArcRegistry inArcRegistry = InArcRegistryImpl.getInstance();
		ArcRegistry outArcRegistry = OutArcRegistryImpl.getInstance();
		String hostComponent;
		String root;
		Set<String> targetRef;
		Scope scope;
		
		hostComponent = arc.getTargetComponent();
		root = arc.getRootTransaction();
		targetRef = new HashSet<String>();
		scope = ComponentStatus.getInstance().getScope();
		
		if(scope == null)
			targetRef.addAll(compositeResolver.getReferenceComponents());
		else
			targetRef.addAll(scope.getSubComponents(hostComponent));
		
		inArcRegistry.addArc(arc);
		
		Class<AuthComponentVcServiceImpl> vcServiceImpl = AuthComponentVcServiceImpl.class;
		Field [] arcServiceFields = vcServiceImpl.getDeclaredFields();
		for(String subComponent : targetRef){
			for(Field field : arcServiceFields){
				if(field.getType().getName().equals(OndemandService.class.getName())
					&& field.getName().toLowerCase().startsWith(subComponent.toLowerCase())){
					Arc futureArc = new Arc(Arc.FUTURE, root, hostComponent, subComponent, null, null);
					if(outArcRegistry.contain(futureArc)){
						continue;
					}
					System.out.println("to notify future Ondemand " + futureArc.toString());
					outArcRegistry.addArc(futureArc);
					try {
						OndemandService ondemandService = (OndemandService)field.get(this);
						ondemandService.notifyFutureOndemand(futureArc);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}//END IF
			}//END FOR
		}//END FOR
		
		return true;
	}

//	@Override
	public boolean notifyPastOndemand(Arc arc) {
		System.out.println("notifyPastOndemand(Arc arc) with " + arc.toString());
		ArcRegistry inArcRegistry = InArcRegistryImpl.getInstance();
		ArcRegistry outArcRegistry = OutArcRegistryImpl.getInstance();
		String hostComponent;
		String root;
		Set<String> targetRef;
		Scope scope;
		
		hostComponent = arc.getTargetComponent();
		root = arc.getRootTransaction();
		targetRef = new HashSet<String>();
		scope = ComponentStatus.getInstance().getScope();
		
		if(scope == null)
			targetRef.addAll(compositeResolver.getReferenceComponents());
		else
			targetRef.addAll(scope.getSubComponents(hostComponent));
		
		inArcRegistry.addArc(arc);
		
		Class<AuthComponentVcServiceImpl> vcServiceImpl = AuthComponentVcServiceImpl.class;
		Field [] arcServiceFields = vcServiceImpl.getDeclaredFields();
		for(String subComponent : targetRef){
			for(Field field : arcServiceFields){
				if(field.getType().getName().equals(OndemandService.class.getName())
					&& field.getName().toLowerCase().startsWith(subComponent.toLowerCase())){
					Arc pastArc = new Arc(Arc.PAST, root, hostComponent, subComponent, null, null);
					if(outArcRegistry.contain(pastArc)){
						continue;
					}
					System.out.println("to notify past Ondemand " + pastArc.toString());
					outArcRegistry.addArc(pastArc);
					try {
						OndemandService ondemandService = (OndemandService)field.get(this);
						ondemandService.notifyPastOndemand(pastArc);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}//END IF
			}
		}//END FOR
		
		return true;
	}

//	@Override
	public boolean notifySubFutureOndemand(Arc arc) {
		System.out.println("notifySubFutureOndemand(Arc arc) with " + arc.toString());
		ArcRegistry outArcRegistry = OutArcRegistryImpl.getInstance();
		String hostComponent;
		String root;
		Set<String> targetRef;
		Scope scope;
		String subTx = null;
		Set<Arc> fArcs = new HashSet<Arc>();
		Set<Arc> sArcs = new HashSet<Arc>();
		
		hostComponent = arc.getTargetComponent();
		root = arc.getRootTransaction();
		targetRef = new HashSet<String>();
		scope = ComponentStatus.getInstance().getScope();
		subTx = getHostSubTransaction(root);
		
		if(scope == null)
			targetRef.addAll(compositeResolver.getReferenceComponents());
		else
			targetRef.addAll(scope.getSubComponents(hostComponent));
		
		Class<AuthComponentVcServiceImpl> vcServiceImpl = AuthComponentVcServiceImpl.class;
		Field [] arcServiceFields = vcServiceImpl.getDeclaredFields();
		
		fArcs = getFArcs(hostComponent, subTx);
		System.out.println("fArcs:");
		for(Arc tmpArc : fArcs){
			String subComponent = tmpArc.getTargetComponent();
			tmpArc.setType(Arc.FUTURE);
			tmpArc.setRootTransaction(root);
			if(outArcRegistry.contain(tmpArc))
				continue;
			tmpArc.setType(Arc.PAST);
			if(outArcRegistry.contain(tmpArc)){
				outArcRegistry.removeArc(tmpArc);
				continue;
			}
			for(Field field : arcServiceFields){
				if(field.getType().getName().equals(OndemandService.class.getName())
					&& field.getName().toLowerCase().startsWith(subComponent.toLowerCase())){
					Arc futureArc = new Arc(Arc.FUTURE, root, hostComponent, subComponent, null, null);
					System.out.println("\tto notify future ondemand " + futureArc.toString());
					outArcRegistry.addArc(futureArc);
					try {
						OndemandService ondemandService = (OndemandService)field.get(this);
						ondemandService.notifyFutureOndemand(futureArc);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}//END IF
			}
		}//END FOR
		
		sArcs = getSArcs(hostComponent, subTx);
		System.out.println("sArcs:");
		for(Arc tmpArc : sArcs){
			String subComponent = tmpArc.getTargetComponent();
			tmpArc.setType(Arc.FUTURE);
			tmpArc.setRootTransaction(root);
			if(fArcs.contains(tmpArc))
				continue;
			tmpArc.setType(Arc.PAST);
			if(fArcs.contains(tmpArc))
				continue;
			for(Field field : arcServiceFields){
				if(field.getType().getName().equals(OndemandService.class.getName())
					&& field.getName().toLowerCase().startsWith(subComponent.toLowerCase())){
					Arc futureArc = new Arc(Arc.FUTURE, root, hostComponent, subComponent, null, null);
					System.out.println("\tto notify sub future ondemand " + futureArc.toString());
//					outArcRegistry.addArc(futureArc);
					try {
						OndemandService ondemandService = (OndemandService)field.get(this);
						ondemandService.notifySubFutureOndemand(futureArc);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}//END IF
			}
		}//END FOR
		
		return true;
	}

	@Override
	public boolean notifySubPastOndemand(Arc arc) {
		System.out.println("notifySubPastOndemand(Arc arc) with " + arc.toString());
		ArcRegistry outArcRegistry = OutArcRegistryImpl.getInstance();
		String hostComponent;
		String root;
		Set<String> targetRef;
		Scope scope;
		String subTx = null;
		Set<Arc> pArcs = new HashSet<Arc>();
		Set<Arc> sArcs = new HashSet<Arc>();
		
		hostComponent = arc.getTargetComponent();
		root = arc.getRootTransaction();
		targetRef = new HashSet<String>();
		scope = ComponentStatus.getInstance().getScope();
		subTx = getHostSubTransaction(root);
		
		if(scope == null)
			targetRef.addAll(compositeResolver.getReferenceComponents());
		else
			targetRef.addAll(scope.getSubComponents(hostComponent));
		
		Class<AuthComponentVcServiceImpl> vcServiceImpl = AuthComponentVcServiceImpl.class;
		Field [] arcServiceFields = vcServiceImpl.getDeclaredFields();
		
		pArcs = getPArcs(hostComponent, subTx);
		System.out.println("pArcs:");
		for(Arc tmpArc : pArcs){
			String subComponent = tmpArc.getTargetComponent();
			tmpArc.setType(Arc.PAST);
			tmpArc.setRootTransaction(root);
			if(outArcRegistry.contain(tmpArc))
				continue;
			tmpArc.setType(Arc.FUTURE);
			if(outArcRegistry.contain(tmpArc)){
				outArcRegistry.removeArc(tmpArc);
//				continue;
			}
			for(Field field : arcServiceFields){
				if(field.getType().getName().equals(OndemandService.class.getName())
					&& field.getName().toLowerCase().startsWith(subComponent.toLowerCase())){
					Arc pastArc = new Arc(Arc.PAST, root, hostComponent, subComponent, null, null);
					System.out.println("\tto notify past ondemand " + pastArc.toString());
					outArcRegistry.addArc(pastArc);
					try {
						OndemandService ondemandService = (OndemandService)field.get(this);
						ondemandService.notifyPastOndemand(pastArc);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}//END IF
			}
		}//END FOR
		
		sArcs = getSArcs(hostComponent, subTx);
		System.out.println("sArcs:");
		for(Arc tmpArc : sArcs){
			String subComponent = tmpArc.getTargetComponent();
			tmpArc.setType(Arc.PAST);
			tmpArc.setRootTransaction(root);
			if(pArcs.contains(tmpArc))
				continue;
			tmpArc.setType(Arc.FUTURE);
			if(pArcs.contains(tmpArc))
				continue;
			for(Field field : arcServiceFields){
				if(field.getType().getName().equals(OndemandService.class.getName())
					&& field.getName().toLowerCase().startsWith(subComponent.toLowerCase())){
					Arc pastArc = new Arc(Arc.PAST, root, hostComponent, subComponent, null, null);
					System.out.println("\tto notify sub past ondemand " + pastArc.toString());
//					outArcRegistry.addArc(pastArc);
					try {
						OndemandService ondemandService = (OndemandService)field.get(this);
						ondemandService.notifySubFutureOndemand(pastArc);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}//END IF
			}
		}//END FOR

		return true;
	}
	
	private void receivedReqOndemandSetup(
			String requestSourceComponent, String hostComponent, Set<String> parentComponents){
		// update current component's OndemandRequestStatus
		if (OndemandRequestStatus.containsKey(requestSourceComponent))
			OndemandRequestStatus.put(requestSourceComponent, true);
		else
			System.out.println("OndemandRequestStatus doesn't contain " + requestSourceComponent);
		
		//print OndemandRequestStatus
		System.out.println("OndemandRequestStatus:");
		for(Entry entry : OndemandRequestStatus.entrySet()){
			System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
		}

		/*
		 * To judge whether current component has received reqOndemandSetup(...)
		 * from every in-scope outgoing static edge
		 */
		boolean isReceivedAll = true;
		for (Entry entry : OndemandRequestStatus.entrySet()) {
			isReceivedAll = isReceivedAll && (Boolean) entry.getValue();
		}

		// if received all
		ComponentStatus componentStatus = ComponentStatus.getInstance();
		Scope scope = componentStatus.getScope();
		if (isReceivedAll) {
			System.out.println("Received reqOndemandSetup(...) from " + requestSourceComponent);
			System.out.println("Received all reqOndemandSetup(...)");
			System.out.println("Before changing mode to ondemand, component status=" + componentStatus.getCurrentStatus());
			//change current componentStatus to 'ondemand'
			if (componentStatus.getCurrentStatus().equals(ComponentStatus.NORMAL)){
				componentStatus.getNext();
				System.out.println("After changing, component status is " + componentStatus.getCurrentStatus());
			}
			else
				System.out.println("Invalid component status found while reqOndemandSetup(...)");
			//send reqOndemandSetup(...) to parent components
			sendReqOndemandSetup(parentComponents, hostComponent);
			//onDemandSetUp
			onDemandSetUp();
			//isConfirmedAll?
			boolean isConfirmedAll = true;
			for (Entry entry : ConfirmOndemandStatus.entrySet()) {
				isConfirmedAll = isConfirmedAll && (Boolean) entry.getValue();
			}
			
			//print ConfirmOndemandStatus
			System.out.println("ConfirmOndemandStatus:");
			for(Entry entry : ConfirmOndemandStatus.entrySet()){
				System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
			}
			
			if(isConfirmedAll){
				if(componentStatus.getCurrentStatus().equals(ComponentStatus.VALID)){
					System.out.println("Confirmed all, and component status=" + componentStatus.getCurrentStatus());
					return;
				}
				System.out.println("Confirmed from all parent components in receivedReqOndemandSetup(...)");
				System.out.println("Before changing mode to valid, component status=" + componentStatus.getCurrentStatus());
				//change current componentStatus to 'valid'
				if (componentStatus.getCurrentStatus().equals(ComponentStatus.ON_DEMAND)){
					componentStatus.getNext();
					System.out.println("After changing, component status=" + componentStatus.getCurrentStatus());
				} else
					System.out.println("Invalid component status found while reqOndemandSetup(...)");
				//send confirmOndemandSetup(...)
				sendConfirmOndemandSetup(hostComponent);
			}
		}//END IF

	}
	
	private boolean sendReqOndemandSetup(
			Set<String> parentComponents, String hostComponent){
		System.out.println("sendReqOndemandSetup(...) to parent components:");
		for(String component : parentComponents)
			System.out.println("\t" + component);
		
		ComponentStatus componentStatus = ComponentStatus.getInstance();
		Scope scope = componentStatus.getScope();
		String freenessSetup = componentStatus.getFreenessSetup();
		Node communicationNode = LaunchCommunication.node;
		
//		String endpoint = null;
//		OndemandService ondemandService;
//		for(String component : parentComponents){
//			endpoint = component + "Comm#service-binding(OndemandService/OndemandService)";
//			try {
//				ondemandService = communicationNode.getService(OndemandService.class,endpoint);
//				ondemandService.reqOndemandSetup(component, hostComponent, scope, freenessSetup);
//			} catch (NoSuchServiceException e) {
//				e.printStackTrace();
//			}
//		}//END FOR
		
		ReqOndemandSetupSender reqSender;
		for(String component : parentComponents){
			//start a new thread to send ReqOndemandSetup(...)
			reqSender = new ReqOndemandSetupSender(
					communicationNode, component, hostComponent, scope, freenessSetup);
			reqSender.start();
		}//END FOR
		
		return true;
	}
	
	private void sendConfirmOndemandSetup(String hostComponent){
		Set<String> targetRef;
		Scope scope;
		
		targetRef = new HashSet<String>();
		scope = ComponentStatus.getInstance().getScope();
		
		if(scope == null)
			targetRef.addAll(compositeResolver.getReferenceComponents());
		else
			targetRef.addAll(scope.getSubComponents(hostComponent));
		
		System.out.println("sendConfirmOndemandSetup(...) to sub components:");
		for(String component : targetRef)
			System.out.println("\t" + component);
		
		Class<AuthComponentVcServiceImpl> vcServiceImpl = AuthComponentVcServiceImpl.class;
		Field [] arcServiceFields = vcServiceImpl.getDeclaredFields();
		for(String subComponent : targetRef){
			for(Field field : arcServiceFields){
//				System.out.println("field.getType().getName()=" + field.getType().getName());
//				System.out.println("ArcService.class.getName()=" + OndemandService.class.getName());
//				System.out.println("field.getName()=" + field.getName());
				if(field.getType().getName().equals(OndemandService.class.getName())
					&& field.getName().toLowerCase().startsWith(subComponent.toLowerCase())){
					try {
						OndemandService ondemandService = (OndemandService)field.get(this);
//						ondemandService.confirmOndemandSetup(hostComponent, subComponent);
						//start a new thread
						ConfirmOndemandSetupSender confirmSender;
						confirmSender = new ConfirmOndemandSetupSender(
								ondemandService, hostComponent, subComponent);
						confirmSender.start();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}//END IF
			}
		}//END FOR
		
		System.out.println("ConfirmOndemandSetup is sent, now notifyAll()...");
		OndemandThreadBuffer threadBuffer;
		Set<Runnable> threads;
		threadBuffer = OndemandThreadBuffer.getInstance();
		threads = threadBuffer.getThreads();
		Iterator<Runnable> iteratorThreads;
		iteratorThreads = threads.iterator();
		synchronized (threadBuffer) {
			threadBuffer.notifyAll();
//			while(iteratorThreads.hasNext()){
//				iteratorThreads.next().notify();
//			}
			threads.clear();
		}
		
	}
	
	private Set<Arc> getFArcs(String hostComponent, String transactionID){
		Set<Arc> result = new HashSet<Arc>();
		Set<String> futureC = new HashSet<String>();
		TransactionRegistry txRegistry;
		txRegistry = TransactionRegistryImpl.getInstance();
		String root = null;
		
		//read transaction dependencies from TransactionRegistry
		Iterator<Entry<String, TransactionDependency>> txIterator;
		txIterator = txRegistry.getDependencies().entrySet().iterator();
		TransactionDependency dependency;
		while (txIterator.hasNext()) {
			dependency = txIterator.next().getValue();
			if (dependency.getRootTx().equals(transactionID) 
				|| dependency.getCurrentTx().equals(transactionID)) {
				if(dependency.getFutureComponents() != null
					|| dependency.getFutureComponents().size()!=0){
					root = dependency.getRootTx();
					futureC.addAll(dependency.getFutureComponents());
					break;
				}
			}
		}// END WHILE
		
		for(String component : futureC){
			Arc arc = new Arc();
			arc.setType(Arc.FUTURE);
			arc.setRootTransaction(root);
			arc.setSourceComponent(hostComponent);
			arc.setTargetComponent(component);
			result.add(arc);
		}//END FOR
		
		//FOR TEST
		System.out.println("In getFArcs(...), size=" + result.size());
		for(Arc arc : result){
			System.out.println("\t" + arc.toString());
		}
		
		return result;
	}
	
	private Set<Arc> getPArcs(String hostComponent, String transactionID){
		Set<Arc> result = new HashSet<Arc>();
		Set<String> pastC = new HashSet<String>();
		TransactionRegistry txRegistry;
		txRegistry = TransactionRegistryImpl.getInstance();
		String root = null;
		
		//read transaction dependencies from TransactionRegistry
		Iterator<Entry<String, TransactionDependency>> txIterator;
		txIterator = txRegistry.getDependencies().entrySet().iterator();
		TransactionDependency dependency;
		while (txIterator.hasNext()) {
			dependency = txIterator.next().getValue();
			if (dependency.getRootTx().equals(transactionID) 
				|| dependency.getCurrentTx().equals(transactionID)) {
//				if(dependency.getStatus().equals(TransactionSnapshot.END)){
					if(dependency.getPastComponents() != null
						|| dependency.getPastComponents().size()!=0){
						root = dependency.getRootTx();
						pastC.addAll(dependency.getPastComponents());
//						System.out.println("dependency.getPastComponents().size()=" +
//								dependency.getPastComponents().size());
						break;
					}
//				}
			}
		}// END WHILE
		
		for(String component : pastC){
			Arc arc = new Arc();
			arc.setType(Arc.PAST);
			arc.setRootTransaction(root);
			arc.setSourceComponent(hostComponent);
			arc.setTargetComponent(component);
			result.add(arc);
		}//END FOR
		
		//FOR TEST
		
		System.out.println("In getPArcs(...), size=" + result.size());
		for(Arc arc : result){
			System.out.println("\t" + arc.toString());
		}
		
		return result;
	}
	
	private Set<Arc> getSArcs(String hostComponent, String transactionID){
		Set<Arc> result = new HashSet<Arc>();
		Set<String> ongoingC = new HashSet<String>();
		TransactionRegistry txRegistry;
		txRegistry = TransactionRegistryImpl.getInstance();
		String root = null;
		
		//read transaction dependencies from TransactionRegistry
		Iterator<Entry<String, TransactionDependency>> txIterator;
		Iterator<Entry<String, SubTransaction>> subTxIterator;
		txIterator = txRegistry.getDependencies().entrySet().iterator();
		TransactionDependency dependency;
		SubTransaction subTx;
		while (txIterator.hasNext()) {
			dependency = txIterator.next().getValue();
			if (dependency.getRootTx().equals(transactionID) 
				|| dependency.getCurrentTx().equals(transactionID)) {
				root = dependency.getRootTx();
				if(dependency.getSubTxs() == null){
					continue;
				}
				subTxIterator = dependency.getSubTxs().entrySet().iterator();
				Entry<String, SubTransaction> subTxEntry;
				while(subTxIterator.hasNext()){
					subTxEntry = subTxIterator.next();
					subTx = subTxEntry.getValue();
					if(subTx.getSubTxStatus().equals(TransactionSnapshot.START)
						|| subTx.getSubTxStatus().equals(TransactionSnapshot.RUNNING)){
						ongoingC.add(subTx.getSubTxHost());
					}
				}//END WHILE
				
//				if( !dependency.getStatus().equals(TransactionSnapshot.END) ){
//					if(dependency.getPastComponents() != null
//							|| dependency.getPastComponents().size()!=0){
//						root = dependency.getRootTx();
//						ongoingC.addAll(dependency.getPastComponents());
//						break;
//					}
//				}
			}//END IF
		}// END WHILE
		
		for(String component : ongoingC){
			Arc arc = new Arc();
			arc.setType(Arc.FUTURE);
			arc.setRootTransaction(root);
			arc.setSourceComponent(hostComponent);
			arc.setTargetComponent(component);
			result.add(arc);
		}//END FOR
		
		//FOR TEST
		System.out.println("In getSArcs(...), size=" + result.size());
		for(Arc arc : result){
			System.out.println("\t" + arc.toString());
		}
		
		return result;
	}
	
	private String getHostSubTransaction(String root){
		String subTx = null;
		TransactionRegistry txRegistry;
		txRegistry = TransactionRegistryImpl.getInstance();
		
		//query TransactionRegistry
		Iterator<Entry<String, TransactionDependency>> txIterator;
		txIterator = txRegistry.getDependencies().entrySet().iterator();
		TransactionDependency dependency;
		String currentTx = null;
		Entry<String, TransactionDependency> entry;
		while (txIterator.hasNext()) {
			entry = txIterator.next();
			currentTx = entry.getKey();
			dependency = entry.getValue();
			if (dependency.getRootTx().equals(root) 
				&& dependency.getCurrentTx().equals(currentTx)) {
				subTx = currentTx;
			}
		}// END WHILE
		
		System.out.println("getHostSubTransaction(" + root + ")=" + subTx);
		return subTx;
	}
	
	private Set<String>  getParentComponents(String hostComponent, Scope scope){
		Set<String> parentComponents = new HashSet<String>();
		Node communicationNode = LaunchCommunication.node;
		
		if(scope != null){
			parentComponents = scope.getParentComponents(hostComponent);
		} else{
			String endpoint = null;
			StaticConfigService staticConfigService;
			endpoint = "DomainManagerComponent#service-binding(StaticConfigService/StaticConfigService)";
			try {
				staticConfigService = communicationNode.getService(StaticConfigService.class, endpoint);
				parentComponents = staticConfigService.getParentComponents(hostComponent);
			} catch (NoSuchServiceException e) {
				e.printStackTrace();
			}
		}//END ELSE
			
		return parentComponents;
	}
	
	private boolean removeTransactionID(String id){
		boolean result = false;
		Node communicationNode = LaunchCommunication.node;
		String targetEndpoint = 
				"DomainManagerComponent#service-binding(TransactionIDService/TransactionIDService)";
		TransactionIDService transactionIDService;
		try {
			transactionIDService = communicationNode.getService(
					TransactionIDService.class, targetEndpoint);
			result = transactionIDService.removeID(id);
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private class CompositeResolver{
//		private String compositeUri = null;
		private URL compositeURL = null;
		private String hostComponent = null;
		private Set<String> referenceComponents = new HashSet<String>(); 
		
		public CompositeResolver(){
//			getCompositeUri();
//			resolve();
			VcContainer container = null;
			String absContributionPath = null;
			String compositeFileName = null;
			ContributionResolver resolver = null;
			
			container = VcContainerImpl.getInstance();
			absContributionPath = container.getAbsContributionPath();
			compositeFileName = container.getCompositeFileName();
			if(absContributionPath.endsWith(".jar")){
				resolver = new JarContributionResolver();
			} else{
				resolver = new DirectoryContributionResolver();
			}
			compositeURL = resolver.getCompositeURL(absContributionPath, compositeFileName);
			
			resolve();
		}
		
		public String getHostComponentName(){
			return hostComponent;
		}
		
		public Set<String> getReferenceComponents() {
			return referenceComponents;
		}
		
		private void resolve(){
			try {
				SAXBuilder sb = new SAXBuilder();
//				Document doc = sb.build(compositeUri);
				Document doc = sb.build(compositeURL);
				Element root = doc.getRootElement();
				Namespace ns = Namespace.getNamespace("http://docs.oasis-open.org/ns/opencsa/sca/200912");
				Element component = root.getChild("component", ns);
				hostComponent = component.getAttributeValue("name");
				List referenceList = component.getChildren("reference",ns);
				Iterator iterator = referenceList.iterator();
				while (iterator.hasNext()) {
					Element reference = (Element) iterator.next();
					List binding = reference.getChildren();
					Iterator bindingIterator = binding.iterator();
					while (bindingIterator.hasNext()) {
						Element specificBinding = (Element) bindingIterator.next();
						String bindingUri = specificBinding.getAttributeValue("uri");
						int lastIndex = bindingUri.lastIndexOf("/");
						String subStr = bindingUri.substring(0, lastIndex);
						lastIndex = subStr.lastIndexOf("/");
						String targetComponentName = subStr.substring(lastIndex + 1, subStr.length());
						referenceComponents.add(targetComponentName);
						break;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}//END TRY
		}
		
//		private void getCompositeUri(){
//			String baseUri = new File("").getAbsolutePath();
//			String compositeLocation = baseUri + File.separator + 
//					"src" + File.separator +"main" + File.separator + "resources" + File.separator;
//			File [] files = new File(compositeLocation).listFiles();
//			for(File file : files){
//				if(file.isFile() && 
//					file.getName().contains(CompositeConvention.CompositeExtension)){
//					compositeUri = compositeLocation + file.getName();
//					break;
//				}
//			}
//		}
	}//END CompositeResolver

	@Override
	public void notifySubTxStatus(String parentTx, 
			String subTxID, String subTxHost, String subTxStatus){
		LOGGER.info("Notified by subTx: " + subTxID + " on " + subTxHost + " status:" + subTxStatus);
//		System.out.println("Notified " + subTxStatus + " of subTx " + subTxID + " on " + subTxHost);
		
		TransactionRegistry txRegistry;
		TransactionDependency dependency;
		Map<String, SubTransaction> subTxs;
		SubTransaction subTx;
		
		txRegistry = TransactionRegistryImpl.getInstance();
		dependency = txRegistry.getDependency(parentTx);
		String rootTxID = dependency.getRootTx();
		
		subTxs = dependency.getSubTxs();
		subTx = dependency.getSubTx(subTxID);
		if(subTx == null){
			LOGGER.info("**** subTx is not in txRegistry");
//			System.out.println("subTx is null in txRegistry");
			if(subTxStatus.equals(TransactionSnapshot.START)
				|| subTxStatus.equals(TransactionSnapshot.RUNNING)){
				subTx = new SubTransaction();
				subTx.setSubTxHost(subTxHost);
				subTx.setSubTxStatus(subTxStatus);
				subTxs.put(subTxID, subTx);
			} else{
				LOGGER.warning("**** Illegal sub transaction status(" + 
						subTxStatus + ")in notifySubTxStatus(...)");
//				System.out.println("Illegal sub transaction status(" + 
//						subTxStatus + ")in notifySubTxStatus(...)");
			}
		} else{
			LOGGER.info("**** subTx is in txRegistry");
//			System.out.println("subTx is not null in txRegistry");
			if(subTxStatus.equals(TransactionSnapshot.END)){
				subTx.setSubTxStatus(subTxStatus);
			} else{
				LOGGER.warning("**** Illegal sub transaction status(" + 
						subTxStatus + ")in notifySubTxStatus(...)");
//				System.out.println("Illegal sub transaction status(" + 
//						subTxStatus + ")in notifySubTxStatus(...)");
			}
		}//END ELSE
		
		if(subTxStatus.equals("start")){
			//ACK_SUBTX_INIT
			String parentTxCompName = dependency.getHostComponent();
			ArcRegistry inArcRegistry = InArcRegistryImpl.getInstance();
			ArcRegistry outArcRegistry = OutArcRegistryImpl.getInstance();
			Set<Arc> futureArcsInInArcRegistry = inArcRegistry.getArcsViaType("future");
			
			boolean willBeUsedFlag = false;
			for(Arc arc : futureArcsInInArcRegistry){
				if(arc.getRootTransaction().equals(rootTxID) && !arc.getSourceComponent().equals(arc.getTargetComponent())){
					willBeUsedFlag = true;
				}
			}
			
			if(willBeUsedFlag){
				LOGGER.info(dependency.getHostComponent() + "'s --> " + subTxHost + "'s future arc should not be removed!");
//				System.out.println(dependency.getHostComponent() + "'s --> " + subTxHost + "'s future arc should not be removed!");
			}else{
				Arc arc = new Arc();
				arc.setSourceComponent(dependency.getHostComponent());
				arc.setTargetComponent(subTxHost);
				arc.setRootTransaction(rootTxID);
				arc.setType("future");
				outArcRegistry.removeArc(arc);
				
				//NOTIFY_SUB_FUTURE_REMOVE
				Node communicationNode = LaunchCommunication.node;
				String endpoint = getServiceString(subTxHost);
				ArcService arcService;
				try{
					arcService = communicationNode.getService(ArcService.class, endpoint);
					LOGGER.info("**** Sub components will not host any sub tx, notify sub-component to remove future arc. ");
//					System.out.println("Access endpoint in AuthComponentVcServiceImpl: " + endpoint);
					arcService.removeArc(arc);
				} catch(NoSuchServiceException e){
					e.printStackTrace();
				}
				
			}
		}// END IF
		
		if(subTxStatus.equals("end")){
			String parentTxCompName = dependency.getHostComponent();
			ArcRegistry inArcRegistry = InArcRegistryImpl.getInstance();
			ArcRegistry outArcRegistry = OutArcRegistryImpl.getInstance();
			
			Arc arc = new Arc();
			arc.setSourceComponent(dependency.getHostComponent());
			arc.setTargetComponent(subTxHost);
			arc.setRootTransaction(rootTxID);
			arc.setType("past");
			outArcRegistry.addArc(arc);
			
			//NOTIFY_SUB_PAST_ADD
			Node communicationNode = LaunchCommunication.node;
			String endpoint = getServiceString(subTxHost);
			ArcService arcService;
			try{
				arcService = communicationNode.getService(ArcService.class, endpoint);
				LOGGER.info("**** Sub tx is end, and will not be used any more, notify host component to add past arc.");
//				System.out.println("Access endpoint in AuthComponentVcServiceImpl: " + endpoint);
				arcService.createArc(arc);
			} catch(NoSuchServiceException e){
				e.printStackTrace();
			}
			
			
		}//END IF
		
		ContainerPrinter containerPrinter = new ContainerPrinter();
		LOGGER.info("TransactionRegistry when notifySubTxStatus(...) is done:");
//		System.out.println("TransactionRegistry when notifySubTxStatus(...) is done:");
		containerPrinter.printTransactionRegistry(TransactionRegistryImpl.getInstance());
		containerPrinter.printInArcRegistry(InArcRegistryImpl.getInstance());
		containerPrinter.printOutArcRegistry(OutArcRegistryImpl.getInstance());
			
	}
	
	private String getServiceString(String subTxHost) {
		return subTxHost + "Comm#service-binding(ArcService/ArcService)";
	}

	@Override
	public boolean update(String baseDir, String classPath, String contributionURI, String compositeURI) {
//		DynamicUpdate du = null;
		ComponentStatus cs = ComponentStatus.getInstance();
		String achieveFreeness = cs.getFreenessSetup();
		if(ReconfigurationVersion.getInstance().isLoaded()){
			return true;
		}
		
		if(cs.getCurrentStatus().equals(ComponentStatus.NORMAL)){
			String targetComponent = compositeResolver.getHostComponentName();
			String endPoint = targetComponent + "Comm#service-binding(OndemandService/OndemandService)";
			Node communicationNode = VcContainerImpl.getInstance().getCommunicationNode();
			OndemandService ondemandService;
			try {
				ondemandService = communicationNode.getService(OndemandService.class, endPoint);
				return ondemandService.reqOndemandSetup(targetComponent, 
						targetComponent, cs.getScope(), cs.getFreenessSetup());
			} catch (NoSuchServiceException e) {
				e.printStackTrace();
			}
		}
		
		while(cs.getCurrentStatus().equals(ComponentStatus.ON_DEMAND)){
			try {
//				System.out.println("------------------sleep(400)------------------------");
				LOGGER.info("------------------sleep(400)------------------------");
				Thread.currentThread().sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Map<String, DeployedComposite> startedComposites = ((NodeImpl)VcContainerImpl.getInstance().getBusinessNode()).getStartedComposites();
		DeployedComposite dc = startedComposites.get(contributionURI + "/" + compositeURI);
		Composite composite = dc.getBuiltComposite();
		List<Component> components = composite.getComponents();
		Iterator componentsIterator = components.iterator();
			while(componentsIterator.hasNext()){
				RuntimeComponent runtimeComponent = (RuntimeComponent)componentsIterator.next();
				ImplementationProvider implementationProvider = runtimeComponent.getImplementationProvider();
				if(implementationProvider instanceof JavaImplementationProvider){
//					du = new JavaDynamicUpdateImpl();
					
					JavaImplementationProvider javaImplementationProvider = (JavaImplementationProvider)implementationProvider;
					JavaComponentContextProvider javaComponentContextProvider = javaImplementationProvider.getComponentContextProvider();
					ReflectiveInstanceFactory instanceFactory = (ReflectiveInstanceFactory) javaComponentContextProvider.getInstanceFactory();
					
					JavaImplementationImpl javaImpl = (JavaImplementationImpl) runtimeComponent.getImplementation();
					Class originalClass = javaImpl.getJavaClass();
					
					//TODO before loading the new class file, use suping's tool to analyse the source code
//					ProgramAnalyzer analyse=new ProgramAnalyzer();
//					analyse.analyzeSource(baseDir);
					Class newClass = loadClass(baseDir, new String[]{classPath});
					ReconfigurationVersion.getInstance().setOldVersion(originalClass);
					ReconfigurationVersion.getInstance().setNewVersion(newClass);
					ReconfigurationVersion.getInstance().setInstanceFactory(instanceFactory);
					ReconfigurationVersion.getInstance().setLoaded(true);
					
					if(!cs.getCurrentStatus().equals(ComponentStatus.VALID)){
						LOGGER.warning("--------------status error--------------");
//						System.out.println("--------------status error--------------");
					}else{
						cs.getNext();						//valid---------------------->WAITING/CONCURRENT/BLOCKING
						LOGGER.info("CurrentComponentStatus: " + ComponentStatus.getInstance().getCurrentStatus() + ", which is supposed to be WAITING/CONCURRENT/BLOCKING.");
//						System.out.println("CurrentComponentStatus: " + ComponentStatus.getInstance().getCurrentStatus() + ", which is supposed to be WAITING/CONCURRENT/BLOCKING.");
					}
					
				}
			}
		
			return true;
		
//		if(achieveFreeness.equals(ComponentStatus.CONCURRENT)){
//			Map<String, DeployedComposite> startedComposites = ((NodeImpl)VcContainerImpl.getInstance().getBusinessNode()).getStartedComposites();
//			DeployedComposite dc = startedComposites.get(contributionURI + "/" + compositeURI);
//			Composite composite = dc.getBuiltComposite();
//			List<Component> components = composite.getComponents();
//			Iterator componentsIterator = components.iterator();
//				while(componentsIterator.hasNext()){
//					RuntimeComponent runtimeComponent = (RuntimeComponent)componentsIterator.next();
//					ImplementationProvider implementationProvider = runtimeComponent.getImplementationProvider();
//					if(implementationProvider instanceof JavaImplementationProvider){
//						du = new JavaDynamicUpdateImpl();
//						
//						JavaImplementationProvider javaImplementationProvider = (JavaImplementationProvider)implementationProvider;
//						JavaComponentContextProvider javaComponentContextProvider = javaImplementationProvider.getComponentContextProvider();
//						ReflectiveInstanceFactory instanceFactory = (ReflectiveInstanceFactory) javaComponentContextProvider.getInstanceFactory();
//						
//						JavaImplementationImpl javaImpl = (JavaImplementationImpl) runtimeComponent.getImplementation();
//						Class originalClass = javaImpl.getJavaClass();
//						Class newClass = loadClass(baseDir, new String[]{classPath});
//						ReconfigurationVersion.getInstance().setOldVersion(originalClass);
//						ReconfigurationVersion.getInstance().setNewVersion(newClass);
//						
//						if(!cs.getCurrentStatus().equals(ComponentStatus.VALID)){
//							System.out.println("status error----------------------------");
//						}else{
//							cs.getNext();						//valid----->free
//						}
//						
//					}
//				}
//			
//			return true;
//		}else if(achieveFreeness.equals(ComponentStatus.WAITING)){
////		first. check whether the dependences is setup
////		then, query whether component is free
//			
//			while(!cs.getCurrentStatus().equals("Valid")){
//				System.out.println("wait for the Valid Status.");
//			}
//			System.out.println("go to next status: WaitingForFreeness---" + cs.getNext());
////		while(!isFreeness(compositeResolver.getHostComponentName())){
//			while(!isFreeness("AuthComponent")){
//				System.out.println("wait for the Freeness Status.");
//			}
//			
//			System.out.println("achieve freeness, current status: Freeness---" + cs.getNext());
//			System.out.println("we go to next status: Updating---" + cs.getNext());
////		second. update, when we finish update, then change ComponentStatus to updated
////		absent of parameters: dirName, updateClassNames
////		dirName, updateClassNames should be provided by users.
//			
//			Map<String, DeployedComposite> startedComposites = ((NodeImpl)VcContainerImpl.getInstance().getBusinessNode()).getStartedComposites();
//			DeployedComposite dc = startedComposites.get(contributionURI + "/" + compositeURI);
//			Composite composite = dc.getBuiltComposite();
//			List<Component> components = composite.getComponents();
//			Iterator componentsIterator = components.iterator();
//			while(componentsIterator.hasNext()){
//				RuntimeComponent runtimeComponent = (RuntimeComponent)componentsIterator.next();
//				ImplementationProvider implementationProvider = runtimeComponent.getImplementationProvider();
//				if(implementationProvider instanceof JavaImplementationProvider){
//					du = new JavaDynamicUpdateImpl();
//					du.update(runtimeComponent, baseDir, classPath, contributionURI, compositeURI);
//				}
////			other implementation conditions
////			else if(implementationProvider instanceof WidgetImplementationProvider){
////				
////			}else{
////				
////			}
//			}
//			
////		last. resume all cached msgs
////		after finishing the update, we should move the status to valid status
//			System.out.println("finish update, go to next status: Updated--- " + cs.getNext());
//			MessageQueue mq = MessageQueue.getInstance();
//			Map<PhasedInterceptor, Queue<Message>> msgMap = mq.getMsgMap();
//			Iterator iterator = msgMap.entrySet().iterator();
//			while(iterator.hasNext()){
//				System.out.println("in while.....");
//				Map.Entry<Interceptor, Message> entry = (Entry<Interceptor, Message>) iterator.next();
//				Interceptor interceptor = entry.getKey();
//				interceptor.notify();
//			}
//			System.out.println("resume all blocked messages: " + cs.getNext());
//			String defaultStatus = cs.getDefaultStatus();
//			while(!cs.getCurrentStatus().equals(defaultStatus)){
//				cs.getNext();
//			}
//			System.out.println("return to default status : " + cs.getCurrentStatus());
//			return true;
//		}
//		return false;
	}

	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}
	
	
	private Class loadClass(String baseDir, String[] classNames) {
		Class c = null;
		try {
			ReplaceClassLoader cl = new ReplaceClassLoader(baseDir, classNames);
			c = cl.loadClass(classNames[0]);
			LOGGER.info("load class: " + c);
//			System.out.println("c: " + c);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return c;
	}
//	@Override
//	public void notifySubTxEnd(Arc arc, String txID) {
//		// TODO Auto-generated method stub
//	}
	
}
