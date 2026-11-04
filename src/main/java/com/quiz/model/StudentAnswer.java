package com.quiz.model;

import java.io.Serializable;

/**
 * Entity representing an individual question answer recorded for an attempt.
 */
public class StudentAnswer implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int attemptId;
    private int questionId;
    private String selectedAnswer; // "A", "B", "C", "D" or null if skipped

    public StudentAnswer() {
    }

    public StudentAnswer(int id, int attemptId, int questionId, String selectedAnswer) {
        this.id = id;
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedAnswer = selectedAnswer;
    }

    public StudentAnswer(int attemptId, int questionId, String selectedAnswer) {
        this(0, attemptId, questionId, selectedAnswer);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer != null ? selectedAnswer.trim().toUpperCase() : null;
    }
}

