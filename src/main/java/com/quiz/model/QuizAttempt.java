package com.quiz.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

/**
 * Entity representing an individual Quiz Attempt record.
 */
public class QuizAttempt implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int studentId;
    private int quizId;
    private int score;
    private double percentage;
    private Timestamp attemptDate;
    
    // Supplementary joined fields for rich presentation
    private String studentName;
    private String studentEmail;
    private String quizTitle;
    private int totalMarks;

    public QuizAttempt() {
        this.attemptDate = new Timestamp(new Date().getTime());
    }

    public QuizAttempt(int id, int studentId, int quizId, int score, double percentage, Timestamp attemptDate) {
        this.id = id;
        this.studentId = studentId;
        this.quizId = quizId;
        this.score = score;
        this.percentage = percentage;
        this.attemptDate = attemptDate != null ? attemptDate : new Timestamp(new Date().getTime());
    }

    public QuizAttempt(int studentId, int quizId, int score, double percentage) {
        this(0, studentId, quizId, score, percentage, new Timestamp(new Date().getTime()));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Timestamp getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(Timestamp attemptDate) {
        this.attemptDate = attemptDate;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuizAttempt)) return false;
        QuizAttempt that = (QuizAttempt) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

