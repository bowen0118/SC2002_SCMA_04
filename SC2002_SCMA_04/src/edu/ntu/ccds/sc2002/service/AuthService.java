package edu.ntu.ccds.sc2002.service;

import edu.ntu.ccds.sc2002.model.*;
import edu.ntu.ccds.sc2002.repo.DataStore;

public class AuthService {
    private final DataStore db;
    public AuthService(DataStore db){ this.db = db; }

    public User login(String id, String password){
        // Students
        if (db.students.containsKey(id)) {
            Student s = db.students.get(id);
            if (s.getPassword().equals(password)) return s;
            System.out.println("Incorrect password.");
            return null;
        }

        // Staff
        if (db.staff.containsKey(id)) {
            Staff s = db.staff.get(id);
            if (s.getPassword().equals(password)) return s;
            System.out.println("Incorrect password.");
            return null;
        }

        // Company reps
        if (db.reps.containsKey(id)) {
            CompanyRep r = db.reps.get(id);
            if (r.getPassword().equals(password)) return r;
            System.out.println("Incorrect password.");
            return null;
        }

        // ID not found in any map
        System.out.println("ID does not exist.");
        return null;
    }

    public boolean changePassword(User u, String newPass){
        u.setPassword(newPass);
        if(u.getRole()==Role.STUDENT) db.students.put(u.getId(), (Student)u);
        if(u.getRole()==Role.STAFF) db.staff.put(u.getId(), (Staff)u);
        if(u.getRole()==Role.COMPANY_REP) db.reps.put(u.getId(), (CompanyRep)u);
        return true;
    }

    public CompanyRep registerRep(String email, String name, String company, String dept, String pos){
        CompanyRep r = new CompanyRep(email, name, "password", company, dept, pos, false);
        db.reps.put(email, r);
        return r;
    }
}