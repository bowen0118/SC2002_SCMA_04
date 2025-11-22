package edu.ntu.ccds.sc2002.model;

public class CompanyRep extends User {
    private String companyName;
    private String department;
    private String position;
    private boolean approved; // must be approved by Staff before login enabled

    public CompanyRep(String email, String name, String password, String companyName,
                      String department, String position, boolean approved) {
        super(email, name, password, Role.COMPANY_REP);
        this.companyName = companyName;
        this.department = department;
        this.position = position;
        this.approved = approved;
    }
    public String getCompanyName(){ return companyName; }
    public String getDepartment(){ return department; }
    public String getPosition(){ return position; }
    public boolean isApproved(){ return approved; }
    public void setApproved(boolean a){ this.approved = a; }

    @Override public String toString(){
        return super.toString() + " [" + companyName + " / " + department + " / " + position + (approved? " / APPROVED":" / PENDING") + "]";
    }
}
