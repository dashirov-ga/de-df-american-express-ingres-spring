package ly.generalassemb.de.american.express.ingress.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.jcraft.jsch.ChannelSftp;
import ly.generalassemb.de.american.express.ingress.filter.FixedWidthFileComponentFilter;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.SerializedComponent;
import ly.generalassemb.de.american.express.ingress.model.file.reader.AmexFileItemReader;
import ly.generalassemb.de.american.express.ingress.model.file.writer.AmexS3ComponentWriter;
import ly.generalassemb.de.american.express.ingress.processor.AmexPoJoToSerializerProcessor;
import ly.generalassemb.de.american.express.ingress.tasklet.FileDeletingTasklet;
import ly.generalassemb.de.american.express.ingress.transformer.FileMessageToJobRequest;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.launch.JobLaunchingGateway;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
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
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Configuration
@EnableBatchProcessing
@ComponentScan

public class AppConfig extends DefaultBatchConfigurer {


    @Autowired
    private AmazonS3 amazonS3;
    public void setAmazonS3(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Autowired
    private DataSource jobRepositoryDataSource;
    public void setJobRepositoryDataSource(DataSource jobRepositoryDataSource) {
        this.jobRepositoryDataSource = jobRepositoryDataSource;
    }

    @Autowired
    private JobBuilderFactory jobs;
    public void setJobs(JobBuilderFactory jobs) {
        this.jobs = jobs;
    }

    @Autowired
    private StepBuilderFactory steps;
    public void setSteps(StepBuilderFactory steps) {
        this.steps = steps;
    }

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

    @Value("${source.amex.sftp.filename_pattern:*}")
    private String sftpFileNamePattern;

    @Value("${source.amex.sftp.allow-unknown-hosts:false}")
    private boolean allowUnknownKeys;

    @Value("${sink.aws.s3.component-filter:#{null}}")
    private String sinkAwsS3ComponentFilter;

    @Value("${sink.aws.redshift.component-filter:#{null}}")
    private String sinkAwsRedshiftComponentFilter;

    @Value("${sink.aws.s3.bucket}")
    private Bucket s3Bucket;

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


    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller() {
        return Pollers.fixedDelay(1000).get();
    }

    @Bean(name="sftpMessageSource")
    @InboundChannelAdapter(channel = "sftpChannel", poller = @Poller(cron = "0/5 * * * * *", maxMessagesPerPoll ="1"))
    public MessageSource<File> sftpMessageSource() throws UnsupportedEncodingException {
        SftpInboundFileSynchronizingMessageSource source =
                new SftpInboundFileSynchronizingMessageSource(sftpInboundFileSynchronizer());
        source.setLocalDirectory(new File(sftpLocalDirectory));
        source.setAutoCreateLocalDirectory(true);
        source.setLocalFilter(new AcceptOnceFileListFilter<File>());
        return source;
    }

    @Bean
    public FileMessageToJobRequest fileMessageToJobRequest() {
        FileMessageToJobRequest fileMessageToJobRequest = new FileMessageToJobRequest();
        fileMessageToJobRequest.setFileParameterName("input.file.name");
        fileMessageToJobRequest.setJob(americanExpressFileImportJob());
        return fileMessageToJobRequest;
    }

    @Bean
    public IntegrationFlow integrationFlow(JobLaunchingGateway jobLaunchingGateway) throws UnsupportedEncodingException {
        return IntegrationFlows
                .from(sftpMessageSource())
                .handle(fileMessageToJobRequest())
                .handle(jobLaunchingGateway)
                .log(LoggingHandler.Level.WARN, "headers.id + ': ' + payload")
                .get();
    }

    @Bean()
    public JobRepository jobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(jobRepositoryDataSource);
        factory.setTransactionManager(getTransactionManager());
        factory.afterPropertiesSet();
        return  factory.getObject();
    }

    @Bean
    public ExecutionContextPromotionListener promotionListener(){
        ExecutionContextPromotionListener promotionListener = new ExecutionContextPromotionListener();
        String[] keys = {"s3SerializedComponents","s3Manifests"};
        promotionListener.setKeys(keys);
        return promotionListener;
    }

    @Bean
    public Job americanExpressFileImportJob(
            @Qualifier("readFileWriteComponentstoS3") Step step1,
            @Qualifier("generateManifesttoS3") Step step2,
            @Qualifier("loadManifeststoRedshift") Step step3
    ) {
        return jobs.get("americanExpressFileImportJob")
                .flow( step1 )
                .next( step2 )
                .end()
                .build();
    }

