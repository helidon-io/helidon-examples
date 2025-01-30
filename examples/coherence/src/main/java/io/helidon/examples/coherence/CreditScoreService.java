/*
 * Copyright (c) 2025 Oracle and/or its affiliates.
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
package io.helidon.examples.coherence;

import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

import static java.lang.System.Logger.Level.INFO;

/**
 * Credit score service.
 */
class CreditScoreService implements HttpService {

    private static final System.Logger LOGGER = System.getLogger(CreditScoreService.class.getName());
    private static final int SCORE_MAX = 800;
    private static final int SCORE_MIN = 550;

    private final NamedCache<String, Integer> creditScoreCache = CacheFactory.getCache("creditScoreCache");

    @Override
    public void routing(HttpRules rules) {
        rules.post(this::postCreditScore);
    }

    private void postCreditScore(ServerRequest req, ServerResponse res) {
        Person person = req.content().as(Person.class);
        if (person.firstName() == null || person.lastName() == null || person.dateOfBirth() == null || person.ssn() == null) {
            res.status(Status.BAD_REQUEST_400).send("Bad Request");
            return;
        }

        LOGGER.log(INFO, "Computing credit score for " + person.firstName() + " " + person.lastName());

        String ssn = person.ssn();
        Integer creditScore = creditScoreCache.get(ssn);

        if (creditScore == null) {
            creditScore = calculateCreditScore(person);
            creditScoreCache.put(ssn, creditScore);
        }
        res.send(new Person(person.firstName(), person.lastName(), person.dateOfBirth(), "NNN-NN-NNNN", creditScore));
    }

    private int calculateCreditScore(Person p) {
        int score = Math.abs(p.hashCode()) % SCORE_MAX;
        while (score < SCORE_MIN) {
            score += 100;
        }
        // Pause for dramatic effect
        sleep();
        return score;
    }

    private void sleep() {
        try {
            Thread.sleep(2_000L);
        } catch (InterruptedException ignored) {
        }
    }
}
