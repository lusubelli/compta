package fr.usubelli.compta.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.usubelli.compta.backend.dto.CreateAccountWithOrganisationRequest;
import fr.usubelli.compta.backend.dto.CreateCustomerRequest;
import fr.usubelli.compta.backend.port.NetUserGateway;
import fr.usubelli.compta.backend.usecase.CreateAccountWithOrganisation;
import fr.usubelli.compta.backend.usecase.CreateCustomer;
import fr.usubelli.compta.backend.port.NetOrganisationGateway;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

public class VertxBackend extends AbstractVerticle {

    void init(int port) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        final Vertx vertx = Vertx.vertx(new VertxOptions());
        vertx.deployVerticle(this);
        final Router router = Router.router(vertx);
        /*router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type"));*/
        router.route().handler(BodyHandler.create());
        router.post("/backend/signin")
                .produces("application/json")
                .handler(rc -> {
                    Cookie cookie = Cookie.cookie("SESSIONID", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
                    String path = "/";
                    cookie.setPath(path);
                    cookie.setMaxAge(10000000);
                    rc.addCookie(cookie);
                    rc.response().setChunked(true);
                    rc.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                    rc.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET");
                    rc.response().setStatusCode(200).end();
                });
        router.post("/backend/create-account")
                .produces("application/json")
                .handler(rc -> {
                    try {
                        final CreateAccountWithOrganisationRequest account = objectMapper.readValue(rc.getBodyAsString(), CreateAccountWithOrganisationRequest.class);
                        rc.response().setStatusCode(200).end(
                                objectMapper.writeValueAsString(new CreateAccountWithOrganisation(new NetUserGateway(), new NetOrganisationGateway()).createAccountWithOrganisation(account)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        rc.response().setStatusCode(500).end();
                    }
                });
        router.post("/backend/create-customer")
                .produces("application/json")
                .handler(rc -> {
                    try {
                        final CreateCustomerRequest customerRequest = objectMapper.readValue(rc.getBodyAsString(), CreateCustomerRequest.class);
                        rc.response().setStatusCode(200).end(
                                objectMapper.writeValueAsString(new CreateCustomer(new NetOrganisationGateway()).createCustomer(
                                        customerRequest.getSiren(),
                                        customerRequest.getOrganisation(),
                                        customerRequest.getBillingContactEmail())));
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
        new VertxBackend().init(8080);
    }

}
