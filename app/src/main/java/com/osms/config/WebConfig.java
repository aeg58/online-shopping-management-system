package com.osms.config;

import com.osms.model.SessionUser;
import com.osms.repository.UserRepository;
import com.osms.service.ProductImageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserRepository userRepository;
    private final ProductImageService productImages;

    public WebConfig(UserRepository userRepository, ProductImageService productImages) {
        this.userRepository = userRepository;
        this.productImages = productImages;
    }

    /** Photos uploaded by sellers are served from the upload folder, outside the application jar. */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(productImages.getUploadDir().toUri().toString());
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
