package edu.ntu.ccds.sc2002.model;
import java.time.LocalDate;
import java.util.UUID;

public class Internship {
    private String id; // UUID
    private String title;
    private String description;
    private InternshipLevel level;
    private String preferredMajor;
    private LocalDate openingDate;
    private LocalDate closingDate;
    private InternshipStatus status;
    private String companyName;
    private String repId; // creator
    private int slots;
    private boolean visible;

    public Internship(String id, String title, String description, InternshipLevel level, String preferredMajor,
                      LocalDate openingDate, LocalDate closingDate, InternshipStatus status,
                      String companyName, String repId, int slots, boolean visible){
        this.id = (id==null || id.isEmpty()) ? UUID.randomUUID().toString() : id;
        this.title = title;
        this.description = description;
        this.level = level;
        this.preferredMajor = preferredMajor;
        this.openingDate = openingDate;
        this.closingDate = closingDate;
        this.status = status;
        this.companyName = companyName;
        this.repId = repId;
        this.slots = slots;
        this.visible = visible;
    }
    public String getId(){ return id; }
    public String getTitle(){ return title; }
    public String getDescription(){ return description; }
    public InternshipLevel getLevel(){ return level; }
    public String getPreferredMajor(){ return preferredMajor; }
    public LocalDate getOpeningDate(){ return openingDate; }
    public LocalDate getClosingDate(){ return closingDate; }
    public InternshipStatus getStatus(){ return status; }
    public void setStatus(InternshipStatus s){ this.status = s; }
    public String getCompanyName(){ return companyName; }
    public String getRepId(){ return repId; }
    public int getSlots(){ return slots; }
    public void setSlots(int s){ this.slots = s; }
    public boolean isVisible(){ return visible; }
    public void setVisible(boolean v){ this.visible = v; }
 // --- Safe draft editing (only when still pending) ---
    public void ensurePending() {
        if (this.status != InternshipStatus.PENDING) {
            throw new IllegalStateException("Internship already decided by Career Staff; cannot modify.");
        }
    }

    /** Edit allowed fields while the posting is still PENDING. Also turns visible=false. */
    public void editDraft(
            String title,
            String description,
            InternshipLevel level,
            String preferredMajor,
            java.time.LocalDate openingDate,
            java.time.LocalDate closingDate,
            int slots
    ) {
        ensurePending();
        if (slots <= 0) throw new IllegalArgumentException("Slots must be > 0");
        if (openingDate.isAfter(closingDate)) {
            throw new IllegalArgumentException("Opening date must be on/before closing date.");
        }

        this.title = title;
        this.description = description;
        this.level = level;
        this.preferredMajor = preferredMajor;
        this.openingDate = openingDate;
        this.closingDate = closingDate;
        this.slots = slots;

        // Edits create a new draft state that must be re-toggled by rep
        this.visible = false;
    }

    // Optional granular setters (each enforces PENDING)
    public void setTitle(String title) { ensurePending(); this.title = title; this.visible = false; }
    public void setDescription(String description) { ensurePending(); this.description = description; this.visible = false; }
    public void setLevel(InternshipLevel level) { ensurePending(); this.level = level; this.visible = false; }
    public void setPreferredMajor(String preferredMajor) { ensurePending(); this.preferredMajor = preferredMajor; this.visible = false; }
    public void setOpeningDate(java.time.LocalDate openingDate) {
        ensurePending();
        if (openingDate.isAfter(this.closingDate)) throw new IllegalArgumentException("Opening date after current closing date.");
        this.openingDate = openingDate; this.visible = false;
    }
    public void setClosingDate(java.time.LocalDate closingDate) {
        ensurePending();
        if (this.openingDate.isAfter(closingDate)) throw new IllegalArgumentException("Closing date before current opening date.");
        this.closingDate = closingDate; this.visible = false;
    }


    @Override public String toString(){
        return String.format("[%s] %s (%s) %s | Major:%s | %s→%s | Slots:%d | %s | Visible:%s",
            id.substring(0,8), title, level, companyName, preferredMajor, openingDate, closingDate, slots, status, visible);
    }
}
