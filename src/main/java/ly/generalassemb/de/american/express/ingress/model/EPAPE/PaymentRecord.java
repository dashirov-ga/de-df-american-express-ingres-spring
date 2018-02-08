package ly.generalassemb.de.american.express.ingress.model.EPAPE;

import com.ancientprogramming.fixedformat4j.annotation.*;
import com.fasterxml.jackson.annotation.*;
import ly.generalassemb.de.american.express.ingress.formatter.AmexSignedNumericFixedFormatter;
import ly.generalassemb.de.american.express.ingress.formatter.LocalDateFormatter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static java.time.temporal.ChronoField.YEAR;

/**
 * Created by davidashirov on 12/1/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "SETTLEMENT_SE_ACCOUNT_NUMBER",
        "SETTLEMENT_ACCOUNT_NAME_CODE",
        "SETTLEMENT_DATE",


        "RECORD_CODE",
        "SOC_RECORD_CODE",

        "SETTLEMENT_AMOUNT",
        "SE_BANK_SORT_CODE",
        "SE_BANK_ACCOUNT_NUMBER",
        "SETTLEMENT_GROSS_AMOUNT",
        "TAX_AMOUNT",
        "TAX_RATE",
        "SERVICE_FEE_AMOUNT",

        "SERVICE_FEE_RATE",

        "SETTLEMENT_ADJUSTMENT_AMOUNT",
        "PAY_PLAN_SHORT_NAME",
        "PAYEE_NAME",
        "SETTLEMENT_ACCOUNT_NAME_CODE_EXTENDED",
        "SETTLEMENT_CURRENCY_CODE",
        "PREVIOUS_DEBIT_BALANCE",
        "IBAN",
        "BIC"
})
@Record(length = 440)
public class PaymentRecord {

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("GENERATED_PAYMENT_NUMBER")
    public String getPaymentId() {
        return uniqueCode().toString();
    }

    @JsonProperty("SETTLEMENT_SE_ACCOUNT_NUMBER")
    
    @javax.validation.constraints.NotNull
    private String settlementSeAccountNumber;

    @JsonProperty("SETTLEMENT_ACCOUNT_NAME_CODE")
    
    @javax.validation.constraints.NotNull
    private String settlementAccountNameCode;

    // The date when the funds for this payment appear in your bank account - CCYYMMDD
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("SETTLEMENT_DATE")
    
    @javax.validation.constraints.NotNull
    private LocalDate settlementDate;


    @JsonProperty("RECORD_CODE")
    
    @javax.validation.constraints.NotNull

    private Integer recordCode;
    @JsonProperty("SOC_RECORD_CODE")
    
    @javax.validation.constraints.NotNull
    private String recordSubCode;

    @JsonProperty("SETTLEMENT_AMOUNT")
    
    @javax.validation.constraints.NotNull
    private BigDecimal settlementAmount;

    @JsonProperty("SE_BANK_SORT_CODE")
    
    @javax.validation.constraints.NotNull
    private String seBankSortCode;

    @JsonProperty("SE_BANK_ACCOUNT_NUMBER")
    
    @javax.validation.constraints.NotNull
    private String seBankAccountNumber;
    @JsonProperty("SETTLEMENT_GROSS_AMOUNT")
    
    @javax.validation.constraints.NotNull
    private BigDecimal settlementGrossAmount;
    @JsonProperty("TAX_AMOUNT")
    
    @javax.validation.constraints.NotNull
    private BigDecimal taxAmount;
    @JsonProperty("TAX_RATE")
    
    @javax.validation.constraints.NotNull
    private BigDecimal taxRate;
    @JsonProperty("SERVICE_FEE_AMOUNT")
    
    @javax.validation.constraints.NotNull
    private BigDecimal serviceFeeAmount;

    @JsonProperty("SERVICE_FEE_RATE")
    
    @javax.validation.constraints.NotNull
    private BigDecimal serviceFeeRate;

    @JsonProperty("SETTLEMENT_ADJUSTMENT_AMOUNT")
    
    @javax.validation.constraints.NotNull
    private BigDecimal settlementAdjustmentAmount;
    @JsonProperty("PAY_PLAN_SHORT_NAME")
    
    @javax.validation.constraints.NotNull
    private String payPlanShortName;
    @JsonProperty("PAYEE_NAME")
    
    @javax.validation.constraints.NotNull
    private String payeeName;
    @JsonProperty("SETTLEMENT_ACCOUNT_NAME_CODE_EXTENDED")
    
    @javax.validation.constraints.NotNull
    private String settlementAccountNameCodeExtended;
    @JsonProperty("SETTLEMENT_CURRENCY_CODE")
    
    @javax.validation.constraints.NotNull
    private String settlementCurrencyCode;
    @JsonProperty("PREVIOUS_DEBIT_BALANCE")
    
    @javax.validation.constraints.NotNull
    private BigDecimal previousDebitBalance;
    @JsonProperty("IBAN")
    
    @javax.validation.constraints.NotNull
    private String iban;
    @JsonProperty("BIC")
    
    @javax.validation.constraints.NotNull
    private String bic;


    @Field(offset = 1, length = 10, align = Align.LEFT, paddingChar = ' ')
    public String getSettlementSeAccountNumber() {
        return settlementSeAccountNumber;
    }

    @Field(offset = 11, length = 3, align = Align.LEFT, paddingChar = ' ')
    public String getSettlementAccountNameCode() {
        return settlementAccountNameCode;
    }

    @Field(offset = 14, length = 8, align = Align.RIGHT, paddingChar = '0',formatter = LocalDateFormatter.class)
    @FixedFormatPattern("yyyyMMdd")
    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    @Field(offset = 33, length = 1, align = Align.RIGHT, paddingChar = '0')
    public Integer getRecordCode() {
        return recordCode;
    }

    @Field(offset = 34, length = 2, align = Align.LEFT, paddingChar = ' ')
    public String getRecordSubCode() {
        return recordSubCode;
    }

    @Field(offset = 41, length = 15, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
    @FixedFormatDecimal(decimals = 2)
    public BigDecimal getSettlementAmount() {
        return settlementAmount;
    }

    @Field(offset = 56, length = 15, align = Align.LEFT, paddingChar = ' ')
    public String getSeBankSortCode() {
        return seBankSortCode;
    }

    @Field(offset = 71, length = 20, align = Align.LEFT, paddingChar = ' ')
    public String getSeBankAccountNumber() {
        return seBankAccountNumber;
    }

    @Field(offset = 91, length = 15, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
    public BigDecimal getSettlementGrossAmount() {
        return settlementGrossAmount;
    }

    @Field(offset = 106, length = 15, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    @Field(offset = 121, length = 7, align = Align.RIGHT, paddingChar = '0')
    @FixedFormatDecimal(decimals = 5)
    public BigDecimal getTaxRate() {
        return taxRate;
    }

    @Field(offset = 128, length = 15, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
    public BigDecimal getServiceFeeAmount() {
        return serviceFeeAmount;
    }

    @Field(offset = 158, length = 7, align = Align.RIGHT, paddingChar = '0')
    @FixedFormatDecimal(decimals = 5)
    public BigDecimal getServiceFeeRate() {
        return serviceFeeRate;
    }

    @Field(offset = 220, length = 15, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
    public BigDecimal getSettlementAdjustmentAmount() {
        return settlementAdjustmentAmount;
    }

    @Field(offset = 235, length = 30, align = Align.LEFT, paddingChar = ' ')
    public String getPayPlanShortName() {
        return payPlanShortName;
    }

    @Field(offset = 265, length = 38, align = Align.LEFT, paddingChar = ' ')
    public String getPayeeName() {
        return payeeName;
    }

    @Field(offset = 303, length = 20, align = Align.LEFT, paddingChar = ' ')
    public String getSettlementAccountNameCodeExtended() {
        return settlementAccountNameCodeExtended;
    }

    @Field(offset = 323, length = 3, align = Align.LEFT, paddingChar = ' ')
    public String getSettlementCurrencyCode() {
        return settlementCurrencyCode;
    }

    @Field(offset = 326, length = 15, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
    public BigDecimal getPreviousDebitBalance() {
        return previousDebitBalance;
    }

    @Field(offset = 341, length = 34, align = Align.LEFT, paddingChar = ' ')
    public String getIban() {
        return iban;
    }

    @Field(offset = 375, length = 12, align = Align.LEFT, paddingChar = ' ')
    public String getBic() {
        return bic;
    }

    public void setSettlementSeAccountNumber(String settlementSeAccountNumber) {
        this.settlementSeAccountNumber = settlementSeAccountNumber;
    }

    public void setSettlementAccountNameCode(String settlementAccountNameCode) {
        this.settlementAccountNameCode = settlementAccountNameCode;
    }

    public void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }

    public void setRecordCode(Integer recordCode) {
        this.recordCode = recordCode;
    }

    public void setRecordSubCode(String recordSubCode) {
        this.recordSubCode = recordSubCode;
    }

    public void setSettlementAmount(BigDecimal settlementAmount) {
        this.settlementAmount = settlementAmount;
    }

    public void setSeBankSortCode(String seBankSortCode) {
        this.seBankSortCode = seBankSortCode;
    }

    public void setSeBankAccountNumber(String seBankAccountNumber) {
        this.seBankAccountNumber = seBankAccountNumber;
    }

    public void setSettlementGrossAmount(BigDecimal settlementGrossAmount) {
        this.settlementGrossAmount = settlementGrossAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public void setServiceFeeAmount(BigDecimal serviceFeeAmount) {
        this.serviceFeeAmount = serviceFeeAmount;
    }

    public void setServiceFeeRate(BigDecimal serviceFeeRate) {
        this.serviceFeeRate = serviceFeeRate;
    }

    public void setSettlementAdjustmentAmount(BigDecimal settlementAdjustmentAmount) {
        this.settlementAdjustmentAmount = settlementAdjustmentAmount;
    }

    public void setPayPlanShortName(String payPlanShortName) {
        this.payPlanShortName = payPlanShortName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public void setSettlementAccountNameCodeExtended(String settlementAccountNameCodeExtended) {
        this.settlementAccountNameCodeExtended = settlementAccountNameCodeExtended;
    }

    public void setSettlementCurrencyCode(String settlementCurrencyCode) {
        this.settlementCurrencyCode = settlementCurrencyCode;
    }

    public void setPreviousDebitBalance(BigDecimal previousDebitBalance) {
        this.previousDebitBalance = previousDebitBalance;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    @JsonIgnore
    public Integer getPaymentYear(){
        return getSettlementDate().get(YEAR);
    }

    @JsonIgnore
    public LocalDate getPaymentDate(){
        return this.getSettlementDate();
    }



    public UUID uniqueCode() {
        return UUID.nameUUIDFromBytes(
                (settlementSeAccountNumber +
                        settlementAccountNameCode +
                        settlementDate +
                        recordCode +
                        recordSubCode +
                        settlementAmount +
                        seBankSortCode +
                        seBankAccountNumber +
                        settlementGrossAmount +
                        taxAmount +
                        taxRate +
                        serviceFeeAmount +
                        serviceFeeRate +
                        settlementAdjustmentAmount +
                        payPlanShortName +
                        payeeName +
                        settlementAccountNameCodeExtended +
                        settlementCurrencyCode +
                        previousDebitBalance +
                        iban +
                        bic).getBytes()
        );
    }
}