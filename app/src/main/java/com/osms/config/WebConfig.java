package com.osms.config;

import com.osms.model.SessionUser;
import com.osms.repository.UserRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserRepository userRepository;

    public WebConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RoleInterceptor(SessionUser.ROLE_ADMIN, userRepository))
                .addPathPatterns("/admin/**");
        registry.addInterceptor(new RoleInterceptor(SessionUser.ROLE_SELLER, userRepository))
                .addPathPatterns("/seller/**");
        registry.addInterceptor(new RoleInterceptor(SessionUser.ROLE_CUSTOMER, userRepository))
                .addPathPatterns("/shop/cart/**", "/shop/checkout/**", "/shop/orders/**",
                        "/shop/addresses/**");
    }
}
