package cn.edu.nju.moon.conup.communication.generator;


/**
 * It's used to generate a .composite file in directory ./src/main/resources
 * 
 * */
public interface CompositeGenerator {
	public String generate();
	public String getCompositeName();
}
