package cn.edu.nju.moon.conup.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;

public class ReplaceClassLoaderVersion2 extends ClassLoader {
	private String baseDir; 
	private String dynamicClassName;

    public ReplaceClassLoaderVersion2(String baseDir, String clazns) throws IOException { 
        super(null);  
        this.baseDir = baseDir; 
        this.dynamicClassName = clazns;
        loadClassByMe(clazns); 
    } 
    
    
    private void loadClassByMe(String clazns) throws IOException { 
            loadDirectly(clazns); 
            dynamicClassName = clazns;
    } 

    private Class loadDirectly(String name) throws IOException { 
        Class cls = null; 
        StringBuffer sb = new StringBuffer(baseDir); 
        String classname = name.replace('.', File.separatorChar) + ".class";
        sb.append(File.separator + classname); 
        System.out.println(sb.toString());
        File classF = new File(sb.toString()); 
        cls = instantiateClass(name,new FileInputStream(classF), classF.length()); 
        
        return cls; 
    }   		

    private Class instantiateClass(String name,InputStream fin,long len) throws IOException{ 
        byte[] raw = new byte[(int) len]; 
        fin.read(raw); 
        fin.close(); 
        return defineClass(name,raw,0,raw.length); 
    } 
    
	protected Class loadClass(String name, boolean resolve) 
            throws ClassNotFoundException { 
        Class cls = null; 
        cls = findLoadedClass(name); 
//        if(!this.dynaclazns.contains(name) && cls == null) 
        if(!this.dynamicClassName.equals(name) && cls == null)
            cls = getSystemClassLoader().loadClass(name); 
        if (cls == null) 
            throw new ClassNotFoundException(name); 
        if (resolve) 
            resolveClass(cls); 
        return cls; 
    } 
	
	public static void main(String[] args) {
		String basedir = "/home/nju/workspace/conup-sample-auth/target/classes";
		String classns = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
		try {
			ReplaceClassLoaderVersion2 rc = new ReplaceClassLoaderVersion2(basedir, classns);
			Class c = rc.loadClass(classns);
			Object obj = c.newInstance();
			Method method = obj.getClass().getMethod("getToken", new Class[]{String.class});
			method.invoke(obj, "nju,cs");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}
