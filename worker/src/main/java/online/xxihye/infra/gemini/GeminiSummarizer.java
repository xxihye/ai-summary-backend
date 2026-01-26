package online.xxihye.infra.gemini;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import online.xxihye.summary.summarizer.Summarizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GeminiSummarizer implements Summarizer {
    private final Client client;
    private final String model;

    public GeminiSummarizer(Client client, @Value("${gemini.model}") String model) {
        this.client = client;
        this.model = model;
    }

    public String getModelName(){
        return model;
    }

    public String summarize(String text) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("text is empty");
        }

        Content content = Content.fromParts(
            Part.fromText("""
                You are a professional summarization assistant.
                
                Please summarize the following text with these requirements:
                - Output language: Korean
                - Length: 5 to 8 bullet points
                - Focus only on key ideas and important facts
                - Do NOT add assumptions or information not present in the text
                - Keep the summary concise and clear
                
                Text to summarize:
                """ + text)
        );

        //gemini 응답
        GenerateContentResponse response = client.models.generateContent(model, content, null);
        String out = response.text();

        if (!StringUtils.hasText(out)) {
            throw new IllegalStateException("Empty response from model");
        }

        return out.trim();
    }

}
