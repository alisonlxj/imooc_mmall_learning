package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String username);

    int checkEmail(String email);

    /**
     * myabtis在传多个参数时，需要使用 @param 注解，xml中对应使用注解声明的名称
     * xml中的parameterType使用 "map"，标识传如多个参数
     * @param username
     * @param password
     * @return
     */
    User selectLogin(@Param("username") String username, @Param("password") String password);


    String selectQuestionByUsername(String username);

    // 返回查找到的记录条数
    int selectAnswerByUsernameAndQuestion(@Param("username")String username, @Param("question")String question, @Param("answer") String answer);

    // 更新密码
    int updatePasswordByUsername(@Param("username") String username, @Param("passwordNew") String passwordNew);

    // 根据用户id 校验密码
    int checkPassword(@Param("password") String password, @Param("userId") Integer userId);

    // 内部sql是检查 有没有 其他用户是这个email的
    int checkEmailByUserid(@Param("email") String email, @Param("id") Integer id);
}