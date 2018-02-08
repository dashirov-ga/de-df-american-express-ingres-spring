package ly.generalassemb.de.american.express.ingress.config;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import static com.amazonaws.services.s3.internal.Constants.MB;

@Component
public class S3Config {

    @Value("${sink.aws.s3.access_key_id}")
    private String awsId;

    @Value("${sink.aws.s3.secret_access_key}")
    private String awsKey;

    @Value("${sink.aws.s3.region}")
    private String region;

    @Value("${sink.aws.s3.bucket}")
    private String bucketName;

    private Logger logger = LoggerFactory.getLogger(S3Config.class);

    @Bean(name = "awsS3Client")
    public AmazonS3 s3client() {
        AWSCredentialsProvider credentialsProvider;
        if (awsId == null || awsKey == null)
            credentialsProvider = new DefaultAWSCredentialsProviderChain();
        else
            credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsId, awsKey));

        if (region==null)
            region="us-east-1";

        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(credentialsProvider)
                .build();

    }

    @Bean(name = "awsS3TransferManager")
    public TransferManager transferManager(){
        TransferManager tm = TransferManagerBuilder.standard()
                .withS3Client(s3client())
                .withDisableParallelDownloads(false)
                .withMinimumUploadPartSize(5L * MB)
                .withMultipartUploadThreshold(16L * MB)
                .withMultipartCopyPartSize(5L * MB)
                .withMultipartCopyThreshold( 100L * MB)
                .withExecutorFactory(()->createExecutorService(20))
                .build();
        int oneDay = 1000 * 60 * 60 * 24;
        Date oneDayAgo = new Date(System.currentTimeMillis() - oneDay);
        try {
            tm.abortMultipartUploads(bucketName, oneDayAgo);
        } catch (AmazonClientException e) {
            logger.error("Unable to upload file, upload was aborted, reason: " + e.getMessage());
        }
        return tm;
    }

    private ThreadPoolExecutor createExecutorService(int threadNumber) {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int threadCount = 1;
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("aws-s3-transfer-manager-worker-" + threadCount++);
                return thread;
            }
        };
        return (ThreadPoolExecutor)Executors.newFixedThreadPool(threadNumber, threadFactory);
    }
}
