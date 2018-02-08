package ly.generalassemb.de.american.express.ingress.model.EPTRN;

import com.ancientprogramming.fixedformat4j.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ly.generalassemb.de.american.express.ingress.formatter.AmexSignedNumericFixedFormatter;
import ly.generalassemb.de.american.express.ingress.formatter.LocalDateFormatter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Record(length = 450)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "TLRR_AMEX_PAYEE_NUMBER",
        "TLRR_AMEX_SE_NUMBER",
        "TLRR_SE_UNIT_NUMBER",
        "TLRR_PAYMENT_YEAR",
        "TLRR_PAYMENT_NUMBER",
        "TLRR_RECORD_TYPE",
        "TLRR_DETAIL_RECORD_TYPE",
        "TLRR_SE_BUSINESS_DATE",
        "TLRR_AMEX_PROCESS_DATE",
        "TLRR_SOC_INVOICE_NUMBER",
        "TLRR_SOC_AMOUNT",
        "TLRR_ROC_AMOUNT",
        "TLRR_CM_NUMBER",
        "TLRR_CM_REF_NO",
        "TLRR_TRAN_DATE",
        "TLRR_SE_REF_POA",
        "NON-COMPLIANT_INDICATOR",
        "NON-COMPLIANT_ERROR_CODE_1",
        "NON-COMPLIANT_ERROR_CODE_2",
        "NON-COMPLIANT_ERROR_CODE_3",
        "NON-COMPLIANT_ERROR_CODE_4",
        "NON-SWIPED_INDICATOR",
        "TLRR_CM_NUMBER_EXD"

})
public class ROCRecord {

    @JsonProperty("TLRR_AMEX_PAYEE_NUMBER")
    @NotNull
    private BigDecimal payeeNumber;
    @JsonProperty("TLRR_AMEX_SE_NUMBER")
    @NotNull
    private BigDecimal serviceEstablishmentNumber;
    @JsonProperty("TLRR_SE_UNIT_NUMBER")
    private String serviceEstablishmentUnitNumber;
    @JsonProperty("TLRR_PAYMENT_YEAR")
    @NotNull
    private Integer paymentYear;
    @JsonProperty("TLRR_PAYMENT_NUMBER")
    @NotNull
    private String paymentNumber;
    @JsonProperty("TLRR_RECORD_TYPE")
    @NotNull
    private String recordType;
    @JsonProperty("TLRR_DETAIL_RECORD_TYPE")
    @NotNull
    private String recordSubType;
    @JsonProperty("TLRR_SE_BUSINESS_DATE")
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate serviceEstablishmentBusinessDate;
    @JsonProperty("TLRR_AMEX_PROCESS_DATE")
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate processingDate;
    @JsonProperty("TLRR_SOC_INVOICE_NUMBER")
    @NotNull
    private Integer socInvoiceNumber;
    @JsonProperty("TLRR_SOC_AMOUNT")
    @NotNull
    private BigDecimal socAmount;
    @JsonProperty("TLRR_ROC_AMOUNT")
    @NotNull
    private BigDecimal rocAmount;
    @JsonProperty("TLRR_CM_NUMBER")
    @NotNull
    private String cardmemberNumber;
    @JsonProperty("TLRR_CM_REF_NO")
    private String cardmemberReferenceNumber;
    @JsonProperty("TLRR_TRAN_DATE")
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;
    @JsonProperty("TLRR_SE_REF_POA")
    private String serviceEstablishmentReference;
    @JsonProperty("NON-COMPLIANT_INDICATOR")
    private String nonCompliantIndicator;
    @JsonProperty("NON-COMPLIANT_ERROR_CODE_1")
    private String nonCompliantErrorCode1;
    @JsonProperty("NON-COMPLIANT_ERROR_CODE_2")
    private String nonCompliantErrorCode2;
    @JsonProperty("NON-COMPLIANT_ERROR_CODE_3")
    private String nonCompliantErrorCode3;
    @JsonProperty("NON-COMPLIANT_ERROR_CODE_4")
    private String nonCompliantErrorCode4;
    @JsonProperty("NON-SWIPED_INDICATOR")
    private String nonSwipedIndicator;
    @JsonProperty("TLRR_CM_NUMBER_EXD")
    @NotNull
    private String cardmemberNumberExtended;

