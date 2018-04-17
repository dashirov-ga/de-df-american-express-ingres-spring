package ly.generalassemb.de.american.express.ingress.model.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ly.generalassemb.de.american.express.ingress.model.EPAPE.PaymentRecord;
import ly.generalassemb.de.american.express.ingress.model.EPAPE.ReconciledPayment;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileFactory;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.EPAPEFixedWidthDataFileComponentSerializer;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.EPTRNFixedWidthDataFileComponentSerializer;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.SerializedComponent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

public class EPAPEFixedWidthDataFileTest {


    private static PrintWriter epapePayWriter;
    private static PrintWriter epapeSocWriter;
    private static PrintWriter epapeRocWriter;
    private static PrintWriter epapeAdjWriter;

    private static PrintWriter eptrnPayWriter;
    private static PrintWriter eptrnSocWriter;
    private static PrintWriter eptrnRocWriter;
    private static PrintWriter eptrnAdjWriter;

    private  static ObjectMapper jsonMapper;
    private  static CsvMapper csvMapper;
    static {
        try {
            epapePayWriter = new PrintWriter("/tmp/epapePay.csv");
            epapeSocWriter = new PrintWriter("/tmp/epapeSoc.csv");
            epapeRocWriter = new PrintWriter("/tmp/epapeRoc.csv");
            epapeAdjWriter = new PrintWriter("/tmp/epapeAdj.csv");
            
            eptrnPayWriter = new PrintWriter("/tmp/eptrnPay.csv");
            eptrnSocWriter = new PrintWriter("/tmp/eptrnSoc.csv");
            eptrnRocWriter = new PrintWriter("/tmp/eptrnRoc.csv");
            eptrnAdjWriter = new PrintWriter("/tmp/eptrnAdj.csv");

            jsonMapper = new ObjectMapper();
            jsonMapper.registerModule(new JavaTimeModule());

            csvMapper = new CsvMapper();
            csvMapper.registerModule(new JavaTimeModule());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }




    @Before
    public void initialize() {
    }

    @After
    public void destroy() {
        epapePayWriter.close();
        epapeSocWriter.close();
        epapeRocWriter.close();
        epapeAdjWriter.close();
        eptrnPayWriter.close();
        eptrnSocWriter.close();
        eptrnRocWriter.close();
        eptrnAdjWriter.close();
    }

    @Test
    public void testDefaultConstructor() {
        EPAPEFixedWidthDataFile epape = new EPAPEFixedWidthDataFile();
        Assert.assertNotNull(epape);
    }

    @Test
    public void testFileConstructor() throws Exception {
        File testFile = new File("/Users/davidashirov/Source/GA/de-df-american-express-ingres-spring/src/test/resources/EPAPE/GENERALASSEMBLYA60229.EPAPE#E87ID6K41067AT");
        EPAPEFixedWidthDataFile epape;
        epape = new EPAPEFixedWidthDataFile(testFile);
        Assert.assertNotNull(epape);
        testSampleFileContents(epape);
    }


    public void testSampleFileContents(EPAPEFixedWidthDataFile epape) {
        /*
         *  Sample being tested contains 2 payments
         */
        Assert.assertEquals(2, epape.getPaymentList().size());

        // Test Header
        ReconciledPayment firstPayment = epape.getPaymentList().get(0);
        Assert.assertNotNull(firstPayment.getHeader());
        Assert.assertEquals("DFHDR", firstPayment.getHeader().getRecordType());
        Assert.assertEquals(LocalDateTime.parse("201801180541", DateTimeFormatter.ofPattern("yyyyMMddHHmm")), firstPayment.getHeader().getDate());
        Assert.assertEquals("PANEUR", firstPayment.getHeader().getId());
        Assert.assertEquals("PAN-EUROPE EPA FILE", firstPayment.getHeader().getName());

        // Test Trailer
        Assert.assertNotNull(firstPayment.getHeader());
        Assert.assertEquals("DFTLR", firstPayment.getTrailer().getRecordType());
        Assert.assertEquals(LocalDateTime.parse("201801180541", DateTimeFormatter.ofPattern("yyyyMMddHHmm")), firstPayment.getTrailer().getDate());
        Assert.assertEquals("PANEUR", firstPayment.getTrailer().getId());
        Assert.assertEquals("PAN-EUROPE EPA FILE", firstPayment.getTrailer().getName());
        Assert.assertEquals("9420687915", firstPayment.getTrailer().getRecipientKey());
        Assert.assertEquals(10L, firstPayment.getTrailer().getRecordCount().longValue());  // File contains 10 records, not counting header and trailer

        // Test Payment Record
        PaymentRecord firstPaymentSummary = firstPayment.getPaymentSummary();
        Assert.assertNotNull(firstPaymentSummary);
        Assert.assertEquals("9420687915", firstPaymentSummary.getSettlementSeAccountNumber()); // Service Establishment Account Number
        Assert.assertEquals("002", firstPaymentSummary.getSettlementAccountNameCode());
        Assert.assertEquals(LocalDate.of(2018, 1, 22), firstPaymentSummary.getSettlementDate());
        Assert.assertEquals(1L, firstPaymentSummary.getRecordCode().longValue());
        Assert.assertEquals("00", firstPaymentSummary.getRecordSubCode());
        //     Continue: testing BigDecimal is configured correctly
        Assert.assertEquals(BigDecimal.valueOf(120840L, 2), firstPaymentSummary.getSettlementAmount());
        Assert.assertEquals(BigDecimal.valueOf(1208.40).setScale(2, BigDecimal.ROUND_HALF_UP), firstPaymentSummary.getSettlementAmount());
        Assert.assertEquals(1208.40, firstPaymentSummary.getSettlementAmount().doubleValue(), 1);


        // Sample contains 3 pricing records
        Assert.assertEquals(3, firstPayment.getPricingRecords().size());
        // Sample containst 3 socs
        Assert.assertEquals(3, firstPayment.getMerchantSubmissions().size());

        // First submission (SOC) has only one ROC
        Assert.assertEquals(1, firstPayment.getMerchantSubmissions().get(0).getRocRecords().size());


    }

    // Manual processing of the locally cached data files
    @Test
    public void testDirectoryExtractEPAPE() {

        try (Stream<Path> paths = Files.walk(Paths.get("/tmp/sftp-inbound"))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(EPAPEFixedWidthDataFileTest::processExtractEPAPE);
        } catch (IOException e) {
             e.printStackTrace();
        }
        Assert.assertEquals(1,1);
    }


    private static void processExtractEPAPE(Path path) {
        FixedWidthDataFile file = null;

        try {
            file = FixedWidthDataFileFactory.parse(path);
            if (file.getClass().equals(EPAPEFixedWidthDataFile.class)) {
                EPAPEFixedWidthDataFileComponentSerializer serializer = new EPAPEFixedWidthDataFileComponentSerializer();
                List<SerializedComponent<String>> components =  serializer.getComponents((EPAPEFixedWidthDataFile) file, csvMapper, jsonMapper);
                components.forEach(c->{
                    switch (c.getType()){
                        case EPAPE_CSV_PAYMENT_COMPONENT:
                            epapePayWriter.append(c.getPayload());
                            epapePayWriter.flush();
                            break;
                        case EPAPE_CSV_SOC_COMPONENT:
                            epapeSocWriter.append(c.getPayload());
                            epapeSocWriter.flush();
                            break;
                        case EPAPE_CSV_ROC_COMPONENT:
                            epapeRocWriter.append(c.getPayload());
                            epapeRocWriter.flush();
                            break;
                        case EPAPE_CSV_ADJUSTMENT_COMPONENT:
                            epapeAdjWriter.append(c.getPayload());
                            epapeAdjWriter.flush();
                            break;
                        default:
                            break;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    // Manual processing of the locally cached data files
    @Test
    public void testDirectoryExtractEPTRN() {

        try (Stream<Path> paths = Files.walk(Paths.get("/tmp/sftp-inbound"))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(EPAPEFixedWidthDataFileTest::processExtractEPTRN);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(1,1);
    }


    private static void processExtractEPTRN(Path path) {
        FixedWidthDataFile file = null;

        try {
            file = FixedWidthDataFileFactory.parse(path);
            if (file.getClass().equals(EPTRNFixedWidthDataFile.class)) {
                EPTRNFixedWidthDataFileComponentSerializer serializer = new EPTRNFixedWidthDataFileComponentSerializer();
                List<SerializedComponent<String>> components =  serializer.getComponents((EPTRNFixedWidthDataFile) file, csvMapper, jsonMapper);
                components.forEach(c->{
                    switch (c.getType()){
                        case EPTRN_CSV_PAYMENT_COMPONENT:
                            eptrnPayWriter.append(c.getPayload());
                            eptrnPayWriter.flush();
                            break;
                        case EPTRN_CSV_SOC_COMPONENT:
                            eptrnSocWriter.append(c.getPayload());
                            eptrnSocWriter.flush();
                            break;
                        case EPTRN_CSV_ROC_COMPONENT:
                            eptrnRocWriter.append(c.getPayload());
                            eptrnRocWriter.flush();
                            break;
                        case EPTRN_CSV_ADJUSTMENT_COMPONENT:
                            eptrnAdjWriter.append(c.getPayload());
                            eptrnAdjWriter.flush();
                            break;
                        default:
                            break;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
