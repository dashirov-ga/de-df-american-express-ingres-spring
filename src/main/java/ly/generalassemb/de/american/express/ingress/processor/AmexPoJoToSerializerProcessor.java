package ly.generalassemb.de.american.express.ingress.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.file.*;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.*;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

public class AmexPoJoToSerializerProcessor implements ItemProcessor<FixedWidthDataFile,List<SerializedComponent<String>> > {
    private CsvMapper csvMapper;
    private ObjectMapper objectMapper;

    public CsvMapper getCsvMapper() {
        return csvMapper;
    }

    public void setCsvMapper(CsvMapper csvMapper) {
        this.csvMapper = csvMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<SerializedComponent<String>> process(final FixedWidthDataFile somePojo) throws Exception {
        if (somePojo instanceof CBNOTFixedWidthDataFile)
            return new CBNOTFixedWidthDataFileComponentSerializer().getComponents((CBNOTFixedWidthDataFile)somePojo,csvMapper,objectMapper);
        if (somePojo instanceof EMCBKFixedWidthDataFile)
            return new EMCBKFixedWidthDataFileComponentSerializer().getComponents((EMCBKFixedWidthDataFile)somePojo,csvMapper,objectMapper);
        if (somePojo instanceof EMINQFixedWidthDataFile)
            return new EMINQFixedWidthDataFileComponentSerializer().getComponents((EMINQFixedWidthDataFile)somePojo,csvMapper,objectMapper);
        if (somePojo instanceof EPTRNFixedWidthDataFile)
            return new EPTRNFixedWidthDataFileComponentSerializer().getComponents((EPTRNFixedWidthDataFile)somePojo,csvMapper,objectMapper);
        if (somePojo instanceof EPAPEFixedWidthDataFile)
            return new EPAPEFixedWidthDataFileComponentSerializer().getComponents((EPAPEFixedWidthDataFile)somePojo,csvMapper,objectMapper);
        return null;
    }

}