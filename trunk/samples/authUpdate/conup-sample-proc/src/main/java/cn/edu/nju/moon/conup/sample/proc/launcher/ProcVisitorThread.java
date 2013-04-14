package cn.edu.nju.moon.conup.sample.proc.launcher;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.sample.proc.services.ProcService;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class ProcVisitorThread extends Thread {
	private Node node;
	private static Logger LOGGER = Logger.getLogger(ProcVisitorThread.class.getName());

	public ProcVisitorThread(Node node){
		this.node = node;
	}

	public void run(){
		try {
			ProcService pi = node.getService(ProcService.class, "ProcComponent/ProcService");
			LOGGER.fine("\t" + "" + pi.process("emptyExeProc", "nju,cs,pass", ""));
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}//END RUN()
}
