package com.gravel.pneumonia.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * @ClassName MethodExeTimeAop
 * @Description: 方法执行时间，日志处理AOP
 * @Author gravel
 * @Date 2020/1/26
 * @Version V1.0
 **/
@Slf4j
@Aspect
@Component
public class MethodExeTimeAop {

    /**
     * 拦截task包下面所有的方法
     */
    public static final String POINT = "execution (* com.gravel.pneumonia.task..*.*(..))";

    /**
     * 统计方法执行耗时Around环绕通知
     *
     * @param joinPoint
     * @return
     */
    @Around(POINT)
    public Object timeAround(ProceedingJoinPoint joinPoint) {
        // 定义返回对象、得到方法需要的参数
        Object obj = null;
        Object[] args = joinPoint.getArgs();
        // 开始时间
        long startTime = System.currentTimeMillis();
        try {
            obj = joinPoint.proceed(args);
        } catch (Throwable e) {
            log.error("统计某方法执行耗时环绕通知出错", e);
        }
        // 结束时间
        long endTime = System.currentTimeMillis();

        // 获取执行的方法名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();

        // 打印耗时的信息
        this.printExecTime(methodName, startTime, endTime);

        return obj;
    }

    /**
     * 打印方法执行耗时的信息
     *
     * @param methodName
     * @param startTime
     * @param endTime
     */
    private void printExecTime(String methodName, long startTime, long endTime) {
        long diffTime = endTime - startTime;
        log.info(methodName + " 执行完成 :" + diffTime + " :ms");
    }
}
