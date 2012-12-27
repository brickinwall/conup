package cn.edu.nju.moon.conup.algorithm;

import java.util.Set;

public interface VcAlgorithm {
	public void analyze(String transactionStatus, String threadID, Set<String> futureC, Set<String> pastC);
}
