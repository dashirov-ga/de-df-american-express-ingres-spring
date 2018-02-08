package ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import ly.generalassemb.de.american.express.ingress.model.EPTRN.*;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.file.EPTRNFixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.file.FixedWidthDataFileType;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class EPTRNFixedWidthDataFileComponentSerializer implements FixedWidthDataFileComponentSerializer<EPTRNFixedWidthDataFile> {
    @Override
    public List<SerializedComponent<String>> getComponents(EPTRNFixedWidthDataFile pojo, CsvMapper csvMapper, ObjectMapper jsonMapper) throws Exception {
        List<SerializedComponent< String>> output = new ArrayList<>();

        SerializedComponent<String> component;

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPTRN);
        component.setType(FixedWidthDataFileComponent.EPTRN_JSON_OBJECT);
        component.setPayload(jsonMapper.writeValueAsString(pojo));
        output.add(component);


        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPTRN);
        component.setType(FixedWidthDataFileComponent.EPTRN_CSV_HEADER_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(Header.class).withHeader()).writeValueAsString(pojo.getHeader()));
        output.add(component);

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPTRN);
        component.setType(FixedWidthDataFileComponent.EPTRN_CSV_PAYMENT_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(PaymentRecord.class).withHeader()).writeValueAsString(pojo.getAllPaymentRecords()));
        output.add(component);

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPTRN);
        component.setType(FixedWidthDataFileComponent.EPTRN_CSV_CHARGEBACK_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(ChargebackRecord.class).withHeader()).writeValueAsString(pojo.getAllChargebackRecords()) );
        output.add(component);

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPTRN);
        component.setType(FixedWidthDataFileComponent.EPTRN_CSV_ADJUSTMENT_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(AdjustmentRecord.class).withHeader()).writeValueAsString(pojo.getAllAdjustmentRecords()) );
        output.add(component);

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPTRN);
        component.setType(FixedWidthDataFileComponent.EPTRN_CSV_OTHER_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(OtherRecord.class).withHeader()).writeValueAsString(pojo.getAllOtherRecords()) );
        output.add(component);

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPTRN);
        component.setType(FixedWidthDataFileComponent.EPTRN_CSV_SOC_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(SOCRecord.class).withHeader()).writeValueAsString(pojo.getAllSOCRecords()) );
        output.add(component);

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPTRN);
        component.setType(FixedWidthDataFileComponent.EPTRN_CSV_ROC_COMPONENT);
        component.setPayload( csvMapper.writer(csvMapper.schemaFor(ROCRecord.class).withHeader()).writeValueAsString(pojo.getAllROCRecords()));
        output.add(component);

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPTRN);
        component.setType(FixedWidthDataFileComponent.EPTRN_CSV_TRAILER_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(Trailer.class).withHeader()).writeValueAsString(pojo.getTrailer()));
        output.add(component);

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.EPTRN);
        component.setType(FixedWidthDataFileComponent.EPTRN_FIXED_WIDTH_OBJECT);
        component.setPayload(new String(Files.readAllBytes(pojo.getInputFile().toPath())) );
        output.add(component);


        return output;


    }
}
