package ly.generalassemb.de.american.express.ingress.model.EPAPE;

import com.ancientprogramming.fixedformat4j.annotation.Align;
import com.ancientprogramming.fixedformat4j.annotation.Field;
import com.ancientprogramming.fixedformat4j.annotation.FixedFormatPattern;
import com.ancientprogramming.fixedformat4j.annotation.Record;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ly.generalassemb.de.american.express.ingress.formatter.AmexSignedNumericFixedFormatter;
import ly.generalassemb.de.american.express.ingress.formatter.LocalDateFormatter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by davidashirov on 12/2/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "SETTLEMENT_SE_ACCOUNT_NUMBER",
        "SETTLEMENT_ACCOUNT_NAME_CODE",

        "SUBMISSION_SE_ACCOUNT_NUMBER",
        "RECORD_CODE",
        "RECORD_SUB_CODE",

        "CHARGE_AMOUNT",
        "CHARGE_DATE",
        "ROC_REFERENCE_NUMBER",
        "ROC_REFERENCE_NUMBER_CPC_ONLY",
        "THREE_DIGIT_CHARGE_AUTHORIZATION_CODE",
        "CARD_MEMBER_ACCOUNT_NUMBER",
        "AIRLINE_TICKET_NUMBER",
        "SIX_DIGIT_CHARGE_AUTHORIZATION_CODE"

})
@Record(length = 441)
public class ROCRecord {


    private String paymentId;
    @JsonProperty("GENERATED_PAYMENT_NUMBER")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public String getPaymentId() {
        return paymentId;
    }
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    private String socId;
    @JsonProperty("GENERATED_SOC_NUMBER")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public String getSocId() {
        return socId;
    }

    public void setSocId(String socId) {
        this.socId = socId;
    }

    @JsonProperty("SETTLEMENT_SE_ACCOUNT_NUMBER")
    @NotNull
    private String settlementSeAccountNumber;

    @JsonProperty("SETTLEMENT_ACCOUNT_NAME_CODE")
    @NotNull
    private String settlementAccountNameCode;


    @JsonProperty("SUBMISSION_SE_ACCOUNT_NUMBER")
    @NotNull
    private String submissionSeAccountNumber;

    @JsonProperty("RECORD_CODE")
    @NotNull
    private Integer recordCode;

    @JsonProperty("RECORD_SUB_CODE")
    @NotNull
    private String recordSubCode;

    @JsonProperty("CHARGE_AMOUNT")
    @NotNull
    private BigDecimal chargeAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("CHARGE_DATE")
    @NotNull
    private LocalDate chargeDate;

    @JsonProperty("ROC_REFERENCE_NUMBER")
    @NotNull
    private String rocReferenceNumber;

    @JsonProperty("ROC_REFERENCE_NUMBER_CPC_ONLY")
    @NotNull
    private String rocReferenceNumberCpcOnly;

    @JsonProperty("THREE_DIGIT_CHARGE_AUTHORIZATION_CODE")
    @NotNull
    private String threeDigitChargeAuthorizationCode;

    @JsonProperty("CARD_MEMBER_ACCOUNT_NUMBER")
    @NotNull
    private String cardMemberAccountNumber;

    @JsonProperty("AIRLINE_TICKET_NUMBER")
    @Size(max = 14)
    @NotNull
    private String airlineTicketNumber;

    @JsonProperty("SIX_DIGIT_CHARGE_AUTHORIZATION_CODE")
    @NotNull
    private String sixDigitChargeAuthorizationCode;

    @Field(offset = 1, length = 10, align = Align.LEFT)        //  getSettlementSeAccountNumber

    public String getSettlementSeAccountNumber() {
        return settlementSeAccountNumber;
    }

    public void setSettlementSeAccountNumber(String settlementSeAccountNumber) {
        this.settlementSeAccountNumber = settlementSeAccountNumber;
    }

    @Field(offset = 11, length = 3, align = Align.LEFT)        //  getSettlementAccountNameCode

    public String getSettlementAccountNameCode() {
        return settlementAccountNameCode;
    }

