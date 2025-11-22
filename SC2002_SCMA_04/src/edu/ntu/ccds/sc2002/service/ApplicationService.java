package edu.ntu.ccds.sc2002.service;

import edu.ntu.ccds.sc2002.model.*;
import edu.ntu.ccds.sc2002.repo.DataStore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ApplicationService {
    private final DataStore db;

    public ApplicationService(DataStore db) {
        this.db = db;
    }

    // ----------------------------------------------------------------------
    // Apply for internship + upload resume
    // ----------------------------------------------------------------------
    public Application apply(Student s, String internshipId, String resumePath) {
        // 1) Resolve internship
        Internship i = db.resolveInternship(internshipId);
        if (i == null) throw new IllegalArgumentException("Internship not found");

        // 2) Check internship is open & visible
        LocalDate today = LocalDate.now();
        if (!i.isVisible()
                || i.getStatus() != InternshipStatus.APPROVED
                || i.getOpeningDate().compareTo(today) > 0
                || i.getClosingDate().compareTo(today) < 0) {
            throw new IllegalStateException("Not open for application");
        }

        // 3) Check student eligibility (year vs level)
        if (s.getYear() <= 2 && i.getLevel() != InternshipLevel.BASIC)
            throw new IllegalStateException("Year 1-2 can only apply Basic");

        // 4) Check major eligibility (multi-major / ALL support)
        if (!isMajorEligibleForInternship(s, i)) {
            throw new IllegalStateException("Major not eligible");
        }

        // 5) Check concurrent application limit
        long active = db.countActiveApplicationsForStudent(s.getId());
        if (active >= 3) throw new IllegalStateException("Max 3 concurrent applications");

        // Optional: block duplicate applications to the same internship
        boolean alreadyApplied = db.applications.values().stream()
                .anyMatch(a ->
                        a.getStudentId().equals(s.getId()) &&
                        a.getInternshipId().equals(i.getId()) &&
                        (a.getStatus() == ApplicationStatus.PENDING ||
                         (a.getStatus() == ApplicationStatus.SUCCESSFUL && !a.isAccepted()))
                );
        if (alreadyApplied) {
            throw new IllegalStateException("You already have an active application for this internship");
        }

        // 6) Create application with real ID + timestamp
        String appId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Application app = new Application(
                appId,
                s.getId(),
                i.getId(),
                ApplicationStatus.PENDING,
                false,
                now
        );

        db.applications.put(app.getId(), app);
        db.saveApplications();

        // 7) Handle resume upload (optional but expected)
        if (resumePath != null && !resumePath.isBlank()) {
            try {
                // Strip surrounding quotes if user types "C:\path\My Resume.pdf"
                resumePath = resumePath.trim().replaceAll("^\"|\"$", "");

                Path src = Paths.get(resumePath);
                if (!Files.exists(src)) {
                    throw new IllegalArgumentException("Resume file not found: " + resumePath);
                }

                Path dest = db.getResumePath(
                        s.getId(),
                        app.getId(),
                        src.getFileName().toString()
                );

                Files.createDirectories(dest.getParent());
                Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Resume uploaded to: " + dest.toString());
            } catch (Exception e) {
                throw new RuntimeException("Application created but resume upload failed: " + e.getMessage(), e);
            }
        }

        return app;
    }

    // Helper for multi-major / ALL logic
    private boolean isMajorEligibleForInternship(Student s, Internship i) {
        String pref = i.getPreferredMajor();
        if (pref == null || pref.isBlank()) {
            // No restriction -> allow all
            return true;
        }

        pref = pref.trim();
        // Special keyword: ALL -> everyone allowed
        if ("ALL".equalsIgnoreCase(pref)) {
            return true;
        }

        String studentMajor = s.getMajor();
        // Allow formats like "MAE,CSC", "MAE;CSC", "MAE/CSC"
        return java.util.Arrays.stream(pref.split("[,;/]"))
                .map(String::trim)
                .filter(m -> !m.isEmpty())
                .anyMatch(m -> m.equalsIgnoreCase(studentMajor));
    }

    // ----------------------------------------------------------------------
    // Rep approve / reject
    // ----------------------------------------------------------------------
    public void companyApproveOrReject(CompanyRep rep, String applicationId, boolean approve) {
        Application a = db.resolveApplication(applicationId);
        if (a == null) throw new IllegalArgumentException("Application not found");

        Internship i = db.internships.get(a.getInternshipId());
        if (i == null || !i.getRepId().equalsIgnoreCase(rep.getId()))
            throw new IllegalArgumentException("Not your internship");

        a.setStatus(approve ? ApplicationStatus.SUCCESSFUL : ApplicationStatus.UNSUCCESSFUL);
    }

    // ----------------------------------------------------------------------
    // Student accepts offer
    // ----------------------------------------------------------------------
    public void studentAccept(Student s, String applicationId) {
        Application a = db.resolveApplication(applicationId);
        if (a == null || !a.getStudentId().equals(s.getId()))
            throw new IllegalArgumentException("Not found");

        if (a.getStatus() != ApplicationStatus.SUCCESSFUL)
            throw new IllegalStateException("Not successful yet");

        a.setAccepted(true);
        Internship i = db.internships.get(a.getInternshipId());
        if (i.getSlots() > 0) i.setSlots(i.getSlots() - 1);
        if (i.getSlots() == 0) i.setStatus(InternshipStatus.FILLED);

        // Withdraw all other applications from this student
        for (Application other : db.getApplicationsByStudent(s.getId())) {
            if (!other.getId().equals(a.getId())) {
                other.setStatus(ApplicationStatus.WITHDRAWN);
                other.setAccepted(false);
            }
        }
    }

    // ----------------------------------------------------------------------
    // Student requests withdrawal
    // ----------------------------------------------------------------------
    public void requestWithdrawal(Student s, String applicationId) {
        Application a = db.resolveApplication(applicationId);
        if (a == null) throw new IllegalArgumentException("Application not found");
        if (!a.getStudentId().equals(s.getId()))
            throw new IllegalArgumentException("Not your application");

        // Policy: allow withdrawing PENDING or SUCCESSFUL apps
        if (!(a.getStatus() == ApplicationStatus.PENDING || a.getStatus() == ApplicationStatus.SUCCESSFUL))
            throw new IllegalStateException("This application can't be withdrawn in its current state");

        a.setStatus(ApplicationStatus.WITHDRAW_REQUESTED);
    }

    // ----------------------------------------------------------------------
    // Rep opens resume (choice 4a in RepMenu)
    // ----------------------------------------------------------------------
    public void openResume(CompanyRep rep, String applicationId) {
        Application app = db.resolveApplication(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("Application not found");
        }

        Internship i = db.internships.get(app.getInternshipId());
        if (i == null || !i.getRepId().equalsIgnoreCase(rep.getId())) {
            throw new IllegalArgumentException("You are not allowed to view this application's resume");
        }

        java.nio.file.Path resume = db.findResumeForApplication(app);
        if (resume == null) {
            throw new IllegalStateException("Resume not uploaded or file missing");
        }

        try {
            if (!java.awt.Desktop.isDesktopSupported()) {
                throw new UnsupportedOperationException("Desktop open not supported on this system");
            }
            java.awt.Desktop.getDesktop().open(resume.toFile());
            System.out.println("Opening resume: " + resume);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open resume: " + e.getMessage(), e);
        }
    }

    // ----------------------------------------------------------------------
    // Queries
    // ----------------------------------------------------------------------
    public List<Application> applicationsForInternship(String internshipId) {
        return db.applications.values().stream()
                .filter(a -> a.getInternshipId().equals(internshipId))
                .collect(Collectors.toList());
    }

    public List<Application> applicationsForStudent(String studentId) {
        return db.getApplicationsByStudent(studentId);
    }
}
