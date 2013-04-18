
package com.tuscanyscatours.payment.creditcard;

public class ObjectFactory {

    public ObjectFactory() {
    }

    public PayerType createPayerType() {
        return new PayerType();
    }

    public CreditCardDetailsType createCreditCardDetailsType() {
        return new CreditCardDetailsType();
    }

    public AddressType createAddressType() {
        return new AddressType();
    }

}
