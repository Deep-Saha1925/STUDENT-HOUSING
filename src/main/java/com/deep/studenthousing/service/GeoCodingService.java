package com.deep.studenthousing.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
public class GeoCodingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── Forward geocoding: city + area → lat/lng ──
    public double[] getCoordinates(String city, String area) {
        try {
            String query = area + ", " + city + ", India";

            String url = UriComponentsBuilder
                    .fromHttpUrl("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("limit", "1")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "StudentHousingApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());

            if (root.isArray() && !root.isEmpty()) {
                double lat = root.get(0).get("lat").asDouble();
                double lon = root.get(0).get("lon").asDouble(); // ✅ Fixed: was root.get(1)
                return new double[]{lat, lon};
            }

        } catch (Exception e) {
            System.err.println("Forward geocoding failed for: " + city + ", " + area);
            e.printStackTrace();
        }
        return null;
    }

    // ── Reverse geocoding: lat/lng → city + area ──
    public Map<String, String> getCityAndArea(double lat, double lng) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://nominatim.openstreetmap.org/reverse")
                    .queryParam("lat", lat)
                    .queryParam("lon", lng)
                    .queryParam("format", "json")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "StudentHousingApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode address = root.get("address");

            // Extract city — Nominatim may return different field names
            String city = null;
            for (String field : new String[]{"city", "town", "village", "county", "state_district"}) {
                if (address.has(field)) {
                    city = address.get(field).asText();
                    break;
                }
            }

            // Extract area/neighbourhood
            String area = null;
            for (String field : new String[]{"suburb", "neighbourhood", "quarter", "residential"}) {
                if (address.has(field)) {
                    area = address.get(field).asText();
                    break;
                }
            }

            Map<String, String> result = new HashMap<>();
            result.put("city", city != null ? city : "");
            result.put("area", area != null ? area : "");

            System.out.println("Reverse geocoded → city: " + city + ", area: " + area);
            return result;

        } catch (Exception e) {
            System.err.println("Reverse geocoding failed for: lat=" + lat + ", lng=" + lng);
            e.printStackTrace();
            return Map.of("city", "", "area", "");
        }
    }
}