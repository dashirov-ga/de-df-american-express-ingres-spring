package ly.generalassemb.de.american.express.ingress.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class AmexFileTransformer {
    @Autowired
    CsvMapper csvMapper;

    @Autowired
    ObjectMapper jsonMapper;

    @Transformer(inputChannel = "sftpChannel", outputChannel = "fileAggregator")
    public FixedWidthDataFile transform(File aFile) throws Exception {
        FixedWidthDataFile parsed =  FixedWidthDataFileFactory.parse(aFile);
        parsed.setCsvMapper(csvMapper);
        parsed.setJsonMapper(jsonMapper);
        parsed.setInputFile(aFile);
        return parsed;
    }


}



