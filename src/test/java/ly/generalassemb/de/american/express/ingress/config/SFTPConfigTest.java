package ly.generalassemb.de.american.express.ingress.config;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

@RunWith(SpringJUnit4ClassRunner.class)
@Import({AppConfig.class})
@PropertySource("classpath:application.properties")
@EnableAutoConfiguration(exclude = { DataSourceTransactionManagerAutoConfiguration.class, DataSourceAutoConfiguration.class })
public class SFTPConfigTest {
    private final Logger LOGGER = LoggerFactory.getLogger(SFTPConfigTest.class);

    // inbound adapter
    @Autowired
    private MessageSource sftpMessageSource;

    @Before
    public void setUp(){

    }
    @After
    public void tearDown() throws Exception {
    }
    @Test
    public void testConnection() {
        Message message = sftpMessageSource.receive();
        Assert.assertNotNull(message);
        File file = (File)message.getPayload();
        LOGGER.debug(file.getName());

    }

}