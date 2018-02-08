package ly.generalassemb.de.american.express.ingress.model.EPAPE;

import com.ancientprogramming.fixedformat4j.annotation.Align;
import com.ancientprogramming.fixedformat4j.annotation.Field;
import com.ancientprogramming.fixedformat4j.annotation.FixedFormatPattern;
import com.ancientprogramming.fixedformat4j.annotation.Record;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ly.generalassemb.de.american.express.ingress.formatter.LocalDateTimeFormatter;

import java.time.LocalDateTime;

/**
 * Created by davidashirov on 12/4/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "RECORD_TYPE",
        "DATE",
        "TIME",
        "ID",
        "NAME",
        "VERSION_CONTROL_NUMBER"
})
@Record(length = 440)
public class Header {

    @JsonProperty("RECORD_TYPE")
    
    @javax.validation.constraints.NotNull
    private String recordType;

    @Field(offset=1,length=6,align= Align.LEFT)        //  getRecordType
    public String getRecordType() {
        return recordType;
    }
    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    @JsonProperty("DATE")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    
    @javax.validation.constraints.NotNull
    private LocalDateTime date;

    @FixedFormatPattern("yyyyMMddHHmm")
    @Field(offset=7,length=12,align=Align.LEFT,formatter = LocalDateTimeFormatter.class)        //  getDate
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }


    @JsonProperty("ID")
    
    @javax.validation.constraints.NotNull
    private String id;

    @Field(offset=19,length=6,align=Align.LEFT,paddingChar = ' ')        //  getId
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("NAME")
    
    @javax.validation.constraints.NotNull
    private String name;

    @Field(offset=25,length=19,align=Align.LEFT,paddingChar = ' ')        //  getName
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("VERSION_CONTROL_NUMBER")
    
    @javax.validation.constraints.NotNull
    private String versionControlNumber;

    @Field(offset=44,length=4,align=Align.LEFT,paddingChar = ' ')        //  getVersionControlNumber
    public String getVersionControlNumber() {
        return versionControlNumber;
    }
    public void setVersionControlNumber(String versionControlNumber) {
        this.versionControlNumber = versionControlNumber;
    }

}
