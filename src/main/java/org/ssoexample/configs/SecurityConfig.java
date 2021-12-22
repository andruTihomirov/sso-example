//package org.ssoexample.configs;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.ssoexample.services.SAMLUserDetailsServiceImpl;
//
//import static org.springframework.security.extensions.saml2.config.SAMLConfigurer.saml;
//
//@EnableWebSecurity
//@Configuration
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Value("${okta.metadata-path}")
//    private String metadataPath;
//
//    @Value("${okta.sp.host}")
//    private String spHost;
//
//    @Value("${server.ssl.key-store}")
//    private String keyStoreFile;
//
//    @Value("${server.ssl.key-store-password}")
//    private String keyStorePassword;
//
//    @Value("${server.ssl.key-alias}")
//    private String keyStoreAlias;
//
//    @Autowired
//    private SAMLUserDetailsServiceImpl samlUserDetailsServiceImpl;
//
//    @Override
//    protected void configure(final HttpSecurity http) throws Exception {
//        http
//                .csrf()
//                .and()
//                .authorizeRequests()
//                .antMatchers("/saml/**").permitAll()
//                .anyRequest()
//                .authenticated()
//                .and()
//                .apply(saml())
//                .userDetailsService(samlUserDetailsServiceImpl)
//
//                .serviceProvider()
//                .protocol("https")
//                .hostname(spHost)
//                .basePath("/")
//                .keyStore()
//                .storeFilePath(keyStoreFile)
//                .keyPassword(keyStorePassword)
//                .keyname(keyStoreAlias)
//                .and()
//                .and()
//                .identityProvider()
//                .metadataFilePath(metadataPath);
//    }
//}
