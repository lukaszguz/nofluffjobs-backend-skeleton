package com.nofluffjobs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.file.FileSystem;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PostingVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(PostingVerticle.class);
    private final String dbLocation;
    private FileSystem fileSystem;
    private final Gson gson = new Gson();

    private List<Posting> DEFAULT_POSTINGS = Arrays.asList(
            new Posting("Java Developer", "No Fluff Jobs", "Remote", "Plac Kaszubski 8/503",
                    "01-001", 10000, 12000),
            new Posting("Javascript Developer", "No Fluff Jobs", "Remote", "Plac Kaszubski 8/503",
                    "01-001", 10000, 12000)
    );

    public PostingVerticle(String dbLocation) {
        this.dbLocation = dbLocation;
    }

    @Override
    public Completable rxStart() {
        fileSystem = vertx.fileSystem();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post("/posting").handler(this::httpPostPosting);
        router.get("/posting").handler(this::httpGetPostings);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080);

        vertx.eventBus().consumer("posting::get", this::busGetPostings);

        return initDbIfNotExist()
                .doOnComplete(() -> log.info("Started PostingVerticle"));
    }

    private Completable initDbIfNotExist() {
        return fileSystem.rxExists(dbLocation)
                .filter(exists -> !exists)
                .flatMapCompletable(x -> initDb());
    }

    private Completable initDb() {
        return fileSystem.rxCreateFile(dbLocation)
                .andThen(fsWritePostings(DEFAULT_POSTINGS))
                .doOnComplete(() -> log.info("Initialized DB in PostingVerticle"));
    }

    private void httpGetPostings(RoutingContext context) {
        fsReadPostings()
                .map(this::serializeToJson)
                .map(postings -> new JsonObject().put("postings", new JsonArray(postings)))
                .subscribe(json -> context.response()
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .end(json.encodePrettily()));
    }

    private void httpPostPosting(RoutingContext context) {
        final Posting newPosting = gson.fromJson(context.getBodyAsString(), Posting.class);
        fsReadPostings()
                .doOnSuccess(postings -> postings.add(newPosting))
                .flatMapCompletable(this::fsWritePostings)
                .subscribe(() -> context.response().setStatusCode(204).end());
    }

    private void busGetPostings(Message<Void> message) {
        fsReadPostings()
                .map(this::serializeToJson)
                .flatMap(message::rxReply)
                .subscribe();
    }

    private Single<List<Posting>> fsReadPostings() {
        return fileSystem.rxReadFile(dbLocation)
                .map((Buffer buffer) -> deserializeToPostings(buffer.toString()));
    }

    private Completable fsWritePostings(List<Posting> postings) {
        return Single.fromCallable(() -> serializeToJson(postings))
                .flatMapCompletable(json -> fileSystem.rxWriteFile(dbLocation, Buffer.buffer(json)));
    }

    private String serializeToJson(List<Posting> postings) {
        return gson.toJson(postings);
    }

    private List<Posting> deserializeToPostings(String json) {
        return gson.fromJson(json, new TypeToken<Collection<Posting>>() {
        }.getType());
    }
}
