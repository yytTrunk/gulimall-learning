package com.codeyyt.gulimall.member.vo;

import lombok.Data;

@Data
public class WeiboMemberVo {
    private String access_token;
    private String remind_in;
    private Long expires_in;
    private String uid;
    private String isRealName;
}
