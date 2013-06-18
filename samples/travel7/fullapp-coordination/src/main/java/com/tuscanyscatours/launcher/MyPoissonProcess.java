package com.tuscanyscatours.launcher;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import it.unipr.ce.dsg.deus.core.Event;
import it.unipr.ce.dsg.deus.core.InvalidParamsException;
import it.unipr.ce.dsg.deus.core.Node;
import it.unipr.ce.dsg.deus.core.Process;

public class MyPoissonProcess extends Process {

	private static final String MEAN_ARRIVAL = "meanArrival";
	private float meanArrival = 0;
	private Random random = null;

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public MyPoissonProcess(String id, Properties params,
			ArrayList<Node> referencedNodes, ArrayList<Event> referencedEvents)
			throws InvalidParamsException {
		super(id, params, referencedNodes, referencedEvents);
		initialize();
	}

	@Override
	public void initialize() throws InvalidParamsException {
		if (params.getProperty(MEAN_ARRIVAL) == null)
			throw new InvalidParamsException(MEAN_ARRIVAL + " param is expected.");

		try {
			meanArrival = Float.parseFloat(params.getProperty(MEAN_ARRIVAL));
		} catch (NumberFormatException ex) {
			throw new InvalidParamsException(MEAN_ARRIVAL + " must be a valid float value.");
		}
	}

	// returns exponentially distributed random variable
	private float expRandom(Random random, float lambda) {
		float randomFloat = (float) (-Math.log(1 - random.nextFloat()) / lambda);
		return randomFloat;
	}

	@Override
	public float getNextTriggeringTime(Event arg0, float arg1) {
		return expRandom(random, (float) 1 / meanArrival);
	}
	
	public static void main(String[] args) throws InvalidParamsException {
		int seed = 123456789;
		Properties params = new Properties();
		float MeanArrival = 100.0f;
		params.setProperty("meanArrival", Float.toString(MeanArrival));
		ArrayList<Event> refEvents = new ArrayList<Event>();
		MyPoissonProcess mpp = new MyPoissonProcess("myPoissonProcess", params, null, refEvents);
		Event event = null;
		for(int i = 0; i < 10; i++){
			Random random = new Random(seed);
			mpp.setRandom(random);
			for(int j = 0; j < 10; j++){
				System.out.println("round " + i + " j:" +mpp.getNextTriggeringTime(event, 0));
			}
			
			random = new Random(seed);
			mpp.setRandom(random);
			for(int k = 0; k < 10; k++){
				System.out.println("round " + i + " k:" +mpp.getNextTriggeringTime(event, 0));
			}
		}
		
		float sum = 0f;
		for(int m = 0; m < 10000000; m++){
			sum += mpp.getNextTriggeringTime(event, 0);
		}
		System.out.println(sum / 10000000);
	}

}
