
package com.api.pg.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
public class ApiClientService {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RetryTemplate retryTemplate;

    // Discover the API service and invoke it with retry mechanism
    public Map<String, Object> queryApi(String table, List<String> fields, Map<String, Object> conditions, int limit, int offset) {
        // Use retry template to execute the request with retries
        return retryTemplate.execute(context -> {

            // Get the service instance from Discovery Client
            List<ServiceInstance> instances = discoveryClient.getInstances("api-service");  // Assume the API service is registered as 'api-service'
            if (instances.isEmpty()) {
                throw new RuntimeException("API service is not available");
            }

            // Get the first available instance of the API service
            ServiceInstance serviceInstance = instances.get(0);

            // Build the API endpoint URI
            URI uri = UriComponentsBuilder.fromUri(serviceInstance.getUri())
                    .path("/api/v1/dsl/query")
                    .build()
                    .toUri();

            // Create the payload for the request
            Map<String, Object> payload = Map.of(
                "select", fields,
                "from", table,
                "where", conditions,
                "limit", limit,
                "offset", offset
            );

            // Send the request and receive the response
            Map<String, Object> response = restTemplate.postForObject(uri, payload, Map.class);

            return response;

        });
    }
}
