package com.codeyyt.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.codeyyt.gulimall.common.utils.HttpUtils;
import com.codeyyt.gulimall.member.entity.MemberLevelEntity;
import com.codeyyt.gulimall.member.exception.PhoneExistException;
import com.codeyyt.gulimall.member.exception.UsernameExistException;
import com.codeyyt.gulimall.member.service.MemberLevelService;
import com.codeyyt.gulimall.member.vo.MemberLoginVo;
import com.codeyyt.gulimall.member.vo.MemberRegistVo;
import com.codeyyt.gulimall.member.vo.WeiboMemberVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codeyyt.gulimall.common.utils.PageUtils;
import com.codeyyt.gulimall.common.utils.Query;

import com.codeyyt.gulimall.member.dao.MemberDao;
import com.codeyyt.gulimall.member.entity.MemberEntity;
import com.codeyyt.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo memberRegistVo) {
        MemberEntity memberEntity = new MemberEntity();

        // 设置默认等级
        MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        // 检查用户名和手机号是否唯一
        checkUsernameUnique(memberRegistVo.getUserName());
        checkMobileUnique(memberRegistVo.getPhone());

        memberEntity.setUsername(memberRegistVo.getUserName());
        memberEntity.setMobile(memberRegistVo.getPhone());
        memberEntity.setNickname(memberRegistVo.getUserName());

        // 密码加密存储
        memberEntity.setPassword(memberRegistVo.getPassword());
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(memberRegistVo.getPassword());
        memberEntity.setPassword(encode);

        this.baseMapper.insert(memberEntity);
    }

    @Override
    public void checkUsernameUnique(String name) throws UsernameExistException {
        Integer username = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", name));

        if(username > 0){
            throw new UsernameExistException();
        }
    }

    @Override
    public void checkMobileUnique(String mobile) throws PhoneExistException {
        Integer count= this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", mobile));
        if(count > 0){
            throw  new PhoneExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginAcct = vo.getLoginAcct();
        String password = vo.getPassword();

        // 数据库查询
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>()
                .eq("username", loginAcct).or().eq("mobile", loginAcct));
        if(entity == null){
            return null;
        }else{
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(password, passwordDb);
            if(matches){
                return entity;
            }else{
                return null;
            }
        }
    }

    @Override
    public MemberEntity oauthLogin(WeiboMemberVo vo) {
//        String uid = vo.getUid();
//        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
//        if(entity != null){
//            MemberEntity update = new MemberEntity();
//            update.setId(entity.getId());
//            update.setAccessToken(vo.getAccess_token());
//            update.setExpiresIn(vo.getExpires_in());
//            this.baseMapper.updateById(update);
//
//            entity.setAccessToken(vo.getAccess_token());
//            entity.setExpiresIn(vo.getExpires_in());
//            return entity;
//        }else{
//            MemberEntity regist = new MemberEntity();
//            try {
//                // TODO 想微博获取用户信息
//                HashMap<String, String> map = new HashMap<>();
//                map.put("access_token",vo.getAccess_token());
//                map.put("uid",vo.getUid());
//                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), map);
//                if(response.getStatusLine().getStatusCode() == 200){
//                    String json = EntityUtils.toString(response.getEntity());
//                    JSONObject jsonObject = JSON.parseObject(json);
//                    // 昵称
//                    String name = jsonObject.getString("name");
//                    regist.setNickname(name);
//                    //性别
//                    String gender = jsonObject.getString("gender");
//                    regist.setGender("m".equals(gender)?1:0);
//                }
//            }catch (Exception e){}
//            regist.setSocialUid(vo.getUid());
//            regist.setAccessToken(vo.getAccess_token());
//            regist.setExpiresIn(vo.getExpires_in());
//            this.baseMapper.insert(regist);
//            return regist;
//        }

        return null;
    }


}