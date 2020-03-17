package fr.usubelli.compta.organisation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.usubelli.compta.organisation.adapter.MongoOrganisationRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.IOException;

public class OrganisationVertx extends AbstractVerticle {

    private final static String AUTHENTICATION_BASIC_REALM = "compta.fr.organization";

    void init(Router router) {

        final MongoOrganisationRepository organisationRepository = new MongoOrganisationRepository();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        router.route().handler(BasicAuthHandler.create((authInfo, resultHandler) -> {
            if (authInfo.getString("username").equals("login")
                    && authInfo.getString("password").equals("password")) {
                resultHandler.handle(
                        Future.succeededFuture(null));
            } else {
                resultHandler.handle(Future.failedFuture("erreur"));
            }
        }, AUTHENTICATION_BASIC_REALM));
        router.route().handler(BodyHandler.create());
        router.post("/organisation")
                .produces("application/json")
                .handler(rc -> {
                    try {
                        final Organisation organisation = objectMapper.readValue(rc.getBodyAsString(), Organisation.class);
                        try {
                            final Organisation o = organisationRepository.createOrganisation(organisation);
                            try {
                                rc.response().setStatusCode(200).end(
                                        objectMapper.writeValueAsString(o));
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                                rc.response().setStatusCode(500).end();
                            }
                        } catch (OrganisationAlreadyExistsException e) {
                            rc.response().setStatusCode(409).end();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        rc.response().setStatusCode(500).end();
                    }
                });
        router.get("/organisation/:siren")
                .produces("application/json")
                .handler(rc -> {
                    try {
                        try {
                            rc.response().setStatusCode(200).end(
                                    objectMapper.writeValueAsString(organisationRepository.findOrganisation(rc.request().getParam("siren"))));
                        } catch (OrganisationNotFoundException e) {
                            rc.response().setStatusCode(404).end();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        rc.response().setStatusCode(500).end();
                    }
                });
        router.put("/organisation")
                .produces("application/json")
                .handler(rc -> {
                    try {
                        final Organisation organisation = objectMapper.readValue(rc.getBodyAsString(), Organisation.class);
                        try {
                            final Organisation o = organisationRepository.updateOrganisation(organisation);
                            try {
                                rc.response().setStatusCode(200).end(
                                        objectMapper.writeValueAsString(o));
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                                rc.response().setStatusCode(500).end();
                            }
                        } catch (OrganisationNotFoundException e) {
                            rc.response().setStatusCode(404).end();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        rc.response().setStatusCode(500).end();
                    }
                });
    }

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx(new VertxOptions());
        final OrganisationVertx userVertx = new OrganisationVertx();
        vertx.deployVerticle(userVertx);
        final Router router = Router.router(vertx);
        router.route().handler(ctx -> {
            ctx.response()
                    // do not allow proxies to cache the data
                    .putHeader("Cache-Control", "no-store, no-cache")
                    // prevents Internet Explorer from MIME - sniffing a
                    // response away from the declared content-type
                    .putHeader("X-Content-Type-Options", "nosniff")
                    // Strict HTTPS (for about ~6Months)
                    .putHeader("Strict-Transport-Security", "max-age=" + 15768000)
                    // IE8+ do not allow opening of attachments in the context of this resource
                    .putHeader("X-Download-Options", "noopen")
                    // enable XSS for IE
                    .putHeader("X-XSS-Protection", "1; mode=block")
                    // deny frames
                    .putHeader("X-FRAME-OPTIONS", "DENY");
            ctx.next();
        });
        userVertx.init(router);


        final HttpServerOptions options = new HttpServerOptions()
                .setSsl(true)
                .setKeyStoreOptions(new JksOptions()
                        .setPath("D:\\workspace\\compta\\organisation\\ssl\\localhost.keystore")
                        .setPassword("password"));

        vertx.createHttpServer(options)
                .requestHandler(router)
                .listen(8585);

    }

}
