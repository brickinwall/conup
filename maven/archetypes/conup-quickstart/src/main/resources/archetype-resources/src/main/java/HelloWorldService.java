package ${package};

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface HelloWorldService {
	public String sayHello(String name);
}
