package ly.generalassemb.de.american.express.ingress.tasklet;

import com.amazonaws.services.s3.AmazonS3URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileFactory;
import ly.generalassemb.de.american.express.ingress.model.NotRegisteredException;
import ly.generalassemb.de.american.express.ingress.model.file.*;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.*;
import ly.generalassemb.de.american.express.ingress.model.file.writer.SerializedComponentRedshiftWriter;
import ly.generalassemb.de.american.express.ingress.model.file.writer.SerializedComponentS3Writer;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ResourceAware;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
     Given a file, find a way to parse it,
     Then hand off to s3 serializer
     Then hand off to redshift serializer
     then report successful completion of the step
 */
public class AmexFileRedshiftLoadingTasklet  implements Tasklet, InitializingBean, ResourceAware {
    private Resource resource;
    public Resource getResource() {
        return resource;
    }
    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    private SerializedComponentS3Writer s3Writer;
    private SerializedComponentRedshiftWriter redshiftWriter;

    public SerializedComponentS3Writer getS3Writer() {
        return s3Writer;
    }

    public void setS3Writer(SerializedComponentS3Writer s3Writer) {
        this.s3Writer = s3Writer;
    }

    public SerializedComponentRedshiftWriter getRedshiftWriter() {
        return redshiftWriter;
    }

    public void setRedshiftWriter(SerializedComponentRedshiftWriter redshiftWriter) {
        this.redshiftWriter = redshiftWriter;
    }



    @NotNull
    private Set<FixedWidthDataFileComponent> dataLakeIncludeFilter;
    public Set<FixedWidthDataFileComponent> getDataLakeIncludeFilter() {
        return dataLakeIncludeFilter;
    }
    public void setDataLakeIncludeFilter(Set<FixedWidthDataFileComponent> dataLakeIncludeFilter) {
        this.dataLakeIncludeFilter = dataLakeIncludeFilter;
    }


    @NotNull
    private Set<FixedWidthDataFileComponent> dataWarehouseIncludeFilter;

    public Set<FixedWidthDataFileComponent> getDataWarehouseIncludeFilter() {
        return dataWarehouseIncludeFilter;
    }

    public void setDataWarehouseIncludeFilter(Set<FixedWidthDataFileComponent> dataWarehouseIncludeFilter) {
        this.dataWarehouseIncludeFilter = dataWarehouseIncludeFilter;
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
    private ObjectMapper jsonMapper;

    public ObjectMapper getJsonMapper() {
        return jsonMapper;
    }

    public void setJsonMapper(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }



    /**
     * Given the current context in the form of a step contribution, do whatever
     * is necessary to process this unit inside a transaction. Implementations
     * return {@link RepeatStatus#FINISHED} if finished. If not they return
     * {@link RepeatStatus#CONTINUABLE}. On failure throws an exception.
     *
     * @param contribution mutable state to be passed back to update the current
     *                     step execution
     * @param chunkContext attributes shared between invocations but not between
     *                     restarts
     * @return an {@link RepeatStatus} indicating whether processing is
     * continuable.
     * @throws Exception thrown if error occurs during execution.
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        FixedWidthDataFile dataFile = FixedWidthDataFileFactory.parse(resource.getFile());

        List<SerializedComponent<AmazonS3URI>> deliveredToDataLake= getComponents(dataFile)
                .stream()
                .filter(c -> dataLakeIncludeFilter.contains(c.getType()))
                .map(s3Writer::serializeTos3).collect(Collectors.toList());

        for (SerializedComponent<AmazonS3URI> component: deliveredToDataLake) {
            if (dataWarehouseIncludeFilter.contains(component.getType()))
                redshiftWriter.serializeToRedshift(component);
        }
        return RepeatStatus.FINISHED;
    }
    public List<SerializedComponent<String>> getComponents(final FixedWidthDataFile somePojo) throws Exception {
        if (somePojo instanceof CBNOTFixedWidthDataFile)
            return new CBNOTFixedWidthDataFileComponentSerializer().getComponents((CBNOTFixedWidthDataFile)somePojo,csvMapper, jsonMapper);
        if (somePojo instanceof EMCBKFixedWidthDataFile)
            return new EMCBKFixedWidthDataFileComponentSerializer().getComponents((EMCBKFixedWidthDataFile)somePojo,csvMapper, jsonMapper);
        if (somePojo instanceof EMINQFixedWidthDataFile)
            return new EMINQFixedWidthDataFileComponentSerializer().getComponents((EMINQFixedWidthDataFile)somePojo,csvMapper, jsonMapper);
        if (somePojo instanceof EPTRNFixedWidthDataFile)
            return new EPTRNFixedWidthDataFileComponentSerializer().getComponents((EPTRNFixedWidthDataFile)somePojo,csvMapper, jsonMapper);
        if (somePojo instanceof EPAPEFixedWidthDataFile)
            return new EPAPEFixedWidthDataFileComponentSerializer().getComponents((EPAPEFixedWidthDataFile)somePojo,csvMapper, jsonMapper);
        throw new NotRegisteredException(somePojo.getClass() + " : can't determine an appropriate parser to handle this class.");
    }
    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(resource, "Input resource must be set");
    }

}
