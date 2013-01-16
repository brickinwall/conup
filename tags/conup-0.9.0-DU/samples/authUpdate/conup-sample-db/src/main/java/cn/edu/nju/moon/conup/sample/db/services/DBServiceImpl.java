package cn.edu.nju.moon.conup.sample.db.services;

import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;


@Service(DBService.class)
public class DBServiceImpl implements DBService {
	private String version = "version.1";

	@Override
	@ConupTransaction
	public String dbOperation(String exeProc) {
//		List<String> result = new ArrayList<String>();
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		result.add("hello tuscany...");
		String threadID = getThreadID();
		InterceptorCache interceptorCache = InterceptorCache.getInstance("DBComponent");
		TransactionContext txContextInCache = interceptorCache.getTxCtx(threadID);
		String rootTx = txContextInCache.getRootTx();
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance("DBComponent");
		exeRecorder.addAction(rootTx, exeProc);
		exeRecorder.addAction(rootTx, "DBComponent.dbOperation." + version);

		return exeRecorder.getAction(rootTx);
	}
	
	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}
}
