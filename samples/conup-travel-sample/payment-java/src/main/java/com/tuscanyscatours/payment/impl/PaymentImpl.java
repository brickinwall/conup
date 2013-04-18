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

package com.tuscanyscatours.payment.impl;

import java.util.logging.Logger;

import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;

import com.tuscanyscatours.creditcard.CreditCardPayment;
import com.tuscanyscatours.currencyconverter.CurrencyConverter;
import com.tuscanyscatours.customer.Customer;
import com.tuscanyscatours.customer.CustomerNotFoundException;
import com.tuscanyscatours.customer.CustomerRegistry;
import com.tuscanyscatours.emailgateway.EmailGateway;
import com.tuscanyscatours.payment.Payment;

@Service(Payment.class)
public class PaymentImpl implements Payment {
	private static Logger LOGGER = Logger.getLogger(Payment.class.getName());
	
	@Property
	protected float transactionFee = 0.01f;
	
    protected CustomerRegistry customerRegistry;

    protected CreditCardPayment creditCardPayment;

    protected EmailGateway emailGateway;

    protected CurrencyConverter currencyConverter;

    
    public CustomerRegistry getCustomerRegistry() {
		return customerRegistry;
	}

    @Reference
	public void setCustomerRegistry(CustomerRegistry customerRegistry) {
		this.customerRegistry = customerRegistry;
	}

	public CreditCardPayment getCreditCardPayment() {
		return creditCardPayment;
	}

	@Reference
	public void setCreditCardPayment(CreditCardPayment creditCardPayment) {
		this.creditCardPayment = creditCardPayment;
	}

	public EmailGateway getEmailGateway() {
		return emailGateway;
	}

	@Reference
	public void setEmailGateway(EmailGateway emailGateway) {
		this.emailGateway = emailGateway;
	}

	public CurrencyConverter getCurrencyConverter() {
		return currencyConverter;
	}

	@Reference
	public void setCurrencyConverter(CurrencyConverter currencyConverter) {
		this.currencyConverter = currencyConverter;
	}

	@ConupTransaction
    public String makePaymentMember(String customerId, float amount) {
        try {
            Customer customer = customerRegistry.getCustomer(customerId);
            String status= creditCardPayment.authorize(customer.getCreditCard(), amount);
            emailGateway.sendEmail("order@tuscanyscatours.com", customer.getEmail(), "Status for your payment",
                    customer + " >>> Status = " + status);
            
            // add 2000ms delay
            Thread.sleep(2000);
            return status;
        } catch (CustomerNotFoundException ex) {
            return "Payment failed due to " + ex.getMessage();
        } catch (Throwable t) {
        	System.out.println("error in makePaymentMember");
            return "Payment failed due to system error " + t.getMessage();
        }
    }
    
    public String checkSecurity(String securityPrompt) {        
        LOGGER.fine("Extra securiy - " + securityPrompt);
        LOGGER.fine("password = abcxyz");
        return "abcxyz";
    }

}
