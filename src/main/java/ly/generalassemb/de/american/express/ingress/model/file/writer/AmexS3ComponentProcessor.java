package ly.generalassemb.de.american.express.ingress.model.file.writer;

import com.amazonaws.services.s3.AmazonS3URI;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.SerializedComponent;
import org.springframework.batch.item.ItemProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Get a list of components, serialize a subset to S3, return a list of serialized compoents and corresponding locations on s3
 * The list is presumed to belong to a single file
 */
public class AmexS3ComponentProcessor extends SerializedComponentS3Writer implements ItemProcessor<List<SerializedComponent<String>>,List<SerializedComponent<AmazonS3URI>>> {
    private Set<FixedWidthDataFileComponent> includeFilter;
    public Set<FixedWidthDataFileComponent> getIncludeFilter() {
        return includeFilter;
    }
    public void setIncludeFilter(Set<FixedWidthDataFileComponent> includeFilter) {
        this.includeFilter = includeFilter;
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
    @Override
    public List<SerializedComponent<AmazonS3URI>> process(List<SerializedComponent<String>> items) throws Exception {
        List<SerializedComponent<AmazonS3URI>> out = new ArrayList<>();
        for (SerializedComponent<String> item: items) {
            if( includeFilter.contains(item.getType()))
             out.add(serializeTos3(item));
        }
        return out;
    }
}

