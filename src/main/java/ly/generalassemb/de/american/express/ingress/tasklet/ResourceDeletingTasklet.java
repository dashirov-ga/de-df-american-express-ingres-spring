package ly.generalassemb.de.american.express.ingress.tasklet;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.File;

public class ResourceDeletingTasklet implements Tasklet, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceDeletingTasklet.class);

    private Resource resource;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(resource, "resource must be set");
        LOGGER.info("resource " + resource.getFilename() + " is scheduled for removal");
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        File file = resource.getFile();
        Assert.state(file.isFile(),"Resource "+ file.getName() +"is not a file");
            boolean deleted = file.delete();
            if (!deleted) {
                throw new UnexpectedJobExecutionException(
                        "Could not delete file " + file.getPath());
            } else {
                LOGGER.info(file.getPath() + " is deleted!");
            }
        return RepeatStatus.FINISHED;
    }


}