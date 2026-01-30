package com.example.Commerce.aspects;

import com.example.Commerce.config.GraphQLRequiresRole;
import com.example.Commerce.enums.UserRole;
import com.example.Commerce.errorhandlers.UnauthorizedException;
import graphql.schema.DataFetchingEnvironment;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class GraphQLRoleAspect {

    @Around("@annotation(com.example.Commerce.config.GraphQLRequiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        GraphQLRequiresRole annotation = signature.getMethod().getAnnotation(GraphQLRequiresRole.class);
        
        DataFetchingEnvironment env = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof DataFetchingEnvironment) {
                env = (DataFetchingEnvironment) arg;
                break;
            }
        }
        
        if (env == null) {
            throw new UnauthorizedException("Authentication required");
        }
        
        String userRole = env.getGraphQlContext().get("userRole");
        if (userRole == null) {
            throw new UnauthorizedException("Authentication required");
        }
        
        for (UserRole requiredRole : annotation.value()) {
            if (requiredRole.name().equals(userRole)) {
                return joinPoint.proceed();
            }
        }
        
        throw new UnauthorizedException("User does not have required role");
    }
}
