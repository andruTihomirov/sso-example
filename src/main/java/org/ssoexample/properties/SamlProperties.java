package org.ssoexample.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
public class SamlProperties {

    @Value("${saml.load-balancer.scheme}")
    private String loadBalancerScheme;

    @Value("${saml.load-balancer.server-port}")
    private int loadBalancerServerPort;

    @Value("${saml.load-balancer.server-name}")
    private String loadBalancerServerName;

    @Value("${saml.load-balancer.context-path}")
    private String loadBalancerContextPath;

}
