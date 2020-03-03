package fr.usubelli.compta.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.usubelli.compta.user.adapter.MongoUserRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.io.IOException;

public class UserVertx extends AbstractVerticle {

    void init(int port) {

        final MongoUserRepository userRepository = new MongoUserRepository();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        final Vertx vertx = Vertx.vertx(new VertxOptions());
        vertx.deployVerticle(this);
        final Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type"));
        router.route().handler(BodyHandler.create());
        router.post("/user")
                .produces("application/json")
                .handler(rc -> {
                    try {
                        final User user = objectMapper.readValue(rc.getBodyAsString(), User.class);
                        try {
                            final User a = userRepository.createUser(user);
                            try {
                                rc.response().setStatusCode(200).end(
                                        objectMapper.writeValueAsString(a));
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                                rc.response().setStatusCode(500).end();
                            }
                        } catch (UserAlreadyExistsException e) {
                            rc.response().setStatusCode(409).end();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        rc.response().setStatusCode(500).end();
                    }
                });
        router.get("/user/:email")
                .produces("application/json")
                .handler(rc -> {
                    try {
                        try {
                            rc.response().setStatusCode(200).end(
                                    objectMapper.writeValueAsString(userRepository.findUser(rc.request().getParam("email"))));
                        } catch (UserNotFoundException e) {
                            rc.response().setStatusCode(404).end();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        rc.response().setStatusCode(500).end();
                    }
                });
        router.put("/user")
                .produces("application/json")
                .handler(rc -> {
                    try {
                        final User user = objectMapper.readValue(rc.getBodyAsString(), User.class);
                        try {
                            final User a = userRepository.updateUser(user);
                            try {
                                rc.response().setStatusCode(200).end(
                                        objectMapper.writeValueAsString(a));
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                                rc.response().setStatusCode(500).end();
                            }
                        } catch (UserNotFoundException e) {
                            rc.response().setStatusCode(404).end();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        rc.response().setStatusCode(500).end();
                    }
                });
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port);
    }

    public static void main(String[] args) {
        new UserVertx().init(8585);
    }

}
