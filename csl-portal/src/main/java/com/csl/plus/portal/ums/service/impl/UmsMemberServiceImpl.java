package com.csl.plus.portal.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.csl.plus.common.utils.CommonCodeConst;
import com.csl.plus.exception.ApiMallPlusException;
import com.csl.plus.portal.config.WxAppletProperties;
import com.csl.plus.portal.constant.RedisKey;
import com.csl.plus.portal.single.ApiBaseAction;
import com.csl.plus.portal.ums.service.IUmsMemberService;
import com.csl.plus.portal.ums.service.RedisService;
import com.csl.plus.portal.util.*;
import com.csl.plus.portal.vo.MemberDetails;
import com.csl.plus.sys.mapper.SysAreaMapper;
import com.csl.plus.ums.entity.UmsMember;
import com.csl.plus.ums.entity.UmsMemberPermission;
import com.csl.plus.ums.mapper.UmsMemberMapper;
import com.csl.plus.ums.mapper.UmsMemberMemberTagRelationMapper;
import com.csl.plus.ums.mapper.UmsMemberPermissionMapper;
import com.csl.plus.utils.CommonResult;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 会员表 服务实现类
 * </p>
 *
 * @since 2019-04-19
 */
@Service
public class UmsMemberServiceImpl extends ServiceImpl<UmsMemberMapper, UmsMember> implements IUmsMemberService {

