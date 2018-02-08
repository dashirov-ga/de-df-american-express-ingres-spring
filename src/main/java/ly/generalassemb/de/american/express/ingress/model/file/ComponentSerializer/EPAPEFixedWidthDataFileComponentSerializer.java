package ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import ly.generalassemb.de.american.express.ingress.model.EPAPE.*;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.file.EPAPEFixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.file.FixedWidthDataFileType;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EPAPEFixedWidthDataFileComponentSerializer implements FixedWidthDataFileComponentSerializer<EPAPEFixedWidthDataFile> {


    @Override
    public List<SerializedComponent<String>> getComponents(EPAPEFixedWidthDataFile pojo, CsvMapper csvMapper, ObjectMapper jsonMapper) throws Exception {
        List<SerializedComponent< String>> output = new ArrayList<>();

        SerializedComponent<String> component;

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPAPE);
        component.setType(FixedWidthDataFileComponent.EPAPE_JSON_OBJECT);
        component.setPayload(jsonMapper.writeValueAsString(pojo));
        output.add(component);

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPAPE);
        component.setType(FixedWidthDataFileComponent.EPAPE_CSV_HEADER_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(Header.class).withHeader()).writeValueAsString(pojo.getPaymentList().stream().map(ReconciledPayment::getHeader).collect(Collectors.toList())));
        output.add(component);

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPAPE);
        component.setType(FixedWidthDataFileComponent.EPAPE_CSV_PAYMENT_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(PaymentRecord.class).withHeader())
                .writeValueAsString(pojo.getPaymentList()
                        .stream()
                        .map(ReconciledPayment::getPaymentSummary)
                        .collect(Collectors.toList()))
        );
        output.add(component);

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPAPE);
        component.setType(FixedWidthDataFileComponent.EPAPE_CSV_ADJUSTMENT_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(AdjustmentRecord.class).withHeader())
                .writeValueAsString(
                        pojo.getPaymentList().stream()
                                .flatMap(reconciledPayment->reconciledPayment.getMerchantSubmissions().stream())
                                .flatMap(merchantSubmission->merchantSubmission.getAdjustments().stream())
                                .collect(Collectors.toList())
                )
        );

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPAPE);
        component.setType(FixedWidthDataFileComponent.EPAPE_CSV_SOC_COMPONENT);
        component.setPayload(
                csvMapper.writer(csvMapper.schemaFor(SOCRecord.class).withHeader())
                        .writeValueAsString(
                                pojo.getPaymentList()
                                        .stream()
                                        .flatMap(reconciledPayment->reconciledPayment.getMerchantSubmissions().stream())
                                        .map(MerchantSubmission::getSocRecord)
                                        .collect(Collectors.toList())
                )

        );

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPAPE);
        component.setType(FixedWidthDataFileComponent.EPAPE_CSV_ROC_COMPONENT);
        component.setPayload(
                csvMapper.writer(csvMapper.schemaFor(ROCRecord.class).withHeader())
                        .writeValueAsString(
                                pojo.getPaymentList().stream()
                                        .flatMap(reconciledPayment->reconciledPayment.getMerchantSubmissions().stream())
                                        .flatMap(merchantSubmission -> merchantSubmission.getRocRecords().stream())
                                        .collect(Collectors.toList())
                        )

        );

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPAPE);
        component.setType(FixedWidthDataFileComponent.EPAPE_CSV_TRAILER_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(Trailer.class).withHeader())
                .writeValueAsString(pojo.getPaymentList().stream().map(ReconciledPayment::getTrailer).collect(Collectors.toList())));
        output.add(component);


        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPAPE);
        component.setType(FixedWidthDataFileComponent.EPAPE_FIXED_WIDTH_OBJECT);
        component.setPayload(new String(Files.readAllBytes(pojo.getInputFile().toPath())));
        output.add(component);

        return output;    }
}