    @Bean
    protected Step step0(){
        // convert file retrieved via SFTP to POJO
        FileDeletingTasklet t =  new FileDeletingTasklet();
        t.setDirectory(new FileSystemResource("/tmp/david"));
        return  steps.get("step0").tasklet(t).build();
    }

    @Bean(name="readFileWriteComponentstoS3")
    protected Step step1(
            StepBuilderFactory stepBuilderFactory,
            PlatformTransactionManager platformTransactionManager,
            @Qualifier("AmexFileReader") ItemReader<FixedWidthDataFile> ir,
            @Qualifier("AmexPoJoToSerializerProcessor") ItemProcessor<FixedWidthDataFile,List<SerializedComponent<String>>> ip,
            @Qualifier("AmexS3ComponentWriter") ItemWriter<List<SerializedComponent<String>>> iw
    ){

        StepBuilder builder = stepBuilderFactory.get("step1");
        return builder.chunk(10)
                .reader(ir)
                .processor(ip) // <--------------------- ERROR
                .writer(iw)
                .transactionManager(platformTransactionManager)
                .build();
    }



    // integration workflow needs this gateway to be passed into IntegrationWorkflow
    @Bean
    public JobLaunchingGateway jobLaunchingGateway() throws Exception {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(jobRepository());
        simpleJobLauncher.setTaskExecutor(new SyncTaskExecutor());
        return new JobLaunchingGateway(simpleJobLauncher);
    }


    // Amex File POJO does not have mappers set yet, so it won't serialize components properly
    @Bean(name="AmexFileReader")
    @StepScope
    public ItemReader amexReader(@Value("#{jobParameters[input.file.name]}") String resource) {
        AmexFileItemReader amexFileItemReader = new AmexFileItemReader();
        amexFileItemReader.setResource(new FileSystemResource(resource));
        return amexFileItemReader;
    }
    @Bean(name="AmexPoJoToSerializerProcessor")
    @StepScope
    public ItemProcessor amexPoJoToSerializerProcessor(CsvMapper csvMapper, ObjectMapper jsonMapper){
        AmexPoJoToSerializerProcessor amexPoJoToSerializerProcessor = new AmexPoJoToSerializerProcessor();
        amexPoJoToSerializerProcessor.setCsvMapper(csvMapper);
        amexPoJoToSerializerProcessor.setObjectMapper(jsonMapper);
        return amexPoJoToSerializerProcessor; // every possible component is loaded choose what you want in the writer
    }
    @Bean(name="AmexS3ComponentWriter")
    @StepScope
    public ItemWriter amexS3ComponentWriter(AmazonS3 amazonS3){
        Set<FixedWidthDataFileComponent> include;
        if ( sinkAwsS3ComponentFilter != null && !sinkAwsS3ComponentFilter.isEmpty())
            include = EnumSet.copyOf(Arrays.stream(sinkAwsS3ComponentFilter.split("\\s*,\\s*")).map(FixedWidthDataFileComponent::valueOf).collect(Collectors.toList()));
        else
            include = EnumSet.copyOf(Arrays.asList(FixedWidthDataFileComponent.values()));
        AmexS3ComponentWriter writer = new AmexS3ComponentWriter();
        writer.setAmazonS3(amazonS3);
        writer.setIncludeFilter(include);
        writer.setS3Bucket(s3Bucket);
        return writer;

    }

    @Bean(name="redshiftComponentFilter")
    public FixedWidthFileComponentFilter<FixedWidthDataFileComponent> redshiftComponentFilter(){
        FixedWidthFileComponentFilter<FixedWidthDataFileComponent> filter = new FixedWidthFileComponentFilter<>();
        if ( sinkAwsRedshiftComponentFilter != null && !sinkAwsRedshiftComponentFilter.isEmpty())
            filter.setInclude(EnumSet.copyOf(Arrays.stream(sinkAwsRedshiftComponentFilter.split("\\s*,\\s*")).map(FixedWidthDataFileComponent::valueOf).collect(Collectors.toList())));
        return filter;
    }



    // Do not need this jobLauncher - we have a JobLaunchGateway triggered by SFTP file downloads
    /**
    @Bean
    public SimpleJobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository());
        return jobLauncher;
    }

     **/
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


}
