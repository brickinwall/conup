package com.tuscanyscatours.coordination;

import java.util.concurrent.CountDownLatch;
import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.ext.utils.experiments.model.ResponseTimeRecorder;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.RqstInfo;

public class CoordinationVisitorThread extends Thread {

	private Node node;
	private int threadId;
	private CountDownLatch countDown;
	private ResponseTimeRecorder resTimeRec;
	private String execType;
	private Long rqstAbsoluteStartTime = null;

	public CoordinationVisitorThread(Node node) {
		this.node = node;
	}

	public CoordinationVisitorThread(Node node, CountDownLatch countDown) {
		this(node);
		this.countDown = countDown;
	}

	public CoordinationVisitorThread(Node node, int threadId) {
		this(node);
		this.threadId = threadId;
	}

	public CoordinationVisitorThread(Node node, int threadId,
			ResponseTimeRecorder resTimeRec, String execType) {
		this(node, threadId);
		this.resTimeRec = resTimeRec;
		this.execType = execType;
	}

	public CoordinationVisitorThread(Node node, CountDownLatch countDown,
			int threadId, ResponseTimeRecorder resTimeRec, String execType) {
		this(node, countDown);
		this.threadId = threadId;
		this.resTimeRec = resTimeRec;
		this.execType = execType;
	}

	public CoordinationVisitorThread(Node node, int threadId,
			ResponseTimeRecorder resTimeRec, String execType,
			long rqstAbsoluteStartTime) {
		this(node, threadId);
		this.resTimeRec = resTimeRec;
		this.execType = execType;
		this.rqstAbsoluteStartTime = rqstAbsoluteStartTime;
	}

	public CoordinationVisitorThread(Node node, CountDownLatch updateCountDown,
			int threadId, ResponseTimeRecorder resTimeRec, String execType,
			long rqstAbsoluteStartTime) {
		this(node, updateCountDown);
		this.threadId = threadId;
		this.resTimeRec = resTimeRec;
		this.execType = execType;
		this.rqstAbsoluteStartTime = rqstAbsoluteStartTime;
	}

	public void run() {
		try {

			Coordination scaTour = node.getService(Coordination.class,
					"Coordination/Coordination");
			long startTime = System.nanoTime();
			scaTour.coordinate();
			long endTime = System.nanoTime();
			if (countDown != null)
				countDown.countDown();

			if (resTimeRec != null) {
				if (execType == null) {
					System.out.println("response time:" + (endTime - startTime)
							* 1e-6);
					return;
				} else if (execType.equals("normal")) {
					resTimeRec.addNormalResponse(threadId, endTime - startTime);
					System.out.println("normal threadId:" + threadId
							+ " response time:" + (endTime - startTime) * 1e-6);
				} else if (execType.equals("update")) {
					resTimeRec.addUpdateResponse(threadId, endTime - startTime);
					if (rqstAbsoluteStartTime != null)
						resTimeRec.addUpdateResInfo(new RqstInfo(threadId,	startTime, endTime, rqstAbsoluteStartTime));
					else {
						resTimeRec.addUpdateResInfo(new RqstInfo(threadId, startTime, endTime));
					}
					System.out.println("update threadId:" + threadId
							+ " response time:" + (endTime - startTime) * 1e-6);
				}
			} else {
				System.out.println("response time:" + (endTime - startTime)
						* 1e-6);
			}
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}// END RUN()
}