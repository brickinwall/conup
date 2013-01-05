package cn.edu.nju.moon.conup.sample.proc.launcher;
import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.sample.proc.services.ProcService;

public class ProcVisitorThread extends Thread {
	private Node node;

	public ProcVisitorThread(Node node){
		this.node = node;
	}

	public void run(){
		try {
			ProcService pi = node.getService(ProcService.class, "ProcComponent#service-binding(ProcService/ProcService)");
			System.out.println("\t" + "" + pi.process("emptyExeProc", "nju,cs,pass", ""));
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}//END RUN()
}
