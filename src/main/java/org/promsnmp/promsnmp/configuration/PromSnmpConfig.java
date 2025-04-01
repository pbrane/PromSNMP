package org.promsnmp.promsnmp.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PromSnmpConfig implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Value("${SERVER_PORT:8080}")
    private int serverPort;

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        factory.setPort(serverPort);
    }
}
