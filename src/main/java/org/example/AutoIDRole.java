package org.example;

import java.util.ArrayList;

public class AutoIDRole {
    String temp_role_namme;
    String normalized_role_name;
    long member_count;
    long assignment_count;
    long entitlement_count;
    String role_id;
    String status;
    ArrayList<String> entitlements;
    ArrayList<String> justifications;
    public AutoIDRole (String temp_role_namme, String normalized_role_name,
                       long member_count, long assignment_count,long entitlement_count,
                       String role_id, String status, ArrayList<String> entitlements,
                       ArrayList<String> justifications) {
        this.temp_role_namme = temp_role_namme;
        this. normalized_role_name = normalized_role_name;
        this.member_count = member_count;
        this.assignment_count = assignment_count;
        this.entitlement_count = entitlement_count;
        this.entitlements = entitlements;
        this.justifications = justifications;
    }

    public ArrayList<String> getJustifications(){
        return justifications;
    }
    public ArrayList<String> getEntitlements() {
        return entitlements;
    }

    public long getAssignment_count() {
        return assignment_count;
    }

    public long getEntitlement_count() {
        return entitlement_count;
    }

    public long getMember_count() {
        return member_count;
    }

    public String getRole_id() {
        return role_id;
    }

    public String getNormalized_role_name() {
        return normalized_role_name;
    }

    public String getStatus() {
        return status;
    }

    public String getTemp_role_namme() {
        return temp_role_namme;
    }
}
