package ly.generalassemb.de.american.express.ingress.config;

import com.jcraft.jsch.JSchException;
import com.pastdev.jsch.DefaultSessionFactory;
import com.pastdev.jsch.tunnel.TunnelConnectionManager;
import com.pastdev.jsch.tunnel.TunneledDataSourceWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Component
public class DataWarehouseConfig {

    @Value("${sink.aws.redshift.username}")
    private String redshiftUser;

    @Value("${sink.aws.redshift.password}")
    private String redshiftPass;

    @Value("${sink.aws.redshift.host}")
    private String redshiftHost;

    @Value("${sink.aws.redshift.port}")
    private String redshiftPort;

    @Value("${sink.aws.redshift.db_name}")
    private String redshiftDatabaseName;

    @Value("${sink.aws.redshift.driver-class-name}")
    private String redshiftDriverClass;


    @Value("${sink.aws.redshift.ssh-tunnel}")
    private Boolean redshiftTunnel;

    @Value("${sink.aws.redshift.ssh-tunnel-host}")
    private String redshiftTunnelHost;

    @Value("${sink.aws.redshift.ssh-tunnel-port}")
    private String redshiftTunnelPort;

    @Value("${sink.aws.redshift.ssh-tunnel-user}")
    private String redshiftTunnelUser;

    @Value("${sink.aws.redshift.ssh-tunnel-private-key}")
    private String redshiftTunnelPrivateKey;

    @Value("${sink.aws.redshift.ssh-tunnel-known-hosts}")
    private String redshiftTunnelKnownHosts;


    @Bean(name = "dwRedshiftDataSource")
    public DataSource dataSourceRedshift() throws JSchException, ClassNotFoundException, FileNotFoundException {
        if (redshiftTunnel == null)
            return null;
        if (redshiftTunnel) {
            String tunnelDefinition =
                    String.format("%s@%s|127.0.0.1:%s:%s:%s",
                            //System.getProperty("user.name"),
                            redshiftTunnelUser,
                            redshiftTunnelHost,
                            redshiftPort,
                            redshiftHost,
                            redshiftPort);
            SimpleDriverDataSource datasource = new SimpleDriverDataSource();
            datasource.setDriver(new com.amazon.redshift.jdbc42.Driver());
            datasource.setUrl("jdbc:redshift://localhost:" + redshiftPort + "/" + redshiftDatabaseName);
            datasource.setUsername(redshiftUser);
            datasource.setPassword(redshiftPass);
            DefaultSessionFactory defaultSessionFactory = new DefaultSessionFactory();
            defaultSessionFactory.setKnownHosts(new FileInputStream(new File(redshiftTunnelKnownHosts)));
            defaultSessionFactory.setIdentityFromPrivateKey(redshiftTunnelPrivateKey);
            return new TunneledDataSourceWrapper(new TunnelConnectionManager(defaultSessionFactory, tunnelDefinition), new TransactionAwareDataSourceProxy(datasource));
        } else {
            SimpleDriverDataSource datasource = new SimpleDriverDataSource();
            datasource.setDriver(new org.postgresql.Driver());
            datasource.setUrl("jdbc:redshift://" + redshiftHost + ":" + redshiftPort + "/" + redshiftDatabaseName);
            datasource.setUsername(redshiftUser);
            datasource.setPassword(redshiftPass);
            return new TransactionAwareDataSourceProxy(datasource);
        }

    }

}
