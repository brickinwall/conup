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
package com.tuscanyscatours.currencyconverter.impl;

import java.util.HashMap;
import java.util.Map;

import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;

import com.tuscanyscatours.currencyconverter.CurrencyConverter;

/**
 * An implementation of the CurrencyConverter service
 */
@Service(CurrencyConverter.class)
public class CurrencyConverterImpl implements CurrencyConverter {
	
	private String COMP_VERSION= "Ver_0";

    // currency index
    private Map<String, Integer> currencyIndex = new HashMap<String, Integer>();

    // exchange rates
    private final double rates[][] = { {1.00, 0.50, 0.66}, {2.00, 1.00, 1.33}, {1.50, 0.75, 1.00}};

    public CurrencyConverterImpl() {
        currencyIndex.put("USD", new Integer(0));
        currencyIndex.put("GBP", new Integer(1));
        currencyIndex.put("EUR", new Integer(2));
    }

    @ConupTransaction
    public double getExchangeRate(String fromCurrencyCode, String toCurrencyCode) {
        return rates[currencyIndex.get(fromCurrencyCode).intValue()][currencyIndex.get(toCurrencyCode).intValue()];
    }

    @ConupTransaction
    public double convert(String fromCurrencyCode, String toCurrencyCode, double amount) {
    	double exchangeRate = rates[currencyIndex.get(fromCurrencyCode).intValue()][currencyIndex.get(toCurrencyCode).intValue()];
    	//old version component should return amount * getExchangeRate(fromCurrencyCode, toCurrencyCode);
    	//new version component should return 2 * amount * getExchangeRate(fromCurrencyCode, toCurrencyCode);
        return amount * exchangeRate;
    }
}
