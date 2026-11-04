package com.quiz.dao;

import com.quiz.exception.DatabaseException;
import com.quiz.model.CandidateLoginHistory;

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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Data Access Object for candidate login history audit logs.
 * Demonstrates:
 * - JDBC: PreparedStatement, ResultSet
 * - Exception handling
 * - Fallback thread-safe concurrent storage
 */
public class CandidateLoginDao {

    private static final List<CandidateLoginHistory> memoryHistory = new CopyOnWriteArrayList<>();
    private static final AtomicInteger idCounter = new AtomicInteger(100);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        seedSampleLogins();
    }

    private static void seedSampleLogins() {
        memoryHistory.add(new CandidateLoginHistory(1, 2, "Alex Johnson", "alex@student.com",
                LocalDateTime.now().minusDays(2).format(FORMATTER), "127.0.0.1", "Mozilla/5.0 Chrome/120.0", "SUCCESS"));
        memoryHistory.add(new CandidateLoginHistory(2, 2, "Alex Johnson", "alex@student.com",
                LocalDateTime.now().minusDays(1).format(FORMATTER), "127.0.0.1", "Mozilla/5.0 Chrome/120.0", "SUCCESS"));
        memoryHistory.add(new CandidateLoginHistory(3, 3, "Sophia Chen", "sophia@student.com",
                LocalDateTime.now().minusDays(1).format(FORMATTER), "127.0.0.1", "Mozilla/5.0 Firefox/121.0", "SUCCESS"));
    }

    public CandidateLoginHistory recordLogin(int userId, String name, String email, String ip, String userAgent, String status) throws DatabaseException {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        CandidateLoginHistory record = new CandidateLoginHistory(0, userId, name, email, timestamp, ip, userAgent, status);

        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            record.setId(idCounter.incrementAndGet());
            memoryHistory.add(0, record);
            return record;
        }

        String sql = "INSERT INTO candidate_login_history (user_id, login_time, ip_address, user_agent, login_status) VALUES (?, NOW(), ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, ip != null ? ip : "127.0.0.1");
            ps.setString(3, userAgent != null ? userAgent : "Unknown Browser");
            ps.setString(4, status != null ? status : "SUCCESS");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    record.setId(rs.getInt(1));
                }
            }
            return record;
        } catch (SQLException e) {
            // Log and fallback to memory
            record.setId(idCounter.incrementAndGet());
            memoryHistory.add(0, record);
            return record;
        }
    }

    public List<CandidateLoginHistory> findByUserId(int userId) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return memoryHistory.stream()
                    .filter(h -> h.getUserId() == userId)
                    .collect(Collectors.toList());
        }

        List<CandidateLoginHistory> list = new ArrayList<>();
        String sql = "SELECT h.id, h.user_id, u.name, u.email, h.login_time, h.ip_address, h.user_agent, h.login_status " +
                     "FROM candidate_login_history h JOIN users u ON h.user_id = u.id " +
                     "WHERE h.user_id = ? ORDER BY h.login_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CandidateLoginHistory(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("login_time"),
                            rs.getString("ip_address"),
                            rs.getString("user_agent"),
                            rs.getString("login_status")
                    ));
                }
            }
            return list;
        } catch (SQLException e) {
            // fallback
            return memoryHistory.stream()
                    .filter(h -> h.getUserId() == userId)
                    .collect(Collectors.toList());
        }
    }

    public List<CandidateLoginHistory> findRecentLogins(int limit) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return memoryHistory.stream().limit(limit).collect(Collectors.toList());
        }

        List<CandidateLoginHistory> list = new ArrayList<>();
        String sql = "SELECT h.id, h.user_id, u.name, u.email, h.login_time, h.ip_address, h.user_agent, h.login_status " +
                     "FROM candidate_login_history h JOIN users u ON h.user_id = u.id " +
                     "ORDER BY h.login_time DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CandidateLoginHistory(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("login_time"),
                            rs.getString("ip_address"),
                            rs.getString("user_agent"),
                            rs.getString("login_status")
                    ));
                }
            }
            return list;
        } catch (SQLException e) {
            return memoryHistory.stream().limit(limit).collect(Collectors.toList());
        }
    }
}
