package cn.edu.nju.moon.conup.update;

import java.io.IOException;

import org.apache.tuscany.sca.implementation.java.JavaConstructorImpl;
import org.apache.tuscany.sca.implementation.java.context.ReflectiveInstanceFactory;
import org.apache.tuscany.sca.implementation.java.impl.JavaImplementationImpl;
import org.apache.tuscany.sca.implementation.java.invocation.JavaComponentContextProvider;
import org.apache.tuscany.sca.implementation.java.invocation.JavaImplementationProvider;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

public class JavaDynamicUpdateImpl implements DynamicUpdate {

	@Override
	public boolean update(RuntimeComponent runtimeComponent ,String baseDir, String filePath, String contributionURI, String compositeURI) {
		ImplementationProvider implementationProvider = runtimeComponent.getImplementationProvider();
		JavaImplementationProvider javaImplementationProvider = (JavaImplementationProvider)implementationProvider;
		JavaComponentContextProvider javaComponentContextProvider = javaImplementationProvider.getComponentContextProvider();
		ReflectiveInstanceFactory instanceFactory = (ReflectiveInstanceFactory) javaComponentContextProvider.getInstanceFactory();
		
		JavaImplementationImpl javaImpl = (JavaImplementationImpl) runtimeComponent.getImplementation();
		Class javaClass = loadClass(baseDir, new String[]{filePath});
		javaImpl.setJavaClass(javaClass);
		JavaConstructorImpl javaConstructor;
		try {
//			javaConstructor = new JavaConstructorImpl(javaClass.getConstructor());
//			javaImpl.setConstructor(javaConstructor);
			instanceFactory.setCtr(javaClass.getConstructor());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private Class loadClass(String baseDir, String[] classNames) {
		Class c = null;
		try {
			ReplaceClassLoader cl = new ReplaceClassLoader(baseDir, classNames);
			c = cl.loadClass(classNames[0]);
			System.out.println("c: " + c);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return c;
	}

}
