package online.xxihye.infra.gemini;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class GenAiConfig {

    @Bean
    public Client getAiClient(@Value("${gemini.api-key}") String apiKey) {
        if(!StringUtils.hasText(apiKey)){
            throw new IllegalStateException("gemini.api-key is empty. Set gemini.api-key env var");
        }

        return Client.builder()
                     .apiKey(apiKey)
                     .build();
    }
}
