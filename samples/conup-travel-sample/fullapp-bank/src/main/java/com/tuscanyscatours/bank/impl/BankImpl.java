package com.tuscanyscatours.bank.impl;

import java.util.HashMap;
import java.util.Map;

import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;

import com.tuscanyscatours.bank.Bank;

@Service(Bank.class)
public class BankImpl implements Bank {

	// currency index
    private Map<String, Integer> currencyIndex = new HashMap<String, Integer>();

    // exchange rates
    private final double rates[][] = { {1.00, 0.50, 0.66}, {2.00, 1.00, 1.33}, {1.50, 0.75, 1.00}};

    public BankImpl() {
        currencyIndex.put("USD", new Integer(0));
        currencyIndex.put("GBP", new Integer(1));
        currencyIndex.put("EUR", new Integer(2));
    }
	
    @ConupTransaction
	@Override
	public double getExchangeRate(String fromCurrencyCode, String toCurrencyCode) {
		return rates[currencyIndex.get(fromCurrencyCode).intValue()][currencyIndex.get(toCurrencyCode).intValue()];
	}

}
