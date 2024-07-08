
package io.helidon.examples.microprofile.threads;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import io.helidon.microprofile.testing.junit5.HelidonTest;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@HelidonTest
class MainTest {

    @Inject
    private WebTarget target;

    @Test
    void testFanOut() {
        Response response = target.path("thread/fanout/2")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
    }

    @Test
    void testCompute() {
        Response response = target.path("thread/compute/2")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
    }
}
