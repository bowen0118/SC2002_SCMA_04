	package edu.ntu.ccds.sc2002.cli;
	
	import edu.ntu.ccds.sc2002.model.*;
	import edu.ntu.ccds.sc2002.repo.DataStore;
	import edu.ntu.ccds.sc2002.service.*;
	import java.nio.file.Paths;
	import java.util.Scanner;
	
	public class Menu {
	    private final DataStore db;
	    private final AuthService auth;
	    private final InternshipService internships;
	    private final ApplicationService apps;
	    private final AdminService admin;
	
	    public Menu(String dataPath) {
	        this.db = new DataStore(Paths.get(dataPath));
	        this.db.loadAll();
	        this.auth = new AuthService(db);
	        this.internships = new InternshipService(db);
	        this.apps = new ApplicationService(db);
	        this.admin = new AdminService(db);
	    }
	
	    public void run() {
	        Scanner sc = new Scanner(System.in);
	        System.out.println("=== Internship Placement Management System (CLI) ===");
	
	        StudentMenu studentMenu = new StudentMenu(db, auth, internships, apps);
	        RepMenu repMenu = new RepMenu(db, auth, internships, apps);
	        StaffMenu staffMenu = new StaffMenu(db, auth, admin);
	
	        while (true) {
	            System.out.println("1) Login  2) Register Company Rep  3) Exit");
	            String choice = sc.nextLine().trim();
	
	            if ("1".equals(choice)) {
	                System.out.print("User ID/Email: ");
	                String id = sc.nextLine().trim();
	                System.out.print("Password: ");
	                String pw = sc.nextLine().trim();
	
	                User u = auth.login(id, pw);
	                if (u == null) {
	                    System.out.println("Login failed.");
	                } else {
	                    System.out.println("Welcome, " + u.getName() + " [" + u.getRole() + "]");
	                    if (u.getRole() == Role.STUDENT) {
	                        studentMenu.run((Student) u, sc);
	                    } else if (u.getRole() == Role.COMPANY_REP) {
	                        repMenu.run((CompanyRep) u, sc);
	                    } else if (u.getRole() == Role.STAFF) {
	                        staffMenu.run((Staff) u, sc);
	                    }
	                }
	
	            } else if ("2".equals(choice)) {
	                System.out.println("== Register Company Representative ==");
	                System.out.print("Company Email (ID): ");
	                String email = sc.nextLine().trim();
	                System.out.print("Name: ");
	                String name = sc.nextLine().trim();
	                System.out.print("Company Name: ");
	                String company = sc.nextLine().trim();
	                System.out.print("Department: ");
	                String dept = sc.nextLine().trim();
	                System.out.print("Position: ");
	                String pos = sc.nextLine().trim();
	
	                CompanyRep r = auth.registerRep(email, name, company, dept, pos);
	                System.out.println("Registered. Await staff approval. Default password is 'password'.");
	
	            } else if ("3".equals(choice)) {
	                System.out.println("Saving...");
	                db.saveAll();
	                System.out.println("Goodbye.");
	                break;
	            }
	        }
	    }
	}