package ly.generalassemb.de.american.express.ingress.config;

import com.jcraft.jsch.JSchException;
import com.pastdev.jsch.DefaultSessionFactory;
import com.pastdev.jsch.tunnel.TunnelConnectionManager;
import com.pastdev.jsch.tunnel.TunneledDataSourceWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Component
public class JobRepositoryConfig {

    @Value("${job.repo.postgres.username}")
    private String postgresUser;

    @Value("${job.repo.postgres.password}")
    private String postgresPass;

    @Value("${job.repo.postgres.host}")
    private String postgresHost;

    @Value("${job.repo.postgres.port}")
    private String postgresPort;

    @Value("${job.repo.postgres.db_name}")
    private String postgresDatabaseName;

    @Value("${job.repo.postgres.driver-class-name}")
    private String postgresDriverClass;


    @Value("${job.repo.postgres.ssh-tunnel}")
    private Boolean postgresTunnel;

    @Value("${job.repo.postgres.ssh-tunnel-host}")
    private String postgresTunnelHost;

    @Value("${job.repo.postgres.ssh-tunnel-port}")
    private String postgresTunnelPort;

    @Value("${job.repo.postgres.ssh-tunnel-user}")
    private String postgresTunnelUser;

    @Value("${job.repo.postgres.ssh-tunnel-private-key}")
    private String postgresTunnelPrivateKey;

    @Value("${job.repo.postgres.ssh-tunnel-known-hosts}")
    private String postgresTunnelKnownHosts;


    @Bean(name = "jobRepositoryDataSource")
    @Primary
    public DataSource dataSourceRedshift() throws JSchException, ClassNotFoundException, FileNotFoundException {
        if (postgresTunnel == null)
            return null;
        if (postgresTunnel) {
            String tunnelDefinition =
                    String.format("%s@%s|127.0.0.1:%s:%s:%s",
                            //System.getProperty("user.name"),
                            postgresTunnelUser,
                            postgresTunnelHost,
                            postgresPort,
                            postgresHost,
                            postgresPort);  //"davidashirov@localhost->david.ashirov@bastion1.de.ga.co|127.0.0.1:5439:dw.data.generalassemb.ly:5439";
            SimpleDriverDataSource datasource = new SimpleDriverDataSource();
            datasource.setDriver(new org.postgresql.Driver());
            datasource.setUrl("jdbc:postgres://localhost:" + postgresPort + "/" + postgresDatabaseName);
            datasource.setUsername(postgresUser);
            datasource.setPassword(postgresPass);
            DefaultSessionFactory defaultSessionFactory = new DefaultSessionFactory();
            defaultSessionFactory.setKnownHosts(new FileInputStream(new File(postgresTunnelKnownHosts)));
            defaultSessionFactory.setIdentityFromPrivateKey(postgresTunnelPrivateKey);
            return new TunneledDataSourceWrapper(new TunnelConnectionManager(defaultSessionFactory, tunnelDefinition), new TransactionAwareDataSourceProxy(datasource));
        } else {
            SimpleDriverDataSource datasource = new SimpleDriverDataSource();
            datasource.setDriver(new org.postgresql.Driver());
            datasource.setUrl("jdbc:postgresql://" + postgresHost + ":" + postgresPort + "/" + postgresDatabaseName);
            datasource.setUsername(postgresUser);
            datasource.setPassword(postgresPass);
            return new TransactionAwareDataSourceProxy(datasource);
        }

    }

}
