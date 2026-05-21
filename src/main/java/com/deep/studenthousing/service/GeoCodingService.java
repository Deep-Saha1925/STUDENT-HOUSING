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

@Service
public class GeoCodingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

//    Converting city+area into geolocation
    public double[] getCoordinates(String city, String area){
        try{
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

            if(root.isArray() && !root.isEmpty()){
                double lat = root.get(0).get("lat").asDouble();
                double lon = root.get(1).get("lon").asDouble();
                return new double[]{lat, lon};
            }

        } catch (Exception e) {
            System.err.println("Geocoding failed for: " + city + ", " + area);
            e.printStackTrace();
        }

        return null;
    }

}
