package org.promsnmp.promsnmp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayName("Application Smoke Tests")
class PromSnmpApplicationSmokeTest {

    @Test
    @DisplayName("Should load Spring context successfully")
    void shouldLoadSpringContext() {
        // This test verifies that the Spring application context loads successfully
        // If this test passes, it means all beans are properly configured and autowired
        // No assertions needed - test passes if context loads without exceptions
    }
}