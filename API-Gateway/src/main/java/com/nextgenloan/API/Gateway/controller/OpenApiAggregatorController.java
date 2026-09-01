// Purpose: Aggregates OpenAPI specs from all microservices into one unified view
// File: api-gateway/src/main/java/com/nextgenloan/API/Gateway/controller/OpenApiAggregatorController.java

package com.nextgenloan.API.Gateway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v3/api-docs/merged")
public class OpenApiAggregatorController {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    private static final String[] SERVICES = {
            "http://" + System.getenv().getOrDefault("CUSTOMER_SERVICE_HOST", "customer-service") + ":8081",
            "http://" + System.getenv().getOrDefault("LOAN_SERVICE_HOST", "loan-service") + ":8082",
            "http://" + System.getenv().getOrDefault("LIMIT_SERVICE_HOST", "limit-service") + ":8083",
            "http://" + System.getenv().getOrDefault("COLLATERAL_SERVICE_HOST", "collateral-service") + ":8084",
            "http://" + System.getenv().getOrDefault("REPORTING_SERVICE_HOST", "reporting-service") + ":8085",
            "http://" + System.getenv().getOrDefault("NOTIFICATION_SERVICE_HOST", "notification-service") + ":8086",
            "http://" + System.getenv().getOrDefault("AUDIT_SERVICE_HOST", "audit-service") + ":8087",
            "http://" + System.getenv().getOrDefault("MONITORING_SERVICE_HOST", "monitoring-service") + ":8088"
    };

    public OpenApiAggregatorController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getAggregatedOpenApi() {
        List<Mono<JsonNode>> requests = new ArrayList<>();
        for (String serviceUrl : SERVICES) {
            requests.add(fetchSpec(serviceUrl));
        }

        return Mono.zip(requests, results -> {
                    ObjectNode merged = objectMapper.createObjectNode();
                    ObjectNode info = objectMapper.createObjectNode();
                    info.put("title", "NextGen Loan Management Platform - Unified API");
                    info.put("description", "Aggregated OpenAPI specification from all microservices");
                    info.put("version", "1.0.0");
                    merged.set("info", info);
                    merged.put("openapi", "3.0.1");

                    ArrayNode mergedServers = objectMapper.createArrayNode();
                    ObjectNode gatewayServer = objectMapper.createObjectNode();
                    gatewayServer.put("url", "http://localhost:8080");
                    gatewayServer.put("description", "API Gateway");
                    mergedServers.add(gatewayServer);
                    merged.set("servers", mergedServers);

                    ObjectNode mergedPaths = objectMapper.createObjectNode();
                    ArrayNode mergedTags = objectMapper.createArrayNode();
                    ObjectNode mergedSchemas = objectMapper.createObjectNode();
                    ObjectNode mergedResponses = objectMapper.createObjectNode();
                    ObjectNode mergedParameters = objectMapper.createObjectNode();
                    ObjectNode mergedRequestBodies = objectMapper.createObjectNode();

                    List<String> tagNames = new ArrayList<>();

                    for (int i = 0; i < results.length; i++) {
                        JsonNode spec = (JsonNode) results[i];
                        if (spec == null || spec.isMissingNode() || spec.isNull()) continue;

                        if (spec.has("paths")) {
                            spec.get("paths").fields().forEachRemaining(entry -> {
                                String path = entry.getKey();
                                if (path.startsWith("/actuator/")) {
                                    return;
                                }
                                if (!mergedPaths.has(path)) {
                                    mergedPaths.set(path, entry.getValue());
                                }
                            });
                        }

                        if (spec.has("tags")) {
                            for (JsonNode tag : spec.get("tags")) {
                                String tagName = tag.has("name") ? tag.get("name").asText() : null;
                                if (tagName != null && !tagNames.contains(tagName)) {
                                    tagNames.add(tagName);
                                    mergedTags.add(tag);
                                }
                            }
                        }

                        mergeComponents(spec, "components/schemas", mergedSchemas, null);
                        mergeComponents(spec, "components/responses", mergedResponses, null);
                        mergeComponents(spec, "components/parameters", mergedParameters, null);
                        mergeComponents(spec, "components/requestBodies", mergedRequestBodies, null);
                    }

                    if (mergedPaths.size() > 0) merged.set("paths", mergedPaths);
                    if (mergedTags.size() > 0) merged.set("tags", mergedTags);

                    ObjectNode mergedComponents = objectMapper.createObjectNode();
                    if (mergedSchemas.size() > 0) mergedComponents.set("schemas", mergedSchemas);
                    if (mergedResponses.size() > 0) mergedComponents.set("responses", mergedResponses);
                    if (mergedParameters.size() > 0) mergedComponents.set("parameters", mergedParameters);
                    if (mergedRequestBodies.size() > 0) mergedComponents.set("requestBodies", mergedRequestBodies);
                    if (mergedComponents.size() > 0) merged.set("components", mergedComponents);

                    System.out.println("Merged spec - paths: " + mergedPaths.size() + ", tags: " + mergedTags.size() + ", schemas: " + mergedSchemas.size());

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(merged.toString());
                })
                .onErrorResume(e -> {
                    ObjectNode error = objectMapper.createObjectNode();
                    error.put("error", "Failed to aggregate OpenAPI specs");
                    error.put("message", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(error.toString()));
                });
    }

    private Mono<JsonNode> fetchSpec(String serviceUrl) {
        return Mono.fromCallable(() -> {
                    try {
                        ResponseEntity<String> response = restTemplate.exchange(
                                serviceUrl + "/v3/api-docs",
                                HttpMethod.GET,
                                new HttpEntity<>(createHeaders()),
                                String.class
                        );

                        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                            JsonNode spec = objectMapper.readTree(response.getBody());
                            System.out.println("Fetched spec from " + serviceUrl + " - paths: " + (spec.has("paths") ? spec.get("paths").size() : 0));
                            return spec;
                        } else {
                            System.err.println("Failed to fetch from " + serviceUrl + ": HTTP " + response.getStatusCode());
                            return objectMapper.createObjectNode();
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching from " + serviceUrl + ": " + e.getMessage());
                        return objectMapper.createObjectNode();
                    }
                })
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private void mergeComponents(JsonNode spec, String path, ObjectNode target, String serviceName) {
        JsonNode components = spec.at("/" + path.replace("/", "/"));
        if (components.isMissingNode() || components.isNull()) return;

        ObjectNode componentsNode = (ObjectNode) components;
        componentsNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            if (target.has(key)) {
                ObjectNode existing = (ObjectNode) target.get(key);
                if (value.isObject()) {
                    ((ObjectNode) value).fields().forEachRemaining(subEntry -> {
                        String subKey = subEntry.getKey();
                        if (!existing.has(subKey)) {
                            existing.set(subKey, subEntry.getValue());
                        }
                    });
                }
            } else {
                if (value.isObject()) {
                    ObjectNode prefixed = objectMapper.createObjectNode();
                    ((ObjectNode) value).fields().forEachRemaining(subEntry -> {
                        prefixed.set(subEntry.getKey(), subEntry.getValue());
                    });
                    target.set(key, prefixed);
                } else {
                    target.set(key, value);
                }
            }
        });
    }
}
