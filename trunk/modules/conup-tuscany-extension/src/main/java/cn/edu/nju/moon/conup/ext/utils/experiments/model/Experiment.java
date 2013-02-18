package cn.edu.nju.moon.conup.ext.utils.experiments.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.core.algorithm.VersionConsistencyImpl;

public class Experiment {
	private Logger LOGGER = Logger.getLogger(Experiment.class.getName());
	private static String absolutePath= "/home/valerio/workspace/conUp/tuscany-sca/experiments/results/";
	private static String fileName ="timeliness_experiment_results.csv";
	private static Experiment experiment = null;
	private static PrintWriter out = null;
	
	private Experiment() {
		try {
			File file = new File(absolutePath+fileName);
			out = new PrintWriter(new FileWriter(file,true),true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Experiment getInstance() {
		if(experiment == null) {
			synchronized (Experiment.class) {
				experiment = new Experiment();
			}
		}
		return experiment;
	}
	
	public void writeToFile(String data) {
		synchronized (experiment) {
			LOGGER.fine("I'm writing: " + data);
			out.println(data);			
		}
	}
	
	public void close() {
		synchronized (experiment) {
			out.close();
		}
	}

}
