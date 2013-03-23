package com.tuscanyscatours.launcher;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.ext.utils.experiments.ResponseTimeRecorder;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExpSetting;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.Experiment;

import com.tuscanyscatours.coordination.Coordination;

public class CoordinationVisitorThread extends Thread{
	
	private static final Logger LOGGER = Logger.getLogger(CoordinationVisitorThread.class.getName());
	private Node node;
	private int threadId;
	private int roundId;
	private CountDownLatch countDown;
	private ResponseTimeRecorder resTimeRec;
	private String execType;
	
	public CoordinationVisitorThread(Node node) {
		this.node = node;
	}
	
	public CoordinationVisitorThread(Node node, CountDownLatch countDown) {
		this.countDown = countDown;
		this.node = node;
	}
	
	public CoordinationVisitorThread(Node node, int threadId){
		this.node = node;
		this.threadId = threadId;
	}
	
	public CoordinationVisitorThread(Node node, CountDownLatch countDown, int threadId, ResponseTimeRecorder resTimeRec, String execType){
		this.node = node;
		this.countDown = countDown;
		this.threadId = threadId;
		this.resTimeRec = resTimeRec;
		this.execType = execType;
	}
	
	public CoordinationVisitorThread(Node node,int roundId, int threadId){
		this.node = node;
		this.roundId = roundId;
		this.threadId = threadId;
	}

	public void run() {
		try {
			long startTime = System.nanoTime();
			
			Coordination scaTour = node.getService(Coordination.class, "Coordination#service-binding(Coordination/Coordination)");
			scaTour.coordinate();
			countDown.countDown();
			
			long endTime = System.nanoTime();
			
			if( execType == null)
				return;
			else if(execType.equals("normal"))
				resTimeRec.addNormalResponse(threadId, endTime - startTime);
			else if(execType.equals("update"))
				resTimeRec.addUpdateResponse(threadId, endTime - startTime);
			else
				return;
			
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}// END RUN()
}