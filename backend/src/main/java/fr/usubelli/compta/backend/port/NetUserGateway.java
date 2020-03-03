package fr.usubelli.compta.backend.port;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.usubelli.compta.backend.dto.User;
import fr.usubelli.compta.backend.usecase.UserAlreadyExistsException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NetUserGateway implements UserGateway {

    private String url = "http://localhost:8585/user";

    public User createUser(User user) throws UserAlreadyExistsException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .setHeader("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(user)))
                    .build();
            HttpResponse response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 409) {
                    throw new UserAlreadyExistsException();
                }

                return objectMapper.readValue(response.body().toString(), User.class);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public User findUser(String email) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/" + email))
                .setHeader("Content-Type", "application/json")
                .GET()
                .build();
        HttpResponse response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return objectMapper.readValue(response.body().toString(), User.class);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public User updateUser(User user) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .setHeader("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(user)))
                    .build();
            HttpResponse response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 404) {
                    return null;
                }
                return objectMapper.readValue(response.body().toString(), User.class);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
