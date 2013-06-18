package com.tuscanyscatours.creditcard;

import org.oasisopen.sca.annotation.Remotable;

import com.tuscanyscatours.payment.creditcard.CreditCardDetailsType;

@Remotable
public interface CreditCardPayment {
	public String authorize(CreditCardDetailsType creditCard, float amount);
}
