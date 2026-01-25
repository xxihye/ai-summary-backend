package online.xxihye.summary.summarizer;

public interface Summarizer {
    String summarize(String inputText);

    String getModelName();
}
