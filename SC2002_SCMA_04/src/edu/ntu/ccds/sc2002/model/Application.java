package edu.ntu.ccds.sc2002.model;
import java.time.LocalDateTime;
import java.util.UUID;

public class Application {
    private String id; // UUID
    private String studentId;
    private String internshipId;
    private ApplicationStatus status; // Pending, Successful, Unsuccessful, Withdrawn
    private boolean accepted; // student accepted placement
    private LocalDateTime createdAt;

    public Application(String id, String studentId, String internshipId, ApplicationStatus status, boolean accepted, LocalDateTime createdAt){
        this.id = (id==null || id.isEmpty()) ? UUID.randomUUID().toString() : id;
        this.studentId = studentId;
        this.internshipId = internshipId;
        this.status = status;
        this.accepted = accepted;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }
    public String getId(){ return id; }
    public String getStudentId(){ return studentId; }
    public String getInternshipId(){ return internshipId; }
    public ApplicationStatus getStatus(){ return status; }
    public void setStatus(ApplicationStatus s){ this.status = s; }
    public boolean isAccepted(){ return accepted; }
    public void setAccepted(boolean a){ this.accepted = a; }
    public LocalDateTime getCreatedAt(){ return createdAt; }

    @Override
    public String toString() {
        return "App[" + shorten(id) + "] " +
                "stu=" + studentId +
                " int=" + shorten(internshipId) +
                " " + status +
                (accepted ? " (ACCEPTED)" : "");
    }

    private String shorten(String id) {
        if (id == null) return "(null)";
        return id.length() <= 8 ? id : id.substring(0, 8);
    }
}
