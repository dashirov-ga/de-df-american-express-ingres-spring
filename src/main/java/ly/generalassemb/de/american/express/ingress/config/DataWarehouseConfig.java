package ly.generalassemb.de.american.express.ingress.config;

import com.jcraft.jsch.JSchException;
import com.pastdev.jsch.DefaultSessionFactory;
import com.pastdev.jsch.tunnel.TunnelConnectionManager;
import com.pastdev.jsch.tunnel.TunneledDataSourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@Configuration
@ConfigurationProperties(prefix = "sink.aws.redshift")
public class DataWarehouseConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataWarehouseConfig.class);

    public static class SshTunnel {
        private boolean enabled;
        private String user;
        private String host;
        private int port;
        private File privateKey;
        private File knownHosts;

        // standard getters and setters

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public File getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(File privateKey) {
            this.privateKey = privateKey;
        }

        public File getKnownHosts() {
            return knownHosts;
        }

        public void setKnownHosts(File knownHosts) {
            this.knownHosts = knownHosts;
        }
    }

    private String url;

    private SshTunnel sshTunnel;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SshTunnel getSshTunnel() {
        return sshTunnel;
    }

    public void setSshTunnel(SshTunnel sshTunnel) {
        this.sshTunnel = sshTunnel;
    }

    @ConfigurationProperties(prefix = "sink.aws.redshift")
    @Bean(name = "dwRedshiftDataSource")
    public DataSource dwRedshiftDataSource() throws JSchException, IOException, URISyntaxException {
        if (sshTunnel.isEnabled()) {
            URI current = getUri(url);

            String tunnelDefinition =
                    String.format("%s@%s:%s|127.0.0.1:%s:%s:%s",
                            //System.getProperty("user.name"),
                            sshTunnel.getUser(),
                            sshTunnel.getHost(),
                            sshTunnel.getPort(),
                            current.getPort(),
                            current.getHost(),
                            current.getPort());
            String newURL = rewriteJDBCUrl(url, "127.0.0.1", current.getPort());

            LOGGER.info("Connecting to " + newURL + " via tunnel " + tunnelDefinition);

            DataSource dataSource = DataSourceBuilder.create()
                    .url( newURL )
                    .build();



            DefaultSessionFactory defaultSessionFactory = new DefaultSessionFactory();
            defaultSessionFactory.setKnownHosts(new FileInputStream(sshTunnel.getKnownHosts()));
            defaultSessionFactory.setIdentityFromPrivateKey(sshTunnel.getPrivateKey().getAbsolutePath());
            return new TunneledDataSourceWrapper(
                    new TunnelConnectionManager(defaultSessionFactory, tunnelDefinition),
                    dataSource);
        } else {
            LOGGER.info("Connecting to " + url );
            return DataSourceBuilder.create().build();

        }
    }
    /**
     * Take original JDBC URL and replace it's host / port to desired values
     * - Useful in re-writing JDBC urls due to SSH Tunnel / Proxy requirements
     *
     * @param original
     * @param newHost
     * @param newPort
     * @return
     */
    private String rewriteJDBCUrl(String original, String newHost, int newPort) throws URISyntaxException {
        URI uri = getUri(original);
            /*
                 scheme:[//[user[:password]@]host[:port]][/path][?query][#fragment]

             */
        URI out = new URI(
                uri.getScheme().toLowerCase(Locale.US),
                uri.getUserInfo(),
                newHost,
                newPort,
                uri.getPath(),
                uri.getQuery(),
                uri.getFragment()
        );
        return "jdbc:" + out.toString();
    }

    /**
     * extract URI from  JDBC URL String
     * @param jdbcUrl - JDBC URL String
     * @return
     * @throws URISyntaxException
     */
    private URI getUri (String jdbcUrl ) throws URISyntaxException {
        String cleanURI = jdbcUrl.substring(5);
        return URI.create(cleanURI);
    }

}
