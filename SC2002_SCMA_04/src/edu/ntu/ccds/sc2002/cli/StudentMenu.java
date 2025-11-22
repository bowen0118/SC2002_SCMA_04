package edu.ntu.ccds.sc2002.cli;

import edu.ntu.ccds.sc2002.model.*;
import edu.ntu.ccds.sc2002.repo.DataStore;
import edu.ntu.ccds.sc2002.service.ApplicationService;
import edu.ntu.ccds.sc2002.service.AuthService;
import edu.ntu.ccds.sc2002.service.InternshipService;

import java.util.List;
import java.util.Scanner;

public class StudentMenu {
    private final DataStore db;
    private final AuthService auth;
    private final InternshipService internships;
    private final ApplicationService apps;

    public StudentMenu(DataStore db,
                       AuthService auth,
                       InternshipService internships,
                       ApplicationService apps) {
        this.db = db;
        this.auth = auth;
        this.internships = internships;
        this.apps = apps;
    }

    public void run(Student s, Scanner sc) {
        while (true) {
            System.out.println("\n-- Student Menu --");
            System.out.println("1) View eligible internships");
            System.out.println("1a) Internship recommendation");
            System.out.println("2) Apply");
            System.out.println("3) My applications");
            System.out.println("4) Accept offer");
            System.out.println("5) Request withdrawal");
            System.out.println("6) Change password");
            System.out.println("7) Logout");
            String c = sc.nextLine().trim();

            // --- 1) View eligible internships ---
            if ("1".equals(c)) {
                List<Internship> list = internships.visibleToStudent(s);
                if (list.isEmpty()) {
                    System.out.println("No internships visible at this time.");
                } else {
                    for (Internship i : list) System.out.println(i);
                }

            // --- 1a) Internship Recommendation ---
            } else if ("1a".equalsIgnoreCase(c)) {
                System.out.print("Enter keyword for internship search: ");
                String keyword = sc.nextLine().trim();

                List<Internship> recs = db.recommendInternships(keyword);

                System.out.println("\n--- Recommended Internships ---");
                if (recs.isEmpty()) {
                    System.out.println("No internships matched your keyword.");
                } else {
                    for (Internship i : recs) {
                        System.out.printf("[%s] %s (%s)\n", 
                                i.getId(), i.getTitle(), i.getCompanyName());
                    }
                }

            // --- 2) Apply for internship ---
            } else if ("2".equals(c)) {
                System.out.print("Enter Internship ID: ");
                String iid = sc.nextLine().trim();

                Internship resolved = db.resolveInternship(iid);
                System.out.println("→ Resolved to: " + (resolved == null ? "(none)" : resolved.getId()));

                System.out.print("Enter path of your resume file: ");
                String resumePath = sc.nextLine().trim();
                // Allow user to type with quotes "C:\path\My Resume.pdf"
                resumePath = resumePath.replaceAll("^\"|\"$", "");

                try {
                    Application a = apps.apply(s, iid, resumePath);
                    System.out.println("Applied: " + a);
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }

            // --- 3) My applications ---
            } else if ("3".equals(c)) {
                var my = apps.applicationsForStudent(s.getId());
                for (var a : my) System.out.println(a);

            // --- 4) Accept offer ---
            } else if ("4".equals(c)) {
                System.out.print("Enter Application ID to accept: ");
                String aid = sc.nextLine().trim();

                Application resolvedApp = db.resolveApplication(aid);
                System.out.println("→ Resolved to: " + 
                        (resolvedApp == null ? "(none)" : resolvedApp.getId()));

                try {
                    apps.studentAccept(s, aid);
                    System.out.println("Accepted. Other applications withdrawn automatically.");
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }

            // --- 5) Request withdrawal ---
            } else if ("5".equals(c)) {
                System.out.print("Enter Application ID to withdraw: ");
                String aid = sc.nextLine().trim();

                Application resolvedApp = db.resolveApplication(aid);
                System.out.println("→ Resolved to: " + 
                        (resolvedApp == null ? "(none)" : resolvedApp.getId()));

                try {
                    apps.requestWithdrawal(s, aid);
                    System.out.println("Withdrawal requested. Awaiting Career Center decision.");
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }

            // --- 6) Change password ---
            } else if ("6".equals(c)) {
                System.out.print("Enter new password: ");
                String np = sc.nextLine().trim();
                try {
                    auth.changePassword(s, np);
                    System.out.println("Password changed successfully. Please re-login to continue.");
                    return;
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }

            // --- 7) Logout ---
            } else if ("7".equals(c)) {
                break;
            }
        }
    }
}
