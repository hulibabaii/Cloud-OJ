package top.cloudli.managerservice.dao;

import org.apache.ibatis.annotations.Mapper;
import top.cloudli.managerservice.model.Problem;

import java.util.List;

@Mapper
public interface ProblemDao {
    long getCount();

    List<Problem> getAll(String userId, int start, int limit);

    long getEnableCount();

    List<Problem> getEnable(String userId, int start, int limit);

    Problem getSingle(int problemId);

    Problem getSingleEnable(int problemId);

    long getSearchCount(String title);

    List<Problem> search(String userId, String keyword, int start, int limit, boolean pro);

    int add(Problem problem);

    int update(Problem problem);

    int delete(int problemId);

    int updateEnable(int problemId, boolean enable);
}
