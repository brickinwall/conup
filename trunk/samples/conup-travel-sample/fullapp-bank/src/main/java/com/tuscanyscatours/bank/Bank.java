package com.tuscanyscatours.bank;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface Bank {
	double getExchangeRate(String fromCurrencyCode, String toCurrencyCode);
}
