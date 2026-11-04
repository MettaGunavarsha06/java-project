package com.quiz.model;

import java.io.Serializable;

/**
 * Model representing an audit record of a candidate's login attempt.
 * Stores login timestamp, IP address, user agent, and status.
 */
public class CandidateLoginHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private String candidateName;
    private String candidateEmail;
    private String loginTime;
    private String ipAddress;
    private String userAgent;
    private String loginStatus; // SUCCESS, FAILED, LOCKED

    public CandidateLoginHistory() {
    }

    public CandidateLoginHistory(int id, int userId, String candidateName, String candidateEmail,
                                 String loginTime, String ipAddress, String userAgent, String loginStatus) {
        this.id = id;
        this.userId = userId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.loginTime = loginTime;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.loginStatus = loginStatus;
    }

    public CandidateLoginHistory(int userId, String candidateName, String candidateEmail,
                                 String ipAddress, String userAgent, String loginStatus) {
        this(0, userId, candidateName, candidateEmail, null, ipAddress, userAgent, loginStatus);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }
}
