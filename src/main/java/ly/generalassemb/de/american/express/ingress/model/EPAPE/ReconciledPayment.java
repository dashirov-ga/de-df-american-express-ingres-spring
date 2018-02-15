package ly.generalassemb.de.american.express.ingress.model.EPAPE;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidashirov on 12/7/17.
 */
public class ReconciledPayment {
    private Header header;
    private PaymentRecord paymentSummary;
    private List<PricingRecord> pricingRecords;
    private List<MerchantSubmission> merchantSubmissions;
    private Trailer trailer;

    public PaymentRecord getPaymentSummary() {
        return paymentSummary;
    }

    public void setPaymentSummary(PaymentRecord paymentSummary) {
        this.paymentSummary = paymentSummary;
    }

    public List<PricingRecord> getPricingRecords() {
        return pricingRecords;
    }

    public void setPricingSummary(List<PricingRecord> pricingSummary) {
        this.pricingRecords = pricingSummary;
    }

    public List<MerchantSubmission> getMerchantSubmissions() {
        return merchantSubmissions;
    }

    public void setMerchantSubmissions(List<MerchantSubmission> merchantSubmissions) {
        this.merchantSubmissions = merchantSubmissions;
    }

    public ReconciledPayment() {
        merchantSubmissions=new ArrayList<>();
        pricingRecords=new ArrayList<>();

    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Trailer getTrailer() {
        return trailer;
    }

    public void setTrailer(Trailer trailer) {
        this.trailer = trailer;
    }

    public void put(MerchantSubmission submission){
        merchantSubmissions.add(submission);
    }

    public void put(PricingRecord pricingRecord){
        this.pricingRecords.add(pricingRecord);
    }

    public void put(PaymentRecord paymentSummary){
        this.paymentSummary = paymentSummary;
    }

}
