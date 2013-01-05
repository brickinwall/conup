package cn.edu.nju.moon.conup.communication.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.communication.model.RequestObject;
import cn.edu.nju.moon.conup.communication.model.ResponseObject;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * @author rgc
 */
public class AsynCommClientTest {
	static AsynCommClient acc = null;
	private Logger LOGGER = Logger.getLogger(AsynCommClientTest.class.getName());
	@Before
	public void setUp() throws Exception {
		acc = new AsynCommClient();
	}

	@Test
	public void testSendMsg() {
		
		new ServerThread().start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String payload = createPayload("PortalComponent", "AuthComponent", UUID.randomUUID().toString(), "NOTIFY_FUTURE_CREATE");
		MsgType msgType = MsgType.DEPENDENCE_MSG;
		acc.sendMsg("localhost", 11111, "PortalComponent", "AuthComponent", "CONSISTENCY", msgType, payload);
	}
	
	class ServerThread extends Thread{
		public void run(){
			IoAcceptor acceptor = new NioSocketAcceptor();

			acceptor.getFilterChain().addLast("codec",
					new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
			acceptor.getFilterChain().addLast("logger", new LoggingFilter());

			acceptor.setHandler(new IoHandlerAdapter(){
				public void messageReceived(IoSession session, Object message) throws Exception {
					RequestObject reqObj = (RequestObject) message;
					ResponseObject reponseObj = process(reqObj);
					session.write(reponseObj);
				}

				private ResponseObject process(RequestObject reqObj) {
					MsgType msgType = reqObj.getMsgType();
					
					if(msgType.equals(MsgType.DEPENDENCE_MSG)){
						return manageDep(reqObj);
					} else{
						return null;
					}
				}


				/**
				 * process an msg about dependence 
				 * @param reqObj
				 * @return
				 */
				private ResponseObject manageDep(RequestObject reqObj) {
					ResponseObject responseObj = new ResponseObject();
					responseObj.setPayload("manageResult:true");
					return responseObj;
				}
			});

			try {
				acceptor.bind(new InetSocketAddress("localhost", 11111));
				LOGGER.info("component communication server(localhost" + ":" + 11111 + ") start...");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
	}
	
	private static String createPayload(String srcComp, String targetComp, String rootTx, String operationType){
		String payload = "ConsistencyPayload.SRC_COMPONENT:" + srcComp + ",ConsistencyPayload.TARGET_COMPONENT:" + targetComp + ",ConsistencyPayload.ROOT_TX:" + rootTx
				+ ",ConsistencyPayload.OPERATION_TYPE:" + operationType;
		return payload;
	}
	
}
