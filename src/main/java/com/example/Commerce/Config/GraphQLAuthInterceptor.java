package com.example.Commerce.Config;

import com.example.Commerce.Entities.UserEntity;
import com.example.Commerce.Repositories.UserRepository;
import com.example.Commerce.errorHandlers.UnauthorizedException;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class GraphQLAuthInterceptor implements WebGraphQlInterceptor {
    
    private final UserRepository userRepository;
    private static final List<String> PUBLIC_QUERIES = List.of("allProducts", "productById", "allCategories", "categoryById");
    private static final List<String> PUBLIC_MUTATIONS = List.of("login", "register");
    
    public GraphQLAuthInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        String operationName = request.getDocument();
        
        // Check if it's a public query or mutation
        boolean isPublic = PUBLIC_QUERIES.stream().anyMatch(op -> operationName != null && operationName.contains(op)) ||
                          PUBLIC_MUTATIONS.stream().anyMatch(op -> operationName != null && operationName.contains(op));
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (isPublic) {
                return chain.next(request);
            }
            return Mono.error(new UnauthorizedException("Missing or invalid Authorization header"));
        }
        
        String token = authHeader.substring(7);
        
        try {
            String[] parts = token.split("-");
            if (parts.length != 2) {
                if (isPublic) {
                    return chain.next(request);
                }
                return Mono.error(new UnauthorizedException("Invalid token format"));
            }
            
            Long userId = Long.parseLong(parts[1]);
            UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Invalid token - user not found"));
            
            request.configureExecutionInput((executionInput, builder) -> 
                builder.graphQLContext(context -> {
                    context.put("userId", user.getId());
                    context.put("userRole", user.getRole().name());
                }).build()
            );
            
            return chain.next(request);
        } catch (NumberFormatException e) {
            if (isPublic) {
                return chain.next(request);
            }
            return Mono.error(new UnauthorizedException("Invalid token format"));
        } catch (UnauthorizedException e) {
            if (isPublic) {
                return chain.next(request);
            }
            return Mono.error(e);
        }
    }
}
