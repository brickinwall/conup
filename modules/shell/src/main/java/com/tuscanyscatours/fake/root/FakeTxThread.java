package com.tuscanyscatours.fake.root;

import java.util.concurrent.CountDownLatch;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.ext.utils.experiments.model.ResponseTimeRecorder;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.RqstInfo;

public class FakeTxThread extends Thread {

	private Node node;
	private int threadId;
	private CountDownLatch countDown;
	private ResponseTimeRecorder resTimeRec;
	private String execType;
	private Long rqstAbsoluteStartTime = null;

	public FakeTxThread(Node node) {
		this.node = node;
	}

	public FakeTxThread(Node node, CountDownLatch countDown) {
		this.countDown = countDown;
		this.node = node;
	}

	public FakeTxThread(Node node, int threadId) {
		this.node = node;
		this.threadId = threadId;
	}

	public FakeTxThread(Node node, int threadId,
			ResponseTimeRecorder resTimeRec, String execType) {
		this.node = node;
		this.threadId = threadId;
		this.resTimeRec = resTimeRec;
		this.execType = execType;
	}

	public FakeTxThread(Node node, CountDownLatch countDown,
			int threadId, ResponseTimeRecorder resTimeRec, String execType) {
		this.node = node;
		this.countDown = countDown;
		this.threadId = threadId;
		this.resTimeRec = resTimeRec;
		this.execType = execType;
	}

	public FakeTxThread(Node node, int threadId,
			ResponseTimeRecorder resTimeRec, String execType,
			long rqstAbsoluteStartTime) {
		this.node = node;
		this.threadId = threadId;
		this.resTimeRec = resTimeRec;
		this.execType = execType;
		this.rqstAbsoluteStartTime = rqstAbsoluteStartTime;
	}

	public FakeTxThread(Node node, CountDownLatch updateCountDown,
			int threadId, ResponseTimeRecorder resTimeRec, String execType,
			long rqstAbsoluteStartTime) {

		this.node = node;
		this.countDown = updateCountDown;
		this.threadId = threadId;
		this.resTimeRec = resTimeRec;
		this.execType = execType;
		this.rqstAbsoluteStartTime = rqstAbsoluteStartTime;
	}

	public void run() {
		try {

			FakeRootTx fakeRootTx = node.getService(FakeRootTx.class,
					"FakeRoot/FakeRootTx");
			long startTime = System.nanoTime();
			fakeRootTx.invokeFakeRootTx();
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
						resTimeRec.addUpdateResInfo(new RqstInfo(threadId,
								startTime, endTime, rqstAbsoluteStartTime));
					else {
						resTimeRec.addUpdateResInfo(new RqstInfo(threadId,
								startTime, endTime));
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
