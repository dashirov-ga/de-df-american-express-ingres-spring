package ly.generalassemb.de.american.express.ingress.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.Bucket;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jcraft.jsch.ChannelSftp;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.SerializedComponent;
import ly.generalassemb.de.american.express.ingress.model.file.reader.AmexFileItemReader;
import ly.generalassemb.de.american.express.ingress.model.file.writer.AmexFileProcessor;
import ly.generalassemb.de.american.express.ingress.model.file.writer.AmexRedshiftWriter;
import ly.generalassemb.de.american.express.ingress.tasklet.ResourceDeletingTasklet;
import ly.generalassemb.de.american.express.ingress.transformer.FileMessageToJobRequest;
import ly.generalassemb.de.databind.AmazonS3URIModule;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.launch.JobLaunchingGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Ideal behaviour:
 * 1. Run as a service, monitored by newrelic
 * 1.1 Awake every 30 minutes
 * 1.1.1 establish an SFTP connection to Amex
 * 1.1.2 list all new files in the target directory matching a regular expression or ant expression pattern
 * 1.1.2 download all new files and store them locally
 * 1.1.3 Kick off a batch import job, one for all files found <- violated
 * 1.2 Process batch, keeping good records, preferrably in a centralized db location to allow cockpit monitoring of all batch executions, re-runs and fault management
 * 1.2.1 de-serialize each file, filling in data gaps as necessary
 * 1.2.2 extract wanted components from the de-serialised object and write them onto S3 Data Lake
 * 1.2.2.1 Components need to be stored in a predefined prefix: /Components/{File Type}/{File Unique Identifier}/{Component Type}.{Component File Name Extension}
 * 1.2.2.2 Original unparsed files need to be stored in /Raw prefix named identically to the file names found on SFTP location
 * 1.2.2.3 Redshift data load manifests need to be stored in /Manifests/{RunId} prefix
 */
// @EnableAutoConfiguration
@Configuration
@EnableBatchProcessing
@EnableTransactionManagement
@EnableIntegration
@EnableIntegrationManagement
@Import({JobRepositoryConfig.class,DataWarehouseConfig.class,S3Config.class,SnowplowTrackerConfig.class})
@ComponentScan(basePackageClasses = BatchConfigurer.class)
@PropertySource("classpath:application.properties")
public class ApplicationConfiguration  {
    /*
    @Bean
    public static PropertySourcesPlaceholderConfigurer
    propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
*/

    @Qualifier("dwRedshiftDataSource")
    @Autowired
    DataSource dwRedshiftDataSource;


    @Value("${source.amex.sftp.remote_host:fsgateway.aexp.com}")
    private String sftpHost;

    @Value("${source.amex.sftp.remote_port:22}")
    private int sftpPort;

    @Value("${source.amex.sftp.username}")
    private String sftpUser;

    @Value("${source.amex.sftp.password:#{null}}")
    private String sftpPasword;

    @Value("${source.amex.sftp.private-key-file:#{null}}")
    private Resource sftpPrivateKey;

    @Value("${source.amex.sftp.known_hosts:#{null}}")
    private String sftpKnownHosts;

    @Value("${source.amex.sftp.private_key_passphrase:}")
    private String sftpPrivateKeyPassphrase;

    @Value("${source.amex.sftp.remote_directory:/sent}")
    private String sftpRemoteDirectory;

    @Value("${source.amex.sftp.local_directory:/tmp}")
    private String sftpLocalDirectory;

    @Value("${source.amex.sftp.filename-pattern:*.*}")
    private String sftpFileNamePattern;

    @Value("${source.amex.sftp.allow-unknown-hosts:false}")
    private boolean allowUnknownKeys;



    @Value("${sink.aws.s3.component-filter:#{null}}")
    private String sinkAwsS3ComponentFilter;

    @Value("${sink.aws.redshift.component-filter:#{null}}")
    private String sinkAwsRedshiftComponentFilter;

    @Value("${sink.aws.s3.bucket}")
    private Bucket s3Bucket;

    @Value("${sink.asw.redshift.namespace}")
    private String redshiftSchemaName;

    @Value("${sink.aws.redshift.credentials}")
    private String redshiftCopyCredentials;



    @Bean(name = "sftpChannel")
    public MessageChannel inboundFileChannel() {
        return new DirectChannel();
    }

    @Bean(name = "outboundJobRequestChannel")
    public MessageChannel outboundJobRequestChannel() {
        return new DirectChannel();
    }

