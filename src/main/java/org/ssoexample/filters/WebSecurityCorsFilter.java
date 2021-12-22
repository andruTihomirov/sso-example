package org.ssoexample.filters;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_MAX_AGE;

public class WebSecurityCorsFilter implements Filter {

    private static final String HTTPS = "https://";
    private static final String DEFAULT_VALUE = "*";

    private List<String> allowedOrigins;
    private boolean isAllowedAll;
    private String allowedOrigin;

    public WebSecurityCorsFilter() {

        allowedOrigins = List.of("*");
        isAllowedAll = allowedOrigins.stream().anyMatch(url -> url.equals(DEFAULT_VALUE));
        allowedOrigin = HTTPS + allowedOrigins.stream().findFirst().orElseThrow(() ->
                new RuntimeException("Allowed Origin should exists"));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;

        setAllowedOriginsHeader((HttpServletRequest) request, res);
        res.setHeader(ACCESS_CONTROL_ALLOW_METHODS,
                "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS");
        res.setHeader(ACCESS_CONTROL_MAX_AGE, "600");
        res.setHeader(ACCESS_CONTROL_ALLOW_HEADERS,
                "Origin, X-Requested-With, Content-type, Accept, DSMS-Token, id, X-Ds-Target-Wg");
        res.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        chain.doFilter(request, res);
    }

    private void setAllowedOriginsHeader(HttpServletRequest request, HttpServletResponse res) {
        String serverName = request.getServerName();
        boolean isUrlValid = allowedOrigins.stream().anyMatch(url -> url.equals(serverName));
        if (isAllowedAll) {
            res.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, DEFAULT_VALUE);
        } else if (isUrlValid) {
            res.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, HTTPS + serverName);
        } else {
            res.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin);
        }
    }

}
