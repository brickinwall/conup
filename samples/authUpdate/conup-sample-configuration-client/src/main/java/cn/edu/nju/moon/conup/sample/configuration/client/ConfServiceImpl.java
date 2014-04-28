package cn.edu.nju.moon.conup.sample.configuration.client;

import java.rmi.ConnectException;
import java.util.logging.Logger;

import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.comm.api.remote.RemoteConfigTool;
import cn.edu.nju.moon.conup.sample.configuration.model.TargetComp;
import cn.edu.nju.moon.conup.sample.configuration.util.UpdateXmlUtil;
import cn.edu.nju.moon.conup.spi.datamodel.RemoteConfigContext;

@Service(ConfService.class)
public class ConfServiceImpl implements ConfService {
	private static final Logger LOGGER = Logger.getLogger(ConfServiceImpl.class.getName());

	@Override
	public void ondemand() {
		RemoteConfigTool rcs =  new RemoteConfigTool();
		UpdateXmlUtil updateXmlUtil = new UpdateXmlUtil();
		try {
			TargetComp targetComp = updateXmlUtil.getTargetComp();
			String ipAddress = targetComp.getIpAddress();
			String targetCompIdentifier = targetComp.getTargetCompIdentifier();
			int port = targetComp.getPort();
			
			rcs.ondemand(ipAddress, port, targetCompIdentifier, "CONSISTENCY", null);
		} catch (ConnectException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void update() {
		RemoteConfigTool rcs =  new RemoteConfigTool();
		UpdateXmlUtil updateXmlUtil = new UpdateXmlUtil();
		try {
			TargetComp targetComp = updateXmlUtil.getTargetComp();
			String ipAddress = targetComp.getIpAddress();
			String targetCompIdentifier = targetComp.getTargetCompIdentifier();
			int port = targetComp.getPort();
			String contributionUri = targetComp.getContributionUri();
			String compsiteUri = targetComp.getCompositeUri();
			String baseDir = targetComp.getBaseDir();
			String compImpl = targetComp.getCompImpl();
			
			String protocol = "CONSISTENCY";
			RemoteConfigContext rcc = new RemoteConfigContext(ipAddress, port,
					targetCompIdentifier, protocol, baseDir, compImpl,
					contributionUri, null, compsiteUri);
			rcs.update(rcc);
			
		} catch (ConnectException e) {
			e.printStackTrace();
		}
	}
}
