package online.xxihye.summary.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateJobReq {

    @NotBlank
    private String text;

    public String getText() { return this.text; }
}
