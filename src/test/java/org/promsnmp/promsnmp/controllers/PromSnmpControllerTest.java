package org.promsnmp.promsnmp.controllers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.promsnmp.promsnmp.services.PromSnmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@WebMvcTest(PromSnmpController.class)
class PromSnmpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @Qualifier("configuredService")
    private PromSnmpService promSnmpService;

    @MockBean
    private CacheManager cacheManager;


    @Test
    void testHelloEndpoint() throws Exception {
        mockMvc.perform(get("/promSnmp/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));
    }

    @Test
    void testSampleEndpoint() throws Exception {
        mockMvc.perform(get("/promSnmp/metrics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(containsString("# HELP ifHCInOctets")))
                .andExpect(content().string(containsString("# TYPE ifHCInOctets counter")))
                .andExpect(content().string(containsString("ifHCInOctets{instance=\"router-1.example.com\", ifIndex=\"1\"}")));
    }
    
    @Test
    void testRouterEndpoint() throws Exception {
        mockMvc.perform(get("/promSnmp/services"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(containsString("# HELP ifHCInOctets")))
                .andExpect(content().string(containsString("# HELP cpuUsage")))
                .andExpect(content().string(containsString("# HELP bgpPeerState")));
    }
    
    @Test
    void testMetricsTypeFiltering() throws Exception {
        mockMvc.perform(get("/promSnmp/metrics/cpuUsage"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(containsString("# HELP cpuUsage")))
                .andExpect(content().string(containsString("# TYPE cpuUsage gauge")))
                .andExpect(content().string(not(containsString("# HELP memoryUsage"))));
    }
    
    @Test
    void testMetricsInstanceFiltering() throws Exception {
        mockMvc.perform(get("/promSnmp/metrics/ifHCInOctets?instance=router.example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(containsString("instance=\"router.example.com\"")));
    }
    
    @Test
    void validatePrometheusFormat() throws Exception {
        String content = mockMvc.perform(get("/promSnmp/router"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
                
        // Check format structure
        assertTrue(content.contains("# HELP"));
        assertTrue(content.contains("# TYPE"));
        
        // Validate metric format with regex for counter metrics
        Pattern counterPattern = Pattern.compile(
            "^[a-zA-Z_][a-zA-Z0-9_]*\\{[^}]+\\} \\d+$", 
            Pattern.MULTILINE
        );
        assertTrue(counterPattern.matcher(content).find(), "Content should contain properly formatted counter metrics");
        
        // Validate metric format with regex for gauge metrics (can have decimal points)
        Pattern gaugePattern = Pattern.compile(
            "^[a-zA-Z_][a-zA-Z0-9_]*\\{[^}]+\\} \\d+(\\.\\d+)?$", 
            Pattern.MULTILINE
        );
        assertTrue(gaugePattern.matcher(content).find(), "Content should contain properly formatted gauge metrics");
    }
    
    @Test
    void validateResourcesExist() throws Exception {
        // Verify both metrics files exist
        ClassPathResource sampleResource = new ClassPathResource("static/prometheus-snmp-export.dat");
        assertTrue(sampleResource.exists(), "The sample metrics file should exist");
        
        ClassPathResource routerResource = new ClassPathResource("static/advanced-router-metrics.dat");
        assertTrue(routerResource.exists(), "The advanced router metrics file should exist");
        
        // Check content of the router metrics file
        try (Scanner scanner = new Scanner(routerResource.getInputStream(), StandardCharsets.UTF_8)) {
            String content = scanner.useDelimiter("\\A").next();
            assertTrue(content.contains("# HELP"), "The metrics file should contain HELP entries");
            assertTrue(content.contains("# TYPE"), "The metrics file should contain TYPE entries");
            assertTrue(content.contains("cpuUsage"), "The metrics file should contain CPU usage metrics");
            assertTrue(content.contains("memoryUsage"), "The metrics file should contain memory usage metrics");
        }
    }
}