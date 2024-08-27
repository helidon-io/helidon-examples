
package io.helidon.bestbank.creditscore;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.metrics.MetricRegistry;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

import io.helidon.microprofile.testing.junit5.HelidonTest;
import io.helidon.metrics.api.MetricsFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@HelidonTest
class MainTest {

    @Inject
    private WebTarget target;

    @Test
    void testCreditScore() {
        try (Response r = target
                .path("creditscore")
                .request()
                .post(Entity.entity("""
                                {
                                    "ssn" : "123-45-6789",
                                    "firstname" : "Frank",
                                    "lastname" : "Helidon",
                                    "dateofbirth" : "01/30/2018"
                                }
                                """, MediaType.APPLICATION_JSON)
                )){
            assertThat(r.getStatus(), is(200));
        }
    }
}
