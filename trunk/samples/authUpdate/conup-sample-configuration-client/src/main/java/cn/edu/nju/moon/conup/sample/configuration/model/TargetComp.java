package cn.edu.nju.moon.conup.sample.configuration.model;

/**
 * @author rgc
 */
public class TargetComp {
	private boolean isTarget;
	private String ipAddress;
	private int port;
	private String targetCompIdentifier;
	private String contributionUri;
	private String compositeUri;
	private String compImpl;
	private String baseDir;

	public TargetComp(boolean isTarget, String ipAddress, int port,
			String targetCompIdentifier, String contributionUri,
			String compositeUri, String compImpl, String baseDir) {
		super();
		this.isTarget = isTarget;
		this.ipAddress = ipAddress;
		this.port = port;
		this.targetCompIdentifier = targetCompIdentifier;
		this.contributionUri = contributionUri;
		this.compositeUri = compositeUri;
		this.compImpl = compImpl;
		this.baseDir = baseDir;
	}

	public boolean isTarget() {
		return isTarget;
	}

	public void setTarget(boolean isTarget) {
		this.isTarget = isTarget;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getTargetCompIdentifier() {
		return targetCompIdentifier;
	}

	public void setTargetCompIdentifier(String targetCompIdentifier) {
		this.targetCompIdentifier = targetCompIdentifier;
	}

	public String getContributionUri() {
		return contributionUri;
	}

	public void setContributionUri(String contributionUri) {
		this.contributionUri = contributionUri;
	}

	public String getCompositeUri() {
		return compositeUri;
	}

	public void setCompositeUri(String compositeUri) {
		this.compositeUri = compositeUri;
	}

	public String getCompImpl() {
		return compImpl;
	}

	public void setCompImpl(String compImpl) {
		this.compImpl = compImpl;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

}
