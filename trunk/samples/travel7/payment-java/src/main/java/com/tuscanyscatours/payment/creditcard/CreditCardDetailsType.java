
package com.tuscanyscatours.payment.creditcard;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CreditCardDetailsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CreditCardDetailsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CreditCardType" type="{http://www.tuscanyscatours.com/CreditCardPayment/}CreditCardTypeType" minOccurs="0"/>
 *         &lt;element name="CreditCardNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ExpMonth" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ExpYear" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="CardOwner" type="{http://www.tuscanyscatours.com/CreditCardPayment/}PayerType" minOccurs="0"/>
 *         &lt;element name="CVV2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreditCardDetailsType", propOrder = {
    "creditCardType",
    "creditCardNumber",
    "expMonth",
    "expYear",
    "cardOwner",
    "cvv2"
})
public class CreditCardDetailsType {

    @XmlElement(name = "CreditCardType")
    protected CreditCardTypeType creditCardType;
    @XmlElement(name = "CreditCardNumber")
    protected String creditCardNumber;
    @XmlElement(name = "ExpMonth")
    protected Integer expMonth;
    @XmlElement(name = "ExpYear")
    protected Integer expYear;
    @XmlElement(name = "CardOwner")
    protected PayerType cardOwner;
    @XmlElement(name = "CVV2")
    protected String cvv2;

    /**
     * Gets the value of the creditCardType property.
     * 
     * @return
     *     possible object is
     *     {@link CreditCardTypeType }
     *     
     */
    public CreditCardTypeType getCreditCardType() {
        return creditCardType;
    }

    /**
     * Sets the value of the creditCardType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CreditCardTypeType }
     *     
     */
    public void setCreditCardType(CreditCardTypeType value) {
        this.creditCardType = value;
    }

    /**
     * Gets the value of the creditCardNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    /**
     * Sets the value of the creditCardNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreditCardNumber(String value) {
        this.creditCardNumber = value;
    }

    /**
     * Gets the value of the expMonth property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getExpMonth() {
        return expMonth;
    }

    /**
     * Sets the value of the expMonth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setExpMonth(Integer value) {
        this.expMonth = value;
    }

    /**
     * Gets the value of the expYear property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getExpYear() {
        return expYear;
    }

    /**
     * Sets the value of the expYear property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setExpYear(Integer value) {
        this.expYear = value;
    }

    /**
     * Gets the value of the cardOwner property.
     * 
     * @return
     *     possible object is
     *     {@link PayerType }
     *     
     */
    public PayerType getCardOwner() {
        return cardOwner;
    }

    /**
     * Sets the value of the cardOwner property.
     * 
     * @param value
     *     allowed object is
     *     {@link PayerType }
     *     
     */
    public void setCardOwner(PayerType value) {
        this.cardOwner = value;
    }

    /**
     * Gets the value of the cvv2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCVV2() {
        return cvv2;
    }

    /**
     * Sets the value of the cvv2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCVV2(String value) {
        this.cvv2 = value;
    }

}
