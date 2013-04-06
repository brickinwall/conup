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

import java.util.UUID;
import java.util.logging.Logger;

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;

import com.tuscanyscatours.common.TripItem;
import com.tuscanyscatours.payment.Payment;
import com.tuscanyscatours.shoppingcart.CartCheckout;
import com.tuscanyscatours.shoppingcart.CartInitialize;
import com.tuscanyscatours.shoppingcart.CartStore;
import com.tuscanyscatours.shoppingcart.CartUpdates;

/**
 * An implementation of the ShoppingCart service
 */
@Service({CartInitialize.class, CartUpdates.class, CartCheckout.class})
public class ShoppingCartImpl implements CartInitialize, CartUpdates, CartCheckout {
	private static String COMP_VER = "Ver_0";
	private static String COMP_NAME = "ShoppingCart";
	
	private static Logger LOGGER = Logger.getLogger(ShoppingCartImpl.class.getName());

    protected CartStore cartStore;
    protected Payment payment;
    
    public CartStore getCartStore() {
		return cartStore;
	}
    
    @Reference
	public void setCartStore(CartStore cartStore) {
		this.cartStore = cartStore;
	}

	public Payment getPayment() {
		return payment;
	}
	
	@Reference
	public void setPayment(Payment payment) {
		this.payment = payment;
	}

//    @Reference
//    protected CartStore cartStore;
//
//    @Reference
//    protected Payment payment;

    @ConupTransaction
    public String newCart() {
//    	String threadID = getThreadID();
//    	ExecutionRecorder exeRecorder;
//		InterceptorCache interceptorCache;
//		TransactionContext txContextInCache;
//		String rootTx;
//		String exeProc;
//		interceptorCache = InterceptorCache.getInstance(COMP_NAME);
//		txContextInCache = interceptorCache.getTxCtx(threadID);
//		rootTx = txContextInCache.getRootTx();
//		exeRecorder = ExecutionRecorder.getInstance(COMP_NAME);
//		exeProc = "newCart." + COMP_VER;
//		exeRecorder.addAction(rootTx, exeProc);
		
        String cartId = UUID.randomUUID().toString();
        return cartId;
    }

    @ConupTransaction
    public void addTrip(String cartId, TripItem trip) {
//    	String threadID = getThreadID();
//    	ExecutionRecorder exeRecorder;
//		InterceptorCache interceptorCache;
//		TransactionContext txContextInCache;
//		String rootTx;
//		String exeProc;
//		interceptorCache = InterceptorCache.getInstance(COMP_NAME);
//		txContextInCache = interceptorCache.getTxCtx(threadID);
//		rootTx = txContextInCache.getRootTx();
//		exeRecorder = ExecutionRecorder.getInstance(COMP_NAME);
//		exeProc = "addTrip." + COMP_VER;
//		exeRecorder.addAction(rootTx, exeProc);
		
    	cartStore.addTrip(cartId, trip);
    }

    @ConupTransaction
    public void removeTrip(String cartId, TripItem trip) {
//    	String threadID = getThreadID();
//    	ExecutionRecorder exeRecorder;
//		InterceptorCache interceptorCache;
//		TransactionContext txContextInCache;
//		String rootTx;
//		String exeProc;
//		interceptorCache = InterceptorCache.getInstance(COMP_NAME);
//		txContextInCache = interceptorCache.getTxCtx(threadID);
//		rootTx = txContextInCache.getRootTx();
//		exeRecorder = ExecutionRecorder.getInstance(COMP_NAME);
//		exeProc = "removeTrip." + COMP_VER;
//		exeRecorder.addAction(rootTx, exeProc);
		
    	cartStore.addTrip(cartId, trip);
    }

    @ConupTransaction
    public TripItem[] getTrips(String cartId) {
//    	String threadID = getThreadID();
//    	ExecutionRecorder exeRecorder;
//		InterceptorCache interceptorCache;
//		TransactionContext txContextInCache;
//		String rootTx;
//		String exeProc;
//		interceptorCache = InterceptorCache.getInstance(COMP_NAME);
//		txContextInCache = interceptorCache.getTxCtx(threadID);
//		rootTx = txContextInCache.getRootTx();
//		exeRecorder = ExecutionRecorder.getInstance(COMP_NAME);
//		exeProc = "getTrips." + COMP_VER;
//		exeRecorder.addAction(rootTx, exeProc);
		
    	return cartStore.getTrips(cartId);
    }

    @ConupTransaction
    public void checkout(String cartId, String customerName) {
//    	String threadID = getThreadID();
//    	ExecutionRecorder exeRecorder;
//		InterceptorCache interceptorCache;
//		TransactionContext txContextInCache;
//		String rootTx;
//		String exeProc;
//		interceptorCache = InterceptorCache.getInstance(COMP_NAME);
//		txContextInCache = interceptorCache.getTxCtx(threadID);
//		rootTx = txContextInCache.getRootTx();
//		exeRecorder = ExecutionRecorder.getInstance(COMP_NAME);
//		exeProc = "checkout." + COMP_VER;
//		exeRecorder.addAction(rootTx, exeProc);
		
    	LOGGER.info("Shoppingcart " + COMP_VER);
        // get users credentials. Hard coded for now but should
        // come from the security context
        String customerId = customerName;

        // get the total for all the trips
        float amount = (float)0.0;

//        TripItem[] trips = getTrips(cartId);
        TripItem[] trips = cartStore.getTrips(cartId);

        for (TripItem trip : trips) {
            if (trip.getType().equals(TripItem.TRIP)) {
                amount += trip.getPrice();
            } else {
                for (TripItem tripItem : trip.getTripItems()) {
                    amount += tripItem.getPrice();
                }
            }
        }
        LOGGER.fine("total amount:" + amount);
        // Take the payment from the customer
        payment.makePaymentMember(customerId, amount);

        // reset the cart store 
        cartStore.reset(cartId);
    }

	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}
}
