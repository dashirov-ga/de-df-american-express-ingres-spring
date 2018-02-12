package ly.generalassemb.de.american.express.ingress.model.file.writer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.*;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.SerializedComponent;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class SerializedComponentS3Writer {
    private static final DateFormat df = new SimpleDateFormat("yyyyMMdd");
    private AmazonS3 amazonS3;
    public AmazonS3 getAmazonS3() {
        return amazonS3;
    }
    public void setAmazonS3(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }
    private Bucket s3Bucket;
    public Bucket getS3Bucket() {
        return s3Bucket;
    }
    public void setS3Bucket(Bucket s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    /**
     * Given a single componsnt and corresponding content, without asking too many questions,
     *                 serialize to S3 and return the component and corresponding S3 URL back
     * @param entry - component, and corresponding conent
     * @return - component, and corresponding S3 url it was stored to
     */
    protected SerializedComponent<AmazonS3URI> serializeTos3(SerializedComponent<String> entry) {
        // create tags to be attached to S3 object in onbject store
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag("Contents", "American Express Credit Card Transactions"));
        tags.add(new Tag("PII", "FALSE"));
        tags.add(new Tag("PCI", "TRUE"));
        tags.add(new Tag("Snapshot Date", df.format(new Date())));
        tags.add(new Tag("Content-Type", entry.getType().getFormat()));
        if (entry.getType().getFormat().equals("text/csv"))
            tags.add(new Tag("CSV:Header", "TRUE"));


        // calculate MD5 of the stream content
        byte[] content = entry.getPayload().getBytes(Charset.forName("UTF-8"));
        String streamMD5 = new String(Base64.getEncoder().encode(DigestUtils.md5(content)));

        // Attach
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(content.length);
        meta.setContentMD5(streamMD5);
        meta.setContentType(entry.getType().getFormat());


        PutObjectRequest req = new PutObjectRequest(
                s3Bucket.getName(),
                entry.getParentKind() + "/" + entry.getParentId() + "/" + entry.getType().getS3Prefix() + entry.getType().getFileNameExtension(),
                new ByteArrayInputStream(content),
                meta);

        req.setTagging(new ObjectTagging(tags));
        // req.setMetadata(meta);
        amazonS3.putObject(req);

        // TODO: move to data lake, do not drop in the top level
        SerializedComponent<AmazonS3URI> component = new SerializedComponent<>();
        component.setType(entry.getType());
        component.setParentId(entry.getParentId());
        component.setParentKind(entry.getParentKind());
        component.setPayload(new AmazonS3URI("s3://" + req.getBucketName() + "/" + req.getKey()));
        return component;
    }
}
