package edu.ntu.ccds.sc2002.model;
import java.util.Objects;

public abstract class User {
    protected String id;
    protected String name;
    protected String password;
    protected Role role;

    protected User(String id, String name, String password, Role role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public void setPassword(String p){ this.password = p; }

    @Override public String toString(){
        return role + "{" + id + ", " + name + "}";
    }
    @Override public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof User)) return false;
        User u=(User)o;
        return Objects.equals(id, u.id);
    }
    @Override public int hashCode(){ return Objects.hash(id); }
}
