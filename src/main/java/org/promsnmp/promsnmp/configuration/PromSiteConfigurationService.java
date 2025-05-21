package org.promsnmp.promsnmp.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Data
@Service
public class PromSiteConfigurationService {

    @Value("${PROM_TENANT_ID:default_tenant}")
    private String tenantId;

    @Value("${PROM_SITE_ID:default_site}")
    private String siteId;

    @Value("${PROM_SITE_LABEL:#{null}}")
    private String siteLabel;

    @Value("${PROM_SITE_DESCR:#{null}}")
    private String siteDescription;

    @Value("${PROM_SITE_ADDR:#{null}}")
    private String siteAddress;

    @Value("${PROM_SITE_LAT:#{null}}")
    private Double latitude;

    @Value("${PROM_SITE_LONG:#{null}}")
    private Double longitude;

    //fixme: leaving this here for now even though there is currently a default value for these
    @PostConstruct
    public void validate() {
        if (tenantId == null || siteId == null) {
            throw new IllegalStateException("PROM_TENANT_ID and PROM_SITE_ID must be defined.");
        }
    }

    public Optional<String> getSiteLabel() {
        return Optional.ofNullable(siteLabel);
    }

    public Optional<String> getSiteDescription() {
        return Optional.ofNullable(siteDescription);
    }

    public Optional<String> getSiteAddress() {
        return Optional.ofNullable(siteAddress);
    }

    public Optional<Double> getLatitude() {
        return Optional.ofNullable(latitude);
    }

    public Optional<Double> getLongitude() {
        return Optional.ofNullable(longitude);
    }
}
