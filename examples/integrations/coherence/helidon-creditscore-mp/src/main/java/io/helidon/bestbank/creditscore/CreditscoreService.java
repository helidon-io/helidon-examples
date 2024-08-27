package io.helidon.bestbank.creditscore;

import com.oracle.coherence.cdi.Name;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import com.tangosol.net.NamedCache;

import static java.lang.System.Logger.Level.INFO;

@ApplicationScoped
@Path("/creditscore")
public class CreditscoreService {

    private static final System.Logger logger = System.getLogger(CreditscoreService.class.getName());
    private static final String CACHE_NAME = "creditScoreCache";

    private static final int SCORE_MAX = 800;
    private static final int SCORE_MIN = 550;

    @Inject
    @Name(CACHE_NAME)
    private NamedCache<String, Integer> creditScoreCache;

    public CreditscoreService() {

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postMethodCreditScore(JsonObject reqJson) {

        logger.log(INFO, "Computing credit score for " + reqJson.getString("firstname"));

        String ssn = reqJson.getString("ssn");
        Integer creditScore = creditScoreCache.get(ssn);

        if (creditScore == null) {
            creditScore = calculateCreditScore(reqJson.getString("firstname"),
                                                reqJson.getString("lastname"),
                                                reqJson.getString("dateofbirth"),
                                                ssn);
            creditScoreCache.put(ssn, creditScore);
        }

        JsonObject resJson = Json.createObjectBuilder(reqJson)
                .add("score", creditScore)
                .build();

        return Response.ok(resJson).build();
    }

    private int calculateCreditScore(String firstName, String lastName, String dateOfBirth, String ssn) {
        int score = Math.abs(firstName.hashCode() + lastName.hashCode() + dateOfBirth.hashCode() + ssn.hashCode());
        score = score % SCORE_MAX;

        while (score < SCORE_MIN) {
            score = score + 100;
        }
        // Pause for dramatic effect
        sleep(2);
        return score;
    }

    private int sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1_000L);
        } catch (InterruptedException ignored) {
            ;
        }
        return seconds;
    }
}
