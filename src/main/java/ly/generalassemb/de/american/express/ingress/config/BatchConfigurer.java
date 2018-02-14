package ly.generalassemb.de.american.express.ingress.config;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.stereotype.Component;

@Component
public class BatchConfigurer extends DefaultBatchConfigurer {
    // must be  empty, @Component annotated
    // https://stackoverflow.com/questions/25540502/use-of-multiple-datasources-in-spring-batch
}
