package online.xxihye.infra.genai;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class GenAiConfig {

    @Bean
    public Client getAiClient(@Value("${genai.api-key:}") String apiKey) {
        if(!StringUtils.hasText(apiKey)){
            throw new IllegalStateException("genai.api-key is empty. Set GOOGLE_API_KEY env var or genai.api-key.");
        }

        return Client.builder()
                     .apiKey(apiKey)
                     .build();
    }
}
