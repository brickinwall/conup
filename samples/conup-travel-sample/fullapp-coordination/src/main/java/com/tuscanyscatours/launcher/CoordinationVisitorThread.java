package com.tuscanyscatours.launcher;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.ext.utils.experiments.ResponseTimeRecorder;
import cn.edu.nju.moon.conup.ext.utils.experiments.TimelinessRecorder;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExpSetting;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.Experiment;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.RqstInfo;

import com.tuscanyscatours.coordination.Coordination;

public class CoordinationVisitorThread extends Thread{
	
	private static final Logger LOGGER = Logger.getLogger(CoordinationVisitorThread.class.getName());
	private Node node;
	private int threadId;
	private int roundId;
	private CountDownLatch countDown;
	private ResponseTimeRecorder resTimeRec;
	private TimelinessRecorder timelinessRec;
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
	
	public CoordinationVisitorThread(Node node,int roundId, int threadId, CountDownLatch countDown){
		this.node = node;
		this.roundId = roundId;
		this.threadId = threadId;
		this.countDown = countDown;
	}

	public CoordinationVisitorThread(Node node,CountDownLatch countDown, int threadId, TimelinessRecorder timelinessRec) {
		this.node = node;
		this.countDown = countDown;
		this.threadId = threadId;
		this.timelinessRec = timelinessRec;
	}

	public void run() {
		try {
			
			Coordination scaTour = node.getService(Coordination.class, "Coordination/Coordination");
			long startTime = System.nanoTime();
			scaTour.coordinate();
			long endTime = System.nanoTime();
			countDown.countDown();
			
			if (resTimeRec != null) {
				if (execType == null){
					System.out.println("response time:" + (endTime - startTime) * 1e-6);
					return;
				}
				else if (execType.equals("normal")){
					resTimeRec.addNormalResponse(threadId, endTime - startTime);
					System.out.println("normal threadId:" + threadId + " response time:" + (endTime - startTime) * 1e-6);
				}
				else if (execType.equals("update")){
					resTimeRec.addUpdateResponse(threadId, endTime - startTime);
					resTimeRec.addUpdateResInfo(new RqstInfo(threadId, startTime, endTime));
					System.out.println("update threadId:" + threadId + " response time:" + (endTime - startTime) * 1e-6);
				}
			} else{
				System.out.println("response time:" + (endTime - startTime) * 1e-6);
			}
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}// END RUN()
}