package ly.generalassemb.de.american.express.ingress.config;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RunWith(SpringJUnit4ClassRunner.class)

// @PropertySource("classpath:application.properties")
@TestPropertySource("classpath:application.properties")                              //critical
@Transactional                                                                       //critical
@EnableAutoConfiguration                                                             //critical
@ComponentScan("ly.generalassemb.de.american.express.ingress.config")                //critical
public class JDBCConfigTest {
    private final Logger LOGGER = LoggerFactory.getLogger(JDBCConfigTest.class);

    @Autowired
    @Qualifier("dwRedshiftDataSource")
    private DataSource redshiftDataSource;

    private Connection connection;


    @Before
    public void setUp() throws Exception {
        connection = redshiftDataSource.getConnection();
    }
    @After
    public void tearDown() throws Exception {
        LOGGER.debug("Closing db connection");
        connection.close();
    }
    @Test
    public void testConnection() {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT 1;");
            ResultSet rs = ps.executeQuery();
            rs.next();
            int one = rs.getInt(1);
            rs.close();
            Assert.assertEquals(1, one);
        } catch (Exception e){
            Assert.assertNull(e);
        }
    }
}