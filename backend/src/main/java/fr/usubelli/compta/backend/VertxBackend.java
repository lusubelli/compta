package fr.usubelli.compta.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.usubelli.compta.backend.dto.CreateAccountWithOrganisationRequest;
import fr.usubelli.compta.backend.dto.CreateCustomerRequest;
import fr.usubelli.compta.backend.dto.SigninRequest;
import fr.usubelli.compta.backend.dto.User;
import fr.usubelli.compta.backend.adapter.rest.OrganizationRestClient;
import fr.usubelli.compta.backend.adapter.rest.UserRestClient;
import fr.usubelli.compta.backend.usecase.CreateAccountWithOrganisation;
import fr.usubelli.compta.backend.usecase.CreateCustomer;
import fr.usubelli.compta.backend.usecase.LoadAccount;
import fr.usubelli.compta.backend.usecase.Signin;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.Cookie;
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
    private UserRestClient userGateway;
    private OrganizationRestClient organisationGateway;

    VertxBackend() {
        provider = JWTAuth.create(vertx, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setPublicKey("password")
                        .setSymmetric(true)));
        userGateway = new UserRestClient("https://localhost:8585", "login", "password");
        organisationGateway = new OrganizationRestClient("https://localhost:8484", "login", "password");
    }

    void init(Router router) {


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        /*
        router.route().handler(SessionHandler
                .create(LocalSessionStore.create(vertx))
                .setCookieHttpOnlyFlag(true)
                .setCookieSecureFlag(true)
        );
        */
        //router.route().handler(CSRFHandler.create("password"));
        router.route().handler(BodyHandler.create());
        router.route("/backend/api/v1/secured/*").handler( context -> {
            final String jwt = context.getCookie("SESSIONID").getValue();
            provider.authenticate(new JsonObject().put("jwt", jwt), res -> {
                if (res.succeeded()) {
                    final String headerXsrfToken = context.request().getHeader("x-xsrf-token");
                    final String jwtXsrfToken = res.result().principal().getString("xsrfToken");
                    if (!headerXsrfToken.equals(jwtXsrfToken)) {
                        context.response().setStatusCode(403).end();
                    } else {
                        final String email = res.result().principal().getString("sub");
                        context.put("email", email);
                        context.next();
                    }
                } else {
                    context.response().setStatusCode(403).end();
                }
            });
        });
        router.post("/backend/api/v1/signin")
                .produces("application/json")
                .handler(rc -> {
                    try {
                        final SigninRequest signinRequest = objectMapper.readValue(rc.getBodyAsString(), SigninRequest.class);

                        final User user = new Signin(userGateway).signin(signinRequest);
                        if (user != null) {
                            final String xsrfToken = UUID.randomUUID().toString();
                            String token = createJsonWebToken(user, xsrfToken);
                            // now for any request to protected resources you should pass this string in the HTTP header Authorization as:
                            // Authorization: Bearer <token>
                            rc.addCookie(createCookie(token));
                            rc.response().setStatusCode(200).end("{ \"access_token\": \"" + xsrfToken + "\" }");
                        } else {
                            rc.response().setStatusCode(403).end();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        rc.response().setStatusCode(500).end();
                    }
                });
        router.get("/backend/api/v1/secured/signout")
                .produces("application/json")
                .handler(rc -> {
                    try {
                        rc.addCookie(removeCookie());
                        rc.response().setStatusCode(200).end();
                    } catch (Exception e) {
                        e.printStackTrace();
                        rc.response().setStatusCode(500).end();
                    }
                });
        router.post("/backend/api/v1/create-account")
                .produces("application/json")
                .handler(rc -> {
                    try {
                        final CreateAccountWithOrganisationRequest account = objectMapper.readValue(rc.getBodyAsString(), CreateAccountWithOrganisationRequest.class);
                        rc.response().setStatusCode(200).end(
                                objectMapper.writeValueAsString(new CreateAccountWithOrganisation(userGateway, organisationGateway).createAccountWithOrganisation(account)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        rc.response().setStatusCode(500).end();
                    }
                });
        router.post("/backend/api/v1/create-customer")
                .produces("application/json")
                .handler(rc -> {
                    try {
                        final CreateCustomerRequest customerRequest = objectMapper.readValue(rc.getBodyAsString(), CreateCustomerRequest.class);
                        rc.response().setStatusCode(200).end(
                                objectMapper.writeValueAsString(new CreateCustomer(organisationGateway).createCustomer(
                                        customerRequest.getSiren(),
                                        customerRequest.getOrganization(),
                                        customerRequest.getBillingContactEmail())));
                    } catch (Exception e) {
                        e.printStackTrace();
                        rc.response().setStatusCode(500).end();
                    }
                });
        router.get("/backend/api/v1/secured/account")
                .produces("application/json")
                .handler(context -> {
                    String email = context.get("email");
                    try {
                        context.response().setStatusCode(200).end(objectMapper.writeValueAsString(new LoadAccount().loadAccount(email)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        context.response().setStatusCode(500).end();
                    }
                });

    }

    private Cookie removeCookie() {
        Cookie cookie = Cookie.cookie("SESSIONID", "");
        String path = "/";
        cookie.setPath(path);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        return cookie;
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
        final Vertx vertx = Vertx.vertx(new VertxOptions());
        final VertxBackend vertxBackend = new VertxBackend();
        vertx.deployVerticle(vertxBackend);
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
        vertxBackend.init(router);


        final HttpServerOptions options = new HttpServerOptions()
                .setSsl(true)
                .setKeyStoreOptions(new JksOptions()
                        .setPath("D:\\workspace\\compta\\backend\\ssl\\localhost.keystore")
                        .setPassword("password"));

        vertx.createHttpServer(options)
                .requestHandler(router)
                .listen(8080);

    }


}
