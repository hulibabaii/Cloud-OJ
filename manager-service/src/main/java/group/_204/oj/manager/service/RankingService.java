package group._204.oj.manager.service;

import group._204.oj.manager.dao.RankingDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RankingService {

    @Resource
    private RankingDao rankingDao;

    public List<List<?>> getRanking(int page, int limit) {
        return rankingDao.getRanking((page - 1) * limit, limit);
    }

    public List<List<?>> getContestRanking(int contestId, int page, int limit) {
        return rankingDao.getContestRanking(contestId, (page - 1) * limit, limit);
    }
}
