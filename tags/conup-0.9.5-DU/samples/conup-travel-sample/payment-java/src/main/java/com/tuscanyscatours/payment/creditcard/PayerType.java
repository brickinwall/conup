
package com.tuscanyscatours.payment.creditcard;

public class PayerType {

    protected String name;
    protected AddressType address;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public AddressType getAddress() {
        return address;
    }

    public void setAddress(AddressType value) {
        this.address = value;
    }

}
