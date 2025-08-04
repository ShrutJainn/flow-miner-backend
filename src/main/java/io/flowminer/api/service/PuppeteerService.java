package io.flowminer.api.service;

import io.flowminer.api.dto.ScreenshotRequestDTO;
import io.flowminer.api.dto.ScreenshotResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PuppeteerService {
    private final RestTemplate restTemplate;

    @Autowired
    PuppeteerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public ScreenshotResponse takeScreenshot(String url) {
        String nodeServiceUrl = "http://localhost:4000/screenshot";
        ScreenshotRequestDTO request = new ScreenshotRequestDTO(url);

        return restTemplate.postForObject(
                nodeServiceUrl,
                request,
                ScreenshotResponse.class
        );
    }


}
