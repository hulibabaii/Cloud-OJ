package group._204.oj.judge.dao;

import group._204.oj.judge.model.Runtime;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RuntimeDao {
    int add(Runtime runtime);
    int update(Runtime runtime);
    Long getTimeout(int problemId);
}
