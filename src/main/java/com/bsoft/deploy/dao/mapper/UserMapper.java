package com.bsoft.deploy.dao.mapper;

import com.bsoft.deploy.dao.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户
 * Created on 2018/8/6.
 *
 * @author yangl
 */
@Mapper
public interface UserMapper {

    @Select("select id,name,password,status,createDt,loginName from base_user where loginName=#{loginName}")
    User findUser(String loginName);
}
