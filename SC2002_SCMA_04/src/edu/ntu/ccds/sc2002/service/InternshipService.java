package edu.ntu.ccds.sc2002.service;

import edu.ntu.ccds.sc2002.model.*;
import edu.ntu.ccds.sc2002.repo.DataStore;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InternshipService {
    private final DataStore db;
    public InternshipService(DataStore db){ this.db = db; }

    public Internship createByRep(CompanyRep rep, String title, String desc, InternshipLevel level,
                                  String major, LocalDate open, LocalDate close, int slots){
        if(db.internships.values().stream().filter(i -> Objects.equals(i.getRepId(), rep.getId())).count() >= 5)
            throw new IllegalArgumentException("Max 5 internships per rep");
        if(slots<1 || slots>10) throw new IllegalArgumentException("Slots must be 1..10");
        Internship i = new Internship(null, title, desc, level, major, open, close, InternshipStatus.PENDING,
                rep.getCompanyName(), rep.getId(), slots, false);
        db.internships.put(i.getId(), i);
        return i;
    }

    public List<Internship> visibleToStudent(Student s) {
        return db.internships.values().stream()
            .filter(i -> i.isVisible())
            .filter(i -> i.getStatus() == InternshipStatus.APPROVED)
            .filter(i -> isMajorEligible(s.getMajor(), i.getPreferredMajor()))
            .filter(i -> isLevelAllowed(s.getYear(), i.getLevel()))
            .collect(Collectors.toList());
    }

    private boolean isMajorEligible(String studentMajor, String pref) {
        if (pref == null || pref.isBlank()) return true;
        if (pref.equalsIgnoreCase("ALL")) return true;

        return java.util.Arrays.stream(pref.split("[,;/]"))
                .map(String::trim)
                .anyMatch(m -> m.equalsIgnoreCase(studentMajor));
    }

    private boolean isLevelAllowed(int year, InternshipLevel level) {
        // Year 1–2 → Basic only
        if (year <= 2)
            return level == InternshipLevel.BASIC;

        // Year 3–4 → All levels allowed
        return true;
    }
    
    public void toggleVisibility(CompanyRep rep, String internshipId, boolean visible){
        Internship i = db.resolveInternship(internshipId);
        if (i == null || !i.getRepId().equals(rep.getId()))
            throw new IllegalArgumentException("Not found or not owner");
        i.setVisible(visible);
    }
 // Edit fields of a PENDING internship that belongs to this rep
    public void updateInternshipDraft(
            CompanyRep rep,
            String anyId,                 // short or full id
            String newTitle,
            String newDesc,
            InternshipLevel newLevel,
            String newPreferredMajor,
            java.time.LocalDate newOpen,
            java.time.LocalDate newClose,
            int newSlots) {

        Internship i = db.resolveInternship(anyId);
        if (i == null) throw new IllegalArgumentException("Internship not found");

        // Only the owner rep can edit
        if (!rep.getId().equals(i.getRepId()))
            throw new IllegalArgumentException("Not your internship");

        // Only while still pending staff decision
        if (i.getStatus() != InternshipStatus.PENDING)
            throw new IllegalStateException("Already decided by Career Staff; cannot modify");

        // Basic validations
        if (newSlots <= 0) throw new IllegalArgumentException("Slots must be > 0");
        if (newOpen.isAfter(newClose))
            throw new IllegalArgumentException("Opening date must be on/before closing date");

        // Apply edits
        i.setTitle(newTitle);
        i.setDescription(newDesc);
        i.setLevel(newLevel);
        i.setPreferredMajor(newPreferredMajor);
        i.setOpeningDate(newOpen);
        i.setClosingDate(newClose);
        i.setSlots(newSlots);

        // Keep drafts hidden until rep explicitly toggles
        i.setVisible(false);
    }

    // Remove a PENDING internship that belongs to this rep
    public void deleteInternshipDraft(CompanyRep rep, String anyId) {
        Internship i = db.resolveInternship(anyId);
        if (i == null) throw new IllegalArgumentException("Internship not found");

        if (!rep.getId().equals(i.getRepId()))
            throw new IllegalArgumentException("Not your internship");

        if (i.getStatus() != InternshipStatus.PENDING)
            throw new IllegalStateException("Already decided by Career Staff; cannot delete");

        // Ensure there are no applications attached (policy: drafts only)
        boolean hasApps = db.applications.values().stream()
                .anyMatch(a -> a.getInternshipId().equals(i.getId()));
        if (hasApps)
            throw new IllegalStateException("Cannot delete: applications already exist");

        db.internships.remove(i.getId());
    }
}
