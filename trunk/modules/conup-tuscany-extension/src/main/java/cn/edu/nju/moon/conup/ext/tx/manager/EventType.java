/**
 * 
 */
package cn.edu.nju.moon.conup.ext.tx.manager;

/**
 *when the event, including TransactionStart, TransactionEnd, FirstRequestService, DependencesChanged, happen, 
 *its dynamicdenpendencesManager will notify the transaction manager. 
 *
 */
enum EventType{
	TransactionStart,
	TransactionEnd,
	FirstRequestService,
	DependencesChanged;
}
