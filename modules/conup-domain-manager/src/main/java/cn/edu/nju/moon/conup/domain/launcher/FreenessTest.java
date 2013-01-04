package cn.edu.nju.moon.conup.domain.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.domain.services.FreenessService;

public class FreenessTest {
	private static String DB_COMPONENT = "DBComponent";
	private static String AUTH_COMPONENT = "AuthComponent";
	private static String PROC_COMPONENT = "ProcComponent";
	private static String PORTAL_COMPONENT = "PortalComponent";
	private static long DELAY = 8 * 1000;
	private static long PERIOD = 1000;
	private Node node;
	
	public FreenessTest(Node node){
		this.node = node;
	}
	
	public void test(){
//		timer();
		command();
	}
	
	/* interactive approach, need to press Enter or any key to check for freeness. */
	private void command(){
		//input command
		System.out.println("Please press Enter to check for freeness:");
		InputStreamReader is = new InputStreamReader(System.in);
		BufferedReader reaer = new BufferedReader(is);
		String commond = null;
		String endpoint = "DomainManagerComponent#service-binding(FreenessService/FreenessService)";
		String [] componentNames = {FreenessTest.DB_COMPONENT, FreenessTest.AUTH_COMPONENT, 
				FreenessTest.PROC_COMPONENT, FreenessTest.PORTAL_COMPONENT};
		boolean isFreeness = false;
		FreenessService freenessService;
		while(true){
			try {
				commond = reaer.readLine();
				for (int i = 0; i < componentNames.length; i++) {
					freenessService = node.getService(FreenessService.class,
							endpoint);
					isFreeness = freenessService.isFreeness(componentNames[i]);
					System.out.println(componentNames[i] + ".isFreeness: " + isFreeness);
				}// for
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchServiceException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/* it's supposed to start multi thread, but too many info is printed in the console and hard to follow,
	 * so it's temporally deprecated.
	 *  */
	private void timer(){
		//is DBComponent freeness
		Timer dbTimer = new Timer();
		dbTimer.schedule(new FreenessTask(FreenessTest.DB_COMPONENT), FreenessTest.DELAY, FreenessTest.PERIOD);
		//is AuthComponent freeness
		Timer authTimer = new Timer();
		authTimer.schedule(new FreenessTask(FreenessTest.AUTH_COMPONENT), FreenessTest.DELAY, FreenessTest.PERIOD);
		//is ProcComponent freeness
		Timer procTimer = new Timer();
		procTimer.schedule(new FreenessTask(FreenessTest.PROC_COMPONENT), FreenessTest.DELAY, FreenessTest.PERIOD);
		//is PortalComponent freeness
		Timer portalTimer = new Timer();
		portalTimer.schedule(new FreenessTask(FreenessTest.PORTAL_COMPONENT), FreenessTest.DELAY, FreenessTest.PERIOD);
	}
	
	
	class FreenessTask extends TimerTask{
		private String componentName = null;
		String endpoint = null;
		
		public FreenessTask(String componentName){
			this.componentName = componentName;
			endpoint = "DomainManagerComponent#service-binding(FreenessService/FreenessService)";
		}

		@Override
		public void run() {
			boolean isFreeness = false;
			try {
				FreenessService freenessService = node.getService(FreenessService.class, endpoint);
				isFreeness = freenessService.isFreeness(componentName);
				System.out.println(componentName + ".isFreeness: " + isFreeness);
			} catch (NoSuchServiceException e) {
//				e.printStackTrace();
			}
		}//run
	}
}
