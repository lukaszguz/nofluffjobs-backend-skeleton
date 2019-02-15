package com.nofluffjobs;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(VertxExtension.class)
public class PostingVerticleTest {

    private String dbTestLocation = "nfj_skeleton_test_db.json";
    private final String javaDeveloper = "{\"title\":\"Java Developer\",\"company\":\"No Fluff Jobs\",\"city\":\"Remote\",\"street\":\"Plac Kaszubski 8/503\",\"postalCode\":\"01-001\",\"salaryMin\":10000,\"salaryMax\":12000}";
    private final String javaScriptDeveloper = "{\"title\":\"Javascript Developer\",\"company\":\"No Fluff Jobs\",\"city\":\"Remote\",\"street\":\"Plac Kaszubski 8/503\",\"postalCode\":\"01-001\",\"salaryMin\":10000,\"salaryMax\":12000}";
    private final String cDeveloper = "{\"title\":\"C Developer\",\"company\":\"No Fluff Jobs\",\"city\":\"Remote\",\"street\":\"Plac Kaszubski 8/503\",\"postalCode\":\"01-001\",\"salaryMin\":10000,\"salaryMax\":12000}";

    @BeforeEach
    void deployVerticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new com.nofluffjobs.PostingVerticle(dbTestLocation), testContext.succeeding(x -> testContext.completeNow()));
    }

    @AfterEach
    void cleanupDB(Vertx vertx) {
        vertx.fileSystem().rxDelete(dbTestLocation).blockingAwait();
    }

    @Test
    @DisplayName("Should init db")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    public void should_init_db(Vertx vertx, VertxTestContext testContext) {
        final WebClient webClient = WebClient.create(vertx);
        webClient.get(8080, "127.0.0.1", "/posting")
                .as(BodyCodec.jsonObject())
                .send(asyncResult ->
                        testContext.verify(() -> {
                            final HttpResponse<JsonObject> response = asyncResult.result();
                            assertEquals(200, response.statusCode());
                            assertEquals(response.body().encode(), "{\"postings\":[" + javaDeveloper + "," + javaScriptDeveloper + "]}");
                            testContext.completeNow();
                        }));
    }

    @Test
    @DisplayName("Should save new posting")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    public void should_save_new_posting(Vertx vertx, VertxTestContext testContext) {
        final WebClient webClient = WebClient.create(vertx);
        final HttpResponse<Buffer> postResponse = webClient.post(8080, "127.0.0.1", "/posting")
                .rxSendBuffer(Buffer.buffer(cDeveloper))
                .blockingGet();
        testContext.verify(() -> assertEquals(204, postResponse.statusCode()));

        webClient.get(8080, "127.0.0.1", "/posting")
                .as(BodyCodec.jsonObject())
                .send(asyncResult ->
                        testContext.verify(() -> {
                            final HttpResponse<JsonObject> response = asyncResult.result();
                            assertEquals(200, response.statusCode());
                            assertEquals(response.body().encode(), "{\"postings\":[" + javaDeveloper + "," + javaScriptDeveloper + "," + cDeveloper + "]}");
                            testContext.completeNow();
                        }));
    }
}