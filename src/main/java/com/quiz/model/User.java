package com.quiz.model;

import com.quiz.interfaces.Authenticatable;
import com.quiz.interfaces.QuizOperations;
import com.quiz.util.PasswordUtil;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base class representing a system user and candidate.
 * Demonstrates:
 * - Abstract classes & inheritance
 * - Encapsulation (private fields, getters & setters with validation)
 * - Constructor overloading
 * - Interface implementation (Authenticatable, QuizOperations, Serializable)
 * - Input validation throwing unchecked IllegalArgumentException
 * - Full candidate profile and login information management
 */
public abstract class User implements Authenticatable, QuizOperations, Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String email;
    private String password; // SHA-256 hashed password
    private String role;     // "ADMIN" or "STUDENT"

    // Candidate Profile Details
    private String phone = "";
    private String gender = "";
    private String dob = "";
    private String address = "";
    private String city = "";
    private String state = "";
    private String pincode = "";
    private String institution = "";
    private String department = "";
    private String rollNumber = "";
    private String degree = "";
    private String yearOfStudy = "";
    private String cgpa = "";
    private String bio = "";

    // Candidate Login & Audit Tracking Details
    private String lastLoginAt = "";
    private String lastLoginIp = "";
    private int loginCount = 0;
    private String status = "ACTIVE";

    // Default Constructor
    public User() {
    }

    // Parameterized Constructor
    public User(int id, String name, String email, String password, String role) {
        setId(id);
        setName(name);
        setEmail(email);
        setPassword(password);
        setRole(role);
    }

    public User(String name, String email, String password, String role) {
        this(0, name, email, password, role);
    }

    // Abstract methods demonstrating Polymorphism
    public abstract String getRoleDescription();

    @Override
    public boolean authenticate(String rawPassword) {
        if (rawPassword == null || this.password == null) {
            return false;
        }
        return PasswordUtil.verifyPassword(rawPassword, this.password);
    }

    // Getters and Setters with Encapsulation & Validation
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("User ID cannot be negative");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }
        this.name = name.trim();
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Invalid email address format: " + email);
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        this.password = password;
    }

    @Override
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("STUDENT"))) {
            throw new IllegalArgumentException("Role must be either ADMIN or STUDENT");
        }
        this.role = role.toUpperCase();
    }

    // Candidate Profile Getters & Setters
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : "";
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender != null ? gender.trim() : "";
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob != null ? dob.trim() : "";
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address != null ? address.trim() : "";
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city != null ? city.trim() : "";
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state != null ? state.trim() : "";
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode != null ? pincode.trim() : "";
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution != null ? institution.trim() : "";
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department != null ? department.trim() : "";
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber != null ? rollNumber.trim() : "";
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree != null ? degree.trim() : "";
    }

    public String getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(String yearOfStudy) {
        this.yearOfStudy = yearOfStudy != null ? yearOfStudy.trim() : "";
    }

    public String getCgpa() {
        return cgpa;
    }

    public void setCgpa(String cgpa) {
        this.cgpa = cgpa != null ? cgpa.trim() : "";
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio != null ? bio.trim() : "";
    }

    // Candidate Login & Audit Tracking Getters & Setters
    public String getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(String lastLoginAt) {
        this.lastLoginAt = lastLoginAt != null ? lastLoginAt.trim() : "";
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp != null ? lastLoginIp.trim() : "";
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = Math.max(0, loginCount);
    }

    public void incrementLoginCount() {
        this.loginCount++;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status.trim().toUpperCase() : "ACTIVE";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, name='%s', email='%s', role='%s', roll='%s', college='%s']", 
                id, name, email, role, rollNumber, institution);
    }
}
