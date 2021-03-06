package group._204.oj.judge.model;

import lombok.Data;

import java.sql.Date;

@Data
public class JudgeResult {
    private String solutionId;
    private int problemId;
    private String userId;
    private int language;
    private int state;
    private int result;
    private int compileState;
    private String compileInfo;
    private double passRate;
    private double score;
    private long time;
    private Date submitTime;
}
