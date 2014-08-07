package cn.edu.nju.moon.conup.ext.update;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.core.factory.ObjectFactory;
import org.apache.tuscany.sca.core.invocation.WireObjectFactory;
import org.apache.tuscany.sca.impl.DeployedComposite;
import org.apache.tuscany.sca.impl.NodeImpl;
import org.apache.tuscany.sca.implementation.java.context.ReflectiveInstanceFactory;
import org.apache.tuscany.sca.implementation.java.impl.JavaImplementationImpl;
import org.apache.tuscany.sca.implementation.java.injection.FieldInjector;
import org.apache.tuscany.sca.implementation.java.injection.Injector;
import org.apache.tuscany.sca.implementation.java.injection.MethodInjector;
import org.apache.tuscany.sca.implementation.java.invocation.JavaComponentContextProvider;
import org.apache.tuscany.sca.implementation.java.invocation.JavaImplementationProvider;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.datamodel.POJODynamicUpdateContext;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.update.ComponentUpdator;
import cn.edu.nju.moon.conup.spi.update.DynamicUpdateContext;
import cn.edu.nju.moon.conup.spi.update.Transformer;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;


/**
 * A class for update components implemented in Java POJO.
 * 
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class JavaCompUpdatorImpl implements ComponentUpdator {
	private final static Logger LOGGER = Logger.getLogger(JavaCompUpdatorImpl.class.getName());
	
	/** Implementation type for the component */
	public static final String IMPL_TYPE = "JAVA_POJO";
	
	private JavaImplementationImpl javaImpl = null;
	
	private boolean isUpdated = false;
	
	@SuppressWarnings("rawtypes")
	private ReflectiveInstanceFactory instanceFactory;
	
	public static Logger getLogger() {
		return LOGGER;
	}
	
	private Node tuscanyNode;
	
	@SuppressWarnings({ "unused", "rawtypes" })
	public boolean initUpdator(String baseDir, String classPath, String contributionURI, String compositeURI, String compIdentifier) {
		NodeManager nodeMgr = NodeManager.getInstance();
		UpdateManager updateMgr = nodeMgr.getUpdateManageer(compIdentifier);
		CompLifecycleManagerImpl compLcMgr = (CompLifecycleManagerImpl) nodeMgr.getCompLifecycleManager(compIdentifier);
//		compLcMgr = (CompLifecycleManagerImpl) CompLifecycleManagerImpl.getInstance(compIdentifier);
		
//		Set<String> oldVersionRootTxIds = depMgr.getOldVersionRootTxs();
		if(tuscanyNode == null){
//			tuscanyNode = compLcMgr.getNode();
			tuscanyNode = (Node) nodeMgr.getTuscanyNode();
		}
		Map<String, DeployedComposite> startedComposites = ((NodeImpl)tuscanyNode).getStartedComposites();
		DeployedComposite dc = startedComposites.get(contributionURI + "/" + compositeURI);
		Composite composite = dc.getBuiltComposite();
		List<Component> components = composite.getComponents();
		Iterator<Component> componentsIterator = components.iterator();
		while (componentsIterator.hasNext()) {
			RuntimeComponent runtimeComponent = (RuntimeComponent) componentsIterator
					.next();
			if( !runtimeComponent.getName().equals(compIdentifier) ){
				continue;
			}
			ImplementationProvider implementationProvider = runtimeComponent
					.getImplementationProvider();
			if (implementationProvider instanceof JavaImplementationProvider) {
				JavaImplementationProvider javaImplementationProvider = (JavaImplementationProvider) implementationProvider;
				JavaComponentContextProvider javaComponentContextProvider = javaImplementationProvider
						.getComponentContextProvider();
				ReflectiveInstanceFactory instanceFactory = (ReflectiveInstanceFactory) javaComponentContextProvider
						.getInstanceFactory();

				JavaImplementationImpl javaImpl = (JavaImplementationImpl) runtimeComponent
						.getImplementation();
				this.javaImpl = javaImpl;
				Class<?> originalClass = javaImpl.getJavaClass();

				Class<?> newClass = loadClass(baseDir, new String[] { classPath });
				DynamicUpdateContext updateCtx = updateMgr.getUpdateCtx();
				if(updateCtx == null){
					updateCtx = new POJODynamicUpdateContext();
					updateMgr.setDynamicUpdateContext(updateCtx);
				}
				updateCtx.setOldVerClass(originalClass);
				updateCtx.setNewVerClass(newClass);
				updateCtx.setLoaded(true);
				this.instanceFactory = instanceFactory;
//				compLcMgr.setInstanceFactory(instanceFactory);
				
				//test
//				String str = "oldRootTxs:\n";
//				for (String oldRoot : oldRootTxs) {
//					str += "\t" + oldRoot;
////					System.out.print("\t" + oldRoot);
//				}
//				LOGGER.fine(str);

			}
		}
		
		if( this.javaImpl == null)
			throw new RuntimeException("JavaCompUpdatorImpl initiation failure");
		
		return true;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean executeUpdate(String compIdentifier) {
//		CompLifecycleManagerImpl compLcMgr;
//		ReflectiveInstanceFactory instanceFactory;
		Class<?> compClass;
		NodeManager nodeMgr = NodeManager.getInstance();
		UpdateManager updateMgr = nodeMgr.getUpdateManageer(compIdentifier);
		
//		compLcMgr = (CompLifecycleManagerImpl) CompLifecycleManagerImpl.getInstance(compIdentifier);
//		instanceFactory = compLcMgr.getInstanceFactory();
		compClass = updateMgr.getUpdateCtx().getNewVerClass();
		try {
//			try {
//				Field oldFiled = compLcMgr.getUpdateCtx().getOldVerClass().getDeclaredField("version");
//				oldFiled.setAccessible(true);
//				String oldVersion = oldFiled.get(compLcMgr.getUpdateCtx().getOldVerClass().getConstructor(null).newInstance()).toString();
//				
//				Field newFiled = compLcMgr.getUpdateCtx().getNewVerClass().getDeclaredField("version");
//				newFiled.setAccessible(true);
//				String newVersion = newFiled.get(compLcMgr.getUpdateCtx().getNewVerClass().getConstructor(null).newInstance()).toString();
//				LOGGER.fine("old version class: " + compLcMgr.getUpdateCtx().getOldVerClass().hashCode() + "version : " + oldVersion);
//				LOGGER.fine("new version class: " + compLcMgr.getUpdateCtx().getNewVerClass().hashCode() + "version : " + newVersion);
//			} catch (NoSuchFieldException e) {
//				e.printStackTrace();
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			} catch (InstantiationException e) {
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				e.printStackTrace();
//			}
			
			javaImpl.setJavaClass(compClass);
//			compClass.getf
			
//			Method[] methods = compClass.getMethods();
//			for(Method m : methods){
//				LOGGER.fine("method:" + m);
//			}
			instanceFactory.setCtr(compClass.getConstructor());
			Injector[] injectors = instanceFactory.getInjectors();
			for(Injector injector : injectors){
				if(injector instanceof MethodInjector){
					MethodInjector methodInjector = (MethodInjector) injector;
					Method oldMethod = methodInjector.getMethod();
					
					Method[] methods = compClass.getMethods();
					Method newMethod = null;
					for(Method m : methods){
						if(m.getName().equals(oldMethod.getName())){
							newMethod = m;
							break;
						}
					}
					if(newMethod != null){
						ObjectFactory objFactory = methodInjector.getObjectFactory();
						if(objFactory instanceof WireObjectFactory){
							WireObjectFactory wireObjFactory = (WireObjectFactory)objFactory;
							wireObjFactory.setInterfaze(newMethod.getParameterTypes()[0]);
						}
						methodInjector.setMethod(newMethod);
					}
				} else if(injector instanceof FieldInjector){
					FieldInjector fieldInjector = (FieldInjector) injector;
					Field oldField = fieldInjector.getField();
					Field newField;
					try {
						newField = compClass.getDeclaredField(oldField.getName());
						// field may be private, need privilege access
						newField.setAccessible(true);
						ObjectFactory objFactory = fieldInjector.getObjectFactory();
						if(objFactory instanceof WireObjectFactory){
							WireObjectFactory wireObjFactory = (WireObjectFactory)objFactory;
							wireObjFactory.setInterfaze(newField.getType());
						}
						fieldInjector.setField(newField);
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					}
				} else{
					
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		isUpdated = true;
		
		return true;
	}

	public boolean isUpdated() {
		return isUpdated;
	}

	@Override
	public boolean cleanUpdate(String compIdentifier) {
//		CompLifecycleManagerImpl lcMgr;
//		lcMgr = (CompLifecycleManagerImpl) CompLifecycleManagerImpl.getInstance(compIdentifier);
		NodeManager nodeMgr = NodeManager.getInstance();
		UpdateManager updateMgr = nodeMgr.getUpdateManageer(compIdentifier);
		
		updateMgr.setDynamicUpdateContext(null);
		this.instanceFactory = null;
//		lcMgr.setInstanceFactory(null);
//		depMgr.dynamicUpdateIsDone();
		
		return true;
	}

	private Class<?> loadClass(String baseDir, String[] classNames) {
		Class<?> c = null;
		try {
			DynamicUpdateClassLoader cl = new DynamicUpdateClassLoader(baseDir, classNames);
			c = cl.loadClass(classNames[0]);
			LOGGER.fine("load class: " + c);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return c;
	}

	@Override
	public boolean finalizeOld(String compIdentifier, Class<?> oldVersion,
			Class<?> newVersion, Transformer transfomer) {
		return true;
	}

	@Override
	public boolean initNewVersion(String compName, Class<?> newVersion) {
		return true;
	}

	@Override
	public String getCompImplType() {
		return IMPL_TYPE;
	}
	
	@SuppressWarnings("rawtypes")
	public ReflectiveInstanceFactory getInstanceFactory() {
		return instanceFactory;
	}

	@SuppressWarnings("rawtypes")
	public void setInstanceFactory(ReflectiveInstanceFactory instanceFactory) {
		this.instanceFactory = instanceFactory;
	}

}
