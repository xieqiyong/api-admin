package com.hz.api.admin.stream.util;

import java.util.Random;

public class IdWorkerUtils {

    public static Long getIdWorker(){
        Random random = new Random();
        String randomStr = String.valueOf(random.nextInt(9999));
        String timeStamp = String.valueOf(System.currentTimeMillis());
        return Long.parseLong(randomStr + timeStamp);
    }
}
