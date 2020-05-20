package top.cloudli.managerservice.converter;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultConverter implements TypeHandler<String> {

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, String s, JdbcType jdbcType) {

    }

    @Override
    public String getResult(ResultSet resultSet, String s) throws SQLException {
        if (s.equals("language")) {
            int language = resultSet.getInt(s);
            switch (language) {
                case 0:
                    return "C";
                case 1:
                    return "C++";
                case 2:
                    return "Java";
                case 3:
                    return "Python";
                case 4:
                    return "Shell";
                case 5:
                    return "C Sharp";
            }
        } else if (s.equals("result")) {
            int result = resultSet.getInt(s);
            switch (result) {
                case 4:
                    return "编译错误";
                case 3:
                    return "答案错误";
                case 2:
                    return "部分通过";
                case 1:
                    return "时间超限";
                case 0:
                    return "完全正确";
            }
        }

        return resultSet.getString(s);
    }

    @Override
    public String getResult(ResultSet resultSet, int i) {
        return null;
    }

    @Override
    public String getResult(CallableStatement callableStatement, int i) {
        return null;
    }
}
