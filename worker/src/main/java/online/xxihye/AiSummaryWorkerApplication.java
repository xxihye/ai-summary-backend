package online.xxihye;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class AiSummaryWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiSummaryWorkerApplication.class, args);
    }
}
