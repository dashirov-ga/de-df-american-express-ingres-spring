package ly.generalassemb.de.databind;

import com.amazonaws.services.s3.AmazonS3URI;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class AmazonS3URISerializerTest {
    private ObjectMapper mapper;

    @Before
    public void initialize(){
       mapper = new ObjectMapper();
       mapper.registerModule(new AmazonS3URIModule());
    }


    @Test
    public void deserializeTest() {
        String original = "\"s3://ga-adhoc-data/in/test.csv\"";
        AmazonS3URI object = new AmazonS3URI("s3://ga-adhoc-data/in/test.csv");
        AmazonS3URI deserialized = null;
        try {
            deserialized = mapper.reader().withType(AmazonS3URI.class).readValue(original);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Assert.assertEquals(object,deserialized);
        }
    }

    @Test
    public void serializeTest() {
        String original = "s3://ga-adhoc-data/in/test.csv";
        AmazonS3URI object = new AmazonS3URI(original);
        String serialized = null;
        try {
            serialized = mapper.writer().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } finally {
            Assert.assertEquals("\"" + original + "\"",serialized);
        }
    }
}