/**
 * 
 */
package cn.edu.nju.moon.conup.spi.datamodel;

/**
 *when the event, including TransactionStart, TransactionEnd, FirstRequestService, DependencesChanged, happen, 
 *its dynamicdenpendencesManager will notify the transaction manager. 
 *
 */
public enum EventType{
	TransactionStart,
	TransactionEnd,
	FirstRequestService,
	DependencesChanged;
}
