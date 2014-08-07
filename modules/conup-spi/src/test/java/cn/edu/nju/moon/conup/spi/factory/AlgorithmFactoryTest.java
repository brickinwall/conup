package cn.edu.nju.moon.conup.spi.factory;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class AlgorithmFactoryTest {
	/** represent quiescence algorithm */
	@SuppressWarnings("unused")
	private final String QUIESCENCE_ALGORITHM_TYPE = "QUIESCENCE_ALGORITHM";
	/** represent tranquillity algorithm */
	@SuppressWarnings("unused")
	private final String TRANQUILLITY_ALGORITHM_TYPE = "TRANQUILLITY_ALGORITHM";
	/** represent version-consistency algorithm */
	private final String CONSISTENCY_ALGORITHMTYPE = "CONSISTENCY_ALGORITHM";
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateAlgorithm() {
		AlgorithmFactory factory;
		Algorithm algorithm;
		
		factory = new AlgorithmFactory();
		algorithm = factory.createAlgorithm(this.CONSISTENCY_ALGORITHMTYPE);
		assertNotNull(algorithm);
		assertTrue(algorithm.getAlgorithmType().
				equals(this.CONSISTENCY_ALGORITHMTYPE));
		
	}

	@Test
	public void testCreateOndemandSetup() {
		AlgorithmFactory factory;
		OndemandSetup ondemand;
		
		factory = new AlgorithmFactory();
		ondemand = factory.createOndemandSetup(this.CONSISTENCY_ALGORITHMTYPE);
		assertNotNull(ondemand);
		assertTrue(ondemand.getAlgorithmType().
				equals(this.CONSISTENCY_ALGORITHMTYPE));
		
	}

}
