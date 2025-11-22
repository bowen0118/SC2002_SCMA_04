package edu.ntu.ccds.sc2002.model;
public enum ApplicationStatus {
    PENDING,
    SUCCESSFUL,           // offer issued by rep
    UNSUCCESSFUL,
    WITHDRAW_REQUESTED,   // <-- student initiated
    WITHDRAWN             // <-- staff approved
}