package edu.ntu.ccds.sc2002.model;

public class Staff extends User {
    private String department;
    public Staff(String id, String name, String password, String department){
        super(id, name, password, Role.STAFF);
        this.department = department;
    }
    public String getDepartment(){ return department; }
    @Override public String toString(){
        return super.toString() + " [" + department + "]";
    }
}
