package org.ssoexample.configs;

import lombok.SneakyThrows;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLDiscovery;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.saml.SAMLLogoutProcessingFilter;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.SAMLWebSSOHoKProcessingFilter;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.processor.HTTPArtifactBinding;
import org.springframework.security.saml.processor.HTTPPAOS11Binding;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.HTTPSOAP11Binding;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.trust.httpclient.TLSProtocolConfigurer;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.ArtifactResolutionProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.ssoexample.certificate.KeystoreFactory;
import org.ssoexample.configs.wrappers.SpringResourceWrapperOpenSamlResource;

import javax.servlet.DispatcherType;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;

@AutoConfigureAfter(SamlConfigDefaults.class)
@Configuration
public class SamlConfig implements ApplicationContextAware {

    @Value("${server.ssl.key-alias}")
    private String keyStoreAlias;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SAMLUserDetailsService samlUserDetailsService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider provider = new SAMLAuthenticationProvider();
        provider.setUserDetails(samlUserDetailsService);
        provider.setForcePrincipalAsString(false);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(samlAuthenticationProvider()));
    }

    @Bean(initMethod = "initialize")
    public StaticBasicParserPool parserPool() {
        return new StaticBasicParserPool();
    }

    @Bean
    public SAMLProcessorImpl processor() {
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        ArtifactResolutionProfileImpl artifactResolutionProfile = new ArtifactResolutionProfileImpl(httpClient);
        HTTPSOAP11Binding soapBinding = new HTTPSOAP11Binding(parserPool());
        artifactResolutionProfile.setProcessor(new SAMLProcessorImpl(soapBinding));

        VelocityEngine velocityEngine = VelocityFactory.getEngine();
        Collection<SAMLBinding> bindings = new ArrayList<>();
        bindings.add(new HTTPRedirectDeflateBinding(parserPool()));
        bindings.add(new HTTPPostBinding(parserPool(), velocityEngine));
        bindings.add(new HTTPArtifactBinding(parserPool(), velocityEngine, artifactResolutionProfile));
        bindings.add(new HTTPSOAP11Binding(parserPool()));
        bindings.add(new HTTPPAOS11Binding(parserPool()));
        return new SAMLProcessorImpl(bindings);
    }

    @Bean
    public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
        SimpleUrlLogoutSuccessHandler handler = new SimpleUrlLogoutSuccessHandler();
        handler.setDefaultTargetUrl("/");
        handler.setAlwaysUseDefaultTargetUrl(true);
        return handler;
    }

    @Bean
    public SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler handler = new SecurityContextLogoutHandler();
        handler.setInvalidateHttpSession(true);
        handler.setClearAuthentication(true);
        return handler;
    }

    @Bean
    public SAMLLogoutFilter samlLogoutFilter() {
        SAMLLogoutFilter filter = new SAMLLogoutFilter(successLogoutHandler(),
                new LogoutHandler[]{logoutHandler()}, new LogoutHandler[]{logoutHandler()});
        filter.setFilterProcessesUrl("/saml/logout");
        return filter;
    }

    @Bean
    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
        SAMLLogoutProcessingFilter filter =
                new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
        filter.setFilterProcessesUrl("/saml/SingleLogout");
        return filter;
    }

    @Bean
    public MetadataGeneratorFilter metadataGeneratorFilter(MetadataGenerator metadataGenerator) {
        return new MetadataGeneratorFilter(metadataGenerator);
    }

    @Bean
    public MetadataDisplayFilter metadataDisplayFilter() {
        MetadataDisplayFilter filter = new MetadataDisplayFilter();
        filter.setFilterProcessesUrl("/saml/metadata");
        return filter;
    }

    @Bean
    public List<MetadataProvider> metadataProviders() {
        Resource resource = getResourceByName("sso/okta-metadata.xml");
        MetadataProvider metadataProvider = getProvider(resource);
        return Collections.singletonList(metadataProvider);
    }

    @Bean
    public ExtendedMetadata extendedMetadata() {
        ExtendedMetadata metadata = new ExtendedMetadata();
        metadata.setIdpDiscoveryEnabled(true);
        metadata.setRequireLogoutRequestSigned(true);
        metadata.setSignMetadata(false);
        return metadata;
    }

    @Bean
    public MetadataGenerator metadataGenerator(KeyManager keyManager) {
        MetadataGenerator generator = new MetadataGenerator();

        generator.setEntityId("onelogin");
        generator.setWantAssertionSigned(false);
        generator.setRequestSigned(false);
        generator.setEntityBaseURL("/");
        generator.setSamlLogoutProcessingFilter(samlLogoutProcessingFilter());

        generator.setExtendedMetadata(extendedMetadata());
        generator.setIncludeDiscoveryExtension(false);
        generator.setKeyManager(keyManager);
        return generator;
    }

    @Bean(name = "samlWebSSOProcessingFilter")
    @Primary
    public SAMLProcessingFilter samlWebSsoProcessingFilter() {
        SAMLProcessingFilter filter = new SAMLProcessingFilter();
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationSuccessHandler(successRedirectHandler());
        filter.setAuthenticationFailureHandler(authenticationFailureHandler());
        filter.setFilterProcessesUrl("/saml/SSO");
        return filter;
    }

    @Bean
    public SAMLWebSSOHoKProcessingFilter samlWebSsoHoKProcessingFilter() {
        SAMLWebSSOHoKProcessingFilter filter = new SAMLWebSSOHoKProcessingFilter();
        filter.setAuthenticationSuccessHandler(successRedirectHandler());
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return filter;
    }

    @Bean
    public RedirectStrategy redirectStrategy() {
        return new DefaultRedirectStrategy();
    }

    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setRedirectStrategy(redirectStrategy());
        handler.setDefaultTargetUrl("/login");
        return handler;
    }

    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        SimpleUrlAuthenticationFailureHandler handler = new SimpleUrlAuthenticationFailureHandler();
        handler.setUseForward(false);
        handler.setDefaultFailureUrl("/sso/failure");
        return handler;
    }

