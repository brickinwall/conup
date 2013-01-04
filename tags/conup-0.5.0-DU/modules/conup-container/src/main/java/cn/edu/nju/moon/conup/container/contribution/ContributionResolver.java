package cn.edu.nju.moon.conup.container.contribution;

import java.net.URL;

public interface ContributionResolver {
	public URL getCompositeURL(String absContributionPath, String compositeName);
}
