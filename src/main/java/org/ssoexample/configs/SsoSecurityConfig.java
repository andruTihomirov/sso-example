package org.ssoexample.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.SAMLDiscovery;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.saml.SAMLLogoutProcessingFilter;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.SAMLWebSSOHoKProcessingFilter;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.ssoexample.filters.WebSecurityCorsFilter;
import org.ssoexample.filters.matchers.StrictTransportSecurityMatcher;

import javax.annotation.PostConstruct;

@AutoConfigureAfter(SamlConfig.class)
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SsoSecurityConfig extends WebSecurityConfigurerAdapter {

    private final SAMLLogoutFilter samlLogoutFilter;
    private final SAMLLogoutProcessingFilter samlLogoutProcessingFilter;
    private final MetadataDisplayFilter metadataDisplayFilter;
    private final MetadataGeneratorFilter metadataGeneratorFilter;
    private final SAMLProcessingFilter samlWebSsoProcessingFilter;
    private final SAMLWebSSOHoKProcessingFilter samlWebSsoHoKProcessingFilter;
    private final SAMLEntryPoint samlEntryPoint;
//    private final SAMLDiscovery samlIdpDiscovery;
    private final AuthenticationManager authenticationManager;

    private WebSecurityCorsFilter webSecurityCorsFilter;

    private static final String[] WHITE_LIST_URLS = {
            // SSO
            "/saml/**",
            "/login/**",
            "/logout/**",

            // Auth
            "/api/user"
    };

    @Override
    public void init(WebSecurity web) throws Exception {
        super.init(web);
    }

    @PostConstruct
    public void init() {
        webSecurityCorsFilter = new WebSecurityCorsFilter();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
        securityContextRepository.setSpringSecurityContextKey("SPRING_SECURITY_CONTEXT_SAML");
        http.headers()
                .httpStrictTransportSecurity()
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000)
                .requestMatcher(StrictTransportSecurityMatcher.getInstance());
        http.securityContext()
                .securityContextRepository(securityContextRepository);
        http.httpBasic()
                .disable();
        http.authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS)
                .permitAll();
        http.addFilterBefore(webSecurityCorsFilter, ChannelProcessingFilter.class)
                .addFilterAfter(metadataGeneratorFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(metadataDisplayFilter, MetadataGeneratorFilter.class)
                .addFilterAfter(samlEntryPoint, MetadataDisplayFilter.class)
                .addFilterAfter(samlWebSsoProcessingFilter, SAMLEntryPoint.class)
                .addFilterAfter(samlWebSsoHoKProcessingFilter, SAMLProcessingFilter.class)
                .addFilterAfter(samlLogoutProcessingFilter, SAMLWebSSOHoKProcessingFilter.class)
//                .addFilterAfter(samlIdpDiscovery, SAMLLogoutProcessingFilter.class)
                .addFilterAfter(samlLogoutFilter, LogoutFilter.class);
        http.authorizeRequests()
                .antMatchers(WHITE_LIST_URLS)
                .permitAll()
                .anyRequest()
                .authenticated();
        http.exceptionHandling()
                .authenticationEntryPoint(samlEntryPoint);
        http.logout()
                .disable();

        http.csrf()
                .disable();
        http.cors()
                .disable();
    }

    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return authenticationManager;
    }

}
