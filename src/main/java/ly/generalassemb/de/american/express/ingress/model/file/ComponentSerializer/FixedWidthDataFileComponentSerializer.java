package ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import java.util.List;

public  interface FixedWidthDataFileComponentSerializer<T> {
      List<SerializedComponent<String>> getComponents(T pojo, CsvMapper csvMapper, ObjectMapper jsonMapper) throws Exception;
}
