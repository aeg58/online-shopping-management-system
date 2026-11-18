package com.osms.config;

import com.osms.model.SessionUser;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RoleInterceptor(SessionUser.ROLE_ADMIN))
                .addPathPatterns("/admin/**");
        registry.addInterceptor(new RoleInterceptor(SessionUser.ROLE_SELLER))
                .addPathPatterns("/seller/**");
        registry.addInterceptor(new RoleInterceptor(SessionUser.ROLE_CUSTOMER))
                .addPathPatterns("/shop/cart/**", "/shop/checkout/**", "/shop/orders/**");
    }
}
