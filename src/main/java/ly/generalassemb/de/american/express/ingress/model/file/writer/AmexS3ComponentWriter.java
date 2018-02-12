package ly.generalassemb.de.american.express.ingress.model.file.writer;

import com.amazonaws.services.s3.AmazonS3URI;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.SerializedComponent;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AmexS3ComponentWriter extends SerializedComponentS3Writer implements ItemWriter<List<SerializedComponent<String>>> {

    private StepExecution stepExecution;

    public StepExecution getStepExecution() {
        return stepExecution;
    }

    public void setStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    private Set<FixedWidthDataFileComponent> includeFilter;

    public Set<FixedWidthDataFileComponent> getIncludeFilter() {
        return includeFilter;
    }

    public void setIncludeFilter(Set<FixedWidthDataFileComponent> includeFilter) {
        this.includeFilter = includeFilter;
    }


    @BeforeStep
    public void saveStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
    @AfterStep
    public void afterStepExecution(StepExecution stepExecution) {
    }

    @Override
    public void write(List<? extends List<SerializedComponent<String>>> items) throws Exception {
        List<SerializedComponent<AmazonS3URI>> out = new ArrayList<>();
        // Apply filters
        List<SerializedComponent<String>> wantedComponents = items.stream().flatMap(Collection::stream).filter(item -> getIncludeFilter().contains(item.getType())).collect(Collectors.toList());
        // iterate through the list of desired items
        for (SerializedComponent<String> entry : wantedComponents) {
            SerializedComponent<AmazonS3URI> component = this.serializeTos3(entry);
            out.add(component);
        }
         ExecutionContext stepContext = this.stepExecution.getExecutionContext();
         stepContext.put("s3SerializedComponents", out);
    }

}

