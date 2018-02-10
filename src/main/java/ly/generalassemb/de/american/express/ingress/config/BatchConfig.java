package ly.generalassemb.de.american.express.ingress.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.SerializedComponent;
import ly.generalassemb.de.american.express.ingress.model.file.reader.AmexFileItemReader;
import ly.generalassemb.de.american.express.ingress.model.file.writer.AmexS3ComponentWriter;
import ly.generalassemb.de.american.express.ingress.processor.AmexPoJoToSerializerProcessor;
import ly.generalassemb.de.databind.AmazonS3URIModule;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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


    @Autowired
    @Bean
    public Jackson2ExecutionContextStringSerializer serializer(ObjectMapper objectMapper) {
        Jackson2ExecutionContextStringSerializer serializer = new Jackson2ExecutionContextStringSerializer();
        // replace object mapper with one that is capable of com.amazonaws.services.s3.AmazonS3URI serialization/de-serialization
        System.out.println("Setting custom objectMapper");
        serializer.setObjectMapper(objectMapper);
        return serializer;
    }

    @Autowired
    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


    // need a custom JobRepository to replace ObjectMapper with a smarter one
    @Bean
    @Autowired
    public JobRepository jobRepository(DataSource dataSource,
                                       DataSourceTransactionManager txManager,
                                       Jackson2ExecutionContextStringSerializer serializer
    ) throws Exception
    {
        JobRepositoryFactoryBean fac = new JobRepositoryFactoryBean();
        fac.setDataSource( dataSource );
        fac.setTransactionManager( txManager );
        fac.setSerializer( serializer );
        fac.afterPropertiesSet();
        return fac.getObject();
    }


    // need a custom JobExplorer to replace ObjectMapper with a smarter one
    @Bean
    @Autowired
    public JobExplorer jobExplorer(
            DataSource dataSource ,
            Jackson2ExecutionContextStringSerializer serializer ) throws Exception
    {

        JobExplorerFactoryBean fac = new JobExplorerFactoryBean();
        fac.setDataSource( dataSource );
        fac.setSerializer( serializer );
        fac.afterPropertiesSet();
        return fac.getObject();
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
        AmexFileItemReader amexFileItemReader = new AmexFileItemReader();
        amexFileItemReader.setResource(new FileSystemResource(resource));
        return amexFileItemReader;
    }

    @Bean(name = "AmexPoJoToSerializerProcessor")
    @StepScope
    @Autowired
    public AmexPoJoToSerializerProcessor  amexPoJoToSerializerProcessor(CsvMapper csvMapper, ObjectMapper jsonMapper) {
        AmexPoJoToSerializerProcessor amexPoJoToSerializerProcessor = new AmexPoJoToSerializerProcessor();
        amexPoJoToSerializerProcessor.setCsvMapper(csvMapper);
        amexPoJoToSerializerProcessor.setObjectMapper(jsonMapper);
        return amexPoJoToSerializerProcessor; // every possible component is loaded choose what you want in the writer
    }

    @Bean(name = "AmexS3ComponentWriter")
    @StepScope
    @Autowired
    public AmexS3ComponentWriter  amexS3ComponentWriter(AmazonS3 amazonS3) {
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
    @Autowired
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

    //@JobScope
    @Autowired
    @Bean(name = "americanExpressFileImportJob")
    public  Job americanExpressFileImportJob(
            JobBuilderFactory jobBuilderFactory,@Qualifier("readFileWriteComponentstoS3") Step step1
    ) {
        return jobBuilderFactory.get("americanExpressFileImportJob")
                .flow(step1)
                .end()
                .build();
    }

    /**
     * Need a custom serializer to take care of LocalDate LocalDateTime and AmazonS3URI
     * This should be used in both batch and application contexts, no competing, alternate ObjectMapper is needed
     * @return
     */
    @Primary
    @Bean(name="americanExpressJsonMapper")
    public ObjectMapper defaultObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        objectMapper.enableDefaultTyping();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new AmazonS3URIModule());

        return objectMapper;
    }


}
