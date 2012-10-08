package cn.edu.nju.moon.conup.container.contribution;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.tuscany.sca.common.java.io.IOHelper;

public class JarContributionResolver implements ContributionResolver {

//	public static void main(String []args){
//		File file = new File("");
//		String absPath = file.getAbsolutePath();
//		String jarUri = absPath + File.separator + "target" + File.separator + "conup-sample-db.jar";
//		JarContributionResolver jarResolver = new JarContributionResolver();
//		try {
//			URL url = jarResolver.getCompositeURL(jarUri, null);
//			InputStreamReader isr = new InputStreamReader(url.openStream());
//			BufferedReader br = new BufferedReader(isr);
//			String line = null;
//			while((line=br.readLine()) != null){
//				System.out.println(line);
//			}
//			
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//	}
	
	/**
	 * @param absContributionUri absolute path the jar contribution, contains jar's name
	 * @param compositeName composite that is looking for. if the para is null, then it
	 *		  will return the first .composite file in the jar 
	 *
	 * */
	public URL getCompositeURL(String absContributionUri, String compositeName) {
		Set<String> names = new HashSet<String>();
		if(!absContributionUri.startsWith("file://")){
			absContributionUri = "file://" + absContributionUri;
		}
		
		try {
			URL url = new URL(absContributionUri);
			JarInputStream jar = new JarInputStream(IOHelper.openStream(url));
			try {
				while (true) {
					JarEntry entry = jar.getNextJarEntry();
					if (entry == null) {
						// EOF
						break;
					}
					String name = entry.getName();
					if (name.length() != 0 && !name.startsWith(".")) {
						// Trim trailing /
						if (name.endsWith("/")) {
							name = name.substring(0, name.length() - 1);
						}
						// Add the entry name
						if (!names.contains(name)) {
							names.add(name);
							// Add parent folder names to the list too
							for (;;) {
								int s = name.lastIndexOf('/');
								if (s == -1) {
									name = "";
								} else {
									name = name.substring(0, s);
								}
								if (name.length() != 0 && !names.contains(name)) {
									names.add(name);
								} else {
									break;
								}
							}
						}
					}
				}// END WHILE
			} finally {
				jar.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		compositeName = null;
		if(compositeName == null){
			for(String uri : names){
				if(uri.endsWith(".composite")){
					compositeName = uri;
					break;
				}
			}
		}//END IF
		
		URL compositeUrl = null;
		try {
			compositeUrl = new URL("jar:" + absContributionUri + "!/" + compositeName);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return compositeUrl;
	}
}
