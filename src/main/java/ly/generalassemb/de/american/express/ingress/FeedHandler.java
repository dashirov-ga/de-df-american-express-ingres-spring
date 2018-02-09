package ly.generalassemb.de.american.express.ingress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "ly.generalassemb.de.american.express.ingress.config")
@SpringBootApplication // same as @Configuration @EnableAutoConfiguration @ComponentScan
public class FeedHandler {
    public static void main(String[] args){
        SpringApplication.run(FeedHandler.class, args);
    }


}
