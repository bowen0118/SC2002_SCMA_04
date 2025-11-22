package edu.ntu.ccds.sc2002.cli;

import edu.ntu.ccds.sc2002.model.*;
import edu.ntu.ccds.sc2002.repo.DataStore;
import edu.ntu.ccds.sc2002.service.AdminService;
import edu.ntu.ccds.sc2002.service.AuthService;

import java.util.Scanner;

public class StaffMenu {
    private final DataStore db;
    private final AuthService auth;
    private final AdminService admin;

    public StaffMenu(DataStore db,
                     AuthService auth,
                     AdminService admin) {
        this.db = db;
        this.auth = auth;
        this.admin = admin;
    }

    public void run(Staff st, Scanner sc) {
        while (true) {
            System.out.println("\n-- Staff Menu --");
            System.out.println("1) Approve/Reject Rep  2) Approve/Reject Internship  3) Process Withdrawal  4) Generate Report  5) Change password  6) Logout");
            String c = sc.nextLine().trim();

            if ("1".equals(c)) {
                System.out.println("All reps:");
                for (CompanyRep r : db.reps.values()) System.out.println(r);
                System.out.print("Rep email: ");
                String email = sc.nextLine().trim();
                System.out.print("Approve? (true/false): ");
                boolean ok = Boolean.parseBoolean(sc.nextLine().trim());
                try {
                    admin.approveRep(email, ok);
                    System.out.println("Updated.");
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }

            } else if ("2".equals(c)) {
                for (Internship i : db.internships.values()) System.out.println(i);
                System.out.print("Internship ID: ");
                String iid = sc.nextLine().trim();
                Internship resolved = db.resolveInternship(iid);
                System.out.println("→ Resolved to: " + (resolved == null ? "(none)" : resolved.getId()));
                System.out.print("Approve? (true/false): ");
                boolean ok = Boolean.parseBoolean(sc.nextLine().trim());
                try {
                    admin.approveInternship(iid, ok);
                    System.out.println("Updated.");
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }

            } else if ("3".equals(c)) {
                db.applications.values().stream()
                        .filter(a -> a.getStatus() == ApplicationStatus.WITHDRAW_REQUESTED)
                        .forEach(a -> System.out.println(a));

                System.out.print("Application ID: ");
                String aid = sc.nextLine().trim();

                Application resolvedApp = db.resolveApplication(aid);
                System.out.println("→ Resolved to: " + (resolvedApp == null ? "(none)" : resolvedApp.getId()));

                System.out.print("Approve withdrawal? (true/false): ");
                boolean ok = Boolean.parseBoolean(sc.nextLine().trim());

                try {
                    admin.processWithdrawal(aid, ok);
                    System.out.println("Decision recorded.");
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }

            } else if ("4".equals(c)) {
                System.out.print("Filter by Status (PENDING/APPROVED/REJECTED/FILLED or *): ");
                String fs = sc.nextLine().trim();
                System.out.print("Filter by Major (e.g., MAE or *): ");
                String fm = sc.nextLine().trim();
                System.out.print("Filter by Level (BASIC/INTERMEDIATE/ADVANCED or *): ");
                String fl = sc.nextLine().trim();

                for (Internship i : db.internships.values()) {
                    boolean ok =
                            (fs.equals("*") || i.getStatus().name().equalsIgnoreCase(fs)) &&
                            (fm.equals("*") || i.getPreferredMajor().equalsIgnoreCase(fm)) &&
                            (fl.equals("*") || i.getLevel().name().equalsIgnoreCase(fl));
                    if (ok) System.out.println(i);
                }

            } else if ("5".equals(c)) {
                System.out.print("Enter new password: ");
                String np = sc.nextLine().trim();
                try {
                    auth.changePassword(st, np);
                    System.out.println("Password changed successfully. Please re-login to continue.");
                    return;
                } catch (Exception ex) {
                    System.out.println("Failed: " + ex.getMessage());
                }

            } else if ("6".equals(c)) {
                break;
            }
        }
    }
}