package ly.generalassemb.de.american.express.ingress.model.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import ly.generalassemb.de.american.express.ingress.model.EMCBK.Detail;
import ly.generalassemb.de.american.express.ingress.model.EMCBK.Header;
import ly.generalassemb.de.american.express.ingress.model.EMCBK.Trailer;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileImpl;
import ly.generalassemb.de.american.express.ingress.model.NotRegisteredException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EMCBKFixedWidthDataFile extends FixedWidthDataFileImpl {

    private Header header;
    private List<Detail> details;
    private Trailer trailer;

    public void put(Trailer t) {
        this.trailer = t;
    }

    public EMCBKFixedWidthDataFile() {
    }
    public EMCBKFixedWidthDataFile(File file) throws Exception {
        details = new ArrayList<>();
        parse(file);
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }

    public Trailer getTrailer() {
        return trailer;
    }

    public void setTrailer(Trailer trailer) {
        this.trailer = trailer;
    }



    @Override
    public FixedWidthDataFile parse(File fixedWidthDataFile) throws IOException, NotRegisteredException {
        BufferedReader reader = new BufferedReader(new FileReader(fixedWidthDataFile));
        String line;
        int lineNo = 0;
        while ((line = reader.readLine()) != null) {
            ++lineNo;
            String typeIndicator = line.substring(0, 1);
            switch (typeIndicator) {
                case "H":
                    this.header = manager.load(Header.class, line);
                    break;
                case "T":
                    this.trailer =manager.load(Trailer.class, line);
                    break;
                case "D":
                    this.details.add(manager.load(Detail.class, line));
                    break;
                default:
                    throw new NotRegisteredException("Don't know how to parse this line.");
            }
        }
        return this;
    }

    @Override
    public Map<FixedWidthDataFileComponent, String> toLoadableComponents() throws Exception {
        Map<FixedWidthDataFileComponent,String> output = new HashMap<>();
        CsvMapper csvMapper = getCsvMapper();
        ObjectMapper jsonMapper = getJsonMapper();
        // processed file is a json serialized "this"
        output.put(FixedWidthDataFileComponent.EMCBK_JSON_OBJECT,jsonMapper.writeValueAsString(this));
        output.put(FixedWidthDataFileComponent.EMCBK_CSV_HEADER_COMPONENT,csvMapper.writer(csvMapper.schemaFor(Header.class).withHeader()).writeValueAsString(this.header));
        output.put(FixedWidthDataFileComponent.EMCBK_CSV_TRAILER_COMPONENT,csvMapper.writer(csvMapper.schemaFor(Trailer.class).withHeader()).writeValueAsString(this.trailer));
        output.put(FixedWidthDataFileComponent.EMCBK_CSV_CHARGEBACK_COMPONENT,csvMapper.writer(csvMapper.schemaFor(Detail.class).withHeader()).writeValueAsString(this.details));
        output.put(FixedWidthDataFileComponent.EMCBK_FIXED_WIDTH_OBJECT,new String(Files.readAllBytes(getInputFile().toPath())));
        return output;
    }

}
