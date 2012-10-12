package cn.edu.nju.moon.conup.pre;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 
 * 
 * @author <a href="mailto:njupsu@gmail.com">Su Ping</a>
 */
public class UnjarTool {

	public static void main(String args[]) {

		UnjarTool f = new UnjarTool();
		f.unjar("/home/nju/conup-sample-auth.jar","/home/temp");
	}

	/**
	 * install jar package
	 * 
	 */
	public void unjar(String jarFileName,String outputPath) {
		
		//String jarFileName = "e:\\kichen.jar";
		//Date d = new Date();
		//String outputPath = "e:\\temp";
		try {
			//execute unjar 
			decompress(jarFileName, outputPath);
			//System.out.println("Extracting  OK!");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Extracting File Failed!");
			dealError(outputPath);
			System.out.println("Installing output file Failed");
			return;
		}

		String systemName = System.getProperty("os.name");
		//System.out.println("System is " + systemName);
		// command in unix 
		if (!systemName.toLowerCase().contains("windows")) {
//			System.out.println("Start Granting User Excecute Rights......");
			try {
				Process p1 = Runtime.getRuntime().exec("chmod +x portal.sh");
				p1.waitFor();
//				System.out.println("OK......");
//				Process p2 = Runtime.getRuntime().exec("portal.sh");
//				p2.waitFor();
//				System.out.println("Granting User Excecute Rights OK!");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Granting User Excecute Rights Failed!");
				dealError(outputPath);
				System.out.println("Installing output file Failed");
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

		for (Enumeration e = jf.entries(); e.hasMoreElements();) {
			JarEntry je = (JarEntry) e.nextElement();
			String outFileName = outputPath + je.getName();
			File f = new File(outFileName);
			//System.out.println(f.getAbsolutePath());

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
			System.out.println(path + " Not Exist!");
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
					//System.out.println(file.getAbsolutePath());
					// delete the super dir
					file.delete();

				}
			}
			// if a file,delete it
			else {
//				System.out.println(file.getAbsolutePath());
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
		System.out.println("Start Deleting Files......");
		try {
			clean(outputPath);
			System.out.println("Deleting Files OK!");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Deleting Files Failed!");
		}
	}

}