    @Resource
    private UmsMemberMapper memberMapper;

    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private RedisService redisService;
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsMemberServiceImpl.class);
    /*
     * @Resource private AuthenticationManager authenticationManager;
     */
    @Resource
    private UserDetailsService userDetailsService;

    @Resource
    private SysAreaMapper areaMapper;
    @Resource
    private JwtTokenUtil jwtTokenUtil;
    @Value("${redis.key.prefix.authCode}")
    private String REDIS_KEY_PREFIX_AUTH_CODE;
    @Value("${authCode.expire.seconds}")
    private Long AUTH_CODE_EXPIRE_SECONDS;
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Resource
    private UmsMemberMemberTagRelationMapper umsMemberMemberTagRelationMapper;

    @Resource
    private UmsMemberPermissionMapper umsMemberPermissionMapper;

    @Override
    public UmsMember getByUsername(String username) {
        UmsMember umsMember = new UmsMember();
        umsMember.setUsername(username);
        return memberMapper.getByUsername(username);
    }

    @Override
    public UmsMember getById(Long id) {
        return memberMapper.selectById(id);
    }

    @Override
    public CommonResult register(String username, String password, String telephone, String authCode) {

        // 没有该用户进行添加操作
        UmsMember umsMember = new UmsMember();
        umsMember.setUsername(username);
        umsMember.setPhone(telephone);
        umsMember.setPassword(password);
        this.register(umsMember);
        return new CommonResult().success("注册成功", null);
    }

    @Override
    public CommonResult register(UmsMember user) {
        // 验证验证码
        /*
         * if (!verifyAuthCode(authCode, telephone)) { return new
         * CommonResult().failed("验证码错误"); }
         */
        if (!user.getPassword().equals(user.getConfimpassword())) {
            return new CommonResult().failed("密码不一致");
        }
        // 查询是否已有该用户
        UmsMember umsMembers = getByUsername(user.getUsername());
        if (umsMembers != null) {
            return new CommonResult().failed("该用户已经存在");
        }
        // 没有该用户进行添加操作
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(new Date());
        // default in review status
        user.setStatus(CommonCodeConst.STATUS_REVIEW);
        memberMapper.insert(user);
        user.setPassword(null);
        return new CommonResult().success("注册成功", user);
    }

    @Override
    public Object activate(UmsMember user) {
        // 验证邀请码
        if (!verifyInviteCode(user.getInvitecode(), user.getPhone(), user.getPassword())) {
            return new
                    CommonResult().failed("邀请码错误");
        }

        // 查询是否已有该用户
        UmsMember umsMembers = getByInvitecode(user.getInvitecode());
        if (umsMembers == null) {
            return new CommonResult().failed("该用户不存在");
        }
        // 没有该用户进行添加操作
        umsMembers.setPassword(passwordEncoder.encode(user.getPassword()));
        umsMembers.setCreateTime(new Date());
        // default in review status
        umsMembers.setStatus(CommonCodeConst.STATUS_ACTIVE);
        memberMapper.updateById(umsMembers);
        umsMembers.setPassword(null);
        return new CommonResult().success("激活成功", umsMembers);
    }

    private UmsMember getByInvitecode(String invitecode) {
        return memberMapper.getByInvitecode(invitecode);
    }

    /**
     * 验证邀请码和手机号
     */
    private boolean verifyInviteCode(String invitecode, String phone, String password) {
        return !StringUtils.isEmpty(invitecode) && !StringUtils.isEmpty(phone) && !StringUtils.isEmpty(password);
    }

    @Override
    public CommonResult generateAuthCode(String telephone) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        // 验证码绑定手机号并存储到redis
        redisService.set(REDIS_KEY_PREFIX_AUTH_CODE + telephone, sb.toString());
        redisService.expire(REDIS_KEY_PREFIX_AUTH_CODE + telephone, AUTH_CODE_EXPIRE_SECONDS);
        return new CommonResult().success("获取验证码成功", sb.toString());
    }

    @Override
    public CommonResult updatePassword(String telephone, String password, String authCode) {
        UmsMember example = new UmsMember();
        example.setPhone(telephone);
        UmsMember member = memberMapper.selectOne(new QueryWrapper<>(example));
        if (member == null) {
            return new CommonResult().failed("该账号不存在");
        }
        // 验证验证码
        if (!verifyAuthCode(authCode, telephone)) {
            return new CommonResult().failed("验证码错误");
        }

        member.setPassword(passwordEncoder.encode(password));
        memberMapper.updateById(member);
        return new CommonResult().success("密码修改成功", null);
    }

    @Override
    public UmsMember getCurrentMember() {
        try {
            SecurityContext ctx = SecurityContextHolder.getContext();
            Authentication auth = ctx.getAuthentication();
            MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();
            return memberDetails.getUmsMember();
        } catch (Exception e) {
            return new UmsMember();
        }
    }

    @Override
    public void updateIntegration(Long id, Integer integration) {
        UmsMember record = new UmsMember();
        record.setId(id);
//        record.setIntegration(integration);
        memberMapper.updateById(record);
    }

    // 对输入的验证码进行校验
    private boolean verifyAuthCode(String authCode, String telephone) {
        if (StringUtils.isEmpty(authCode)) {
            return false;
        }
        String realAuthCode = redisService.get(REDIS_KEY_PREFIX_AUTH_CODE + telephone);
        return authCode.equals(realAuthCode);
    }

    @Override
    public UmsMember queryByOpenId(String openId) {
        UmsMember queryO = new UmsMember();
        queryO.setWeixinOpenid(openId);
        return memberMapper.selectOne(new QueryWrapper<>(queryO));
    }

    @Override
    public Object loginByWeixin(HttpServletRequest req) {
        try {
            String code = req.getParameter("code");
            if (StringUtils.isEmpty(code)) {
                System.out.println("code is empty");
            }
            String userInfos = req.getParameter("userInfo");

            String signature = req.getParameter("signature");

            Map<String, Object> me = JsonUtils.readJsonToMap(userInfos);
            if (null == me) {
                return ApiBaseAction.toResponsFail("登录失败");
            }

            Map<String, Object> resultObj = new HashMap<String, Object>();
            //
            // 获取openid
            String requestUrl = this.getWebAccess(code);// 通过自定义工具类组合出小程序需要的登录凭证 code

            JSONObject sessionData = CommonUtil.httpsRequest(requestUrl, "GET", null);

            if (null == sessionData || StringUtils.isEmpty(sessionData.getString("openid"))) {
                return ApiBaseAction.toResponsFail("登录失败");
            }
            // 验证用户信息完整性
            String sha1 = CommonUtil.getSha1(userInfos + sessionData.getString("session_key"));
            if (!signature.equals(sha1)) {
                return ApiBaseAction.toResponsFail("登录失败");
            }
            UmsMember userVo = this.queryByOpenId(sessionData.getString("openid"));
            String token = null;
            if (null == userVo) {
                UmsMember umsMember = new UmsMember();
                umsMember.setUsername("wxapplet" + CharUtil.getRandomString(12));
                umsMember.setSourceType(1);
                umsMember.setPassword(passwordEncoder.encode("123456"));
                umsMember.setCreateTime(new Date());
                umsMember.setStatus(CommonCodeConst.STATUS_ACTIVE);
//                umsMember.setBlance(new BigDecimal(0));
//                umsMember.setIntegration(0);
//                umsMember.setHistoryIntegration(0);
                umsMember.setWeixinOpenid(sessionData.getString("openid"));
                if (StringUtils.isEmpty(me.get("avatarUrl").toString())) {
                    // 会员头像(默认头像)
                    umsMember.setIcon("/upload/img/avatar/01.jpg");
                } else {
                    umsMember.setIcon(me.get("avatarUrl").toString());
                }
                // umsMember.setGender(Integer.parseInt(me.get("gender")));
                umsMember.setNickname(me.get("nickName").toString());

                memberMapper.insert(umsMember);
                token = jwtTokenUtil.generateToken(umsMember.getUsername());
                resultObj.put("userId", umsMember.getId());
            } else {
                token = jwtTokenUtil.generateToken(userVo.getUsername());
                resultObj.put("userId", userVo.getId());
            }

            if (StringUtils.isEmpty(token)) {
                return ApiBaseAction.toResponsFail("登录失败");
            }
            resultObj.put("tokenHead", tokenHead);
            resultObj.put("token", token);
            resultObj.put("userInfo", me);

            return ApiBaseAction.toResponsSuccess(resultObj);
        } catch (ApiMallPlusException e) {
            e.printStackTrace();
            return ApiBaseAction.toResponsFail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiBaseAction.toResponsFail(e.getMessage());
        }

    }

    @Override
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> tokenMap = new HashMap<>();
        String token = null;
        // 密码需要客户端加密后传递
