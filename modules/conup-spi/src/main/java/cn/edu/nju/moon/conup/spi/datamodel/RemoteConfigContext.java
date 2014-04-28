package cn.edu.nju.moon.conup.spi.datamodel;


public class RemoteConfigContext {
	private String ip;
	private int port;
	private String srcIdentifier;
	private String targetIdentifier;
	private String protocol;
	private String baseDir;
	private String classFilePath;
	private String contributionUri;
	private Scope scope;
	
	private String compsiteUri;

	public RemoteConfigContext(){
		
	}

	public RemoteConfigContext(String ip, int port, String targetIdentifier,
			String protocol, String baseDir, String classFilePath,
			String contributionUri, Scope scope, String compsiteUri) {
		super();
		this.ip = ip;
		this.port = port;
		this.targetIdentifier = targetIdentifier;
		this.protocol = protocol;
		this.baseDir = baseDir;
		this.classFilePath = classFilePath;
		this.contributionUri = contributionUri;
		this.scope = scope;
		this.compsiteUri = compsiteUri;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getSrcIdentifier() {
		return srcIdentifier;
	}

	public void setSrcIdentifier(String srcIdentifier) {
		this.srcIdentifier = srcIdentifier;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getTargetIdentifier() {
		return targetIdentifier;
	}

	public void setTargetIdentifier(String targetIdentifier) {
		this.targetIdentifier = targetIdentifier;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public String getClassFilePath() {
		return classFilePath;
	}

	public void setClassFilePath(String classFilePath) {
		this.classFilePath = classFilePath;
	}

	public String getContributionUri() {
		return contributionUri;
	}

	public void setContributionUri(String contributionUri) {
		this.contributionUri = contributionUri;
	}

	public String getCompsiteUri() {
		return compsiteUri;
	}

	public void setCompsiteUri(String compsiteUri) {
		this.compsiteUri = compsiteUri;
	}

}
