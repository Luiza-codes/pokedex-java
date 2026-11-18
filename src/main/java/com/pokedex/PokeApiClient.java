package com.pokedex;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;


public class PokeApiClient {

    private static final String BASE_URL = "https://pokeapi.co/api/v2/pokemon/";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final Gson gson = new Gson();


    public Optional<Pokemon> buscar(String nomeOuId) throws IOException, InterruptedException {
        String termo = URLEncoder.encode(nomeOuId.trim().toLowerCase(), StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + termo))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            return Optional.empty();
        }
        if (response.statusCode() != 200) {
            throw new IOException("Erro na PokéAPI. Status HTTP: " + response.statusCode());
        }
        return Optional.of(gson.fromJson(response.body(), Pokemon.class));
    }
}
