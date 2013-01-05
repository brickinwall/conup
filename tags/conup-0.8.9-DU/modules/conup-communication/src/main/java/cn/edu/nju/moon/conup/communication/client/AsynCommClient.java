package cn.edu.nju.moon.conup.communication.client;

import java.net.InetSocketAddress;
import java.util.logging.Logger;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import cn.edu.nju.moon.conup.communication.model.RequestObject;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
/**
 * @author rgc
 */
public class AsynCommClient {
	private static final Logger LOGGER = Logger.getLogger(AsynCommClient.class.getName());
	private RequestObject reqObj = null;

	/**
	 * send msg to target component
	 * @param ipAndPort
	 * @param srcIdentifier ---> who send this msg
	 * @param targetIdentifier --->  who receive this msg
	 * @param proctocol
	 * @param payload
	 */
	public void sendMsg(String ip, int port, String srcIdentifier, String targetIdentifier, String proctocol, MsgType msgType, String payload){
		reqObj = new RequestObject();
		reqObj.setSrcIdentifier(srcIdentifier);
		reqObj.setTargetIdentifier(targetIdentifier);
		reqObj.setProtocol(proctocol);
		reqObj.setPayload(payload);
		reqObj.setMsgType(msgType);
		
		IoConnector connector = new NioSocketConnector(); 
		connector.setConnectTimeoutMillis(10000);
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
		LoggingFilter logFilter = new LoggingFilter();
		logFilter.setSessionClosedLogLevel(LogLevel.DEBUG);
		logFilter.setSessionCreatedLogLevel(LogLevel.DEBUG);
		logFilter.setSessionOpenedLogLevel(LogLevel.DEBUG);
		logFilter.setSessionIdleLogLevel(LogLevel.DEBUG);
		logFilter.setMessageSentLogLevel(LogLevel.DEBUG);
		logFilter.setMessageReceivedLogLevel(LogLevel.DEBUG);
		connector.getFilterChain().addLast("logger", logFilter);
		connector.setHandler(new IoHandlerAdapter(){
			@Override
			public void messageReceived(IoSession session, Object message) throws Exception {
				// add callback to tell the result
			}
			@Override
			public void exceptionCaught(IoSession session, Throwable cause)
					throws Exception {
				LOGGER.warning(cause.getMessage());
				session.close(true);
			}
		});
		
		IoSession session = null;
		
		ConnectFuture future = connector.connect(new InetSocketAddress(ip, port));
		future.awaitUninterruptibly();								// block the thread running to wait for the connection established
		
		session = future.getSession();
		session.write(reqObj);										// send msg 
		session.close(false);
		connector.dispose();
		//asyn do not care the result!!!!
//		session.getCloseFuture().awaitUninterruptibly();			// wait for connection off
//		connector.dispose();
		
	}
}
