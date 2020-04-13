package top.cloudli.judgeservice.model;

import lombok.Data;

@Data
public class SourceCode {
    private int codeId;
    private String solutionId;
    private String code;

    public SourceCode(String solutionId, String code) {
        this.solutionId = solutionId;
        this.code = code;
    }
}