    @Bean(name = "jobLaunchReplyChannel")
    public MessageChannel jobLaunchReplyChannel() {
        return new DirectChannel();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller() {
        return Pollers.fixedDelay(1000).get();
    }

    @Bean
    public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory() throws UnsupportedEncodingException {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(sftpHost);
        factory.setPort(sftpPort);
        factory.setUser(sftpUser);
        factory.setPrivateKey(sftpPrivateKey);
        factory.setPrivateKeyPassphrase(sftpPrivateKeyPassphrase);
        factory.setPassword(sftpPasword);
        factory.setKnownHosts(sftpKnownHosts);
        factory.setAllowUnknownKeys(allowUnknownKeys);
        return new CachingSessionFactory<ChannelSftp.LsEntry>(factory);
    }

    @Bean
    public SftpInboundFileSynchronizer sftpInboundFileSynchronizer() throws UnsupportedEncodingException {
        SftpInboundFileSynchronizer fileSynchronizer = new SftpInboundFileSynchronizer(sftpSessionFactory());
        fileSynchronizer.setDeleteRemoteFiles(false);
        fileSynchronizer.setRemoteDirectory(sftpRemoteDirectory);
        fileSynchronizer.setFilter(new SftpSimplePatternFileListFilter(sftpFileNamePattern));
        return fileSynchronizer;
    }

    /**
     * Files from Amex are pulled through an InboundChannelAdapter every five minutes
     * To start, we are pulling one file at a time and immediately triggering a corresponding spring batch job
     *
     * @return
     * @throws UnsupportedEncodingException
     */

    @Bean
    @InboundChannelAdapter(channel = "sftpChannel", poller = @Poller(cron = "0/5 * * * * *", maxMessagesPerPoll = "1"), autoStartup = "false")
    public SftpInboundFileSynchronizingMessageSource sftpMessageSource() throws UnsupportedEncodingException {
        SftpInboundFileSynchronizingMessageSource source =
                new SftpInboundFileSynchronizingMessageSource(sftpInboundFileSynchronizer());
        source.setLocalDirectory(new File(sftpLocalDirectory));
        source.setAutoCreateLocalDirectory(true);
        source.setLocalFilter(new AcceptOnceFileListFilter<File>());
        return source;
    }

    @Autowired
    @Bean
    public FileMessageToJobRequest fileMessageToJobRequest(Job americanExpressFileImportJob) {
        FileMessageToJobRequest fileMessageToJobRequest = new FileMessageToJobRequest();
        fileMessageToJobRequest.setFileParameterName("input.file.name");
        fileMessageToJobRequest.setJob(americanExpressFileImportJob);
        return fileMessageToJobRequest;
    }

    // integration workflow needs this gateway to be passed into IntegrationWorkflow
    @Autowired
    @Bean
    public JobLaunchingGateway jobLaunchingGateway(JobRepository jobRepository) throws Exception {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(jobRepository);
        simpleJobLauncher.setTaskExecutor(new SyncTaskExecutor());

        JobLaunchingGateway gateway = new JobLaunchingGateway(simpleJobLauncher);
        gateway.setLoggingEnabled(true);
        return gateway;
    }

    /**
     * Integration flow takes a message (Message<File>)
     * A. from the MessageSource<File> sftpMessageSource()
     * B. converts this Message<File> into a JobLaunchRequest(Job=americanExpressFileImportJob, JobParameters={"'input.file.name' := FilePath"}
     * C. then it invokes the JobLaunchingGateway to process this batch job request
     * D. then logs shit with loggingHandler attached to the stepExecutionsChannel
     * E.G.
     * 2018-02-08 18:02:28.674  INFO 36709 --- [ask-scheduler-5] o.s.i.file.FileReadingMessageSource      : Created message: [GenericMessage [payload=/tmp/sftp-inbound/GENERALASSEMBLYA59662.CBNOT#E87IB6N3913QQ1, headers={id=a9790a94-7ed1-9165-b8fa-50b4a807938e, timestamp=1518130948674}]]
     * 2018-02-08 18:02:28.702  INFO 36709 --- [ask-scheduler-5] o.s.b.c.l.support.SimpleJobLauncher      : Job: [FlowJob: [name=americanExpressFileImportJob]] launched with the following parameters: [{input.file.name=/tmp/sftp-inbound/GENERALASSEMBLYA59662.CBNOT#E87IB6N3913QQ1}]
     *
     * @param jobLaunchingGateway
     * @return
     * @throws UnsupportedEncodingException
     */
    @Autowired
    @Bean
    public IntegrationFlow integrationFlow(
            SftpInboundFileSynchronizingMessageSource sftpMessageSource,
            JobLaunchingGateway jobLaunchingGateway,
            FileMessageToJobRequest fileMessageToJobRequest
    ) throws UnsupportedEncodingException {
        return IntegrationFlows
                .from(sftpMessageSource)
                .handle(fileMessageToJobRequest)    // <-- MessageHandler
                .handle(jobLaunchingGateway)          // <-- MessageHandler
                .log(LoggingHandler.Level.WARN, "headers.id + ': ' + payload")
                .get();
    }


/*
    @Bean
    @ServiceActivator(inputChannel = "stepExecutionsChannel")
    public LoggingHandler loggingHandler() {
        LoggingHandler adapter = new LoggingHandler(LoggingHandler.Level.WARN);
        adapter.setLoggerName("TEST_LOGGER");
        adapter.setLogExpressionString("headers.id + ': ' + payload");
        return adapter;
    }
    @MessagingGateway(name = "notificationExecutionsListener", defaultRequestChannel = "stepExecutionsChannel")
    public interface NotificationExecutionListener extends StepExecutionListener {}

*/


    /**
     * Need a custom serializer to take care of LocalDate LocalDateTime and AmazonS3URI
     * This should be used in both batch and application contexts, no competing, alternate ObjectMapper is needed
     *
     * @return
     */
    @Primary
    @Bean(name = "americanExpressJsonMapper")
    public ObjectMapper defaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        objectMapper.enableDefaultTyping();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new AmazonS3URIModule());

