package ly.generalassemb.de.american.express.ingress.model;

import com.ancientprogramming.fixedformat4j.format.FixedFormatManager;
import com.ancientprogramming.fixedformat4j.format.impl.FixedFormatManagerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

public interface FixedWidthDataFile {
    FixedFormatManager manager = new FixedFormatManagerImpl();

     void setInputFile(File fixedWidthFile);
     static FixedFormatManager getManager() {
        return manager;
    }

    void setCsvMapper(CsvMapper csvMapper);

    void setJsonMapper(ObjectMapper objectMapper);

    Map<FixedWidthDataFileComponent, String> toLoadableComponents() throws Exception;
     FixedWidthDataFile parse(File fixedWidthDataFile) throws IOException, NotRegisteredException, ParseException;
}
