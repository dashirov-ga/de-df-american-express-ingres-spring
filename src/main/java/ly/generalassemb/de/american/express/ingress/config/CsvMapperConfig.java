package ly.generalassemb.de.american.express.ingress.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CsvMapperConfig {
    @Bean
    public CsvMapper csvMapper(){
        CsvMapper csvMapper= new CsvMapper();
        csvMapper.registerModule(new JavaTimeModule());
        csvMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return csvMapper;
    }
}
