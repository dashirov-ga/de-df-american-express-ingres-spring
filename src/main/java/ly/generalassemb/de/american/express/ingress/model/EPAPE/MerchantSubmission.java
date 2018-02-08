package ly.generalassemb.de.american.express.ingress.model.EPAPE;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidashirov on 12/7/17.
 */
public class MerchantSubmission {
    private SOCRecord summaryOfCharges;
    private List<ROCRecord> recordsOfCharges;
    private List<AdjustmentRecord> adjustments;

    public MerchantSubmission() {
        recordsOfCharges=new ArrayList<>();
        adjustments = new ArrayList<>();

    }

    public SOCRecord getSummaryOfCharges() {
        return summaryOfCharges;
    }

    public List<ROCRecord> getRecordsOfCharges() {
        return recordsOfCharges;
    }

    public List<AdjustmentRecord> getAdjustments() {
        return adjustments;
    }

    public SOCRecord getSocRecord() {
        return summaryOfCharges;
    }

    public void setSocRecord(SOCRecord socRecord) {
        this.summaryOfCharges = socRecord;
    }

    public List<ROCRecord> getRocRecords() {
        return recordsOfCharges;
    }

    public void setRocRecords(List<ROCRecord> rocRecords) {
        this.recordsOfCharges = rocRecords;
    }

    public void put(SOCRecord summaryOfCharges){
        this.summaryOfCharges = summaryOfCharges;
    }
    public void put(ROCRecord recordOfCharges) {
        this.recordsOfCharges.add(recordOfCharges);
    }
    public void put(AdjustmentRecord adjustment){
        this.adjustments.add(adjustment);
    }
}
