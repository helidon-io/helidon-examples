/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.examples.declarative.scheduling;

import java.time.Duration;

import io.helidon.service.registry.Lookup;
import io.helidon.service.registry.ServiceRegistry;
import io.helidon.testing.junit5.Testing;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@Testing.Test
public class DeclarativeSchedulingTest {
    private final ScheduledTask task;

    public DeclarativeSchedulingTest(ServiceRegistry registry, ScheduledTask task) {
        this.task = task;
        assertThat(task.counter(), is(0));

        // start the scheduler - a service at run level 70 (post construct is used to start it, so this is enough)
        registry.get(Lookup.builder()
                             .runLevel(70.0D)
                             .build());
    }

    @Test
    public void testSchedule() throws InterruptedException {
        // we should run every 2 seconds, lets do 2 runs, and give it up to 9 seconds (it should be less than the
        // value configured in annotation, to see that our config override is working
        long now = System.currentTimeMillis();

        int count;
        do {
            if (System.currentTimeMillis() - now > 9000) {
                fail("Scheduled task was not invoked twice within the expected time of 9 seconds");
            }
            Thread.sleep(Duration.ofSeconds(1));
            count = task.counter();
        } while (count < 2);
    }
}
