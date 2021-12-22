package org.ssoexample.filters.matchers;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StrictTransportSecurityMatcher implements RequestMatcher {

    private static final StrictTransportSecurityMatcher instance = new StrictTransportSecurityMatcher();

    private StrictTransportSecurityMatcher() {
    }

    public static StrictTransportSecurityMatcher getInstance() {
        return instance;
    }

    private final Set<String> urls = new HashSet<>(Arrays.asList(
            "/login",
            "/login/sso"));

    @Override
    public boolean matches(HttpServletRequest request) {
        return urls.stream().anyMatch(url -> new AntPathRequestMatcher(url, null, false)
                .matches(request));
    }

}