        return objectMapper;
    }
    @Bean
    public CsvMapper csvMapper() {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.registerModule(new JavaTimeModule());
        csvMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return csvMapper;
    }
   /*
    @Primary
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
    @Primary
    @Bean
    @Autowired
    public JobRepository jobRepository(DataSource dataSource,
                                       DataSourceTransactionManager txManager,
                                       Jackson2ExecutionContextStringSerializer serializer
    ) throws Exception {
        JobRepositoryFactoryBean fac = new JobRepositoryFactoryBean();
        fac.setDatabaseType("POSTGRES");
        fac.setDataSource(dataSource);
        fac.setTransactionManager(txManager);
        fac.setSerializer(serializer);
        fac.afterPropertiesSet();

        return fac.getObject();

    }


    // need a custom JobExplorer to replace ObjectMapper with a smarter one
    @Bean
    @Autowired
    public JobExplorer jobExplorer(
            DataSource dataSource,
            Jackson2ExecutionContextStringSerializer serializer) throws Exception {

        JobExplorerFactoryBean fac = new JobExplorerFactoryBean();
        fac.setDataSource(dataSource);
        fac.setSerializer(serializer);
        fac.afterPropertiesSet();
        return fac.getObject();
    }


    @Bean(name = "ExecutionContextPromotionListener")
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener promotionListener = new ExecutionContextPromotionListener();
        String[] keys = {"s3SerializedComponents"};
        promotionListener.setKeys(keys);
        return promotionListener;
    }
    */

    // Amex File POJO does not have mappers set yet, so it won't serialize components properly

    @StepScope
    @Bean(name = "AmexFileItemReader")
    public AmexFileItemReader amexReader(@Value("#{jobParameters['input.file.name']}") String resource) {
        AmexFileItemReader amexFileItemReader = new AmexFileItemReader();
        amexFileItemReader.setResource(new FileSystemResource(resource));
        return amexFileItemReader;
    }

    @Bean(name = "AmexFileProcessor")
    @Autowired
    public AmexFileProcessor amexFileProcessor(AmazonS3 s3, CsvMapper csvMapper, ObjectMapper jsonMapper) {
        Set<FixedWidthDataFileComponent> include;
        if (sinkAwsS3ComponentFilter != null && !sinkAwsS3ComponentFilter.isEmpty())
            include = EnumSet.copyOf(Arrays.stream(sinkAwsS3ComponentFilter.split("\\s*,\\s*")).map(FixedWidthDataFileComponent::valueOf).collect(Collectors.toList()));
        else
            include = EnumSet.copyOf(Arrays.asList(FixedWidthDataFileComponent.values()));

        AmexFileProcessor amexFileProcessor = new AmexFileProcessor();
        amexFileProcessor.setCsvMapper(csvMapper);
        amexFileProcessor.setObjectMapper(jsonMapper);
        amexFileProcessor.setIncludeFilter(include);
        amexFileProcessor.setAmazonS3(s3);
        amexFileProcessor.setS3Bucket(s3Bucket);

        return amexFileProcessor;
    }

    @Bean(name = "AmexRedshiftWriter")
    @Autowired
    public AmexRedshiftWriter amexRedshiftWriter(@Qualifier("dwRedshiftDataSource") DataSource dwRedshiftDataSource) {
        Set<FixedWidthDataFileComponent> include;
        if (sinkAwsS3ComponentFilter != null && !sinkAwsS3ComponentFilter.isEmpty())
            include = EnumSet.copyOf(Arrays.stream(sinkAwsS3ComponentFilter.split("\\s*,\\s*")).map(FixedWidthDataFileComponent::valueOf).collect(Collectors.toList()));
        else
            include = EnumSet.copyOf(Arrays.asList(FixedWidthDataFileComponent.values()));
        AmexRedshiftWriter amexRedshiftWriter = new AmexRedshiftWriter();
        amexRedshiftWriter.setSchemaName(redshiftSchemaName);
        amexRedshiftWriter.setIncludeFilter(include);
        amexRedshiftWriter.setDataSource(dwRedshiftDataSource);
        amexRedshiftWriter.setRedshiftS3AccessCredentials(redshiftCopyCredentials);

        return amexRedshiftWriter;
    }

    /**
     * Batch Job step #1 :
     * 1. Read locally persisted file with name matching that found on Amex SFTP site and load a corresponding object
     * ItemReader returns one FixedWidthDataFile per one input file, who's name is communicated via job parameters
     * 2. Process each FixedWidthDataFile object into a series of components
     * 3. Serialize the components onto Amazon S3 Service, into Bucket, following data lake component naming and
     * placement conventions.
     * 3.1 Apply a filter to selectively upload components
     * 3.2 Store the locations of the uploaded components for subsequent steps to locate and make use of
     */

    @Autowired
    @Bean(name = "LoadAmexFileDataWarehouseViaDataLake")
    protected Step step1(
            StepBuilderFactory stepBuilderFactory,
            // DataSourceTransactionManager dataSourceTransactionManager,
            @Qualifier("AmexFileItemReader") AmexFileItemReader itemReader,
            @Qualifier("AmexFileProcessor") AmexFileProcessor itemProcessor,
            @Qualifier("AmexRedshiftWriter") AmexRedshiftWriter itemWriter /*,
            @Qualifier("ExecutionContextPromotionListener") ExecutionContextPromotionListener promotionListener
            */
    ) {

        // Assert.notNull(promotionListener, "ExecutionContextPromotionListener must be set");
        return stepBuilderFactory.get("step1")
                .<FixedWidthDataFile, List<SerializedComponent<AmazonS3URI>>>chunk(1)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
              //  .transactionManager(dataSourceTransactionManager)
                //.listener(promotionListener)
                .allowStartIfComplete(true)
                .build();
    }

    @StepScope
    @Bean(name = "resourceDeletingTasklet")
    public ResourceDeletingTasklet resourceDeletingTasklet(@Value("#{jobParameters['input.file.name']}") String resource) {
        ResourceDeletingTasklet resourceDeletingTasklet = new ResourceDeletingTasklet();
        resourceDeletingTasklet.setResource(new FileSystemResource(resource));
        return resourceDeletingTasklet;
    }


    @Autowired
    @Bean(name="DeleteLocallyPersistedFileAfterLoadComplete")
    protected Step step2(
            StepBuilderFactory stepBuilderFactory,
           // DataSourceTransactionManager dataSourceTransactionManager,
            ResourceDeletingTasklet resourceDeletingTasklet /*,
            @Qualifier("ExecutionContextPromotionListener") ExecutionContextPromotionListener promotionListener
            */

    ) {
        return stepBuilderFactory.get("step2")
                .tasklet(resourceDeletingTasklet)
                .allowStartIfComplete(false)
                .build();
    }

    @Autowired
    @Bean(name = "americanExpressFileImportJob")
    public Job americanExpressFileImportJob(
            JobBuilderFactory jobBuilderFactory,
            @Qualifier("LoadAmexFileDataWarehouseViaDataLake") Step step1,
            @Qualifier("DeleteLocallyPersistedFileAfterLoadComplete") Step step2
    ) {
        return jobBuilderFactory.get("americanExpressFileImportJob")
                .flow(step1)
                .next(step2)
                .end()
                .build();
    }



}