    @Field(offset = 1, length = 10, align = Align.RIGHT, paddingChar = '0')        //  getPayeeNumber()
    @FixedFormatDecimal(decimals = 0)
    @FixedFormatNumber(sign = Sign.NOSIGN)
    public BigDecimal getPayeeNumber() {
        return payeeNumber;
    }

    public void setPayeeNumber(BigDecimal payeeNumber) {
        this.payeeNumber = payeeNumber;
    }

    @Field(offset = 11, length = 10, align = Align.RIGHT, paddingChar = '0')        //  getServiceEstablishmentNumber()
    @FixedFormatDecimal(decimals = 0)
    @FixedFormatNumber(sign = Sign.NOSIGN)
    public BigDecimal getServiceEstablishmentNumber() {
        return serviceEstablishmentNumber;
    }

    public void setServiceEstablishmentNumber(BigDecimal serviceEstablishmentNumber) {
        this.serviceEstablishmentNumber = serviceEstablishmentNumber;
    }

    @Field(offset = 21, length = 10, align = Align.LEFT)
    //  getServiceEstablishmentUnitNumber()
    public String getServiceEstablishmentUnitNumber() {
        return serviceEstablishmentUnitNumber;
    }

    public void setServiceEstablishmentUnitNumber(String serviceEstablishmentUnitNumber) {
        this.serviceEstablishmentUnitNumber = serviceEstablishmentUnitNumber;
    }

    @Field(offset = 31, length = 4, align = Align.RIGHT, paddingChar = '0')
    @FixedFormatDecimal(decimals = 0)
    @FixedFormatNumber(sign = Sign.NOSIGN)
    public Integer getPaymentYear() {
        return paymentYear;
    }

    public void setPaymentYear(Integer paymentYear) {
        this.paymentYear = paymentYear;
    }

    @Field(offset = 35, length = 8, align = Align.LEFT)        //  getPaymentNumber()
    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    @Field(offset = 43, length = 1, align = Align.LEFT)
    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    @Field(offset = 44, length = 2, align = Align.LEFT)
    public String getRecordSubType() {
        return recordSubType;
    }

    public void setRecordSubType(String recordSubType) {
        this.recordSubType = recordSubType;
    }

    @Field(offset = 46, length = 7, align = Align.RIGHT, paddingChar = '0', formatter = LocalDateFormatter.class)
    @FixedFormatPattern("yyyyDDD")
    public LocalDate getServiceEstablishmentBusinessDate() {
        return serviceEstablishmentBusinessDate;
    }

    public void setServiceEstablishmentBusinessDate(LocalDate serviceEstablishmentBusinessDate) {
        this.serviceEstablishmentBusinessDate = serviceEstablishmentBusinessDate;
    }

    @Field(offset = 53, length = 7, align = Align.RIGHT, paddingChar = '0', formatter = LocalDateFormatter.class)
    @FixedFormatPattern("yyyyDDD")
    public LocalDate getProcessingDate() {
        return processingDate;
    }

    public void setProcessingDate(LocalDate processingDate) {
        this.processingDate = processingDate;
    }

    @Field(offset = 60, length = 6, align = Align.RIGHT, paddingChar = '0')        //  getSocInvoiceNumber()
    @FixedFormatDecimal(decimals = 0)
    @FixedFormatNumber(sign = Sign.NOSIGN)
    public Integer getSocInvoiceNumber() {
        return socInvoiceNumber;
    }

    public void setSocInvoiceNumber(Integer socInvoiceNumber) {
        this.socInvoiceNumber = socInvoiceNumber;
    }

