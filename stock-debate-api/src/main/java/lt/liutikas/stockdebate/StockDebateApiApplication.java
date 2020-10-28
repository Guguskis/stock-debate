package lt.liutikas.stockdebate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
public class StockDebateApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockDebateApiApplication.class, args);
    }

}