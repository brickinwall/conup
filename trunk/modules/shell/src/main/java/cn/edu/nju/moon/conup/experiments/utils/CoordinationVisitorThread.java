package cn.edu.nju.moon.conup.experiments.utils;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import com.tuscanyscatours.coordination.Coordination;

public class CoordinationVisitorThread extends Thread{
	private Node node;

	public CoordinationVisitorThread(Node node) {
		this.node = node;
	}

	public void run() {
		try {
			Coordination scaTour = node.getService(Coordination.class, "Coordination#service-binding(Coordination/Coordination)");
			scaTour.coordinate();
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}// END RUN()
}
