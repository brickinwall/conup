package cn.edu.nju.moon.conup.pre;




import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides utility services for jarring and unjarring files and directories.
 * Note that a given instance of JarHelper is not threadsafe with respect to
 * multiple jar operations.
 * 
 * @author <a href="mailto:njupsu@gmail.com">Su Ping</a>
 * modify version ( Patrick Calahan <pcal@bea.com>)
 */
public class JarTool {
	private final static Logger LOGGER = Logger.getLogger(JarTool.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	}

	// ========================================================================
	// Constants

	private static final int BUFFER_SIZE = 2156;

	private static String MAIN_CLASS;

	// ========================================================================
	// Variables

	private byte[] mBuffer = new byte[BUFFER_SIZE];
	private int mByteCount = 0;
	private boolean mVerbose = false;
	private String mDestJarName = "";

	// ========================================================================
	// Constructor

	/**
	 * Instantiates a new JarTool.
	 */
	public JarTool() {
	}

	// ========================================================================
	// Public methods

	/**
	 * Jars a given directory or single file into a JarOutputStream.
	 */
	public void jarDir(File dirOrFile2Jar, File destJar) throws IOException {

		if (dirOrFile2Jar == null || destJar == null)
			throw new IllegalArgumentException();

		mDestJarName = destJar.getCanonicalPath();
		FileOutputStream fout = new FileOutputStream(destJar);

		JarOutputStream jout;

		if (MAIN_CLASS != null) {
			Manifest manifest = new Manifest();
			Attributes attrs = manifest.getMainAttributes();
			attrs.putValue("Manifest-Version", "1.0");
			attrs.putValue("Class-Path", ".");
			attrs.putValue("Main-Class", MAIN_CLASS);
			/**
			 * Manifest-Version: 1.0 Class-Path: . Main-Class: test.JarTool
			 */
			jout = new JarOutputStream(fout, manifest);
		} else {
			jout = new JarOutputStream(fout);
		}
		// jout.setLevel(0);
		try {
			jarDir(dirOrFile2Jar, jout, null);
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			jout.close();
			fout.close();
		}
	}
	public void setVerbose(boolean b) {
		mVerbose = b;
	}

	// ========================================================================
	// Private methods

	private static final char SEP = '/';

	/**
	 * Recursively jars up the given path under the given directory.
	 */
	private void jarDir(File dirOrFile2jar, JarOutputStream jos, String path)
			throws IOException {
		if (mVerbose)
			LOGGER.fine("checking " + dirOrFile2jar);
		if (dirOrFile2jar.isDirectory()) {
			String[] dirList = dirOrFile2jar.list();
			String subPath = (path == null) ? "" : (path
					+ dirOrFile2jar.getName() + SEP);
			if (path != null) {
				JarEntry je = new JarEntry(subPath);
				je.setTime(dirOrFile2jar.lastModified());
				jos.putNextEntry(je);
				jos.flush();
				jos.closeEntry();
			}
			for (int i = 0; i < dirList.length; i++) {
				File f = new File(dirOrFile2jar, dirList[i]);
				jarDir(f, jos, subPath);
			}
		} else {
			if (dirOrFile2jar.getCanonicalPath().equals(mDestJarName)) {
				if (mVerbose)
					LOGGER.fine("skipping " + dirOrFile2jar.getPath());
				return;
			}

			if (mVerbose)
				LOGGER.fine("adding " + dirOrFile2jar.getPath());
			FileInputStream fis = new FileInputStream(dirOrFile2jar);
			try {
				JarEntry entry = new JarEntry(path + dirOrFile2jar.getName());
				entry.setTime(dirOrFile2jar.lastModified());
				jos.putNextEntry(entry);
				while ((mByteCount = fis.read(mBuffer)) != -1) {
					jos.write(mBuffer, 0, mByteCount);
					if (mVerbose)
						LOGGER.fine("wrote " + mByteCount + " bytes");
				}
				jos.flush();
				jos.closeEntry();
			} catch (IOException ioe) {
				throw ioe;
			} finally {
				fis.close();
			}
		}
	}

	// for debugging
	public static void main(String[] args) throws IOException {

		JarTool jarHelper = new JarTool();
		
		//File destJar = new File("E:\\kichen.jar");
		File dirOrFile2Jar = new File("e:\\bbb.jar");
		File jar2Dir = new File("E:\\portal");
		jar2Dir.mkdir();
		
		jarHelper.jarDir(jar2Dir, dirOrFile2Jar);
	}
}
