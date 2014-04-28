/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package com.tuscanyscatours.trip.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;

import com.tuscanyscatours.common.TripItem;
import com.tuscanyscatours.common.TripLeg;

/**
 * An implementation of the Trip service
 */
//@Scope("STATELESS")
@Service( {TripSearch.class, TripBook.class})
public class TripImpl implements TripSearch, TripBook {
	private Logger LOGGER = Logger.getLogger(TripImpl.class.getName());
	/** it's used to identify component version */
	private String COMP_VERSION= "Ver_0";
	private String COMP_NAME = "TripPartner";
    private List<TripInfo> trips = new ArrayList<TripInfo>();
    
    public TripImpl(){
    	trips.add(new TripInfo("FS1DEC06", "Florence and Siena pre-packaged tour", "LGW", "FLR", "06/12/09",
                "13/12/09", "27", 450, "EUR", "http://localhost:8085/tbd"));
    	trips.add(new TripInfo("FS1DEC13", "Florence and Siena pre-packaged tour 2", "LGW", "FLR", "13/12/09",
                "20/12/09", "27", 550, "EUR", "http://localhost:8085/tbd"));
    }
    
//    @Init
//    public void init() {
//        trips.add(new TripInfo("FS1DEC06", "Florence and Siena pre-packaged tour", "LGW", "FLR", "06/12/09",
//                               "13/12/09", "27", 450, "EUR", "http://localhost:8085/tbd"));
//        trips.add(new TripInfo("FS1DEC13", "Florence and Siena pre-packaged tour 2", "LGW", "FLR", "13/12/09",
//                               "20/12/09", "27", 550, "EUR", "http://localhost:8085/tbd"));
//    }
//    String threadID = getThreadID();
//    ExecutionRecorder exeRecorder;
//    InterceptorCache interceptorCache;
//    TransactionContext txContextInCache;
//    String rootTx;
//    String exeProc;
//    interceptorCache = InterceptorCache.getInstance(COMP_NAME);
//    txContextInCache = interceptorCache.getTxCtx(threadID);
//    rootTx = txContextInCache.getRootTx();
//    exeRecorder = ExecutionRecorder.getInstance(COMP_NAME);
//    exeProc = "TripPartner.searchSynch." + COMP_VER;
//    exeRecorder.addAction(rootTx, exeProc);

    @ConupTransaction
    public TripItem[] searchSynch(TripLeg tripLeg) {
    	LOGGER.fine("TripPartner " + COMP_VERSION);
    	
    	String threadID = getThreadID();
    	ExecutionRecorder exeRecorder;
		InterceptorCache interceptorCache;
		TransactionContext txContextInCache;
		String rootTx;
		String exeProc;
		interceptorCache = InterceptorCache.getInstance(COMP_NAME);
		txContextInCache = interceptorCache.getTxCtx(threadID);
		rootTx = txContextInCache.getRootTx();
		exeRecorder = ExecutionRecorder.getInstance(COMP_NAME);
		exeProc = "TripPartner.searchSynch." + COMP_VERSION;
		exeRecorder.addAction(rootTx, exeProc);
    	
        List<TripItem> items = new ArrayList<TripItem>();
        // find the pre-package trip
        for (TripInfo trip : trips) {
            if ((trip.getFromLocation().equals(tripLeg.getFromLocation())) && (trip.getToLocation().equals(tripLeg
                .getToLocation()))
                && (trip.getFromDate().equals(tripLeg.getFromDate()))) {
                TripItem item =
                    new TripItem("", "", TripItem.TRIP, trip.getName(), trip.getDescription(),
                                 trip.getFromLocation() + " - " + trip.getToLocation(), trip.getFromDate(), trip
                                     .getToDate(), trip.getPricePerPerson(), trip.getCurrency(), trip.getLink());
                items.add(item);
            }
        }
        
        // add 200ms delay
        try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        return items.toArray(new TripItem[items.size()]);
    }

    @ConupTransaction
    public int getPercentComplete() {
    	String threadID = getThreadID();
    	ExecutionRecorder exeRecorder;
		InterceptorCache interceptorCache;
		TransactionContext txContextInCache;
		String rootTx;
		String exeProc;
		interceptorCache = InterceptorCache.getInstance(COMP_NAME);
		txContextInCache = interceptorCache.getTxCtx(threadID);
		rootTx = txContextInCache.getRootTx();
		exeRecorder = ExecutionRecorder.getInstance(COMP_NAME);
		exeProc = "getPercentComplete." + COMP_VERSION;
		exeRecorder.addAction(rootTx, exeProc);
		
        return 100;
    }

    @ConupTransaction
    public String book(TripItem tripItem) {
    	String threadID = getThreadID();
    	ExecutionRecorder exeRecorder;
		InterceptorCache interceptorCache;
		TransactionContext txContextInCache;
		String rootTx;
		String exeProc;
		interceptorCache = InterceptorCache.getInstance(COMP_NAME);
		txContextInCache = interceptorCache.getTxCtx(threadID);
		rootTx = txContextInCache.getRootTx();
		exeRecorder = ExecutionRecorder.getInstance(COMP_NAME);
		exeProc = "TripPartner.book." + COMP_VERSION;
		exeRecorder.addAction(rootTx, exeProc);
		
        return "trip1";
    }
    
	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}
}
