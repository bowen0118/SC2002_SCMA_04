package edu.ntu.ccds.sc2002.repo;

import edu.ntu.ccds.sc2002.model.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DataStore {
    private final Path dataDir;

    public Map<String, Student> students = new HashMap<>();
    public Map<String, Staff> staff = new HashMap<>();
    public Map<String, CompanyRep> reps = new HashMap<>();
    public Map<String, Internship> internships = new LinkedHashMap<>();
    public Map<String, Application> applications = new LinkedHashMap<>();

    public DataStore(Path dataDir){
        this.dataDir = dataDir;
        try { Files.createDirectories(dataDir); } catch (IOException ignored){}
    }

    private Path p(String name){ return dataDir.resolve(name); }

    public void loadAll(){
        loadStudents(); loadStaff(); loadReps(); loadInternships(); loadApplications();
    }
    public void saveAll(){
        saveStudents(); saveStaff(); saveReps(); saveInternships(); saveApplications();
    }

    // ---------- Students CSV: id,name,password,year,major ----------
    public void loadStudents(){
        students.clear();
        Path f = p("sample_student_list_FIXED.csv");
        if(!Files.exists(f)) return;
        try(BufferedReader br = Files.newBufferedReader(f)){
            String line; boolean header=true;
            while((line=br.readLine())!=null){
                if(header){ header=false; continue; }
                String[] a = line.split(",", -1);
                if(a.length<5) continue;
                Student s = new Student(a[0], a[1], a[2], Integer.parseInt(a[3]), a[4]);
                students.put(s.getId(), s);
            }
        }catch(Exception e){ e.printStackTrace(); }
    }
    public void saveStudents(){
        Path f = p("sample_student_list_FIXED.csv");
        try(PrintWriter pw = new PrintWriter(Files.newBufferedWriter(f))){
            pw.println("id,name,password,year,major");
            for(Student s: students.values()){
                pw.printf("%s,%s,%s,%d,%s%n", s.getId(), s.getName(), s.getPassword(), s.getYear(), s.getMajor());
            }
        }catch(Exception e){ e.printStackTrace(); }
    }

    // ---------- Staff CSV: id,name,password,department ----------
    public void loadStaff(){
        staff.clear();
        Path f = p("sample_staff_list_FIXED.csv");
        if(!Files.exists(f)) return;
        try(BufferedReader br = Files.newBufferedReader(f)){
            String line; boolean header=true;
            while((line=br.readLine())!=null){
                if(header){ header=false; continue; }
                String[] a = line.split(",", -1);
                if(a.length<4) continue;
                Staff s = new Staff(a[0], a[1], a[2], a[3]);
                staff.put(s.getId(), s);
            }
        }catch(Exception e){ e.printStackTrace(); }
    }
    public void saveStaff(){
        Path f = p("sample_staff_list_FIXED.csv");
        try(PrintWriter pw = new PrintWriter(Files.newBufferedWriter(f))){
            pw.println("id,name,password,department");
            for(Staff s: staff.values()){
                pw.printf("%s,%s,%s,%s%n", s.getId(), s.getName(), s.getPassword(), s.getDepartment());
            }
        }catch(Exception e){ e.printStackTrace(); }
    }

    // ---------- Reps CSV: email,name,password,company,department,position,approved ----------
    public void loadReps(){
        reps.clear();
        Path f = p("sample_company_representative_list_FIXED.csv");
        if(!Files.exists(f)) return;
        try(BufferedReader br = Files.newBufferedReader(f)){
            String line; boolean header=true;
            while((line=br.readLine())!=null){
                if(header){ header=false; continue; }
                String[] a = line.split(",", -1);
                if(a.length<7) continue;
                CompanyRep r = new CompanyRep(a[0], a[1], a[2], a[3], a[4], a[5], Boolean.parseBoolean(a[6]));
                reps.put(r.getId(), r);
            }
        }catch(Exception e){ e.printStackTrace(); }
    }
    public void saveReps(){
        Path f = p("sample_company_representative_list_FIXED.csv");
        try(PrintWriter pw = new PrintWriter(Files.newBufferedWriter(f))){
            pw.println("email,name,password,company,department,position,approved");
            for(CompanyRep r: reps.values()){
                pw.printf("%s,%s,%s,%s,%s,%s,%s%n", r.getId(), r.getName(), r.getPassword(), r.getCompanyName(), r.getDepartment(), r.getPosition(), r.isApproved());
            }
        }catch(Exception e){ e.printStackTrace(); }
    }
    
 // ---------- Internship Recommendation ----------
 // Returns a list of internships whose title or description matches the given keyword.
 public List<Internship> recommendInternships(String keyword) {
     if (keyword == null || keyword.isBlank()) return Collections.emptyList();

     String key = keyword.trim().toLowerCase();

     return internships.values().stream()
             .filter(i -> i.isVisible()) // only show visible internships
             .filter(i -> 
                 i.getTitle().toLowerCase().contains(key) ||
                 i.getDescription().toLowerCase().contains(key)
             )
             .sorted(Comparator.comparing(Internship::getOpeningDate).reversed())
             .collect(Collectors.toList());
 }
 
 public Path getResumePath(String studentId, String applicationId, String originalFilename) {
	    // Ensure directory exists: data/resumes/<studentId>/
	    Path dir = dataDir.resolve("resumes").resolve(studentId);
	    try { Files.createDirectories(dir); } catch (IOException ignored) {}

	    // Save as: <applicationId> - <original filename>
	    return dir.resolve(applicationId + " - " + originalFilename);
	}

    // ---------- Internships CSV ----------
    // id,title,description,level,major,open,close,status,company,repId,slots,visible
    public void loadInternships(){
        internships.clear();
        Path f = p("internships.csv");
        if(!Files.exists(f)) return;
        try(BufferedReader br = Files.newBufferedReader(f)){
            String line; boolean header=true;
            while((line=br.readLine())!=null){
                if(header){ header=false; continue; }
                String[] a = line.split(",", -1);
                if(a.length<12) continue;
                Internship i = new Internship(
                    a[0], a[1], a[2],
                    InternshipLevel.valueOf(a[3]), a[4],
                    LocalDate.parse(a[5]), LocalDate.parse(a[6]),
                    InternshipStatus.valueOf(a[7]), a[8], a[9],
                    Integer.parseInt(a[10]), Boolean.parseBoolean(a[11])
                );
                internships.put(i.getId(), i);
            }
        }catch(Exception e){ e.printStackTrace(); }
    }
    public void saveInternships(){
        Path f = p("internships.csv");
        try(PrintWriter pw = new PrintWriter(Files.newBufferedWriter(f))){
            pw.println("id,title,description,level,major,open,close,status,company,repId,slots,visible");
            for(Internship i: internships.values()){
                pw.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%s%n",
                    i.getId(), escape(i.getTitle()), escape(i.getDescription()), i.getLevel(), i.getPreferredMajor(),
                    i.getOpeningDate(), i.getClosingDate(), i.getStatus(), i.getCompanyName(), i.getRepId(),
                    i.getSlots(), i.isVisible());
            }
        }catch(Exception e){ e.printStackTrace(); }
    }
    private String escape(String s){
        if(s==null) return "";
        return s.replace(",", ";");
    }

    // ---------- Applications CSV ----------
    // id,studentId,internshipId,status,accepted,createdAt
    public void loadApplications(){
        applications.clear();
        Path f = p("applications.csv");
        if(!Files.exists(f)) return;
        try(BufferedReader br = Files.newBufferedReader(f)){
            String line; boolean header=true;
            while((line=br.readLine())!=null){
                if(header){ header=false; continue; }
                String[] a = line.split(",", -1);
                if(a.length<6) continue;
                Application app = new Application(a[0], a[1], a[2], ApplicationStatus.valueOf(a[3]), Boolean.parseBoolean(a[4]), LocalDateTime.parse(a[5]));
                applications.put(app.getId(), app);
            }
        }catch(Exception e){ e.printStackTrace(); }
    }
    public void saveApplications(){
        Path f = p("applications.csv");
        try(PrintWriter pw = new PrintWriter(Files.newBufferedWriter(f))){
            pw.println("id,studentId,internshipId,status,accepted,createdAt");
            for(Application a: applications.values()){
                pw.printf("%s,%s,%s,%s,%s,%s%n",
                    a.getId(), a.getStudentId(), a.getInternshipId(), a.getStatus(), a.isAccepted(), a.getCreatedAt());
            }
        }catch(Exception e){ e.printStackTrace(); }
    }

    // Helpers
    public List<Application> getApplicationsByStudent(String studentId){
        return applications.values().stream().filter(a -> a.getStudentId().equals(studentId)).collect(Collectors.toList());
    }
    public long countActiveApplicationsForStudent(String studentId){
        return applications.values().stream().filter(a -> a.getStudentId().equals(studentId))
                .filter(a -> a.getStatus()==ApplicationStatus.PENDING || a.getStatus()==ApplicationStatus.SUCCESSFUL && !a.isAccepted())
                .count();
    }
    public long countAcceptedForInternship(String internshipId){
        return applications.values().stream().filter(a -> a.getInternshipId().equals(internshipId) && a.isAccepted()).count();
    }
    
    public Internship resolveInternship(String anyId) {
        if (anyId == null) return null;
        String key = anyId.replace("[", "").replace("]", "").trim();
        for (Internship t : internships.values()) {
            String id = t.getId();
            if (id.equalsIgnoreCase(key) || id.toLowerCase().startsWith(key.toLowerCase())) {
                return t;
            }
        }
        return null;
    }

    public Application resolveApplication(String anyId) {
        if (anyId == null) return null;
        String key = anyId.replace("[", "").replace("]", "").trim();
        for (Application a : applications.values()) {
            String id = a.getId();
            if (id.equalsIgnoreCase(key) || id.toLowerCase().startsWith(key.toLowerCase())) {
                return a;
            }
        }
        return null;
    }
    
 // Where resumes will be stored: data/resumes/<studentId>/<applicationId> - OriginalFileName.pdf
    public java.nio.file.Path getResumePath(String studentId, String applicationId, String originalFilename) {
        java.nio.file.Path dir = dataDir.resolve("resumes").resolve(studentId);
        try {
            java.nio.file.Files.createDirectories(dir);
        } catch (java.io.IOException ignored) {}
        return dir.resolve(applicationId + " - " + originalFilename);
    }

    // Find resume file for a given application (based on naming pattern)
    public java.nio.file.Path findResumeForApplication(Application app) {
        java.nio.file.Path dir = dataDir.resolve("resumes").resolve(app.getStudentId());
        if (!java.nio.file.Files.exists(dir)) {
            return null;
        }
        try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.list(dir)) {
            String prefix = app.getId() + " - ";
            return files
                    .filter(p -> p.getFileName().toString().startsWith(prefix))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
