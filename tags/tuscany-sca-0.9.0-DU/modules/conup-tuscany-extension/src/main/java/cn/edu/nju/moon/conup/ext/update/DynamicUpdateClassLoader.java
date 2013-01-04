package cn.edu.nju.moon.conup.ext.update;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class DynamicUpdateClassLoader extends ClassLoader {
	private String baseDir; 
    private Set<String> dynaclazns; 

    public DynamicUpdateClassLoader(String baseDir, String[] clazns) throws IOException { 
        super(null);  
        this.baseDir = baseDir; 
        dynaclazns = new HashSet<String>(); 
        loadClassByMe(clazns); 
    } 
    
    private void loadClassByMe(String[] clazns) throws IOException { 
        for (int i = 0; i < clazns.length; i++) { 
            loadDirectly(clazns[i]); 
            dynaclazns.add(clazns[i]); 
        } 
    } 

    private Class<?> loadDirectly(String name) throws IOException { 
        Class<?> cls = null; 
        StringBuffer sb = new StringBuffer(baseDir); 
        String classname = name.replace('.', File.separatorChar) + ".class";
//        System.out.println(classname);
        sb.append(File.separator + classname); 
        File classF = new File(sb.toString()); 
        cls = instantiateClass(name,new FileInputStream(classF),
            classF.length()); 
        return cls; 
    }   		

    private Class<?> instantiateClass(String name,InputStream fin,long len) throws IOException{ 
        byte[] raw = new byte[(int) len]; 
        fin.read(raw); 
        fin.close(); 
        return defineClass(name,raw,0,raw.length); 
    } 
    
	protected Class<?> loadClass(String name, boolean resolve) 
            throws ClassNotFoundException { 
        Class<?> cls = null; 
        cls = findLoadedClass(name); 
        if(!this.dynaclazns.contains(name) && cls == null){
        	
        	try {
//        		System.out.println("try getClass().getClassLoader().loadClass " + name);
            	cls = getClass().getClassLoader().loadClass(name);
			} catch (ClassNotFoundException e) {
				cls = findClass(name);
//				e.printStackTrace();
			}
        }
        if (cls == null) {
        	System.out.println("cls is null");
//            cls = findClass(name);
            throw new ClassNotFoundException(name); 
        }
        if (resolve) 
            resolveClass(cls); 
        return cls; 
    } 
	
	protected Class<?> findClass(String name) throws ClassNotFoundException{
//		System.out.println("in method findClass(...): try to find " + name);
		
		Class<?> result = null;
		
		FileInputStream fis = null;  
	    byte[] data = null;
	    try {
	    	String fileName = name.replace('.', File.separatorChar);
	        fis = new FileInputStream(new File(baseDir + File.separator + fileName + ".class"));
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	        int ch = 0;  
	        while ((ch = fis.read()) != -1) {  
	          baos.write(ch);  
	        }
	        data = baos.toByteArray();  
	      } catch (IOException e) {  
	        e.printStackTrace();  
	      }
		
	    result = defineClass(name, data, 0, data.length);
		return result;
	}

}
