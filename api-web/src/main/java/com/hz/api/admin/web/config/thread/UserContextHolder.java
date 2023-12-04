package com.hz.api.admin.web.config.thread;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.hz.api.admin.model.bo.UserInfoBO;

/**
 * @Description 租户上下文-用于保存当前执行线程用户信息-跨线程池传递
 * @Author liusu <xieqiyong66@gmail.com>
 * @Version v4.0.0-saas
 * @Date 2021/8/4
 */
public class UserContextHolder {

    private static TransmittableThreadLocal<UserInfoBO> CONTEXT = new TransmittableThreadLocal<>();

    public static void setContext(UserInfoBO userInfoBO){
        CONTEXT.set(userInfoBO);
    }

    public static UserInfoBO getAccountInfoDTO() {
        return CONTEXT.get();
    }

    public static Long getUserId() {
        return CONTEXT.get().getId();
    }

    public static void mock(UserInfoBO userInfoBO) {
        CONTEXT.set(userInfoBO);
    }

    public static void clear() {
        TransmittableThreadLocal.Transmitter.clear();
    }

}
