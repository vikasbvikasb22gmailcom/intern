package com.codearena.dto;

import com.codearena.model.Submission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubmissionRequest {

    @NotNull(message = "Problem ID is required")
    private Long problemId;

    @NotBlank(message = "Code is required")
    private String code;

    private Submission.Language language = Submission.Language.JAVA;
}
