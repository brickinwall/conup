package com.tuscanyscatours.launcher;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import com.tuscanyscatours.coordination.Coordination;

public class CoordinationVisitorThread extends Thread{
	private Node node;
	private String resultFileName;
	private int num;
	private int totalExecution;
	private static Map<Integer, Long> executionTime = new ConcurrentHashMap<Integer, Long>();
	
	public CoordinationVisitorThread(Node node, String resultFileName, int num, int totalExecution) {
		this.node = node;
		this.resultFileName = resultFileName;
		this.num = num;
		this.totalExecution = totalExecution;
	}

	public void run() {
		try {
			long startTime = System.nanoTime();
			try {
				Coordination scaTour = node
						.getService(Coordination.class,
								"Coordination#service-binding(Coordination/Coordination)");
				scaTour.coordinate();
			} catch (NoSuchServiceException e) {
				e.printStackTrace();
			}
			long endTime = System.nanoTime();
			executionTime.put(num, (endTime - startTime) / 1000000);
			if(executionTime.size() == totalExecution){
				FileWriter fw = new FileWriter(resultFileName, true);
				Iterator<Entry<Integer, Long>> iter = executionTime.entrySet().iterator();
				while(iter.hasNext()){
					Entry<Integer, Long> entry = iter.next();
					int i = entry.getKey();
					long time = entry.getValue();
					fw.write(i + "," + time + "\n");
				}
				executionTime.clear();
				fw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}// END RUN()
}
