package org.promsnmp.promsnmp.configuration;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
public class H2ConsoleConfig {

    /**
     * Starts the H2 Web Console server on port 8082 and allows remote connections.
     * This is required when running the application inside Docker and accessing from host browser.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2WebServer() throws SQLException {
        return Server.createWebServer(
                "-web",              // Enable web console
                "-webAllowOthers",   // Allow non-localhost access
                "-webPort", "8082"   // Serve on port 8082 (not conflicting with your app on 8080)
        );
    }
}
