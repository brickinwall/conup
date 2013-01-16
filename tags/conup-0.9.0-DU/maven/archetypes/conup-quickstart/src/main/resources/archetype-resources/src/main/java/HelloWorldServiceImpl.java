package ${package};

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;

public class HelloWorldServiceImpl implements HelloWorldService{

	@Override
	@ConupTransaction
	public String sayHello(String name){
		return "hello " + name;
	}

}
