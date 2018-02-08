package ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import ly.generalassemb.de.american.express.ingress.model.CBNOT.Detail;
import ly.generalassemb.de.american.express.ingress.model.CBNOT.Header;
import ly.generalassemb.de.american.express.ingress.model.CBNOT.Trailer;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.file.CBNOTFixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.file.FixedWidthDataFileType;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CBNOTFixedWidthDataFileComponentSerializer implements FixedWidthDataFileComponentSerializer<CBNOTFixedWidthDataFile> {
    @Override
    public List<SerializedComponent< String>> getComponents(CBNOTFixedWidthDataFile pojo, CsvMapper csvMapper, ObjectMapper jsonMapper) throws Exception {
        List<SerializedComponent< String>> output = new ArrayList<>();

        SerializedComponent<String> component;

        component=  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.CBNOT);
        component.setType(FixedWidthDataFileComponent.CBNOT_JSON_OBJECT);
        component.setPayload(jsonMapper.writeValueAsString(pojo));
        output.add(component);

        component =  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.CBNOT);
        component.setType(FixedWidthDataFileComponent.CBNOT_CSV_HEADER_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(Header.class).withHeader()).writeValueAsString(pojo.getHeader()));
        output.add(component);

        component =  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.CBNOT);
        component.setType(FixedWidthDataFileComponent.CBNOT_CSV_TRAILER_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(Trailer.class).withHeader()).writeValueAsString(pojo.getTrailer()));
        output.add(component);

        component =  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.CBNOT);
        component.setType(FixedWidthDataFileComponent.CBNOT_CSV_CHARGEBACK_NOTIFICATION_COMPONENT);
        component.setPayload(csvMapper.writer(csvMapper.schemaFor(Detail.class).withHeader()).writeValueAsString(pojo.getDetails()));
        output.add(component);

        component =  new SerializedComponent<>();
        component.setParentId(pojo.getInputFile().getName());
        component.setParentKind(FixedWidthDataFileType.CBNOT);
        component.setType(FixedWidthDataFileComponent.CBNOT_FIXED_WIDTH_OBJECT);
        component.setPayload(new String(Files.readAllBytes(pojo.getInputFile().toPath())));
        output.add(component);

        return output;
    }
}
