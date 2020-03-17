package fr.usubelli.compta.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.usubelli.compta.user.adapter.MongoUserRepository;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;

public class UserVertx extends AbstractVerticle {

    private final static String AUTHENTICATION_BASIC_REALM = "compta.fr.user";

    private final MongoUserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserVertx() {
        userRepository = new MongoUserRepository();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper = objectMapper;
    }

    void init(Router router) {

        router.route().handler(BasicAuthHandler.create(authenticate(), AUTHENTICATION_BASIC_REALM));
        router.route().handler(BodyHandler.create());
        router.post("/user")
                .produces("application/json")
                .handler(createUser());
        router.get("/user/:email")
                .produces("application/json")
                .handler(findUserByEmail());
        router.put("/user")
                .produces("application/json")
                .handler(updateUser());

    }

    private Handler<RoutingContext> createUser() {
        return rc -> {
            try {
                rc.response().setStatusCode(200).end(
                        objectMapper.writeValueAsString(
                                userRepository.createUser(
                                        objectMapper.readValue(rc.getBodyAsString(), User.class))));
            } catch (UserAlreadyExistsException e) {
                rc.response().setStatusCode(409).end();
            } catch (Exception e) {
                e.printStackTrace();
                rc.response().setStatusCode(500).end();
            }
        };
    }

    private Handler<RoutingContext> findUserByEmail() {
        return rc -> {
            try {
                rc.response().setStatusCode(200).end(
                        objectMapper.writeValueAsString(
                                userRepository.findUser(
                                        rc.request().getParam("email"))));
            } catch (UserNotFoundException e) {
                rc.response().setStatusCode(404).end();
            } catch (Exception e) {
                e.printStackTrace();
                rc.response().setStatusCode(500).end();
            }
        };
    }

    private Handler<RoutingContext> updateUser() {
        return rc -> {
            try {
                rc.response().setStatusCode(200).end(
                        objectMapper.writeValueAsString(
                                userRepository.updateUser(
                                        objectMapper.readValue(rc.getBodyAsString(), User.class))));
            } catch (UserNotFoundException e) {
                rc.response().setStatusCode(404).end();
            } catch (Exception e) {
                e.printStackTrace();
                rc.response().setStatusCode(500).end();
            }
        };
    }

    public static void main(String[] args) {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<YAMLConfiguration> builder =
                new FileBasedConfigurationBuilder<>(YAMLConfiguration.class)
                        .configure(params.hierarchical().setFileName("user-config.yml"));
        final Configuration config;
        try {
            config = builder.getConfiguration();
        } catch (ConfigurationException e) {
            e.printStackTrace();
            return;
        }


        final Vertx vertx = Vertx.vertx(new VertxOptions());
        final UserVertx userVertx = new UserVertx();
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
                        .setPath("D:\\workspace\\compta\\user\\ssl\\localhost.keystore")
                        .setPassword("password"));

        vertx.createHttpServer(options)
                .requestHandler(router)
                .listen(8585);
    }

    private AuthProvider authenticate() {
        return (authInfo, resultHandler) -> {
            if (authInfo.getString("username").equals("login")
                    && authInfo.getString("password").equals("password")) {
                resultHandler.handle(
                        Future.succeededFuture(null));
            } else {
                resultHandler.handle(Future.failedFuture("erreur"));
            }
        };
    }

}
