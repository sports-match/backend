package me.zhengjie.config.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

/**
 * Filter to log API calls with their URI and request body
 */
@Slf4j
public class ApiLoggingFilter extends AbstractRequestLoggingFilter {

    public ApiLoggingFilter() {
        setIncludeQueryString(true);
        setIncludePayload(true);
        setMaxPayloadLength(10000);
        setIncludeHeaders(false);
        setAfterMessagePrefix("REQUEST DATA: [");
    }

    @Override
    protected void beforeRequest(@Nonnull HttpServletRequest request,@Nonnull String message) {
        // No logging before request processing
    }

    @Override
    protected void afterRequest(@Nonnull HttpServletRequest request,@Nonnull String message) {
        log.info(message);
    }

    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip logging for static resources
        return !path.contains("/static/") && 
               !path.contains("/favicon.ico") &&
               !path.contains("/druid/");
    }
}
