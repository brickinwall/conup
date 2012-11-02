/**
 * 
 */
package cn.edu.nju.moon.conup.pre;

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
