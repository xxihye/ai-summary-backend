package online.xxihye.infra.gemini;

import com.google.genai.Client;
import com.google.genai.errors.ApiException;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import online.xxihye.summary.domain.JobErrorCode;
import online.xxihye.summary.summarizer.Summarizer;
import online.xxihye.worker.exception.AiProcessException;
import online.xxihye.worker.exception.InvalidInputException;
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
            throw new InvalidInputException();
        }

        try{
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
            return out.trim();

        }catch(ApiException e){
            JobErrorCode errorCode = mapErrorCode(e.code());
            throw new AiProcessException(errorCode, e.getMessage());
        }catch(GenAiIOException e){
            throw new AiProcessException(JobErrorCode.AI_NETWORK_ERROR, e.getMessage());
        }
    }

    private JobErrorCode mapErrorCode(int statusCode) {
        return switch (statusCode) {
            case 400 -> JobErrorCode.AI_BAD_REQUEST;
            case 401 -> JobErrorCode.AI_UNAUTHORIZED;
            case 403 -> JobErrorCode.AI_PERMISSION_DENIED;
            case 404 -> JobErrorCode.AI_NOT_FOUND;
            case 429 -> JobErrorCode.AI_RATE_LIMITED;
            case 500 -> JobErrorCode.AI_INTERNAL_ERROR;
            case 503 -> JobErrorCode.AI_SERVICE_UNAVAILABLE;
            case 504 -> JobErrorCode.AI_TIMEOUT;
            default -> JobErrorCode.AI_UNKNOWN_ERROR;
        };
    }
}
