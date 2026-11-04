package com.quiz.service;

import com.quiz.dao.UserDao;
import com.quiz.exception.AuthenticationException;
import com.quiz.exception.DatabaseException;
import com.quiz.model.Admin;
import com.quiz.model.CandidateLoginHistory;
import com.quiz.model.Student;
import com.quiz.model.User;
import com.quiz.util.PasswordUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service managing user authentication, registration, candidate profile data,
 * login audit tracking, and active sessions.
 * Demonstrates:
 * - Session tracking using Collections (ConcurrentHashMap)
 * - Checked Exception handling (AuthenticationException, DatabaseException)
 * - Secure password hashing
 * - Candidate profile and login tracking management
 */
public class AuthService {

    private final UserDao userDao;
    private final Map<String, User> activeSessions = new ConcurrentHashMap<>();

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User login(String email, String password) throws AuthenticationException, DatabaseException {
        return login(email, password, "127.0.0.1", "Standard Web Browser");
    }

    public User login(String email, String password, String ipAddress, String userAgent) throws AuthenticationException, DatabaseException {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            throw new AuthenticationException("Email and password must not be empty.");
        }

        Optional<User> userOpt = userDao.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("No account registered with email: " + email);
        }

        User user = userOpt.get();
        if (!user.authenticate(password)) {
            throw new AuthenticationException("Invalid password provided.");
        }

        // Record candidate login details and audit trail in MySQL
        userDao.recordCandidateLogin(user, ipAddress, userAgent);

        return user;
    }

    public User registerStudent(String name, String email, String password) throws AuthenticationException, DatabaseException {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("password", password);
        return registerCandidate(data);
    }

    public User registerCandidate(Map<String, Object> data) throws AuthenticationException, DatabaseException {
        String name = (String) data.get("name");
        String email = (String) data.get("email");
        String password = (String) data.get("password");

        if (name == null || name.trim().isEmpty()) {
            throw new AuthenticationException("Candidate name cannot be blank.");
        }
        if (email == null || !email.contains("@")) {
            throw new AuthenticationException("A valid email address is required.");
        }
        if (password == null || password.length() < 6) {
            throw new AuthenticationException("Password must be at least 6 characters long.");
        }

        Optional<User> existing = userDao.findByEmail(email.trim());
        if (existing.isPresent()) {
            throw new AuthenticationException("An account with email " + email + " already exists.");
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        Student student = new Student(name.trim(), email.trim(), hashedPassword);

        // Populate candidate profile details
        if (data.containsKey("phone")) student.setPhone((String) data.get("phone"));
        if (data.containsKey("gender")) student.setGender((String) data.get("gender"));
        if (data.containsKey("dob")) student.setDob((String) data.get("dob"));
        if (data.containsKey("address")) student.setAddress((String) data.get("address"));
        if (data.containsKey("city")) student.setCity((String) data.get("city"));
        if (data.containsKey("state")) student.setState((String) data.get("state"));
        if (data.containsKey("pincode")) student.setPincode((String) data.get("pincode"));
        if (data.containsKey("institution")) student.setInstitution((String) data.get("institution"));
        if (data.containsKey("department")) student.setDepartment((String) data.get("department"));
        if (data.containsKey("rollNumber")) student.setRollNumber((String) data.get("rollNumber"));
        if (data.containsKey("roll_number")) student.setRollNumber((String) data.get("roll_number"));
        if (data.containsKey("degree")) student.setDegree((String) data.get("degree"));
        if (data.containsKey("yearOfStudy")) student.setYearOfStudy((String) data.get("yearOfStudy"));
        if (data.containsKey("year_of_study")) student.setYearOfStudy((String) data.get("year_of_study"));
        if (data.containsKey("cgpa")) student.setCgpa((String) data.get("cgpa"));
        if (data.containsKey("bio")) student.setBio((String) data.get("bio"));

        return userDao.save(student);
    }

    public User updateCandidateProfile(int userId, Map<String, Object> data) throws AuthenticationException, DatabaseException {
        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Candidate not found.");
        }

        User user = userOpt.get();
        if (data.containsKey("name") && data.get("name") != null) user.setName((String) data.get("name"));
        if (data.containsKey("phone")) user.setPhone((String) data.get("phone"));
        if (data.containsKey("gender")) user.setGender((String) data.get("gender"));
        if (data.containsKey("dob")) user.setDob((String) data.get("dob"));
        if (data.containsKey("address")) user.setAddress((String) data.get("address"));
        if (data.containsKey("city")) user.setCity((String) data.get("city"));
        if (data.containsKey("state")) user.setState((String) data.get("state"));
        if (data.containsKey("pincode")) user.setPincode((String) data.get("pincode"));
        if (data.containsKey("institution")) user.setInstitution((String) data.get("institution"));
        if (data.containsKey("department")) user.setDepartment((String) data.get("department"));
        if (data.containsKey("rollNumber")) user.setRollNumber((String) data.get("rollNumber"));
        if (data.containsKey("roll_number")) user.setRollNumber((String) data.get("roll_number"));
        if (data.containsKey("degree")) user.setDegree((String) data.get("degree"));
        if (data.containsKey("yearOfStudy")) user.setYearOfStudy((String) data.get("yearOfStudy"));
        if (data.containsKey("year_of_study")) user.setYearOfStudy((String) data.get("year_of_study"));
        if (data.containsKey("cgpa")) user.setCgpa((String) data.get("cgpa"));
        if (data.containsKey("bio")) user.setBio((String) data.get("bio"));

        userDao.update(user);
        return user;
    }

    public List<CandidateLoginHistory> getCandidateLoginHistory(int userId) throws DatabaseException {
        return userDao.getCandidateLoginHistory(userId);
    }

    public List<Student> getAllCandidates(String query) throws DatabaseException {
        List<Student> students = userDao.findAllStudents();
        if (query == null || query.trim().isEmpty()) {
            return students;
        }

        String q = query.trim().toLowerCase();
        return students.stream()
                .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(q)) ||
                             (s.getEmail() != null && s.getEmail().toLowerCase().contains(q)) ||
                             (s.getRollNumber() != null && s.getRollNumber().toLowerCase().contains(q)) ||
                             (s.getInstitution() != null && s.getInstitution().toLowerCase().contains(q)) ||
                             (s.getDepartment() != null && s.getDepartment().toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }

    public String createSession(User user) {
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, user);
        return token;
    }

    public User getUserBySession(String token) {
        if (token == null) return null;
        return activeSessions.get(token);
    }

    public void invalidateSession(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }
}
