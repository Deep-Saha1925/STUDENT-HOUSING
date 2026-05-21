package com.deep.studenthousing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
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



        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

}
