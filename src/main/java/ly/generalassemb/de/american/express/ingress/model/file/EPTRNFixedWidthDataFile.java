package ly.generalassemb.de.american.express.ingress.model.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import ly.generalassemb.de.american.express.ingress.model.EPTRN.*;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EPTRNFixedWidthDataFile extends FixedWidthDataFileImpl {

    public EPTRNFixedWidthDataFile(File fileName) throws Exception{
        this();
        this.parse(fileName);
    }
    private Header header;
      List <PaymentIssued> payments;
    private Trailer trailer;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<PaymentIssued> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentIssued> payments) {
        this.payments = payments;
    }

    public Trailer getTrailer() {
        return trailer;
    }

    public void setTrailer(Trailer trailer) {
        this.trailer = trailer;
    }

    public EPTRNFixedWidthDataFile() {
        payments = new ArrayList<>();
    }

    @Override
    public FixedWidthDataFile parse(File fixedWidthDataFile) throws IOException, ParseException {


        BufferedReader reader = new BufferedReader(new FileReader(fixedWidthDataFile));
        String line;
        MerchantSubmission currentMerchantSubmission = null;
        PaymentIssued currentPaymentIssued = null;
        int lineNo = 0;
        while ((line = reader.readLine())!=null){
            ++lineNo;

            String htIndicator = line.substring(0,5); // first 5 bytes
            if ("DFHDR".equals(htIndicator)) {
                // when you see a header, it means you need to create a new reconciled payment object
                currentPaymentIssued = new PaymentIssued();
                this.header =manager.load(Header.class, line);
            } else if ("DFTRL".equals(htIndicator)){
                this.trailer =manager.load(Trailer.class,line);
                payments.add(currentPaymentIssued);
                currentPaymentIssued=null;
            } else {
                if (currentPaymentIssued == null){
                    throw new ParseException("Detail record not expected", lineNo);
                }
                String dtIndicator = line.substring(42,45);
                if ("100".equals(dtIndicator)) {
                    // payment
                    PaymentRecord paymentRecord = manager.load(PaymentRecord.class,line);
                    currentPaymentIssued.setPaymentRecord(paymentRecord);
                    this.payments.add(currentPaymentIssued);

                } else if ("210".equals(dtIndicator)) {
                    //SOC
                    SOCRecord socRecord =manager.load(SOCRecord.class,line);
                    currentMerchantSubmission = new MerchantSubmission();
                    currentMerchantSubmission.setSocRecord(socRecord);
                    currentPaymentIssued.getSubmissions().add(currentMerchantSubmission);
                }else if ("311".equals(dtIndicator)) {
                    if (currentMerchantSubmission==null){
                        throw new ParseException("ROC Record not expected", lineNo);
                    }
                    //ROC
                    ROCRecord roc = manager.load(ROCRecord.class,line);
                    currentMerchantSubmission.getRocRecords().add(roc);
                }else if ("220".equals(dtIndicator)) {
                    //Chargeback
                    ChargebackRecord chargebackRecord = manager.load(ChargebackRecord.class,line);
                    currentPaymentIssued.getChargebacks().add(chargebackRecord);
                }else if ("230".equals(dtIndicator)) {
                    //Adjustment
                    AdjustmentRecord adjustmentRecord =manager.load(AdjustmentRecord.class,line);
                    currentPaymentIssued.getAdjustments().add(adjustmentRecord);
                }else if ("240".equals(dtIndicator)) {
                    //Asset Billing
                    OtherRecord other = manager.load(OtherRecord.class,line);
                    currentPaymentIssued.getOther().add(other);
                }else if ("241".equals(dtIndicator)) {
                    //Take one commission
                    OtherRecord other = manager.load(OtherRecord.class,line);
                    currentPaymentIssued.getOther().add(other);
                }else if ("250".equals(dtIndicator)) {
                    //Other Fees
                    OtherRecord other = manager.load(OtherRecord.class,line);
                    currentPaymentIssued.getOther().add(other);
                } else {
                    throw new ParseException("Unexpected data record", lineNo);
                }

            }
        }
        reader.close();
        if (this.trailer == null){
            throw new ParseException("No trailer record after parsed file is completely consumed", lineNo);
        } else if (lineNo != this.trailer.getRecordCount()){
            throw new ParseException(String.format("Data file trailer indicates %d data records, but only %d were encountered",this.trailer.getRecordCount(),lineNo ),lineNo);
        }
        return this;
    }

    // given a directory name, dump
    // 1. Payment Records
    // 2. SOC Records
    // 3. ROC Records
    // 4. Adjustment Records
    // 5. Chargeback Records
    // 6. Other Records
    // 7. Header and Trailer Records
    // In CSV or TSV file format. Include column headers

    //

    public List<PaymentRecord> getAllPaymentRecords() {
        List<PaymentRecord> out = new ArrayList<>();
         this.payments.forEach(p->out.add(p.getPaymentRecord()));
         return out;
    }

    public List<ChargebackRecord> getAllChargebackRecords(){
        List<ChargebackRecord> out = new ArrayList<>();
        this.payments.forEach(p -> out.addAll(p.getChargebacks()));
        return out;
    }

    public List<AdjustmentRecord> getAllAdjustmentRecords(){
        List<AdjustmentRecord> out = new ArrayList<>();
        this.payments.forEach(p -> out.addAll(p.getAdjustments()) );
        return out;
    }
    public List<SOCRecord> getAllSOCRecords(){
        List<SOCRecord> out = new ArrayList<>();
        this.payments.forEach(p ->p.getSubmissions().forEach(s->out.add(s.getSocRecord())));
        return out;
    }
    public List<ROCRecord> getAllROCRecords(){
        List<ROCRecord> out = new ArrayList<>();
        this.payments.forEach(p ->p.getSubmissions().forEach(s->out.addAll(s.getRocRecords())));
        return out;
    }
    public List<OtherRecord> getAllOtherRecords(){
        List<OtherRecord> out = new ArrayList<>();
        this.payments.forEach(p->out.addAll(p.getOther()));
        return out;
    }
    @Override
    public Map<FixedWidthDataFileComponent, String> toLoadableComponents() throws Exception {
        Map<FixedWidthDataFileComponent,String> output = new HashMap<>();
        ObjectMapper jsonMapper = this.getJsonMapper();
        CsvMapper csvMapper= this.getCsvMapper();
        output.put(FixedWidthDataFileComponent.EPTRN_JSON_OBJECT,jsonMapper.writeValueAsString(this));
        output.put(FixedWidthDataFileComponent.EPTRN_CSV_HEADER_COMPONENT,csvMapper.writer(csvMapper.schemaFor(Header.class).withHeader()).writeValueAsString(this.getHeader()));
        output.put(FixedWidthDataFileComponent.EPTRN_CSV_PAYMENT_COMPONENT, csvMapper.writer(csvMapper.schemaFor(PaymentRecord.class).withHeader()).writeValueAsString(this.getAllPaymentRecords()));
        output.put(FixedWidthDataFileComponent.EPTRN_CSV_CHARGEBACK_COMPONENT, csvMapper.writer(csvMapper.schemaFor(ChargebackRecord.class).withHeader()).writeValueAsString(this.getAllChargebackRecords()));
        output.put(FixedWidthDataFileComponent.EPTRN_CSV_ADJUSTMENT_COMPONENT,csvMapper.writer(csvMapper.schemaFor(AdjustmentRecord.class).withHeader()).writeValueAsString(this.getAllAdjustmentRecords()));
        output.put(FixedWidthDataFileComponent.EPTRN_CSV_OTHER_COMPONENT,csvMapper.writer(csvMapper.schemaFor(OtherRecord.class).withHeader()).writeValueAsString(this.getAllOtherRecords()));
        output.put(FixedWidthDataFileComponent.EPTRN_CSV_SOC_COMPONENT,csvMapper.writer(csvMapper.schemaFor(SOCRecord.class).withHeader()).writeValueAsString(this.getAllSOCRecords()));
        output.put(FixedWidthDataFileComponent.EPTRN_CSV_ROC_COMPONENT,csvMapper.writer(csvMapper.schemaFor(ROCRecord.class).withHeader()).writeValueAsString(this.getAllROCRecords()));
        output.put(FixedWidthDataFileComponent.EPTRN_CSV_TRAILER_COMPONENT,csvMapper.writer(csvMapper.schemaFor(Trailer.class).withHeader()).writeValueAsString(this.getTrailer()));
        output.put(FixedWidthDataFileComponent.EPTRN_FIXED_WIDTH_OBJECT,new String(Files.readAllBytes(getInputFile().toPath())));
        return output;
    }

}
