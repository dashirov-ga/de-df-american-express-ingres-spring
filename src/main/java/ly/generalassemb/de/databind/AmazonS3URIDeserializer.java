package ly.generalassemb.de.databind;

import com.amazonaws.services.s3.AmazonS3URI;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;

import java.io.IOException;

public class AmazonS3URIDeserializer extends FromStringDeserializer<AmazonS3URI> {

    protected AmazonS3URIDeserializer() {
        super(AmazonS3URI.class);
    }

    protected AmazonS3URI _deserialize(String s, DeserializationContext deserializationContext) throws IOException {
        return new AmazonS3URI(s);
    }
}
