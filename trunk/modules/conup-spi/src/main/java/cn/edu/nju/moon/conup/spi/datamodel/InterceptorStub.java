package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class InterceptorStub {
	
	private Set<Interceptor> interceptors;
	
	public InterceptorStub() {
		this.interceptors = new HashSet<Interceptor>();;
	}

	public Set<Interceptor> getInterceptors() {
		synchronized (interceptors) {
			return interceptors;
		}
	}

	public void addInterceptor(Interceptor interceptor) {
		synchronized (interceptors) {
			this.interceptors.add(interceptor);
		}
	}
	
	public void notifyInterceptors(Object arg){
		synchronized (interceptors) {
			Iterator<Interceptor> iter = interceptors.iterator();
			while(iter.hasNext()){
				iter.next().update(arg);
			}
		}
	}
	
//	private Set<Observer> observers = new ConcurrentSkipListSet<Observer>();
//
//	@Override
//	public void registerObserver(Observer o) {
//		observers.add(o);
//	}
//
//	@Override
//	public void removeObserver(Observer o) {
//		observers.remove(o);
//	}
//
//	@Override
//	public void notifyObservers(Object arg) {
//		Iterator<Observer> iter = observers.iterator();
//		while(iter.hasNext()){
//			iter.next().update(this, arg);
//		}
//	}
//
//	@Override
//	public void setResult(String result) {
//		
//	}
	
}


