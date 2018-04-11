package ly.generalassemb.de.american.express.ingress.model.file;

import ly.generalassemb.de.american.express.ingress.model.EPAPE.PaymentRecord;
import ly.generalassemb.de.american.express.ingress.model.EPAPE.ReconciledPayment;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EPAPEFixedWidthDataFileTest {
    @Test
    public void testDefaultConstructor(){
        EPAPEFixedWidthDataFile epape = new EPAPEFixedWidthDataFile();
        Assert.assertNotNull(epape);
    }
    @Test
    public void testFileConstructor() throws Exception {
        File testFile = new File("/Users/davidashirov/Source/GA/de-df-american-express-ingres-spring/src/test/resources/EPAPE/GENERALASSEMBLYA60229.EPAPE#E87ID6K41067AT");
        EPAPEFixedWidthDataFile epape;
        epape = new EPAPEFixedWidthDataFile(testFile);
        Assert.assertNotNull(epape);
        /**
         *  Sample being tested contains 2 payments
         */
        Assert.assertEquals(2, epape.getPaymentList().size());

        // Test Header
        ReconciledPayment firstPayment = epape.getPaymentList().get(0);
        Assert.assertNotNull(firstPayment.getHeader());
        Assert.assertEquals("DFHDR",firstPayment.getHeader().getRecordType());
        Assert.assertEquals(LocalDateTime.parse("201801180541",DateTimeFormatter.ofPattern("yyyyMMddHHmm")),firstPayment.getHeader().getDate());
        Assert.assertEquals("PANEUR",firstPayment.getHeader().getId());
        Assert.assertEquals("PAN-EUROPE EPA FILE",firstPayment.getHeader().getName());

        // Test Trailer
        Assert.assertNotNull(firstPayment.getHeader());
        Assert.assertEquals("DFTLR",firstPayment.getTrailer().getRecordType());
        Assert.assertEquals(LocalDateTime.parse("201801180541",DateTimeFormatter.ofPattern("yyyyMMddHHmm")),firstPayment.getTrailer().getDate());
        Assert.assertEquals("PANEUR",firstPayment.getTrailer().getId());
        Assert.assertEquals("PAN-EUROPE EPA FILE",firstPayment.getTrailer().getName());
        Assert.assertEquals("9420687915",firstPayment.getTrailer().getRecipientKey());
        Assert.assertEquals(10L, firstPayment.getTrailer().getRecordCount().longValue());  // File contains 10 records, not counting header and trailer

        // Test Payment Record
        PaymentRecord firstPaymentSummary = firstPayment.getPaymentSummary();
        Assert.assertNotNull(firstPaymentSummary);
        Assert.assertEquals("9420687915",firstPaymentSummary.getSettlementSeAccountNumber()); // Service Establishment Account Number
        Assert.assertEquals("002", firstPaymentSummary.getSettlementAccountNameCode());
        Assert.assertEquals(LocalDate.of(2018,1,22), firstPaymentSummary.getSettlementDate());
        Assert.assertEquals(1L, firstPaymentSummary.getRecordCode().longValue());
        Assert.assertEquals("00", firstPaymentSummary.getRecordSubCode());
        //     Continue: testing BigDecimal is configured correctly
        Assert.assertEquals(BigDecimal.valueOf( 120840L,2), firstPaymentSummary.getSettlementAmount());
        Assert.assertEquals(BigDecimal.valueOf(1208.40).setScale(2), firstPaymentSummary.getSettlementAmount());
        Assert.assertEquals(1208.40, firstPaymentSummary.getSettlementAmount().doubleValue());


        // Sample contains 3 pricing records
        Assert.assertEquals(3, firstPayment.getPricingRecords().size());



    }



}
