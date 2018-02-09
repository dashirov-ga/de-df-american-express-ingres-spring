package ly.generalassemb.de.american.express.ingress.config;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.messaging.Message;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

@RunWith(SpringJUnit4ClassRunner.class)
// @Import({AppConfig.class})
// @PropertySource("classpath:application.properties")
@TestPropertySource("classpath:application.properties")                              //critical
// @EnableAutoConfiguration
@ContextConfiguration
public class SFTPConfigTest {
    private final Logger LOGGER = LoggerFactory.getLogger(SFTPConfigTest.class);

    /**
     * https://stackoverflow.com/questions/29203218/instantiating-objects-when-using-spring-for-testing-vs-production
     */

    @ComponentScan("ly.generalassemb.de.american.express.ingress.config")
    @Configuration
    static class TestConfig {
        public SftpInboundFileSynchronizingMessageSource sftpInboundFileSynchronizingMessageSource(){
            return Mockito.mock(SftpInboundFileSynchronizingMessageSource.class);
        }
    }

    @Autowired
    private SftpInboundFileSynchronizingMessageSource sftpMessageSource;

    @Before
    public void setUp(){
        LOGGER.debug("Preparing for a test");

    }
    @After
    public void tearDown() throws Exception {
        LOGGER.debug("Test Completed");
    }
    @Ignore
    @Test
    public void testConnection() {
        LOGGER.debug("Testing ability to read a message from sftp message source, expecting non-null message");
        Message message = sftpMessageSource.receive();
        LOGGER.debug("Message retrieved");
        Assert.assertNotNull(message);
        File file = (File)message.getPayload();
        Assert.assertTrue(file.isFile());
        LOGGER.debug(file.getName());

    }

}