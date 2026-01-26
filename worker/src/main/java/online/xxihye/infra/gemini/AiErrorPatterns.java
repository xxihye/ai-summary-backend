package online.xxihye.infra.gemini;

import java.util.List;

public class AiErrorPatterns {

    private AiErrorPatterns() {}

    public static final List<String> RATE_LIMIT = List.of("429", "RESOURCE_EXHAUSTED");
    public static final List<String> TIMEOUT = List.of("timeout", "timed out", "deadline");
    public static final List<String> UNAVAILABLE = List.of("500", "503", "UNAVAILABLE");
    public static final List<String> BAD_REQUEST = List.of("400", "INVALID_ARGUMENT");
}
