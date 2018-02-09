import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

// @SpringBootApplication // same as @Configuration @EnableAutoConfiguration @ComponentScan
@ComponentScan(basePackages = "ly.generalassemb.de.american.express.ingress.config")
public class TestApp {
    public static void main(String[] args){
        SpringApplication.run(TestApp.class, args);
    }

}
