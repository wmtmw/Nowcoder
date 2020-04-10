package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
public interface LoginTicketMapper {
    //插入
    @Insert(
            {"insert into login_ticket(user_id,ticket,status,expired) ",
                    "values(#{userId},#{ticket},#{status},#{expired})"}
    )
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);
    //查询
    @Select(
            {
                    "select id,user_id,ticket,status,expired from login_ticket where ticket = #{ticket}"
            }
    )
    LoginTicket selectLoginTicket(String ticket);
    //更新
    @Update(
            {
                    "update login_ticket set status = #{status} where ticket = #{ticket}"
            }
    )
    int updateLoginTicket(String ticket,int status);
}
