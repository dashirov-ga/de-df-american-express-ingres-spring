package ly.generalassemb.de.american.express.ingress.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.SerializedComponent;
import ly.generalassemb.de.american.express.ingress.model.file.reader.AmexFileItemReader;
import ly.generalassemb.de.american.express.ingress.model.file.writer.AmexS3ComponentWriter;
import ly.generalassemb.de.american.express.ingress.processor.AmexPoJoToSerializerProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BatchConfig {

    @Autowired
    JobRegistry jobRegistry;
    public void setJobRegistry(JobRegistry jobRegistry) {
        this.jobRegistry = jobRegistry;
    }

    /**
     * Provided by CsvMapperConfig
     */
    @Autowired
    CsvMapper csvMapper;

    public void setCsvMapper(CsvMapper csvMapper) {
        this.csvMapper = csvMapper;
    }

    /**
     * Provided by JsonMapperConfig
     */    @Autowired
    ObjectMapper jsonMapper;

    public void setJsonMapper(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * Provided by JobRepositoryConfig
     */
    @Autowired
    @Qualifier("jobRepositoryDataSource")
    private DataSource jobRepositoryDataSource;

    public void setJobRepositoryDataSource(DataSource jobRepositoryDataSource) {
        this.jobRepositoryDataSource = jobRepositoryDataSource;
    }

    /**
     * Provided by Batch Runtime
     */
    @Autowired
    private  JobBuilderFactory jobs;

    public void setJobs(JobBuilderFactory jobs) {
        this.jobs = jobs;
    }

    /**
     * Provided by Batch Runtime
     */
    @Autowired
    private  StepBuilderFactory steps;

    public void setSteps(StepBuilderFactory steps) {
        this.steps = steps;
    }




    /**
     * Provided by S3Config Component
     */
    @Autowired
    private AmazonS3 amazonS3;

    public void setAmazonS3(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Value("${sink.aws.s3.component-filter:#{null}}")
    private String sinkAwsS3ComponentFilter;

    @Value("${sink.aws.redshift.component-filter:#{null}}")
    private String sinkAwsRedshiftComponentFilter;

    @Value("${sink.aws.s3.bucket}")
    private Bucket s3Bucket;

    public String getSinkAwsS3ComponentFilter() {
        return sinkAwsS3ComponentFilter;
    }

    public void setSinkAwsS3ComponentFilter(String sinkAwsS3ComponentFilter) {
        this.sinkAwsS3ComponentFilter = sinkAwsS3ComponentFilter;
    }

    public String getSinkAwsRedshiftComponentFilter() {
        return sinkAwsRedshiftComponentFilter;
    }

    public void setSinkAwsRedshiftComponentFilter(String sinkAwsRedshiftComponentFilter) {
        this.sinkAwsRedshiftComponentFilter = sinkAwsRedshiftComponentFilter;
    }

    public Bucket getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(Bucket s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("jobRepositoryDataSource") DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean(name="jobRepository")
    public JobRepository jobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(jobRepositoryDataSource);
        factory.setTransactionManager(transactionManager(jobRepositoryDataSource));
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean(name = "ExecutionContextPromotionListener")
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener promotionListener = new ExecutionContextPromotionListener();
        String[] keys = {"s3SerializedComponents", "s3Manifests"};
        promotionListener.setKeys(keys);
        return promotionListener;
    }

    // Amex File POJO does not have mappers set yet, so it won't serialize components properly
    @Bean(name = "AmexFileReader")
    @StepScope
    public AmexFileItemReader amexReader(@Value("#{jobParameters['input.file.name']}") String resource) {
        System.out.println("\n\n\n\t\t\t\t\t" + resource + "\n\n\n");
        AmexFileItemReader amexFileItemReader = new AmexFileItemReader();
        amexFileItemReader.setResource(new FileSystemResource(resource));
        return amexFileItemReader;
    }

    @Bean(name = "AmexPoJoToSerializerProcessor")
    @StepScope
    public AmexPoJoToSerializerProcessor  amexPoJoToSerializerProcessor() {
        AmexPoJoToSerializerProcessor amexPoJoToSerializerProcessor = new AmexPoJoToSerializerProcessor();
        amexPoJoToSerializerProcessor.setCsvMapper(csvMapper);
        amexPoJoToSerializerProcessor.setObjectMapper(jsonMapper);
        return amexPoJoToSerializerProcessor; // every possible component is loaded choose what you want in the writer
    }

    @Bean(name = "AmexS3ComponentWriter")
    @StepScope
    public AmexS3ComponentWriter  amexS3ComponentWriter() {
        Set<FixedWidthDataFileComponent> include;
        if (sinkAwsS3ComponentFilter != null && !sinkAwsS3ComponentFilter.isEmpty())
            include = EnumSet.copyOf(Arrays.stream(sinkAwsS3ComponentFilter.split("\\s*,\\s*")).map(FixedWidthDataFileComponent::valueOf).collect(Collectors.toList()));
        else
            include = EnumSet.copyOf(Arrays.asList(FixedWidthDataFileComponent.values()));
        AmexS3ComponentWriter writer = new AmexS3ComponentWriter();
        writer.setAmazonS3(amazonS3);
        writer.setIncludeFilter(include);
        writer.setS3Bucket(s3Bucket);
        return writer;

    }

    /**
     *
     * Batch Job step #1 :
     * 1. Read locally persisted file with name matching that found on Amex SFTP site and load a corresponding object
     *    ItemReader returns one FixedWidthDataFile per one input file, who's name is communicated via job parameters
     * 2. Process each FixedWidthDataFile object into a series of components
     * 3. Serialize the components onto Amazon S3 Service, into Bucket, following data lake component naming and
     *    placement conventions.
     *    3.1 Apply a filter to selectively upload components
     *    3.2 Store the locations of the uploaded components for subsequent steps to locate and make use of
     */
    @Bean(name = "readFileWriteComponentstoS3")
    protected Step step1(
            StepBuilderFactory stepBuilderFactory,
            PlatformTransactionManager platformTransactionManager,
            @Qualifier("AmexFileReader") ItemReader<FixedWidthDataFile> itemReader,
            @Qualifier("AmexPoJoToSerializerProcessor") ItemProcessor<FixedWidthDataFile,List<SerializedComponent<String>> > itemProcessor,
            @Qualifier("AmexS3ComponentWriter") ItemWriter<List<SerializedComponent<String>> >itemWriter,
            @Qualifier("ExecutionContextPromotionListener") ExecutionContextPromotionListener promotionListener
    ) {

        Assert.notNull(promotionListener,"ExecutionContextPromotionListener must be set");
        return stepBuilderFactory.get("step1")
                .<FixedWidthDataFile, List<SerializedComponent<String>>>chunk(1)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .transactionManager(platformTransactionManager)
                .listener(promotionListener)
                .build();
    }

    @Bean(name = "americanExpressFileImportJob")
    public  Job americanExpressFileImportJob(
            @Qualifier("readFileWriteComponentstoS3") Step step1
    ) {
        return jobs.get("americanExpressFileImportJob")
                .flow(step1)
                .end()
                .build();
    }



}
