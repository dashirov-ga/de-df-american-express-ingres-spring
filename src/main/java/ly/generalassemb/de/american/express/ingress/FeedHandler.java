package ly.generalassemb.de.american.express.ingress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // same as @Configuration @EnableAutoConfiguration @ComponentScan
public class FeedHandler {
    public static void Main(String[] args){
        SpringApplication.run(FeedHandler.class, args);
    }


}
