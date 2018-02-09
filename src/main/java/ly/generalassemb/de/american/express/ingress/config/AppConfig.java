package ly.generalassemb.de.american.express.ingress.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.transaction.annotation.EnableTransactionManagement;


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
@Configuration
@EnableBatchProcessing
@EnableTransactionManagement
@EnableIntegration
@EnableIntegrationManagement
@ComponentScan
@SpringBootApplication
public class AppConfig extends DefaultBatchConfigurer {

    @Autowired
            @Qualifier("jobRegistry")
    JobRegistry  jobRegistry;


}
