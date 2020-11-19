package lt.liutikas.stockdebate.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.time.Clock;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
public class SwaggerConfig {

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(regex("/api/.*"))
                .apis(RequestHandlerSelectors.basePackage("lt.liutikas"))
                .build();
    }

    @Bean
    @Qualifier("discussion")
    public RestTemplate getRestTemplate() {
        return new RestTemplateBuilder()
                .rootUri("http://localhost:8084")
                .build();
    }

    @Bean
    public Clock getClock() {
        return Clock.systemUTC();
    }
}