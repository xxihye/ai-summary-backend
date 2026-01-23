package online.xxihye.summary.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateJobReq {

    @NotBlank
    private String text;

    private String model;

    public String getText() { return text; }
    public String getModel() { return model; }
}
