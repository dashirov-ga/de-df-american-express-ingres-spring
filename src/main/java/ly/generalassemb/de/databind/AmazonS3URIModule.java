package ly.generalassemb.de.databind;

import com.amazonaws.services.s3.AmazonS3URI;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class AmazonS3URIModule extends SimpleModule{
    public AmazonS3URIModule(){
        super();
        this.addSerializer(AmazonS3URI.class, ToStringSerializer.instance);
        AmazonS3URIDeserializer deserializer = new AmazonS3URIDeserializer();
        this.addDeserializer(AmazonS3URI.class, deserializer);
    }
}
