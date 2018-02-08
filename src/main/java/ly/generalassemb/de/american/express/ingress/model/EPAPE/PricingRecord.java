package ly.generalassemb.de.american.express.ingress.model.EPAPE;

import com.ancientprogramming.fixedformat4j.annotation.Align;
import com.ancientprogramming.fixedformat4j.annotation.Field;
import com.ancientprogramming.fixedformat4j.annotation.FixedFormatDecimal;
import com.ancientprogramming.fixedformat4j.annotation.Record;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ly.generalassemb.de.american.express.ingress.formatter.AmexSignedNumericFixedFormatter;

import java.math.BigDecimal;

/**
 * Created by davidashirov on 12/1/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "SETTLEMENT_SE_ACCOUNT_NUMBER",
        "SETTLEMENT_CURRENCY_CODE",
        "RECORD_CODE",
        "RECORD_SUB_CODE",
        "PRICING_DESCRIPTION",
        "DISCOUNT_RATE",
        "FEE_PER_CHARGE",
        "NUMBER_OF_CHARGES",
        "GROSS_AMOUNT",
        "GROSS_DEBIT_AMOUNT",
        "GROSS_CREDEIT_AMOUNT",
        "DISCOUNT_FEE",
        "SERVICE_FEE",
        "NET_AMOUNT"
})
@Record(length = 441)
public class PricingRecord {

    @JsonProperty("GENERATED_PAYMENT_NUMBER")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String paymentId;
    public String getPaymentId() {
        return paymentId;
    }
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    @JsonProperty("SETTLEMENT_SE_ACCOUNT_NUMBER")
    
    @javax.validation.constraints.NotNull
    private String settlementSeAccountNumber;

    @JsonProperty("SETTLEMENT_CURRENCY_CODE")
    
    @javax.validation.constraints.NotNull
    private String settlementCurrencyCode;

    @JsonProperty("RECORD_CODE")
    
    @javax.validation.constraints.NotNull
    private String recordCode;

    @JsonProperty("RECORD_SUB_CODE")
    
    @javax.validation.constraints.NotNull
    private String recordSubCode;

    @JsonProperty("PRICING_DESCRIPTION")
    
    @javax.validation.constraints.NotNull
    private String pricingDescription;

    @JsonProperty("DISCOUNT_RATE")
    
    @javax.validation.constraints.NotNull
    private BigDecimal discountRate;

    @JsonProperty("FEE_PER_CHARGE")
    
    @javax.validation.constraints.NotNull
    private BigDecimal feePerCharge;

    @JsonProperty("NUMBER_OF_CHARGES")
    
    @javax.validation.constraints.NotNull
    private BigDecimal numberOfCharges;

    @JsonProperty("GROSS_AMOUNT")
    
    @javax.validation.constraints.NotNull
    private BigDecimal grossAmount;

    @JsonProperty("GROSS_DEBIT_AMOUNT")
    
    @javax.validation.constraints.NotNull
    private BigDecimal grossDebitAmount;

    @JsonProperty("GROSS_CREDEIT_AMOUNT")
    
    @javax.validation.constraints.NotNull
    private BigDecimal grossCredeitAmount;

    @JsonProperty("DISCOUNT_FEE")
    
    @javax.validation.constraints.NotNull
    private BigDecimal discountFee;

    @JsonProperty("SERVICE_FEE")
    
    @javax.validation.constraints.NotNull
    private BigDecimal serviceFee;

    @JsonProperty("NET_AMOUNT")
    
    @javax.validation.constraints.NotNull
    private BigDecimal netAmount;

    @Field(offset = 1, length = 10, align = Align.RIGHT, paddingChar = '0')//  getSettlementSeAccountNumber
    public String getSettlementSeAccountNumber() {
        return settlementSeAccountNumber;
    }

    public void setSettlementSeAccountNumber(String settlementSeAccountNumber) {
        this.settlementSeAccountNumber = settlementSeAccountNumber;
    }

    @Field(offset = 11, length = 3, align = Align.RIGHT, paddingChar = '0')//  getSettlementCurrencyCode
    public String getSettlementCurrencyCode() {
        return settlementCurrencyCode;
    }

    public void setSettlementCurrencyCode(String settlementCurrencyCode) {
        this.settlementCurrencyCode = settlementCurrencyCode;
    }

    @Field(offset = 33, length = 1, align = Align.RIGHT, paddingChar = '0')//  getRecordCode
    public String getRecordCode() {
        return recordCode;
    }

    public void setRecordCode(String recordCode) {
        this.recordCode = recordCode;
    }

    @Field(offset = 34, length = 2, align = Align.RIGHT, paddingChar = '0')//  getRecordSubCode
    public String getRecordSubCode() {
        return recordSubCode;
    }

    public void setRecordSubCode(String recordSubCode) {
        this.recordSubCode = recordSubCode;
    }

    @Field(offset = 36, length = 64, align = Align.LEFT, paddingChar = ' ')//  getPricingDescription
    public String getPricingDescription() {
        return pricingDescription;
    }

    public void setPricingDescription(String pricingDescription) {
        this.pricingDescription = pricingDescription;
    }

    @Field(offset = 100, length = 7, align = Align.RIGHT, paddingChar = '0' )//  getDiscountRate
    @FixedFormatDecimal(decimals = 5)
    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    @Field(offset = 107, length = 15, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
//  getFeePerCharge
    public BigDecimal getFeePerCharge() {
        return feePerCharge;
    }

    public void setFeePerCharge(BigDecimal feePerCharge) {
        this.feePerCharge = feePerCharge;
    }

    @Field(offset = 122, length = 5, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
    @FixedFormatDecimal(decimals = 0)
    public BigDecimal getNumberOfCharges() {
        return numberOfCharges;
    }

    public void setNumberOfCharges(BigDecimal numberOfCharges) {
        this.numberOfCharges = numberOfCharges;
    }

    @Field(offset = 127, length = 15, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
//  getGrossAmount
    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    @Field(offset = 142, length = 15, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
//  getGrossDebitAmount
    public BigDecimal getGrossDebitAmount() {
        return grossDebitAmount;
    }

    public void setGrossDebitAmount(BigDecimal grossDebitAmount) {
        this.grossDebitAmount = grossDebitAmount;
    }

    @Field(offset = 157, length = 15, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
//  getGrossCredeitAmount
    public BigDecimal getGrossCredeitAmount() {
        return grossCredeitAmount;
    }

    public void setGrossCredeitAmount(BigDecimal grossCredeitAmount) {
        this.grossCredeitAmount = grossCredeitAmount;
    }

    @Field(offset = 172, length = 15, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
//  getDiscountFee
    public BigDecimal getDiscountFee() {
        return discountFee;
    }

    public void setDiscountFee(BigDecimal discountFee) {
        this.discountFee = discountFee;
    }


    @Field(offset = 187, length = 15, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
//  getServiceFee
    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }


    @Field(offset = 202, length = 15, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
//  getNetAmount
    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }


}
