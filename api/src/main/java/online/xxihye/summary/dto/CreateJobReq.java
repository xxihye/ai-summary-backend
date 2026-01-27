package online.xxihye.summary.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateJobReq {

    private String userId;

    @NotBlank
    private String text;


    public String getUserId() {return this.userId;}
    public String getText() { return this.text; }
}
