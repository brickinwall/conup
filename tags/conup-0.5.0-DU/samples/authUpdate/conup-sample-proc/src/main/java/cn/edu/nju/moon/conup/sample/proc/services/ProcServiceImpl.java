package cn.edu.nju.moon.conup.sample.proc.services;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.def.VcTransaction;
import cn.edu.nju.moon.conup.listener.ComponentListener;
import cn.edu.nju.moon.conup.listener.ComponentListenerImpl;

@Service(ProcService.class)
public class ProcServiceImpl implements ProcService {
	private VerificationService verify;
	private DBService db;

	public VerificationService getVerify() {
		return verify;
	}
	@Reference
	public void setVerify(VerificationService verify) {
		this.verify = verify;
	}

	public DBService getDb() {
		return db;
	}

	@Reference
	public void setDb(DBService db) {
		this.db = db;
	}
	@VcTransaction
	public List<String> process(String token, String data) {
//		ComponentListener listener = ComponentListenerImpl.getInstance();
//		Set<String> futureC = new HashSet<String>();
//		futureC.add("DBComponent");
//		futureC.add("AuthComponent");
//		Set<String> pastC = new HashSet<String>();
//		String threadID = new Integer(Thread.currentThread().hashCode()).toString();
//		
//		listener.notify("start", threadID, futureC, pastC);
//		listener.notify("running", threadID, futureC, pastC);
		
		Boolean authResult = verify.verify(token);
		
//		futureC.remove("AuthComponent");
//		pastC.add("AuthComponent");
		
//		listener.notify("running", threadID, futureC, pastC);
		if (authResult) {
			List<String> result = db.dbOperation();
//			futureC.remove("DBComponent");
//			pastC.add("DBComponent");
//			listener.notify("end", threadID, futureC, pastC);
//			printContainerInfo();
			return result;
		} else{
//			futureC.remove("DBComponent");
//			pastC.add("DBComponent");
//			listener.notify("end", threadID, futureC, pastC);
//			printContainerInfo();
			return null;
		}
	}
	
//	private void printContainerInfo(){
//		System.out.println("In ProcServiceImpl.printContainerInfo()...");
//		ContainerPrinter containerPrinter = new ContainerPrinter();
//		containerPrinter.printInArcRegistry(InArcRegistryImpl.getInstance());
//		containerPrinter.printOutArcRegistry(OutArcRegistryImpl.getInstance());
//		System.out.println("outArcRegistry" + OutArcRegistryImpl.getInstance());
//		containerPrinter.printTransactionRegistry(TransactionRegistryImpl.getInstance());
//	}

}