//        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
//                passwordEncoder.encode(password));
        try {
            /*
             * Authentication authentication =
             * authenticationManager.authenticate(authenticationToken);
             * SecurityContextHolder.getContext().setAuthentication(authentication);
             * UserDetails userDetails = userDetailsService.loadUserByUsername(username);
             * UmsMember member = this.getByUsername(username); token =
             * jwtTokenUtil.generateToken(userDetails);
             */

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                throw new BadCredentialsException("密码不正确");
            }
            UmsMember member = this.getByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            token = jwtTokenUtil.generateToken(userDetails);
            tokenMap.put("userInfo", member);
        } catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }

        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);

        return tokenMap;

    }

    @Override
    public String refreshToken(String oldToken) {
        String token = oldToken.substring(tokenHead.length());
        if (jwtTokenUtil.canRefresh(token)) {
            return jwtTokenUtil.refreshToken(token);
        }
        return null;
    }

    @Autowired
    private WxAppletProperties wxAppletProperties;

    // 替换字符串
    public String getCode(String APPID, String REDIRECT_URI, String SCOPE) {
        return String.format(wxAppletProperties.getGetCode(), APPID, REDIRECT_URI, SCOPE);
    }

    // 替换字符串
    public String getWebAccess(String CODE) {

        return String.format(wxAppletProperties.getWebAccessTokenhttps(), wxAppletProperties.getAppId(),
                wxAppletProperties.getSecret(), CODE);
    }

    // 替换字符串
    public String getUserMessage(String access_token, String openid) {
        return String.format(wxAppletProperties.getUserMessage(), access_token, openid);
    }

    @Override
    public List<UmsMemberPermission> listMemberPerms(Long id) {
        if (!redisService.exists(String.format(RedisKey.PermisionListKey, id))) {
            List<UmsMemberPermission> list = umsMemberPermissionMapper.getUmsMemberPerms(id);
            String key = String.format(RedisKey.PermisionListKey, id);
            redisService.set(key, JsonUtil.objectToJson(list));
            return list;
        } else {
            return JsonUtil.jsonToList(redisService.get(String.format(RedisKey.PermisionListKey, id)), UmsMemberPermission.class);
        }

    }

}
