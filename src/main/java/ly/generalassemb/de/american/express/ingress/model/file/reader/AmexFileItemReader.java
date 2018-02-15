package ly.generalassemb.de.american.express.ingress.model.file.reader;

import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class AmexFileItemReader
        extends AbstractItemCountingItemStreamItemReader<FixedWidthDataFile>
        implements ResourceAwareItemReaderItemStream<FixedWidthDataFile>, InitializingBean {
    private static final Log LOGGER = LogFactory.getLog(AmexFileItemReader.class);


    private final static FixedWidthDataFileFactory factory = new FixedWidthDataFileFactory();
    private boolean noInput = false;
    private boolean strict = true;
    private Resource resource;

    public Resource getResource() {
        return resource;
    }

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    protected FixedWidthDataFile doRead() throws Exception {
        if (noInput) {
            return null;
        }
        FixedWidthDataFile dataFile = FixedWidthDataFileFactory.parse(resource.getFile());
        noInput = true;
        return  dataFile;
    }

    public AmexFileItemReader() {
        setName(ClassUtils.getShortName(AmexFileItemReader.class));
    }

    @Override
    protected void doOpen() {
        Assert.notNull(resource, "Input resource must be set");
        noInput = true;
        if (!resource.exists()) {
            if (strict) {
                throw new IllegalStateException("Input resource must exist (reader is in 'strict' mode): " + resource);
            }
            LOGGER.warn("Input resource does not exist " + resource.getDescription());
            return;
        }
        LOGGER.info("Testing if resource is readable " + resource.getFilename());
        if (!resource.isReadable()) {
            if (strict) {
                throw new IllegalStateException("Input resource must be readable (reader is in 'strict' mode): "
                        + resource);
            }
            LOGGER.warn("Input resource is not readable " + resource.getDescription());
            return;
        }
        LOGGER.info("Resource is readable " + resource.getFilename());
        noInput=false;
    }

    @Override
    protected void doClose() {
        noInput=true;
    }

    @Override
    public void afterPropertiesSet() {

    }
}
