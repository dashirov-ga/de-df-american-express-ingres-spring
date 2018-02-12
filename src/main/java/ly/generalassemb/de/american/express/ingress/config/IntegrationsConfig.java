package ly.generalassemb.de.american.express.ingress.config;

import com.jcraft.jsch.ChannelSftp;
import ly.generalassemb.de.american.express.ingress.transformer.FileMessageToJobRequest;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.launch.JobLaunchingGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.channel.DirectChannel;
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
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.UnsupportedEncodingException;

@Component
public class IntegrationsConfig {

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


    @Autowired
    @Qualifier("americanExpressFileImportJob")
    private Job americanExpressFileImportJob;

    public void setAmericanExpressFileImportJob(Job americanExpressFileImportJob) {
        this.americanExpressFileImportJob = americanExpressFileImportJob;
    }

    @Autowired
    private JobRepository jobRepository;

    public void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * Had to create a default poller - wouldn't start without it. Why? I already have a cron poller - exactly where i need one
     * @return
     */


    @Bean(name="sftpChannel")
    public MessageChannel inboundFileChannel(){
        return new DirectChannel();
    }
    @Bean(name="outboundJobRequestChannel")
    public MessageChannel outboundJobRequestChannel(){
        return new DirectChannel();
    }
    @Bean(name="jobLaunchReplyChannel")
    public MessageChannel jobLaunchReplyChannel(){
        return new DirectChannel();
    }
    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller() {
        return Pollers.fixedDelay(10000).get();
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
     *   To start, we are pulling one file at a time and immediately triggering a corresponding spring batch job
     * @return
     * @throws UnsupportedEncodingException
     */

    @Bean
    @InboundChannelAdapter(channel = "sftpChannel", poller = @Poller(cron = "0/5 * * * * *", maxMessagesPerPoll ="1"), autoStartup = "false")
    public SftpInboundFileSynchronizingMessageSource sftpMessageSource() throws UnsupportedEncodingException {
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
        fileMessageToJobRequest.setJob(americanExpressFileImportJob);
        return fileMessageToJobRequest;
    }

    // integration workflow needs this gateway to be passed into IntegrationWorkflow
    @Bean
    public JobLaunchingGateway jobLaunchingGateway() throws Exception {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(jobRepository);
        simpleJobLauncher.setTaskExecutor(new SyncTaskExecutor());

        JobLaunchingGateway gateway =  new JobLaunchingGateway(simpleJobLauncher);
        gateway.setLoggingEnabled(true);
        return gateway;
    }

    /**
     * Integration flow takes a message (Message<File>)
     *    A. from the MessageSource<File> sftpMessageSource()
     *    B. converts this Message<File> into a JobLaunchRequest(Job=americanExpressFileImportJob, JobParameters={"'input.file.name' := FilePath"}
     *    C. then it invokes the JobLaunchingGateway to process this batch job request
     *    D. then logs shit with loggingHandler attached to the stepExecutionsChannel
     *    E.G.
     *    2018-02-08 18:02:28.674  INFO 36709 --- [ask-scheduler-5] o.s.i.file.FileReadingMessageSource      : Created message: [GenericMessage [payload=/tmp/sftp-inbound/GENERALASSEMBLYA59662.CBNOT#E87IB6N3913QQ1, headers={id=a9790a94-7ed1-9165-b8fa-50b4a807938e, timestamp=1518130948674}]]
     *    2018-02-08 18:02:28.702  INFO 36709 --- [ask-scheduler-5] o.s.b.c.l.support.SimpleJobLauncher      : Job: [FlowJob: [name=americanExpressFileImportJob]] launched with the following parameters: [{input.file.name=/tmp/sftp-inbound/GENERALASSEMBLYA59662.CBNOT#E87IB6N3913QQ1}]
     *
     * @param jobLaunchingGateway
     * @return
     * @throws UnsupportedEncodingException
     */
    @Bean
    public IntegrationFlow integrationFlow(JobLaunchingGateway jobLaunchingGateway) throws UnsupportedEncodingException {
        return IntegrationFlows
                .from(sftpMessageSource())
                .handle(fileMessageToJobRequest())    // <-- MessageHandler
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


}