    public void setSettlementAccountNameCode(String settlementAccountNameCode) {
        this.settlementAccountNameCode = settlementAccountNameCode;
    }

    @Field(offset = 23, length = 10, align = Align.LEFT)        //  getSubmissionSeAccountNumber

    public String getSubmissionSeAccountNumber() {
        return submissionSeAccountNumber;
    }

    public void setSubmissionSeAccountNumber(String submissionSeAccountNumber) {
        this.submissionSeAccountNumber = submissionSeAccountNumber;
    }

    @Field(offset = 33, length = 1, align = Align.RIGHT, paddingChar = '0')        //  getRecordCode
    public Integer getRecordCode() {
        return recordCode;
    }


    public void setRecordCode(Integer recordCode) {
        this.recordCode = recordCode;
    }

    @Field(offset = 34, length = 2, align = Align.LEFT)        //  getRecordSubCode


    public String getRecordSubCode() {
        return recordSubCode;
    }

    public void setRecordSubCode(String recordSubCode) {
        this.recordSubCode = recordSubCode;
    }

    @Field(offset = 41, length = 11, align = Align.RIGHT, paddingChar = '0', formatter = AmexSignedNumericFixedFormatter.class)
    //  getChargeAmount
    public BigDecimal getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(BigDecimal chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    @FixedFormatPattern("yyyyMMdd")
    @Field(offset = 52, length = 8, align = Align.RIGHT, paddingChar = '0', formatter = LocalDateFormatter.class)        //  getChargeDate
    public LocalDate getChargeDate() {
        return chargeDate;
    }

    public void setChargeDate(LocalDate chargeDate) {
        this.chargeDate = chargeDate;
    }

    @Field(offset = 60, length = 12, align = Align.LEFT)        //  getRocReferenceNumber

    public String getRocReferenceNumber() {
        return rocReferenceNumber;
    }

    public void setRocReferenceNumber(String rocReferenceNumber) {
        this.rocReferenceNumber = rocReferenceNumber;
    }

    @Field(offset = 72, length = 15, align = Align.LEFT)        //  getRocReferenceNumberCpcOnly
    public String getRocReferenceNumberCpcOnly() {
        return rocReferenceNumberCpcOnly;
    }

    public void setRocReferenceNumberCpcOnly(String rocReferenceNumberCpcOnly) {
        this.rocReferenceNumberCpcOnly = rocReferenceNumberCpcOnly;
    }

    @Field(offset = 87, length = 3, align = Align.LEFT)
    //  getThreeDigitChargeAuthorizationCode
    public String getThreeDigitChargeAuthorizationCode() {
        return threeDigitChargeAuthorizationCode;
    }

    public void setThreeDigitChargeAuthorizationCode(String threeDigitChargeAuthorizationCode) {
        this.threeDigitChargeAuthorizationCode = threeDigitChargeAuthorizationCode;
    }

    @Field(offset = 90, length = 15, align = Align.LEFT)        //  getCardMemberAccountNumber
    public String getCardMemberAccountNumber() {
        return cardMemberAccountNumber;
    }

    public void setCardMemberAccountNumber(String cardMemberAccountNumber) {
        this.cardMemberAccountNumber = cardMemberAccountNumber;
    }

    @Field(offset = 105, length = 14, align = Align.LEFT)        //  getAirlineTicketNumber
    public String getAirlineTicketNumber() {
        return airlineTicketNumber;
    }

    public void setAirlineTicketNumber(String airlineTicketNumber) {
        this.airlineTicketNumber = airlineTicketNumber;
    }

    @Field(offset = 119, length = 6, align = Align.LEFT)
    //  getSixDigitChargeAuthorizationCode
    public String getSixDigitChargeAuthorizationCode() {
        return sixDigitChargeAuthorizationCode;
    }

    public void setSixDigitChargeAuthorizationCode(String sixDigitChargeAuthorizationCode) {
        this.sixDigitChargeAuthorizationCode = sixDigitChargeAuthorizationCode;
    }

}
