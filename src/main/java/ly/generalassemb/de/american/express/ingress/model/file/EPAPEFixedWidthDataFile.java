package ly.generalassemb.de.american.express.ingress.model.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import ly.generalassemb.de.american.express.ingress.model.EPAPE.*;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileImpl;
import ly.generalassemb.de.american.express.ingress.model.NotRegisteredException;

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
import java.util.stream.Collectors;

public class EPAPEFixedWidthDataFile extends FixedWidthDataFileImpl {
    private List<ReconciledPayment> paymentList;
    public void put(ReconciledPayment reconciledPayment) {
        this.paymentList.add(reconciledPayment);
    }
    public EPAPEFixedWidthDataFile(File fileName) throws Exception {
        paymentList = new ArrayList<>();
        this.parse(fileName);
    }
    public EPAPEFixedWidthDataFile() {
        this.paymentList = new ArrayList<>();
    }
    public List<ReconciledPayment> getPaymentList() {
        return paymentList;
    }
    public void setPaymentList(List<ReconciledPayment> paymentList) {
        this.paymentList = paymentList;
    }


    public Object lineToRecord(String line){
        String htIndicator = line.substring(0, 5);
        if ("DFHDR".equals(htIndicator)) {
            return  manager.load(Header.class, line);
        } else if ("DFTLR".equals(htIndicator) ){
            return manager.load(Trailer.class, line);
        } else {
            String dtIndicator = line.substring(32, 35);
            if ("100".equals(dtIndicator)) {
                return manager.load(PaymentRecord.class, line);
            } else if ("110".equals(dtIndicator)) {
                return manager.load(PricingRecord.class,line);
            } else if ("210".equals(dtIndicator)) {
                return manager.load(SOCRecord.class, line);
            } else if ("260".equals(dtIndicator)) {
                return manager.load(ROCRecord.class, line);
            } else if ("230".equals(dtIndicator)) {
                return manager.load(AdjustmentRecord.class,line);
            }
        }
        return null ;
    }


    @Override
    public FixedWidthDataFile parse(File fixedWidthDataFile) throws IOException, ParseException, NotRegisteredException {
        BufferedReader reader = new BufferedReader(new FileReader(fixedWidthDataFile));
        String line;
        ReconciledPayment currentReconciledPayment = null;
        MerchantSubmission currentMerchantSubmission = null;
        String currentSubmissionSeBranchNumber = null;

        int lineNo = 0;
        while ((line = reader.readLine()) != null) {
            ++lineNo;
            String htIndicator = line.substring(0, 5);
            if ("DFHDR".equals(htIndicator)) {
                // when you see a header, it means you need to create a new reconciled payment object
                currentReconciledPayment = new ReconciledPayment();
                currentReconciledPayment.setHeader(manager.load(Header.class, line));

            } else {

                if (currentReconciledPayment == null)
                    throw new ParseException("Trailer record not expected", lineNo);


                if ("DFTLR".equals(htIndicator)) {
                    currentReconciledPayment.setTrailer(manager.load(Trailer.class, line));
                    this.put(currentReconciledPayment);
                    currentReconciledPayment = null; // TODO: test if breaks
                } else {
                    String dtIndicator = line.substring(32, 35);
                    if ("100".equals(dtIndicator)) {
                        // payment summary
                        currentReconciledPayment.put(manager.load(PaymentRecord.class, line));
                    } else if ("110".equals(dtIndicator)) {
                        // pricing summary
                        PricingRecord pricingRecord = manager.load(PricingRecord.class, line);
                        // TODO: Pricing record unit tests can't know ahead of time what payment record id they belong to
                        pricingRecord.setPaymentId(currentReconciledPayment.getPaymentSummary().getPaymentId());
                        currentReconciledPayment.put(pricingRecord);

                    } else {
                        if ("210".equals(dtIndicator)) {
                            // summary of charge
                            SOCRecord soc = manager.load(SOCRecord.class, line);
                            // TODO: SOC record unit tests can't know ahead of time what payment record id they belong to
                            soc.setPaymentId(currentReconciledPayment.getPaymentSummary().getPaymentId());
                            // SOC always opens a new submission!

                            // 1. If previous submission is open for data accumulation, close it and push to master
                            if (currentMerchantSubmission != null)
                                currentReconciledPayment.put(currentMerchantSubmission);
                            // 2. Open a new submission with a SOC record
                            currentMerchantSubmission = new MerchantSubmission();
                            currentMerchantSubmission.put(soc);
                            // 3. Remember Branch number to interrupt accumulation if SOC-less adjustments
                            // for another branch start pouring in
                            currentSubmissionSeBranchNumber = soc.getSubmissionSeBranchNumber();

                        } else if ("260".equals(dtIndicator)) {
                            // record of charge
                            if (currentMerchantSubmission == null)
                                throw new ParseException("ROC record not expected", lineNo);
                            ROCRecord roc = manager.load(ROCRecord.class,line);
                            roc.setPaymentId(currentReconciledPayment.getPaymentSummary().getPaymentId());
                            roc.setSocId(currentMerchantSubmission.getSocRecord().getSocId());
                            currentMerchantSubmission.put(roc);
                        } else if ("230".equals(dtIndicator)) {
                            // adjustment
                            // These are tricky: usually they follow SOC/ROCs, but sometimes they're just on their own
                            // E.g. When you haven't created any new charges or refunds (ROCs) but Chargebacks were there
                            // and Amex reached out to you to satisfy the adjustments.
                            AdjustmentRecord adjustmentRecord = manager.load(AdjustmentRecord.class, line);
                            adjustmentRecord.setPaymentId(currentReconciledPayment.getPaymentSummary().getPaymentId());
                            if (currentSubmissionSeBranchNumber == null) {
                                currentMerchantSubmission = new MerchantSubmission();
                                currentSubmissionSeBranchNumber = adjustmentRecord.getSubmissionSeBranchNumber();
                            } else if (!adjustmentRecord.getSubmissionSeBranchNumber().equals(currentSubmissionSeBranchNumber)) {
                                // If new branch is reporting, ship out previous submission and start a new one
                                currentReconciledPayment.put(currentMerchantSubmission);
                                currentMerchantSubmission = new MerchantSubmission();
                                currentMerchantSubmission.put(adjustmentRecord);
                            }
                            currentMerchantSubmission.put(adjustmentRecord);

                        } else {
                            throw new NotRegisteredException("Don't know how to parse this line.");
                        }
                    }
                }
            }
        }
        return this;
    }

