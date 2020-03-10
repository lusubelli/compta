package fr.usubelli.compta.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.usubelli.compta.backend.dto.CreateAccountWithOrganisationRequest;
import fr.usubelli.compta.backend.dto.CreateCustomerRequest;
import fr.usubelli.compta.backend.dto.SigninRequest;
import fr.usubelli.compta.backend.dto.User;
import fr.usubelli.compta.backend.port.NetOrganisationGateway;
import fr.usubelli.compta.backend.port.NetUserGateway;
import fr.usubelli.compta.backend.usecase.CreateAccountWithOrganisation;
import fr.usubelli.compta.backend.usecase.CreateCustomer;
import fr.usubelli.compta.backend.usecase.Signin;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.UUID;

public class VertxBackend extends AbstractVerticle {


    private JWTAuth provider;

    VertxBackend() {
        provider = JWTAuth.create(vertx, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setPublicKey("keyboard cat")
                        .setSymmetric(true)));
    }

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
                    try {
                        final SigninRequest signinRequest = objectMapper.readValue(rc.getBodyAsString(), SigninRequest.class);

                        final User user = new Signin(new NetUserGateway()).signin(signinRequest);
                        if (user != null) {
                            final String xsrfToken = UUID.randomUUID().toString();
                            String token = createJsonWebToken(user, xsrfToken);
                            // now for any request to protected resources you should pass this string in the HTTP header Authorization as:
                            // Authorization: Bearer <token>
                            rc.addCookie(createCookie(token));
                            rc.response().setChunked(true);
                            rc.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                            rc.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET");
                            rc.response().setStatusCode(200).end("{ \"access_token\": \"" + xsrfToken + "\" }");
                        } else {
                            rc.response().setStatusCode(403).end();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        rc.response().setStatusCode(500).end();
                    }


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
        router.get("/backend/protected")
                .produces("application/json")
                .handler(rc -> {

                    final String headerXsrfToken = rc.request().getHeader("x-xsrf-token");
                    final Cookie sessionid = rc.getCookie("SESSIONID");

                    provider.authenticate(new JsonObject().put("jwt", sessionid.getValue()), res -> {
                        if (res.succeeded()) {
                            String jwtXsrfToken = res.result().principal().getString("xsrfToken");
                            if (!headerXsrfToken.equals(jwtXsrfToken)) {
                                // Failed!
                                System.out.println("failed");
                            } else {
                                // Success
                                System.out.println("success");
                            }
                        } else {
                            // Failed!
                            System.out.println("failed");
                        }
                    });

                });
        //  openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365
        // D:\sdk\jdk-12.0.1\bin\keytool.exe -genkey -v -keystore my-release-key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000
        final HttpServerOptions options = new HttpServerOptions()
                .setSsl(true)
                .setKeyStoreOptions(new JksOptions()
                        .setPath("D:\\workspace\\compta\\local.keystore")
                        .setPassword("password"));

        vertx.createHttpServer(options)
                .requestHandler(router)
                .listen(port);
    }

    private String createJsonWebToken(User user, String xsrfToken) {
        final JsonObject claims = new JsonObject()
                .put("sub", user.getEmail())
                .put("xsrfToken", xsrfToken)
                .put("scopes", new JsonArray().add("admin"));
        return provider.generateToken(claims, new JWTOptions());
    }

    private Cookie createCookie(String token) {
        Cookie cookie = Cookie.cookie("SESSIONID", token);
        String path = "/";
        cookie.setPath(path);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(10000000);
        return cookie;
    }

    public static void main(String[] args) {
        new VertxBackend().init(443);
    }

}
