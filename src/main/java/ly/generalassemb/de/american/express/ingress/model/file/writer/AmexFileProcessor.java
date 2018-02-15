package ly.generalassemb.de.american.express.ingress.model.file.writer;

import com.amazonaws.services.s3.AmazonS3URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.file.*;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Get a list of components, serialize a subset to S3, return a list of serialized compoents and corresponding locations on s3
 * The list is presumed to belong to a single file
 */
public class AmexFileProcessor extends SerializedComponentS3Writer implements ItemProcessor<FixedWidthDataFile,List<SerializedComponent<AmazonS3URI>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmexFileProcessor.class);

    @NotNull
    private Set<FixedWidthDataFileComponent> includeFilter;
    public Set<FixedWidthDataFileComponent> getIncludeFilter() {
        return includeFilter;
    }
    public void setIncludeFilter(Set<FixedWidthDataFileComponent> includeFilter) {
        this.includeFilter = includeFilter;
    }
    @NotNull
    private CsvMapper csvMapper;


    public CsvMapper getCsvMapper() {
        return csvMapper;
    }

    public void setCsvMapper(CsvMapper csvMapper) {
        this.csvMapper = csvMapper;
    }

    @NotNull
    private ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    /**
     * Process the provided item, returning a potentially modified or new item for continued
     * processing.  If the returned result is null, it is assumed that processing of the item
     * should not continue.
     *
     * @param items to be processed
     * @return potentially modified or new item for continued processing, null if processing of the
     * provided item should not continue.
     * @throws Exception
     */
    public List<SerializedComponent<AmazonS3URI>> process(List<SerializedComponent<String>> items) throws Exception {
        List<SerializedComponent<AmazonS3URI>> out = new ArrayList<>();
        for (SerializedComponent<String> item: items) {
            if( includeFilter.contains(item.getType()))
             out.add(serializeTos3(item));
        }
        return out;
    }

    public List<SerializedComponent<String>> getComponents(final FixedWidthDataFile somePojo) throws Exception {
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
    /**
     * Process the provided item, returning a potentially modified or new item for continued
     * processing.  If the returned result is null, it is assumed that processing of the item
     * should not continue.
     *
     * @param item to be processed
     * @return potentially modified or new item for continued processing, null if processing of the
     * provided item should not continue.
     * @throws Exception
     */
    @Override
    public List<SerializedComponent<AmazonS3URI>> process(FixedWidthDataFile item) throws Exception {
        return process(getComponents(item));
    }
}

