package org.ssoexample;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.saml.SAMLDiscovery;
import org.ssoexample.configs.FilterCleanupConfig;
import org.ssoexample.configs.SamlConfig;
import org.ssoexample.configs.SamlConfigDefaults;

import java.util.Arrays;

@SpringBootApplication
@Import({
        FilterCleanupConfig.class,
        SamlConfig.class,
        SamlConfigDefaults.class,
})
public class SsoExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsoExampleApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("BEANS.............................");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }
        };
    }

}
