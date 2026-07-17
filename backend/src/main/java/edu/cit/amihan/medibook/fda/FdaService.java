package edu.cit.amihan.medibook.fda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FdaService {

    private final RestTemplate fdaRestTemplate;
    private final ObjectMapper objectMapper;

    public List<FdaDrugSuggestion> getSuggestions(String diagnosis) {
        if (diagnosis == null || diagnosis.isBlank()) {
            return List.of();
        }

        String escaped = escapeQuery(diagnosis);
        String searchQuery = "indications_and_usage:" + escaped;
        log.debug("OpenFDA query: search={}", searchQuery);

        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://api.fda.gov/drug/label.json")
                    .queryParam("search", searchQuery)
                    .queryParam("limit", 10)
                    .toUriString();

            String responseStr = fdaRestTemplate.getForObject(url, String.class);
            JsonNode response = objectMapper.readTree(responseStr);

            if (response == null || !response.has("results")) {
                log.warn("OpenFDA returned no results for '{}', retrying with description field", diagnosis);
                return fallbackSearch(escaped);
            }

            return parseResults(response);
        } catch (Exception e) {
            log.warn("OpenFDA query failed for '{}': {}", diagnosis, e.getMessage());
            return List.of();
        }
    }

    private List<FdaDrugSuggestion> fallbackSearch(String escaped) {
        try {
            String fallbackQuery = "description:" + escaped;
            log.debug("OpenFDA fallback query: search={}", fallbackQuery);

            String url = UriComponentsBuilder.fromHttpUrl("https://api.fda.gov/drug/label.json")
                    .queryParam("search", fallbackQuery)
                    .queryParam("limit", 10)
                    .toUriString();

            String responseStr = fdaRestTemplate.getForObject(url, String.class);
            JsonNode response = objectMapper.readTree(responseStr);

            if (response == null || !response.has("results")) {
                return List.of();
            }

            return parseResults(response);
        } catch (Exception e) {
            log.warn("OpenFDA fallback query failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<FdaDrugSuggestion> parseResults(JsonNode response) {
        List<FdaDrugSuggestion> suggestions = new ArrayList<>();
        for (JsonNode result : response.get("results")) {
            String brandName = extractFirst(result, "openfda", "brand_name");
            String genericName = extractFirst(result, "openfda", "generic_name");
            String route = extractFirst(result, "openfda", "route");
            String indication = extractFirst(result, "indications_and_usage");
            if (indication == null) {
                indication = extractFirst(result, "description");
            }
            if (indication != null && indication.length() > 300) {
                indication = indication.substring(0, 300) + "...";
            }

            if (brandName == null && genericName == null) {
                String productElement = extractFirst(result, "spl_product_data_elements");
                if (productElement != null) {
                    String firstLine = productElement.split("\\n")[0].trim();
                    if (!firstLine.isBlank()) {
                        genericName = firstLine;
                    }
                }
            }

            if (brandName != null || genericName != null) {
                suggestions.add(new FdaDrugSuggestion(brandName, genericName, route, indication));
            }
        }
        return suggestions;
    }

    private String extractFirst(JsonNode node, String... path) {
        JsonNode current = node;
        for (String key : path) {
            if (current == null || !current.has(key)) return null;
            current = current.get(key);
        }
        if (current.isArray() && current.size() > 0) {
            return current.get(0).asText();
        } else if (current.isTextual()) {
            return current.asText();
        }
        return null;
    }

    private String escapeQuery(String input) {
        return input.replace("\"", "").replace("\\", "");
    }
}
