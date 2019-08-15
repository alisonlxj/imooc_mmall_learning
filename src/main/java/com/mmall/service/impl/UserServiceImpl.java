package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 注入 bean 的名字 是接口名 首字母小写
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int result = userMapper.checkUsername(username);
        if(result == 0){
            return ServerResponse.createByErrorMsg("用户名不存在");
        }

        //todo 密码MD5加密

        User user = userMapper.selectLogin(username, password);
        if(user == null){
            return ServerResponse.createByErrorMsg("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> valid = checkValid(user.getUsername(), Const.USERNAME);
        if(!valid.isSuccess()){
            return valid;
        }
        valid = checkValid(user.getEmail(),Const.EMAIL);
        if(!valid.isSuccess()){
            return valid;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int result = userMapper.insertSelective(user);
        if(result == 0 ){
            return ServerResponse.createByErrorMsg("注册失败");
        }
        return ServerResponse.createBySuccessMsg("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if(StringUtils.isNotBlank(type)){
            if(Const.USERNAME.equals(type)){
                int record = userMapper.checkUsername(str);
                if(record > 0){
                    return ServerResponse.createByErrorMsg("已经存在用户");
                }
            }else if(Const.EMAIL.equals(type)){
                int record = userMapper.checkEmail(str);
                if(record > 0){
                    return ServerResponse.createByErrorMsg("邮箱已经存在");
                }
            }else{
                return ServerResponse.createByErrorMsg("type类型有误");
            }
        }else{
            return ServerResponse.createByErrorMsg("参数校验错误");
        }

        return ServerResponse.createBySuccessMsg("参数校验成功");
    }
}
