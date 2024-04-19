/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyUpdateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.audit.AuditLog;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Writer for audit logs.
 */
@Slf4j
@Aspect
@Component
public class AuditLogWriter {

    private static final String DEFAULT_GET_CSP_METHOD_NAME = "getCspFromRequestUri";

    @Resource
    private PluginManager pluginManager;
    @Resource
    private UserServiceHelper userServiceHelper;
    @Resource
    private GetCspInfoFromRequest getCspInfoFromRequest;

    /**
     * Pointcut for all controller methods.
     */
    @Pointcut("execution(* org.eclipse.xpanse.api.controllers.*.*(..))")
    public void controllerMethods() {
    }

    /**
     * Validate request parameters.
     *
     * @param joinPoint The join point.
     */
    @Around("controllerMethods()")
    public Object auditRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        Csp csp = getCsp(joinPoint);
        log.info("Get csp from request attributes: {}", csp);
        Object result = joinPoint.proceed();
        if (Objects.nonNull(csp)) {
            HttpServletRequest request =
                    ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                            .getRequest();

            OrchestratorPlugin orchestratorPlugin = pluginManager.getOrchestratorPlugin(csp);
            orchestratorPlugin.auditApiRequest(
                    getAuditLog(csp, joinPoint.getSignature().getName(), request.getMethod(),
                            joinPoint.getArgs(), result));
        }
        return result;
    }

    private Csp getCsp(ProceedingJoinPoint joinPoint) {
        if (pluginManager.getPluginsMap().size() == 1) {
            return pluginManager.getPluginsMap().keySet().iterator().next();
        }
        Method requestMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        AuditApiRequest auditApiRequest = requestMethod.getAnnotation(AuditApiRequest.class);
        if (Objects.isNull(auditApiRequest) || !auditApiRequest.enabled()) {
            return null;
        }
        return getCspFromMethodInfo(auditApiRequest, joinPoint.getArgs());
    }

    private Csp getCspFromMethodInfo(AuditApiRequest auditApiRequest, Object[] args) {
        String methodName = auditApiRequest.methodName();
        if (DEFAULT_GET_CSP_METHOD_NAME.equals(methodName)) {
            return getCspFromRequestParams(args);
        }
        try {
            Class<?> clazz = auditApiRequest.clazz();
            Class<?>[] paramTypes = auditApiRequest.paramTypes();
            Method method = clazz.getMethod(methodName, paramTypes);
            int[] paramIndexes = auditApiRequest.paramIndexes();
            Object[] paramValues = new Object[paramIndexes.length];
            for (int i = 0; i < paramIndexes.length; i++) {
                paramValues[i] = args[paramIndexes[i]];
            }
            Object result = method.invoke(getCspInfoFromRequest, paramValues);
            if (result instanceof Csp csp) {
                return csp;
            }
        } catch (Exception e) {
            log.error("Get csp with annotation @AuditApiRequest error.", e);
        }
        return null;
    }

    private Csp getCspFromRequestParams(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Ocl ocl) {
                return ocl.getCloudServiceProvider().getName();
            } else if (arg instanceof DeployRequest deployRequest) {
                return deployRequest.getCsp();
            } else if (arg instanceof MigrateRequest migrateRequest) {
                return migrateRequest.getCsp();
            } else if (arg instanceof CreateCredential createCredential) {
                return createCredential.getCsp();
            } else if (arg instanceof UserPolicyCreateRequest userPolicyCreateRequest) {
                return userPolicyCreateRequest.getCsp();
            } else if (arg instanceof UserPolicyUpdateRequest userPolicyUpdateRequest) {
                return userPolicyUpdateRequest.getCsp();
            } else if (arg instanceof Csp csp) {
                return csp;
            } else {
                return null;
            }
        }
        return null;
    }

    private AuditLog getAuditLog(Csp csp, String methodName, String methodType, Object[] args,
            Object result) {
        AuditLog auditLog = new AuditLog();
        auditLog.setMethodName(methodName);
        auditLog.setMethodType(methodType);
        auditLog.setParams(args);
        auditLog.setResult(result);
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        auditLog.setUrl(String.valueOf(request.getRequestURL()));
        auditLog.setCsp(csp);
        auditLog.setOperatingTime(OffsetDateTime.now());
        auditLog.setUserId(userServiceHelper.getCurrentUserId());
        return auditLog;
    }
}
