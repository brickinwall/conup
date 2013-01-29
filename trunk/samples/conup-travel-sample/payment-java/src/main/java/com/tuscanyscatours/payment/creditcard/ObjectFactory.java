
package com.tuscanyscatours.payment.creditcard;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.tuscanyscatours.payment.creditcard package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.tuscanyscatours.payment.creditcard
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PayerType }
     * 
     */
    public PayerType createPayerType() {
        return new PayerType();
    }

    /**
     * Create an instance of {@link CreditCardDetailsType }
     * 
     */
    public CreditCardDetailsType createCreditCardDetailsType() {
        return new CreditCardDetailsType();
    }

    /**
     * Create an instance of {@link AddressType }
     * 
     */
    public AddressType createAddressType() {
        return new AddressType();
    }

    /**
     * Create an instance of {@link AuthorizeResponse }
     * 
     */
    public AuthorizeResponse createAuthorizeResponse() {
        return new AuthorizeResponse();
    }

    /**
     * Create an instance of {@link Authorize }
     * 
     */
    public Authorize createAuthorize() {
        return new Authorize();
    }

    /**
     * Create an instance of {@link AuthorizeFault }
     * 
     */
    public AuthorizeFault createAuthorizeFault() {
        return new AuthorizeFault();
    }

}
