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
package com.tuscanyscatours.shoppingcart.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;

import com.tuscanyscatours.common.TripItem;
import com.tuscanyscatours.shoppingcart.CartStore;

/**
 * An implementation of the CartStore service
 */
//TODO: adapting to Tuscany 2
//@Scope("CONVERSATION")
@Service(CartStore.class)
public class CartStoreImpl implements CartStore {
	
	private String COMP_VERSION= "Ver_0";

    //@ConversationID
//    protected String cartId;

    private static Map<String, List<TripItem>> trips = new ConcurrentHashMap<String, List<TripItem>>(); 
//    		new ArrayList<TripItem>();

//    @Init
//    public void initCart() {
//        LOGGER.fine("CartStore init for id: " + cartId);
//    }
//
//    @Destroy
//    public void destroyCart() {
//        LOGGER.fine("CartStore destroy for id: " + cartId);
//    }

    @ConupTransaction
    public void addTrip(String cartID, TripItem trip) {
//        trips.add(trip);
    	if(trips.get(cartID) != null)
    		trips.get(cartID).add(trip);
    	else{
    		trips.put(cartID, new ArrayList<TripItem>());
    		trips.get(cartID).add(trip);
    	}
    }

    @ConupTransaction
    public void removeTrip(String cartID, TripItem trip) {
//        trips.remove(trip);
    	if(trips.get(cartID) != null)
    		trips.get(cartID).remove(trip);
    	else
    		trips.put(cartID, new ArrayList<TripItem>());
    	
    }

    @ConupTransaction
    public TripItem[] getTrips(String cartID) {
    	return trips.get(cartID).toArray(new TripItem[trips.get(cartID).size()]);
//        return trips.toArray(new TripItem[trips.size()]);
    }

    @ConupTransaction
    public void reset(String cartID) {
//        trips.clear();
    	trips.remove(cartID);
    }
}
