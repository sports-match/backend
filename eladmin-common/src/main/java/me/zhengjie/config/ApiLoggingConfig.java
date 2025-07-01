package me.zhengjie.config;

import me.zhengjie.config.filter.ApiLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuration class for API logging
 */
@Configuration
public class ApiLoggingConfig {

    @Bean
    public FilterRegistrationBean<ApiLoggingFilter> apiLoggingFilterRegistration() {
        FilterRegistrationBean<ApiLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        
        // Create and set the filter
        registrationBean.setFilter(new ApiLoggingFilter());
        
        // Set the URL patterns to apply this filter to - only filter actual API endpoints
        // Explicitly exclude Swagger paths
        registrationBean.addUrlPatterns("/api/*");
        
        // Set a name for the filter
        registrationBean.setName("apiLoggingFilter");
        
        // Set the order to ensure this runs AFTER Spring Security filters
        // This is important to prevent authentication issues
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        
        return registrationBean;
    }
}
