package fr.usubelli.compta.backend.port;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.usubelli.compta.backend.dto.Organisation;
import fr.usubelli.compta.backend.usecase.OrganisationAlreadyExistsException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NetOrganisationGateway implements OrganisationGateway {

    private String url = "http://localhost:8484/organisation";

    @Override
    public Organisation createOrganisation(Organisation organisation) throws OrganisationAlreadyExistsException {
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
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(organisation)))
                    .build();
            HttpResponse response;
            try {
                response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 409) {
                    throw new OrganisationAlreadyExistsException();
                }

                return objectMapper.readValue(response.body().toString(), Organisation.class);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Organisation findOrganisation(String siren) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/" + siren))
                .setHeader("Content-Type", "application/json")
                .GET()
                .build();
        HttpResponse response;
        try {
            response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            return objectMapper.readValue(response.body().toString(), Organisation.class);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Organisation updateOrganisation(Organisation organisation) {
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
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(organisation)))
                    .build();
            HttpResponse response;
            try {
                response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 404) {
                    return null;
                }
                return objectMapper.readValue(response.body().toString(), Organisation.class);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
