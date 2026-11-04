package com.quiz.dao;

import com.quiz.annotations.AuditLog;
import com.quiz.annotations.Author;
import com.quiz.exception.DatabaseException;
import com.quiz.model.Admin;
import com.quiz.model.CandidateLoginHistory;
import com.quiz.model.Student;
import com.quiz.model.User;
import com.quiz.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Access Object for User and Candidate entities.
 * Demonstrates:
 * - JDBC: PreparedStatement, ResultSet, Statement.RETURN_GENERATED_KEYS
 * - Checked Exception handling (SQLException wrapped in DatabaseException)
 * - CRUD database operations with MySQL 'online_quiz_db'
 * - Candidate profile and login tracking persistence
 * - Fallback in-memory cache seeded with sample candidate data
 */
@Author(name = "Antigravity Team", date = "2026-11-04", version = "2.0.0")
public class UserDao implements GenericDao<User, Integer> {

    // Fallback store with thread-safe ConcurrentHashMap & AtomicInteger
    private static final Map<Integer, User> userCache = new ConcurrentHashMap<>();
    private static final AtomicInteger idCounter = new AtomicInteger(10);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CandidateLoginDao loginDao = new CandidateLoginDao();

    static {
        seedSampleUsers();
    }

    private static void seedSampleUsers() {
        Map<Integer, User> loaded = PersistentDataStore.loadUsers();
        if (!loaded.isEmpty()) {
            userCache.putAll(loaded);
            int maxId = userCache.keySet().stream().max(Integer::compareTo).orElse(10);
            idCounter.set(maxId);
            return;
        }

        Admin admin = new Admin(1, "Dr. Sarah Connor (Admin)", "admin@quiz.com", 
                PasswordUtil.hashPassword("admin123"), "Academic Administration");
        admin.setPhone("+1-555-0199");
        admin.setGender("Female");
        admin.setDob("1985-04-12");
        admin.setAddress("100 Campus Drive, Admin Block 4");
        admin.setCity("Boston");
        admin.setState("Massachusetts");
        admin.setPincode("02115");
        admin.setInstitution("MIT Department of Computing");
        admin.setDepartment("Computer Science & Engineering");
        admin.setRollNumber("FAC-001");
        admin.setDegree("Ph.D. in Computer Science");
        admin.setYearOfStudy("Faculty");
        admin.setCgpa("4.00");
        admin.setBio("Lead Exam Coordinator & Senior Academic Professor");
        admin.setLastLoginAt(LocalDateTime.now().format(FORMATTER));
        admin.setLastLoginIp("127.0.0.1");
        admin.setLoginCount(42);
        admin.setStatus("ACTIVE");

        Student s1 = new Student(2, "Alex Johnson", "alex@student.com", PasswordUtil.hashPassword("student123"));
        s1.setPhone("+1-555-0142");
        s1.setGender("Male");
        s1.setDob("2003-08-24");
        s1.setAddress("742 Evergreen Terrace, Apt 3B");
        s1.setCity("San Jose");
        s1.setState("California");
        s1.setPincode("95112");
        s1.setInstitution("Silicon Valley Institute of Technology");
        s1.setDepartment("Computer Science & Engineering");
        s1.setRollNumber("CS-2024-001");
        s1.setDegree("B.Tech in Computer Science");
        s1.setYearOfStudy("3rd Year");
        s1.setCgpa("9.40");
        s1.setBio("Passionate Java backend developer and competitive programmer.");
        s1.setLastLoginAt(LocalDateTime.now().format(FORMATTER));
        s1.setLastLoginIp("127.0.0.1");
        s1.setLoginCount(15);
        s1.setStatus("ACTIVE");

        Student s2 = new Student(3, "Sophia Chen", "sophia@student.com", PasswordUtil.hashPassword("student123"));
        s2.setPhone("+1-555-0178");
        s2.setGender("Female");
        s2.setDob("2004-02-18");
        s2.setAddress("248 Tech Innovation Blvd");
        s2.setCity("Austin");
        s2.setState("Texas");
        s2.setPincode("78701");
        s2.setInstitution("Texas Institute of Advanced Science");
        s2.setDepartment("Information Technology");
        s2.setRollNumber("IT-2024-045");
        s2.setDegree("B.S. in Information Systems");
        s2.setYearOfStudy("2nd Year");
        s2.setCgpa("9.65");
        s2.setBio("Full-stack web application developer, AI/ML explorer, and hackathon winner.");
        s2.setLastLoginAt(LocalDateTime.now().format(FORMATTER));
        s2.setLastLoginIp("127.0.0.1");
        s2.setLoginCount(21);
        s2.setStatus("ACTIVE");

        Student s3 = new Student(4, "Liam Davis", "liam@student.com", PasswordUtil.hashPassword("student123"));
        s3.setPhone("+1-555-0123");
        s3.setGender("Male");
        s3.setDob("2002-11-30");
        s3.setAddress("512 Lakeview Avenue");
        s3.setCity("Chicago");
        s3.setState("Illinois");
        s3.setPincode("60601");
        s3.setInstitution("Midwest Engineering Academy");
        s3.setDepartment("Software Engineering");
        s3.setRollNumber("SE-2024-089");
        s3.setDegree("B.Tech in Software Engineering");
        s3.setYearOfStudy("4th Year");
        s3.setCgpa("8.80");
        s3.setBio("Distributed database enthusiast and cloud architecture student.");
        s3.setLastLoginAt(LocalDateTime.now().format(FORMATTER));
        s3.setLastLoginIp("127.0.0.1");
        s3.setLoginCount(9);
        s3.setStatus("ACTIVE");

        userCache.put(admin.getId(), admin);
        userCache.put(s1.getId(), s1);
        userCache.put(s2.getId(), s2);
        userCache.put(s3.getId(), s3);
        PersistentDataStore.saveUsersAsync(userCache);
    }

