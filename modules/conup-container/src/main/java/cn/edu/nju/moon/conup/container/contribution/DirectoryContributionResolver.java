package cn.edu.nju.moon.conup.container.contribution;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tuscany.sca.contribution.processor.ContributionReadException;

public class DirectoryContributionResolver implements ContributionResolver {
	
		public static void main(String []args){
		File file = new File("");
		String absPath = file.getAbsolutePath();
		String dirUri = absPath + File.separator + "target" + File.separator + "classes";
		ContributionResolver dirResolver = new DirectoryContributionResolver();
		try {
			URL url = dirResolver.getCompositeURL(dirUri, "DBComponentComm.composite");
			InputStreamReader isr = new InputStreamReader(url.openStream());
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while((line=br.readLine()) != null){
				System.out.println(line);
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public URL getCompositeURL(String absContributionPath, String compositeName) {
		URL compositeURL = null;
		if(!absContributionPath.startsWith("file://")){
			absContributionPath = "file://" + absContributionPath;
		}
		if(!absContributionPath.endsWith(File.separator)){
			absContributionPath = absContributionPath + File.separator;
		}
		try {
			List<String> artifacts = scanContributionArtifacts(absContributionPath);
			if(compositeName == null){
				for(String uri : artifacts){
					if(uri.endsWith(".composite")){
						compositeName = uri;
						break;
					}
				}
			}//END IF
			
			try {
//				compositeURL = new URL("jar:" + absContributionPath + "!/" + compositeName);
				compositeURL = new URL(absContributionPath + compositeName);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} catch (ContributionReadException e) {
			e.printStackTrace();
		}
		return compositeURL;
	}
	
	/**
     * Scan the contribution to retrieve all artifact uris
     *
     * @param contribution
     * @return
     * @throws ContributionReadException
     */
    private List<String> scanContributionArtifacts(String absContributionPath) throws ContributionReadException {
        File directory = directory(absContributionPath);
        List<String> artifacts = new ArrayList<String>();
        // [rfeng] There are cases that the folder contains symbolic links that point to the same physical directory
        Set<File> visited = new HashSet<File>();
        try {
            traverse(artifacts, directory, directory, visited);
        } catch (IOException e) {
            throw new ContributionReadException(e);
        }

        return artifacts;
    }

    /**
     * Recursively traverse a root directory
     *
     * @param fileList
     * @param file
     * @param root
     * @param visited The visited directories
     * @throws IOException
     */
    private static void traverse(List<String> fileList, File file, File root, Set<File> visited) throws IOException {

    	//TUSCANY-3667 - Google add some private directories when you deploy your application
    	//to GAE and trying to execute file IO operations on it's contents fails with AccessControlException
    	try {
            if (file.isFile()) {
                fileList.add(root.toURI().relativize(file.toURI()).toString());
            } else if (file.isDirectory()) {
                File dir = file.getCanonicalFile();
                if (!visited.contains(dir)) {
                    // [rfeng] Add the canonical file into the visited set to avoid duplicate navigation of directories
                    // following the symbolic links
                    visited.add(dir);
                    String uri = root.toURI().relativize(file.toURI()).toString();
                    if (uri.endsWith("/")) {
                        uri = uri.substring(0, uri.length() - 1);
                    }
                    fileList.add(uri);

                    File[] files = file.listFiles();
                    for (File f : files) {
                        if (!f.getName().startsWith(".")) {
                            traverse(fileList, f, root, visited);
                        }
                    }
                }
            }
    	} catch (AccessControlException e) {
    		e.printStackTrace();
    	}

    }

    /**
     * Get the contribution location as a file
     *
     * @param contribution
     * @return
     * @throws ContributionReadException
     */
    private File directory(String absContributionPath) throws ContributionReadException {
        File file;
        URI uri = null;
        try {
            uri = new URI(absContributionPath);
            file = new File(uri);
        } catch (URISyntaxException e) {
            throw new ContributionReadException(e);
        } catch(IllegalArgumentException e) {
            // Hack for file:./a.txt or file:../a/c.wsdl
            return new File(uri.getPath());
        }
        if (!file.exists() || !file.isDirectory()) {
            System.out.println("contribution: " + absContributionPath + " does not exist");
        }
        return file;
    }

}
