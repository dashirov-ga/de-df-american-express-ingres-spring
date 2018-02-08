package ly.generalassemb.de.american.express.ingress.tasklet;


import com.amazonaws.services.s3.AmazonS3Client;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.File;

public class DataLakeUploadingTasklet implements Tasklet, InitializingBean {

    private Resource resource;
    private FixedWidthDataFile amexFileObject;
    private AmazonS3Client amazonS3Client;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(resource, "original file must be set");
        Assert.notNull(amexFileObject, "original file must be set");
        Assert.notNull(amazonS3Client,"Amazon s3 client required");
    }

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) throws Exception {

        File file = resource.getFile();
        Assert.state(file.isFile(),"Resource "+ file.getName() +"is not a file");

        return RepeatStatus.FINISHED;
    }


    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public FixedWidthDataFile getAmexFileObject() {
        return amexFileObject;
    }

    public void setAmexFileObject(FixedWidthDataFile amexFileObject) {
        this.amexFileObject = amexFileObject;
    }
}