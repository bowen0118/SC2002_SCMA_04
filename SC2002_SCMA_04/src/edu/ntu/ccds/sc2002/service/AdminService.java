package edu.ntu.ccds.sc2002.service;

import edu.ntu.ccds.sc2002.model.*;
import edu.ntu.ccds.sc2002.repo.DataStore;

public class AdminService {
    private final DataStore db;
    public AdminService(DataStore db){ this.db = db; }

    public void approveRep(String email, boolean approve){
        CompanyRep r = db.reps.get(email);
        if(r==null) throw new IllegalArgumentException("Rep not found");
        r.setApproved(approve);
    }

    public void approveInternship(String internshipId, boolean approve) {
        // Resolve short or full UUID, case-insensitive
        Internship i = db.resolveInternship(internshipId);
        if (i == null) throw new IllegalArgumentException("Internship not found");

        if (approve) {
            i.setStatus(InternshipStatus.APPROVED);
            i.setVisible(true);          // <-- auto-publish on approval
        } else {
            i.setStatus(InternshipStatus.REJECTED);
            i.setVisible(false);         // hide if rejected
        }
    }

    // Withdrawal request simulated by flipping status With Staff approval
    public void processWithdrawal(String applicationId, boolean approve) {
        Application a = db.resolveApplication(applicationId);
        if (a == null) throw new IllegalArgumentException("Application not found");

        if (a.getStatus() != ApplicationStatus.WITHDRAW_REQUESTED)
            throw new IllegalStateException("No withdrawal is pending for this application");

        if (!approve) {
            // Revert to the last valid state before the request.
            // If your app tracks "accepted" separately, keep SUCCESSFUL.
            a.setStatus(ApplicationStatus.SUCCESSFUL);
            return;
        }

        // Approve → finalize and free slot if needed
        a.setStatus(ApplicationStatus.WITHDRAWN);
        a.setAccepted(false);

        Internship i = db.internships.get(a.getInternshipId());
        if (i != null) {
            boolean wasFilled = (i.getStatus() == InternshipStatus.FILLED);
            i.setSlots(i.getSlots() + 1);                // free 1 slot
            if (wasFilled && i.getSlots() > 0) {
                i.setStatus(InternshipStatus.APPROVED);  // reopen if previously filled
            }
        }
    }
}
