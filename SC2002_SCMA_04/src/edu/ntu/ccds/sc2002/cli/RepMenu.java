package edu.ntu.ccds.sc2002.cli;

import edu.ntu.ccds.sc2002.model.*;
import edu.ntu.ccds.sc2002.repo.DataStore;
import edu.ntu.ccds.sc2002.service.ApplicationService;
import edu.ntu.ccds.sc2002.service.AuthService;
import edu.ntu.ccds.sc2002.service.InternshipService;

import java.time.LocalDate;
import java.util.Scanner;

public class RepMenu {
    private final DataStore db;
    private final AuthService auth;
    private final InternshipService internships;
    private final ApplicationService apps;

    public RepMenu(DataStore db,
                   AuthService auth,
                   InternshipService internships,
                   ApplicationService apps) {
        this.db = db;
        this.auth = auth;
        this.internships = internships;
        this.apps = apps;
    }

    public void run(CompanyRep r, Scanner sc) {
        while (true) {
            System.out.println("\n-- Company Rep Menu --");
            System.out.println("1) Create internship");
            System.out.println("2) Toggle visibility");
            System.out.println("3) View my internships + applications");
            System.out.println("4) Open applicant resume by application ID");
            System.out.println("5) Approve/Reject application");
            System.out.println("6) Edit draft internship");
            System.out.println("7) Delete draft internship");
            System.out.println("8) Change password");
            System.out.println("9) Logout");

            String c = sc.nextLine().trim();

            // ------------------------------------------------------------------
            // OPTION 1 – Create internship
            // ------------------------------------------------------------------
            if ("1".equals(c)) {
                try {
                    System.out.print("Title: ");
                    String title = sc.nextLine();
                    System.out.print("Description: ");
                    String desc = sc.nextLine();
                    System.out.print("Level (BASIC/INTERMEDIATE/ADVANCED): ");
                    InternshipLevel lvl = InternshipLevel.valueOf(sc.nextLine().trim().toUpperCase());
                    System.out.print("Preferred Major(s) (e.g., MAE,CSC or ALL): ");
                    String major = sc.nextLine().trim();
                    System.out.print("Open date (YYYY-MM-DD): ");
                    LocalDate open = LocalDate.parse(sc.nextLine().trim());
                    System.out.print("Close date (YYYY-MM-DD): ");
                    LocalDate close = LocalDate.parse(sc.nextLine().trim());
                    System.out.print("Slots (1..10): ");
                    int slots = Integer.parseInt(sc.nextLine().trim());

                    Internship i = internships.createByRep(r, title, desc, lvl, major, open, close, slots);
                    System.out.println("Created (PENDING approval): " + i);
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }
            }

            // ------------------------------------------------------------------
            // OPTION 2 – Toggle visibility
            // ------------------------------------------------------------------
            else if ("2".equals(c)) {
                System.out.print("Internship ID: ");
                String iid = sc.nextLine().trim();
                Internship resolved = db.resolveInternship(iid);
                System.out.println("→ Resolved to: " + (resolved == null ? "(none)" : resolved.getId()));
                System.out.print("Visible? (true/false): ");
                boolean v = Boolean.parseBoolean(sc.nextLine().trim());

                try {
                    internships.toggleVisibility(r, iid, v);
                    System.out.println("Updated.");
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }
            }

            // ------------------------------------------------------------------
            // OPTION 3 – View internships + applications automatically
            // ------------------------------------------------------------------
            else if ("3".equals(c)) {
                System.out.println("\n-- Your Internships + Applications --");

                for (Internship i : db.internships.values()) {
                    if (!i.getRepId().equals(r.getId()))
                        continue;

                    System.out.println("\n----------------------------------------");
                    System.out.println("Internship: " + i.getTitle() + " (" + i.getLevel() + ")");
                    System.out.println("ID: " + shorten(i.getId()));
                    System.out.println("Major(s): " + i.getPreferredMajor());
                    System.out.println("Slots: " + i.getSlots());
                    System.out.println("Status: " + i.getStatus());
                    System.out.println("Visible: " + i.isVisible());
                    System.out.println("\nApplications:");

                    var appsList = apps.applicationsForInternship(i.getId());
                    if (appsList.isEmpty()) {
                        System.out.println("  (none)");
                    } else {
                        for (Application a : appsList) {
                            System.out.println("  " + a);
                        }
                    }
                }

                System.out.println("----------------------------------------\n");
            }

            // ------------------------------------------------------------------
            // OPTION 4 – Open resume
            // ------------------------------------------------------------------
            else if ("4".equals(c)) {
                System.out.print("Application ID to open resume: ");
                String aid = sc.nextLine().trim();

                Application resolvedApp = db.resolveApplication(aid);
                System.out.println("→ Resolved to: " + (resolvedApp == null ? "(none)" : resolvedApp.getId()));

                if (resolvedApp == null) {
                    System.out.println("Failed: Application not found");
                    continue;
                }

                try {
                    apps.openResume(r, aid);
                } catch (Exception ex) {
                    System.out.println("Failed to open resume: " + ex.getMessage());
                }
            }

            // ------------------------------------------------------------------
            // OPTION 5 – Approve/Reject application
            // ------------------------------------------------------------------
            else if ("5".equals(c)) {
                System.out.print("Application ID: ");
                String aid = sc.nextLine().trim();

                Application resolvedApp = db.resolveApplication(aid);
                System.out.println("→ Resolved to: " + (resolvedApp == null ? "(none)" : resolvedApp.getId()));

                System.out.print("Approve? (true/false): ");
                boolean ok = Boolean.parseBoolean(sc.nextLine().trim());

                try {
                    apps.companyApproveOrReject(r, aid, ok);
                    System.out.println("Decision recorded.");
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }
            }

            // ------------------------------------------------------------------
            // OPTION 6 – Edit draft internship
            // ------------------------------------------------------------------
            else if ("6".equals(c)) {
                System.out.print("Internship ID: ");
                String iid = sc.nextLine().trim();
                Internship resolved = db.resolveInternship(iid);

                System.out.println("→ Resolved to: " + (resolved == null ? "(none)" : resolved.getId()));
                if (resolved == null) {
                    System.out.println("Not found.");
                    continue;
                }

                String title = promptDefault(sc, "Title", resolved.getTitle());
                String desc = promptDefault(sc, "Description", resolved.getDescription());
                InternshipLevel level = parseLevel(promptDefault(sc,
                        "Level (BASIC/INTERMEDIATE/ADVANCED)",
                        resolved.getLevel().name()));
                String major = promptDefault(sc, "Preferred Major(s)", resolved.getPreferredMajor());
                LocalDate open = parseDate(promptDefault(sc,
                        "Opening date (yyyy-MM-dd)",
                        resolved.getOpeningDate().toString()));
                LocalDate close = parseDate(promptDefault(sc,
                        "Closing date (yyyy-MM-dd)",
                        resolved.getClosingDate().toString()));
                int slots = Integer.parseInt(promptDefault(sc,
                        "Slots",
                        Integer.toString(resolved.getSlots())));

                try {
                    internships.updateInternshipDraft(r, iid, title, desc, level, major, open, close, slots);
                    System.out.println("Draft updated (Visible=false until you toggle).");
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }
            }

            // ------------------------------------------------------------------
            // OPTION 7 – Delete draft internship
            // ------------------------------------------------------------------
            else if ("7".equals(c)) {
                System.out.print("Internship ID: ");
                String iid = sc.nextLine().trim();
                Internship resolved = db.resolveInternship(iid);

                System.out.println("→ Resolved to: " + (resolved == null ? "(none)" : resolved.getId()));
                if (resolved == null) {
                    System.out.println("Not found.");
                    continue;
                }

                System.out.print("Confirm delete (yes/no): ");
                if (!"yes".equalsIgnoreCase(sc.nextLine().trim())) {
                    System.out.println("Cancelled.");
                    continue;
                }

                try {
                    internships.deleteInternshipDraft(r, iid);
                    System.out.println("Draft deleted.");
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }
            }

            // ------------------------------------------------------------------
            // OPTION 8 – Change password
            // ------------------------------------------------------------------
            else if ("8".equals(c)) {
                System.out.print("Enter new password: ");
                String np = sc.nextLine().trim();
                try {
                    auth.changePassword(r, np);
                    System.out.println("Password changed successfully. Please re-login to continue.");
                    return;
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }
            }

            // ------------------------------------------------------------------
            // OPTION 9 – Logout
            // ------------------------------------------------------------------
            else if ("9".equals(c)) {
                break;
            }
        }
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------
    private String promptDefault(Scanner sc, String label, String current) {
        System.out.print(label + " [" + current + "]: ");
        String s = sc.nextLine();
        return s.isBlank() ? current : s.trim();
    }
    
    private String shorten(String id) {
        if (id == null) return "(null)";
        return id.length() <= 8 ? id : id.substring(0, 8);
    }

    private LocalDate parseDate(String s) {
        return LocalDate.parse(s);
    }

    private InternshipLevel parseLevel(String s) {
        return InternshipLevel.valueOf(s.trim().toUpperCase());
    }
}
