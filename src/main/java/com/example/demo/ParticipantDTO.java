package com.example.demo;


public class ParticipantDTO {

    private Integer participantID;

    public ParticipantDTO() {}

    public ParticipantDTO(Integer participantID) {
        this.participantID = participantID;
    }

    public Integer getParticipantID() {
        return participantID;
    }

    public void setParticipantID(Integer participantID) {
        this.participantID = participantID;
    }
}
