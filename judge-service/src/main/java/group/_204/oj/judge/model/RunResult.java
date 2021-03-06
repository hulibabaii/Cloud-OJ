package group._204.oj.judge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunResult {
    private Integer status;
    private Long timeUsed;
    private Long memUsed;

    @JsonIgnore
    private String stdout;
}
