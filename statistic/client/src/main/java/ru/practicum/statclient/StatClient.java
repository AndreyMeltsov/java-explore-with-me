package ru.practicum.statclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.statdto.HitDto;
import ru.practicum.statdto.ViewStatsDto;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatClient {
    private final RestTemplate rest;
    private final Gson gson;
    private final ObjectMapper objectMapper;

    @Autowired
    public StatClient(Gson gson, ObjectMapper objectMapper, RestTemplateBuilder builder) {
        this.rest = builder.uriTemplateHandler(new DefaultUriBuilderFactory("http://stats-server:9090"))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
        this.gson = gson;
        this.objectMapper = objectMapper;
    }

    public void post(HitDto hitDto) {
        makeAndSendRequest(HttpMethod.POST, "/hit", null, hitDto);
    }

    public List<ViewStatsDto> get(LocalDateTime start,
                                  LocalDateTime end,
                                  String[] uris,
                                  boolean unique) throws UnsupportedEncodingException, JsonProcessingException {
        String path;
        Map<String, Object> parameters;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startAsString = start.format(formatter);
        String endAsString = end.format(formatter);

        if (uris == null) {
            path = String.format("/stats?start=%s&end=%s&unique=%b", encodeValue(startAsString),
                    encodeValue(endAsString), unique);
            parameters = Map.of("start", startAsString, "end", endAsString, "unique", unique);
        } else {
            String finalUri = "";
            for (String s : uris) {
                finalUri = finalUri.concat(String.format("uri=%s&", s));
            }
            path = String.format("/stats?start=%s&end=%s&%sunique=%b", encodeValue(startAsString),
                    encodeValue(endAsString), finalUri, unique);
            log.info("Окончательный путь будет: {}", path);
            parameters = Map.of("start", start, "end", end, "uris", uris, "unique", unique);
        }
        ResponseEntity<Object> responseEntity = makeAndSendRequest(HttpMethod.GET, path, parameters, null);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String jsonBody = gson.toJson(responseEntity.getBody());
            return objectMapper.readValue(jsonBody, new TypeReference<>() {
            });
        } else return Collections.emptyList();
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path,
                                                          @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> ewmStatServerResponse;
        try {
            if (parameters != null) {
                ewmStatServerResponse = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                ewmStatServerResponse = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareResponse(ewmStatServerResponse);
    }

    private static ResponseEntity<Object> prepareResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }

    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}