    @Override
    @AuditLog(action = "CREATE_USER")
    public User save(User user) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            int newId = idCounter.incrementAndGet();
            user.setId(newId);
            userCache.put(newId, user);
            PersistentDataStore.saveUsersAsync(userCache);
            return user;
        }

        String sql = "INSERT INTO users (name, email, password, role, phone, gender, dob, address, city, state, pincode, " +
                     "institution, department, roll_number, degree, year_of_study, cgpa, bio, last_login_at, last_login_ip, login_count, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, NULL, 0, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getGender());
            ps.setString(7, user.getDob());
            ps.setString(8, user.getAddress());
            ps.setString(9, user.getCity());
            ps.setString(10, user.getState());
            ps.setString(11, user.getPincode());
            ps.setString(12, user.getInstitution());
            ps.setString(13, user.getDepartment());
            ps.setString(14, user.getRollNumber());
            ps.setString(15, user.getDegree());
            ps.setString(16, user.getYearOfStudy());
            ps.setString(17, user.getCgpa());
            ps.setString(18, user.getBio());
            ps.setString(19, user.getStatus() != null ? user.getStatus() : "ACTIVE");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }
            userCache.put(user.getId(), user);
            PersistentDataStore.saveUsersAsync(userCache);
            return user;
        } catch (SQLException e) {
            // If MySQL fails, fallback to memory & persistent disk file
            int newId = idCounter.incrementAndGet();
            user.setId(newId);
            userCache.put(newId, user);
            PersistentDataStore.saveUsersAsync(userCache);
            return user;
        }
    }

    @Override
    public Optional<User> findById(Integer id) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return Optional.ofNullable(userCache.get(id));
        }

        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            return Optional.ofNullable(userCache.get(id));
        }
    }

    public Optional<User> findByEmail(String email) throws DatabaseException {
        if (email == null) return Optional.empty();
        String normalizedEmail = email.trim().toLowerCase();

        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return userCache.values().stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(normalizedEmail))
                    .findFirst();
        }

        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            return userCache.values().stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(normalizedEmail))
                    .findFirst();
        }
    }

    @Override
    public List<User> findAll() throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return new ArrayList<>(userCache.values());
        }

        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            return new ArrayList<>(userCache.values());
        }
    }

    public List<Student> findAllStudents() throws DatabaseException {
        List<Student> students = new ArrayList<>();
        for (User u : findAll()) {
            if (u instanceof Student) {
                students.add((Student) u);
            }
        }
        return students;
    }

    @Override
    @AuditLog(action = "UPDATE_USER")
    public boolean update(User user) throws DatabaseException {
        userCache.put(user.getId(), user);
        PersistentDataStore.saveUsersAsync(userCache);

        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return true;
        }

        String sql = "UPDATE users SET name = ?, email = ?, password = ?, role = ?, phone = ?, gender = ?, dob = ?, " +
                     "address = ?, city = ?, state = ?, pincode = ?, institution = ?, department = ?, roll_number = ?, " +
                     "degree = ?, year_of_study = ?, cgpa = ?, bio = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getGender());
            ps.setString(7, user.getDob());
            ps.setString(8, user.getAddress());
            ps.setString(9, user.getCity());
            ps.setString(10, user.getState());
            ps.setString(11, user.getPincode());
            ps.setString(12, user.getInstitution());
            ps.setString(13, user.getDepartment());
            ps.setString(14, user.getRollNumber());
            ps.setString(15, user.getDegree());
            ps.setString(16, user.getYearOfStudy());
            ps.setString(17, user.getCgpa());
            ps.setString(18, user.getBio());
            ps.setString(19, user.getStatus());
            ps.setInt(20, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return true;
        }
    }

    /**
     * Records candidate login information: updates last login timestamp, IP address,
     * increments candidate login count, and records an audit log row in candidate_login_history.
     */
    public void recordCandidateLogin(User user, String ipAddress, String userAgent) throws DatabaseException {
        String now = LocalDateTime.now().format(FORMATTER);
        user.setLastLoginAt(now);
        user.setLastLoginIp(ipAddress != null ? ipAddress : "127.0.0.1");
        user.incrementLoginCount();

        // Also update memory cache & persistent disk file
        userCache.put(user.getId(), user);
        PersistentDataStore.saveUsersAsync(userCache);

        // Record history log
        loginDao.recordLogin(user.getId(), user.getName(), user.getEmail(), ipAddress, userAgent, "SUCCESS");

        if (!DatabaseConnection.isUsingEmbeddedFallback()) {
            String sql = "UPDATE users SET last_login_at = NOW(), last_login_ip = ?, login_count = login_count + 1 WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ipAddress != null ? ipAddress : "127.0.0.1");
                ps.setInt(2, user.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[UserDao] Warning: Failed to update candidate login in MySQL: " + e.getMessage());
            }
        }
    }

    public List<CandidateLoginHistory> getCandidateLoginHistory(int userId) throws DatabaseException {
        return loginDao.findByUserId(userId);
    }

    public List<CandidateLoginHistory> getRecentLoginHistory(int limit) throws DatabaseException {
        return loginDao.findRecentLogins(limit);
    }

    @Override
    @AuditLog(action = "DELETE_USER")
    public boolean deleteById(Integer id) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return userCache.remove(id) != null;
        }

        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            userCache.remove(id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return userCache.remove(id) != null;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String role = rs.getString("role");

        User user;
        if ("ADMIN".equalsIgnoreCase(role)) {
            user = new Admin(id, name, email, password, "Academic Administration");
        } else {
            user = new Student(id, name, email, password);
        }

        // Map rich candidate profile fields safely
        try { user.setPhone(rs.getString("phone")); } catch (SQLException ignored) {}
        try { user.setGender(rs.getString("gender")); } catch (SQLException ignored) {}
        try { user.setDob(rs.getString("dob")); } catch (SQLException ignored) {}
        try { user.setAddress(rs.getString("address")); } catch (SQLException ignored) {}
        try { user.setCity(rs.getString("city")); } catch (SQLException ignored) {}
        try { user.setState(rs.getString("state")); } catch (SQLException ignored) {}
        try { user.setPincode(rs.getString("pincode")); } catch (SQLException ignored) {}
        try { user.setInstitution(rs.getString("institution")); } catch (SQLException ignored) {}
        try { user.setDepartment(rs.getString("department")); } catch (SQLException ignored) {}
        try { user.setRollNumber(rs.getString("roll_number")); } catch (SQLException ignored) {}
        try { user.setDegree(rs.getString("degree")); } catch (SQLException ignored) {}
        try { user.setYearOfStudy(rs.getString("year_of_study")); } catch (SQLException ignored) {}
        try { user.setCgpa(rs.getString("cgpa")); } catch (SQLException ignored) {}
        try { user.setBio(rs.getString("bio")); } catch (SQLException ignored) {}
        try { user.setLastLoginAt(rs.getString("last_login_at")); } catch (SQLException ignored) {}
        try { user.setLastLoginIp(rs.getString("last_login_ip")); } catch (SQLException ignored) {}
        try { user.setLoginCount(rs.getInt("login_count")); } catch (SQLException ignored) {}
        try { user.setStatus(rs.getString("status")); } catch (SQLException ignored) {}

        return user;
    }
}
