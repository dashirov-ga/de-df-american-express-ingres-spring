package ly.generalassemb.de.american.express.ingress.tasklet;


import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.File;

public class FileDeletingTasklet implements Tasklet, InitializingBean {

    private Resource directory;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(directory, "directory must be set");
    }

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) throws Exception {

        File dir = directory.getFile();
        Assert.state(dir.isDirectory(),"Resource "+ dir.getName() +"is not a directory");

        File[] files = dir.listFiles();
        for (File file : files) {
            boolean deleted = file.delete();
            if (!deleted) {
                throw new UnexpectedJobExecutionException(
                        "Could not delete file " + file.getPath());
            } else {
                System.out.println(file.getPath() + " is deleted!");
            }
        }
        return RepeatStatus.FINISHED;
    }

    public Resource getDirectory() {
        return directory;
    }

    public void setDirectory(Resource directory) {
        this.directory = directory;
    }

}