//    @Bean
//    public SAMLDiscovery samlIdpDiscovery() {
//        SAMLDiscovery filter = new SAMLDiscovery();
//        filter.setFilterProcessesUrl("/saml/discovery");
//        filter.setIdpSelectionPath("/idpselection");
//        return filter;
//    }

    @Bean
    public FilterRegistrationBean registrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(samlEntryPoint());
        filterRegistrationBean.setDispatcherTypes(DispatcherType.FORWARD);
        return filterRegistrationBean;
    }

    @Bean
    public SAMLEntryPoint samlEntryPoint() {
        WebSSOProfileOptions options = new WebSSOProfileOptions();
        options.setIncludeScoping(false);
        SAMLEntryPoint entryPoint = new SAMLEntryPoint();
        entryPoint.setDefaultProfileOptions(options);
        entryPoint.setFilterProcessesUrl("/saml/login");

        SAMLDiscovery filter = new SAMLDiscovery();
        filter.setFilterProcessesUrl("/saml/discovery");
        filter.setIdpSelectionPath("/idpselection");

        entryPoint.setSamlDiscovery(filter);

        return entryPoint;
    }

    @Bean
    public KeystoreFactory keystoreFactory(ResourceLoader resourceLoader) {
        return new KeystoreFactory(resourceLoader);
    }

    @Bean
    public KeyManager keyManager(KeystoreFactory keystoreFactory) {
        KeyStore keystore = keystoreFactory.loadKeystore("classpath:keystore.jks", "secret");

        return new JKSKeyManager(keystore, Map.of("onelogin", "secret"), "secret");
    }

    @Bean
    public TLSProtocolConfigurer tlsProtocolConfigurer(KeyManager keyManager) {
        TLSProtocolConfigurer configurer = new TLSProtocolConfigurer();
        configurer.setKeyManager(keyManager);
        return configurer;
    }

    @SneakyThrows
    private Resource getResourceByName(String pattern) {
        ClassLoader cl = SamlConfig.class.getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        return resolver.getResource(pattern);
    }

    private MetadataProvider getProvider(Resource idpMetadataFile) {
        try {
            Timer refreshTimer = new Timer(true);

            ResourceBackedMetadataProvider delegate = new ResourceBackedMetadataProvider(refreshTimer,
                    new SpringResourceWrapperOpenSamlResource(idpMetadataFile));

            delegate.setParserPool(parserPool());

            ExtendedMetadata extendedMetadata = extendedMetadata().clone();
            ExtendedMetadataDelegate provider = new ExtendedMetadataDelegate(delegate, extendedMetadata);

            provider.setMetadataTrustCheck(true);
            provider.setMetadataRequireSignature(false);

            extendedMetadata.setAlias(keyStoreAlias);

            return provider;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize IDP Metadata", e);
        }
    }

}
