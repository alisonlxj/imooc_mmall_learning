package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
        User user = userMapper.selectLogin(username, MD5Util.MD5EncodeUtf8(password));
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
    // 校验用户或者邮箱是否已经存在
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

    @Override
    public ServerResponse<String> selectQuestions(String username){
        ServerResponse<String> valid = checkValid(username, Const.USERNAME);
        if(!valid.isSuccess()){
            return ServerResponse.createByErrorMsg("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMsg("找回密码的问题是空的");
    }

    @Override
    public ServerResponse<String> checkQuestionAnswer(String username, String question, String answer){
        int recordCount = userMapper.selectAnswerByUsernameAndQuestion(username, question, answer);
        if(recordCount > 0){
            // 返回token，设定缓存
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMsg("提示问题的答案不正确");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        // token blank valid
        if(!StringUtils.isNotBlank(forgetToken)){
            return ServerResponse.createByErrorMsg("token不可为空，请重新传递token");
        }
        // username valid
        if(checkValid(username,Const.USERNAME).isSuccess()){
            ServerResponse.createByErrorMsg("用户不存在");
        }
        // token exist valid
        String keyToken = TokenCache.TOKEN_PREFIX + username;
        String validToken = TokenCache.getKey(keyToken);
        if(StringUtils.isBlank(validToken)){
            ServerResponse.createByErrorMsg("token无效或已过期");
        }
        // forgettoken equals valid
        if(StringUtils.equals(forgetToken,validToken)){
            String encryptPasswordNew = MD5Util.MD5EncodeUtf8(passwordNew);
            int recordCount = userMapper.updatePasswordByUsername(username, encryptPasswordNew);
            if(recordCount > 0){
                return ServerResponse.createBySuccess("修改密码成功");
            }
        }
        else{
            return ServerResponse.createByErrorMsg("token错误，请重新获取修改密码的token");
        }
        return ServerResponse.createByErrorMsg("重设密码错误");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user){
        // 校验旧密码，防止横向越权
        // 可以两种校验形式：1.加密后的passwordOld 和 user.getPassword直接比较；2.查询数据库，进行加密后密码比较；推荐后者
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMsg("旧密码错误！");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int record_user = userMapper.updateByPrimaryKeySelective(user);
        if(record_user > 0){
            return ServerResponse.createBySuccessMsg("更新密码成功");
        }
        return ServerResponse.createByErrorMsg("更新密码失败，未找到用户");
    }

    @Override
    public ServerResponse<User> updateInfomation(User user){
        // username 是不能被更新的
        // email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.
        // 即如果要更新email，则该email不能是其他用户已经使用的
        int resultCount = userMapper.checkEmailByUserid(user.getEmail(), user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMsg("email已存在,请更换email再尝试更新");
        }
        // 更新对象 要专门构建一下，不能直接使用 user---- 因为要有选择性的更新，所以要专门处理下，否则就是全更新user了
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateRecord = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateRecord > 0){
            return ServerResponse.createBySuccess("更新信息成功", updateUser);
        }
        return ServerResponse.createByErrorMsg("更新信息失败");
    }


    @Override
    public ServerResponse<User> getInfomation(Integer userId){
        User recordUser = userMapper.selectByPrimaryKey(userId);
        if(recordUser != null){
            // 返回到前端，一定置空 密码字段
            recordUser.setPassword(StringUtils.EMPTY);
            return ServerResponse.createBySuccess("获取信息成功", recordUser);
        }
        return ServerResponse.createByErrorMsg("用户不存在");
    }


    @Override
    public ServerResponse<User> getUserById(int userId){
        User recUser = userMapper.selectByPrimaryKey(userId);
        if(recUser != null){
            recUser.setPassword(StringUtils.EMPTY);
            return ServerResponse.createBySuccess("成功查询到用户", recUser);
        }
        return ServerResponse.createByErrorMsg("未查找到用户记录");
    }


    /**
     * 检验是否为管理员
     * @param user
     * @return
     */
    @Override
    public ServerResponse<User> checkAdminRole(User user){
        if(user != null && user.getRole() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

}
