package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    @Value("${server.port}")
    private Integer CURRENT_ID;

    @Value("${participants.left.port}")
    private Integer LEFT_PORT = 0;

    @Value("${participants.right.port}")
    private Integer RIGHT_PORT = 0;

    private String LEFT_URL;
    private String RIGHT_URL;

    private final Set<Integer> participants = new HashSet<>();

    @PostConstruct
    public void postConstruct() {
        String urlTemplate = "http://localhost:%d/whoAmI";
        participants.add(CURRENT_ID);
        LEFT_URL = String.format(urlTemplate, LEFT_PORT);
        RIGHT_URL = String.format(urlTemplate, RIGHT_PORT);
        scheduledExecutorService.schedule(this::sendMyNetworkParticipantsToLeftNeighbour, 10, TimeUnit.SECONDS);
        scheduledExecutorService.schedule(this::sendMyNetworkParticipantsToRightNeighbour, 10, TimeUnit.SECONDS);
        System.out.println("initialization complete");
    }

    @PostMapping("/whoAmI")
    public void whoAmI(@RequestBody ParticipantDTO participant) {
        if (participant != null) {
            if (!participants.contains(participant.getParticipantID())) {
                participants.add(participant.getParticipantID());
                sendMyNetworkParticipantsToLeftNeighbour();
                sendMyNetworkParticipantsToRightNeighbour();
            }
        }
    }

    @GetMapping("/chainSize")
    public Integer chainSize() {
        return participants.size();
    }

    private void sendMyNetworkParticipantsToLeftNeighbour() {
        if(LEFT_PORT != 0) {
            send(participants, LEFT_URL);
        }

    }

    private void sendMyNetworkParticipantsToRightNeighbour() {
        if(RIGHT_PORT != 0) {
            send(participants, RIGHT_URL);
        }
    }


    private void send(Set<Integer> participants, String url) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        for (Integer participant : participants) {
            ParticipantDTO participantDTO = new ParticipantDTO(participant);
            HttpEntity<ParticipantDTO> request = new HttpEntity<>(participantDTO, headers);

            try {
                restTemplate.postForObject(url, request, ParticipantDTO.class);
            } catch (Exception ignored) {}

        }
    }

}