    @Override
    public Map<FixedWidthDataFileComponent, String> toLoadableComponents() throws Exception {
        CsvMapper csvMapper = this.getCsvMapper();
        ObjectMapper jsonMapper = this.getJsonMapper();

        Map<FixedWidthDataFileComponent, String> output = new HashMap<>();
        // processed file is a json serialized "this"
        output.put(FixedWidthDataFileComponent.EPAPE_JSON_OBJECT, jsonMapper.writeValueAsString(this));

        output.put(FixedWidthDataFileComponent.EPAPE_CSV_HEADER_COMPONENT,
                csvMapper.writer(csvMapper.schemaFor(Header.class).withHeader())
                        .writeValueAsString(this.getPaymentList().stream().map(ReconciledPayment::getHeader).collect(Collectors.toList()))

        );

        output.put(FixedWidthDataFileComponent.EPAPE_CSV_PAYMENT_COMPONENT,

                csvMapper.writer(csvMapper.schemaFor(PaymentRecord.class).withHeader())
                        .writeValueAsString(this.getPaymentList()
                                .stream()
                                .map(ReconciledPayment::getPaymentSummary)
                                .collect(Collectors.toList()))

        );
        output.put(FixedWidthDataFileComponent.EPAPE_CSV_PRICING_COMPONENT,

                csvMapper.writer(csvMapper.schemaFor(PricingRecord.class).withHeader())
                        .writeValueAsString(this.getPaymentList()
                                .stream()
                                .map(ReconciledPayment::getPricingRecords)
                                .collect(Collectors.toList()))

        );

        output.put(FixedWidthDataFileComponent.EPAPE_CSV_ADJUSTMENT_COMPONENT,
                csvMapper.writer(csvMapper.schemaFor(AdjustmentRecord.class).withHeader())
                        .writeValueAsString(
                                this.getPaymentList().stream()
                                        .flatMap(reconciledPayment->reconciledPayment.getMerchantSubmissions().stream())
                                        .flatMap(merchantSubmission->merchantSubmission.getAdjustments().stream())
                                        .collect(Collectors.toList())
                        )

        );

        output.put(FixedWidthDataFileComponent.EPAPE_CSV_SOC_COMPONENT,

                csvMapper.writer(csvMapper.schemaFor(SOCRecord.class).withHeader()).
                        writeValueAsString(
                                this.getPaymentList().stream()
                                        .flatMap(reconciledPayment->reconciledPayment.getMerchantSubmissions().stream())
                                        .map(MerchantSubmission::getSocRecord)
                                        .collect(Collectors.toList())
                        )

        );

        output.put(FixedWidthDataFileComponent.EPAPE_CSV_ROC_COMPONENT,
                csvMapper.writer(csvMapper.schemaFor(ROCRecord.class).withHeader())
                        .writeValueAsString(
                                this.getPaymentList().stream()
                                        .flatMap(reconciledPayment->reconciledPayment.getMerchantSubmissions().stream())
                                        .flatMap(merchantSubmission -> merchantSubmission.getRocRecords().stream())
                                        .collect(Collectors.toList())
                        )

        );

        output.put(FixedWidthDataFileComponent.EPAPE_CSV_TRAILER_COMPONENT,
                csvMapper.writer(csvMapper.schemaFor(Trailer.class).withHeader())
                        .writeValueAsString(this.getPaymentList().stream().map(ReconciledPayment::getTrailer).collect(Collectors.toList()))

        );
        // input file is a collection of all file lines "as-is"
        output.put(FixedWidthDataFileComponent.EPAPE_FIXED_WIDTH_OBJECT, new String(Files.readAllBytes(getInputFile().toPath())));
        return output;
    }

}
