package org.thivernale.tophits.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Enumeration;
import java.util.logging.Logger;


@Component
public class LoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = Logger.getLogger(LoggingInterceptor.class.getName());

    @Override
    public void afterCompletion(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {
        if (ex != null) {
            log.severe("[afterCompletion][" + request + "][exception: " + ex.getMessage() + "]");
        } else {
            log.info("[afterCompletion][" + request + "]");
        }
    }

    @Override
    public void postHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) throws Exception {
        log.info("[postHandle][" + request + "]");
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        log.info("[preHandle][" + request + "][" + request.getMethod() + "]" + request.getRequestURI() + getParameters(request));
        return true;
    }

    private String getParameters(HttpServletRequest request) {
        StringBuffer posted = new StringBuffer();

        Enumeration<String> parameterNames = request.getParameterNames();
        if (parameterNames != null) {
            posted.append("?");
            while (parameterNames.hasMoreElements()) {
                String parameterName = parameterNames.nextElement();
                posted.append(posted.length() > 1 ? "&" : "")
                    .append(parameterName)
                    .append("=")
                    .append(sanitizeParameterValue(request, parameterName));
            }
        }

        String ipAddr = request.getHeader("X-Forwarded-For");
        if (ipAddr == null || ipAddr.isEmpty()) {
            ipAddr = request.getRemoteAddr();
        }
        if (ipAddr != null && !ipAddr.isEmpty()) {
            posted.append(posted.length() > 1 ? "&" : "")
                .append("_ipaddr=")
                .append(ipAddr);
        }

        return posted
            .toString();
    }

    private String sanitizeParameterValue(HttpServletRequest request, String parameterName) {
        String[] values = request.getParameterValues(parameterName);
        if (values != null && values.length > 0) {
            if (parameterName.toLowerCase()
                .contains("password") || parameterName.toLowerCase()
                .contains("pwd")) {
                return "*".repeat(5);
            } else {
                return String.join(",", values);
            }
        }
        return "";
    }
}
