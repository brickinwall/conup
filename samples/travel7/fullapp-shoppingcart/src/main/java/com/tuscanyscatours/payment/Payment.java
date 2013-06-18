package com.tuscanyscatours.payment;

import org.oasisopen.sca.annotation.Remotable;

@Remotable
public interface Payment {

	public String makePaymentMember(String customerId, float amount);

}
