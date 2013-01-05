package cn.edu.nju.moon.conup.txpre;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.txpre.Event;
import cn.edu.nju.moon.conup.txpre.StateMachine;

public class StateMachineTest {
	
	StateMachine stateMachine = null;
	@Before
	public void setUp() throws Exception {		
		List<Integer> states = new LinkedList<Integer>();
		states.add(1);
		states.add(3);
		states.add(5);
		List<Event> events = new LinkedList<Event>();
		events.add(new Event(1,3,"COM.A.1"));
		events.add(new Event(3,5,"COM.B.3"));
		stateMachine = new StateMachine(states,events);
	}

	@After
	public void tearDown() throws Exception {
	}
	@Test
	public void testAddState()
	{
		int newState = 7;
		assertFalse(stateMachine.getStates().contains(newState));
		stateMachine.addState(newState);
		assertTrue(stateMachine.getStates().contains(newState));
		assertEquals(4,stateMachine.getStatesCount());
	}
	
	@Test
	public void testDeleteState()
	{
		
		int state = 3;
		assertTrue(stateMachine.getStates().contains(state));
		stateMachine.deleteState(state);
		assertFalse(stateMachine.getStates().contains(state));
		assertEquals(2,stateMachine.getStatesCount());
	}
	
	@Test
	public void testAddEvent()
	{
		Event eve = new Event(5,7,"COM.C.5");
		assertFalse(stateMachine.getEvents().contains(eve));
		stateMachine.addEvent(eve);
		assertTrue(stateMachine.getEvents().contains(eve));
		assertNotNull(stateMachine.getEvent("COM.C.5"));
		
		
	}
	
	@Test
	public void testDeleteEvent()
	{
		String eve = "COM.A.1";
		assertNotNull(stateMachine.getEvent(eve));
		Event event = stateMachine.getEvent(eve);
		stateMachine.deleteEvent(event);
		assertNull(stateMachine.getEvent(eve));		
	}
	
	@Test
	public void testMergeStates(){
		String eve = "COM.A.1";
		stateMachine.mergeStates(1,3);
		assertEquals(2,stateMachine.getStatesCount());
		assertEquals(3,stateMachine.getEvent(eve).getHead());	
	}

}
