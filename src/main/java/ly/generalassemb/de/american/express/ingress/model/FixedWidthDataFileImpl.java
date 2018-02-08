package ly.generalassemb.de.american.express.ingress.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import java.io.File;

public abstract class FixedWidthDataFileImpl implements FixedWidthDataFile {
    private CsvMapper csvMapper;
    private ObjectMapper jsonMapper;
    private File inputFile;

    @Override
    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public File getInputFile() {
        return inputFile;
    }

    public CsvMapper getCsvMapper() {
        return csvMapper;
    }

    @Override
    public void setCsvMapper(CsvMapper csvMapper) {
        this.csvMapper = csvMapper;
    }

    public ObjectMapper getJsonMapper() {
        return jsonMapper;
    }

    @Override
    public void setJsonMapper(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }



}
