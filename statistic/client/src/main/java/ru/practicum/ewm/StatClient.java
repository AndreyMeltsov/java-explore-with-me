package ru.practicum.ewm;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.HitDto;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StatClient {
    private final RestTemplate rest = new RestTemplateBuilder()
            .uriTemplateHandler(new DefaultUriBuilderFactory("http://server:9090"))
            .requestFactory(HttpComponentsClientHttpRequestFactory::new)
            .build();

    public ResponseEntity<Object> post(HitDto hitDto) {
        return makeAndSendRequest(HttpMethod.POST, "/hit", null, hitDto);
    }

    public ResponseEntity<Object> get(LocalDateTime start,
                                      LocalDateTime end,
                                      String[] uris,
                                      boolean unique) throws UnsupportedEncodingException {
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
            path = String.format("/stats?start=%s&end=%s&uris=%s&unique=%b", encodeValue(startAsString),
                    encodeValue(endAsString), Arrays.toString(uris), unique);
            parameters = Map.of("start", start, "end", end, "uris", uris, "unique", unique);
        }
        return makeAndSendRequest(HttpMethod.GET, path, parameters, null);
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
        return prepareGatewayResponse(ewmStatServerResponse);
    }

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
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
