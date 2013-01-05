package cn.edu.nju.moon.conup.apppre;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 
 * 
 * @author Ping Su<njupsu@gmail.com>
 */
public class UnjarTool {
	private final static Logger LOGGER = Logger.getLogger(UnjarTool.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	}

	public static void main(String args[]) {

		UnjarTool f = new UnjarTool();
		f.unjar("/home/nju/conup-sample-auth.jar","/home/temp");
	}

	/**
	 * install jar package
	 * 
	 */
	public void unjar(String jarFileName,String outputPath) {
		
		
		try {
			//execute unjar 
			decompress(jarFileName, outputPath);
			//LOGGER.fine("Extracting  OK!");
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.warning("Extracting File Failed!");
			dealError(outputPath);
			LOGGER.warning("Installing output file Failed");
			return;
		}

		String systemName = System.getProperty("os.name");
		//LOGGER.fine("System is " + systemName);
		// command in unix 
		if (!systemName.toLowerCase().contains("windows")) {
//			LOGGER.fine("Start Granting User Excecute Rights......");
			try {
				Process p1 = Runtime.getRuntime().exec("chmod +x portal.sh");
				p1.waitFor();
//				LOGGER.fine("OK......");
//				Process p2 = Runtime.getRuntime().exec("portal.sh");
//				p2.waitFor();
//				LOGGER.fine("Granting User Excecute Rights OK!");
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.warning("Granting User Excecute Rights Failed!");
				dealError(outputPath);
				LOGGER.warning("Installing output file Failed");
				return;
			}
		}
	}

	/**
	 * unjar 
	 * 
	 * @param fileName
	 *            input jar filename
	 * @param outputPath
	 *            output filepath
	 * @throws IOException
	 *             
	 */
	public void decompress(String fileName, String outputPath)
			throws IOException {

		if (!outputPath.endsWith(File.separator)) {

			outputPath += File.separator;

		}

		JarFile jf = new JarFile(fileName);

		for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements();) {
			JarEntry je = (JarEntry) e.nextElement();
			String outFileName = outputPath + je.getName();
			File f = new File(outFileName);
			//LOGGER.fine(f.getAbsolutePath());

			// create  path and all the father path
			makeSupDir(outFileName);

			// if  a directory 
			if (f.isDirectory()) {
				continue;
			}

			InputStream in = null;
			OutputStream out = null;

			try {
				in = jf.getInputStream(je);
				out = new BufferedOutputStream(new FileOutputStream(f));
				byte[] buffer = new byte[2048];
				int nBytes = 0;
				while ((nBytes = in.read(buffer)) > 0) {
					out.write(buffer, 0, nBytes);
				}
			} catch (IOException ioe) {
				throw ioe;
			} finally {
				try {
					if (null != out) {
						out.flush();
						out.close();
					}
				} catch (IOException ioe) {
					throw ioe;
				} finally {
					if (null != in) {
						in.close();
					}
				}
			}
		}
	}

	/**
	 * create super dir iterately
	 * 
	 * @param outFileName
	 */
	public void makeSupDir(String outFileName) {
		// match separator
		Pattern p = Pattern.compile("[/\\" + File.separator + "]");
		Matcher m = p.matcher(outFileName);
		// Everytime find a separator,create a dir
		while (m.find()) {
			int index = m.start();
			String subDir = outFileName.substring(0, index);
			File subDirFile = new File(subDir);
			if (!subDirFile.exists())
				subDirFile.mkdirs();
		}
	}

	/**
	 * delete current directory and all the children dir
	 * 
	 * @param path
	 */
	public void clean(String path) throws IOException {
		File file = new File(path);
		// if path is not exist
		if (!file.exists()) {
			LOGGER.fine(path + " Not Exist!");
		} else {
			// if a directory, delete iteraterly
			if (file.isDirectory()) {
				String[] fileNames = file.list();

				if (null == fileNames) {
					throw new IOException("IO ERROR While Deleting Files");
				}
				// if dir is empty,delete it 
				else if (fileNames.length == 0) {
					file.delete();
				} 
				else {
					for (String fileName : fileNames) {
						File subFile = new File(fileName);
						clean(path + File.separator + subFile);
					}
					//LOGGER.fine(file.getAbsolutePath());
					// delete the super dir
					file.delete();

				}
			}
			// if a file,delete it
			else {
//				LOGGER.fine(file.getAbsolutePath());
				file.delete();
			}
		}
	}

	/**
	 *handle error exception
	 * 
	 * @param outputPath
	 *            
	 * @throws IOException
	 *             
	 */
	public void dealError(String outputPath) {
		// delete the unjared files
		LOGGER.fine("Start Deleting Files......");
		try {
			clean(outputPath);
			LOGGER.fine("Deleting Files OK!");
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.warning("Deleting Files Failed!");
		}
	}

}