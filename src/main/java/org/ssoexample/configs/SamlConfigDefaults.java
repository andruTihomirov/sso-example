package org.ssoexample.configs;

import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.context.SAMLContextProviderLB;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.websso.SingleLogoutProfile;
import org.springframework.security.saml.websso.SingleLogoutProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfile;
import org.springframework.security.saml.websso.WebSSOProfileConsumer;
import org.springframework.security.saml.websso.WebSSOProfileConsumerHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;
import org.springframework.security.saml.websso.WebSSOProfileECPImpl;
import org.springframework.security.saml.websso.WebSSOProfileHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.ssoexample.properties.SamlProperties;

import java.util.List;

@Configuration
@Import(SamlConfig.class)
public class SamlConfigDefaults {

    @Autowired
    private SamlProperties samlProperties;

    @Bean
    public static SAMLBootstrap sAMLBootstrap() {
        return new SAMLBootstrap();
    }

    @Bean
    public ParserPoolHolder parserPoolHolder() {
        return new ParserPoolHolder();
    }

    @Bean
    public SAMLContextProviderImpl contextProvider() {
        SAMLContextProviderLB providerLB = new SAMLContextProviderLB();
        providerLB.setIncludeServerPortInRequestURL(true);
        providerLB.setScheme(samlProperties.getLoadBalancerScheme());
        providerLB.setServerPort(samlProperties.getLoadBalancerServerPort());
        providerLB.setServerName(samlProperties.getLoadBalancerServerName());
        providerLB.setContextPath(samlProperties.getLoadBalancerContextPath());
        return providerLB;
    }

    @Bean
    public SAMLDefaultLogger samlLogger() {
        return new SAMLDefaultLogger();
    }

    @Bean
    public WebSSOProfileConsumer webSSOprofileConsumer() {
        WebSSOProfileConsumerImpl webSSOProfileConsumer = new WebSSOProfileConsumerImpl();
        webSSOProfileConsumer.setResponseSkew(82);
        return webSSOProfileConsumer;
    }

    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
        WebSSOProfileConsumerHoKImpl webSSOProfileConsumerHoK = new WebSSOProfileConsumerHoKImpl();
        webSSOProfileConsumerHoK.setResponseSkew(82);
        return webSSOProfileConsumerHoK;
    }

    @Bean
    public WebSSOProfile webSSOprofile() {
        return new WebSSOProfileImpl();
    }

    @Bean
    public WebSSOProfileECPImpl ecpProfile() {
        return new WebSSOProfileECPImpl();
    }

    @Bean
    public WebSSOProfileHoKImpl hokWebSSOProfile() {
        return new WebSSOProfileHoKImpl();
    }

    @Bean
    public SingleLogoutProfile logoutProfile() {
        return new SingleLogoutProfileImpl();
    }

    @Bean
    public CachingMetadataManager metadataManager(List<MetadataProvider> metadataProviders)
            throws MetadataProviderException {
        return new CachingMetadataManager(metadataProviders);
    }

}