    @Field(offset = 66, length = 13, align = Align.LEFT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
    //  getSocAmount()

    @FixedFormatNumber(sign = Sign.NOSIGN)
    public BigDecimal getSocAmount() {
        return socAmount;
    }

    public void setSocAmount(BigDecimal socAmount) {
        this.socAmount = socAmount;
    }

    @Field(offset = 79, length = 13, align = Align.LEFT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
    //  getRocAmount()

    @FixedFormatNumber(sign = Sign.NOSIGN)
    public BigDecimal getRocAmount() {
        return rocAmount;
    }

    public void setRocAmount(BigDecimal rocAmount) {
        this.rocAmount = rocAmount;
    }

    @Field(offset = 92, length = 15, align = Align.RIGHT)        //  getCardmemberReferenceNumber()
    public String getCardmemberNumber() {
        return cardmemberNumber;
    }

    public void setCardmemberNumber(String cardmemberNumber) {
        this.cardmemberNumber = cardmemberNumber;
    }

    @Field(offset = 107, length = 11, align = Align.LEFT)        //  getCardmemberReferenceNumber()
    public String getCardmemberReferenceNumber() {
        return cardmemberReferenceNumber;
    }

    public void setCardmemberReferenceNumber(String cardmemberReferenceNumber) {
        this.cardmemberReferenceNumber = cardmemberReferenceNumber;
    }

    @Field(offset = 147, length = 7, align = Align.RIGHT, paddingChar = '0', formatter = LocalDateFormatter.class)
    @FixedFormatPattern("yyyyDDD")
    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Field(offset = 154, length = 30, align = Align.LEFT)        //  getServiceEstablishmentReference()
    public String getServiceEstablishmentReference() {
        return serviceEstablishmentReference;
    }

    public void setServiceEstablishmentReference(String serviceEstablishmentReference) {
        this.serviceEstablishmentReference = serviceEstablishmentReference;
    }

    @Field(offset = 184, length = 1, align = Align.LEFT)        //  getNonCompliantIndicator()
    public String getNonCompliantIndicator() {
        return nonCompliantIndicator;
    }

    public void setNonCompliantIndicator(String nonCompliantIndicator) {
        this.nonCompliantIndicator = nonCompliantIndicator;
    }

    @Field(offset = 185, length = 4, align = Align.LEFT)        //  getNonCompliantErrorCode1()
    public String getNonCompliantErrorCode1() {
        return nonCompliantErrorCode1;
    }

    public void setNonCompliantErrorCode1(String nonCompliantErrorCode1) {
        this.nonCompliantErrorCode1 = nonCompliantErrorCode1;
    }

    @Field(offset = 189, length = 4, align = Align.LEFT)        //  getNonCompliantErrorCode2()
    public String getNonCompliantErrorCode2() {
        return nonCompliantErrorCode2;
    }

    public void setNonCompliantErrorCode2(String nonCompliantErrorCode2) {
        this.nonCompliantErrorCode2 = nonCompliantErrorCode2;
    }

    @Field(offset = 193, length = 4, align = Align.LEFT)        //  getNonCompliantErrorCode3()
    public String getNonCompliantErrorCode3() {
        return nonCompliantErrorCode3;
    }

    public void setNonCompliantErrorCode3(String nonCompliantErrorCode3) {
        this.nonCompliantErrorCode3 = nonCompliantErrorCode3;
    }

    @Field(offset = 197, length = 4, align = Align.LEFT)        //  getNonCompliantErrorCode4()
    public String getNonCompliantErrorCode4() {
        return nonCompliantErrorCode4;
    }

    public void setNonCompliantErrorCode4(String nonCompliantErrorCode4) {
        this.nonCompliantErrorCode4 = nonCompliantErrorCode4;
    }

    @Field(offset = 201, length = 1, align = Align.LEFT)        //  getNonSwipedIndicator()
    public String getNonSwipedIndicator() {
        return nonSwipedIndicator;
    }

    public void setNonSwipedIndicator(String nonSwipedIndicator) {
        this.nonSwipedIndicator = nonSwipedIndicator;
    }

    @Field(offset = 229, length = 19, align = Align.LEFT)        //  getCardmemberNumberExtended()
    public String getCardmemberNumberExtended() {
        return cardmemberNumberExtended;
    }

    public void setCardmemberNumberExtended(String cardmemberNumberExtended) {
        this.cardmemberNumberExtended = cardmemberNumberExtended;
    }
}
