package com.quiz.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity representing a Multiple-Choice Question (MCQ).
 * Demonstrates encapsulation, validation, and object state management.
 */
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int quizId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer; // "A", "B", "C", "D"

    public Question() {
    }

    public Question(int id, int quizId, String questionText, String optionA, String optionB, String optionC, String optionD, String correctAnswer) {
        setId(id);
        setQuizId(quizId);
        setQuestionText(questionText);
        setOptionA(optionA);
        setOptionB(optionB);
        setOptionC(optionC);
        setOptionD(optionD);
        setCorrectAnswer(correctAnswer);
    }

    public Question(int quizId, String questionText, String optionA, String optionB, String optionC, String optionD, String correctAnswer) {
        this(0, quizId, questionText, optionA, optionB, optionC, optionD, correctAnswer);
    }

    // Getters and Setters with Validation
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Question ID cannot be negative");
        }
        this.id = id;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        if (quizId < 0) {
            throw new IllegalArgumentException("Quiz ID cannot be negative");
        }
        this.quizId = quizId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        if (questionText == null || questionText.trim().isEmpty()) {
            throw new IllegalArgumentException("Question text cannot be blank");
        }
        this.questionText = questionText.trim();
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        if (optionA == null || optionA.trim().isEmpty()) {
            throw new IllegalArgumentException("Option A cannot be empty");
        }
        this.optionA = optionA.trim();
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        if (optionB == null || optionB.trim().isEmpty()) {
            throw new IllegalArgumentException("Option B cannot be empty");
        }
        this.optionB = optionB.trim();
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        if (optionC == null || optionC.trim().isEmpty()) {
            throw new IllegalArgumentException("Option C cannot be empty");
        }
        this.optionC = optionC.trim();
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        if (optionD == null || optionD.trim().isEmpty()) {
            throw new IllegalArgumentException("Option D cannot be empty");
        }
        this.optionD = optionD.trim();
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        if (correctAnswer == null) {
            throw new IllegalArgumentException("Correct answer cannot be null");
        }
        String formatted = correctAnswer.trim().toUpperCase();
        if (!formatted.matches("[A-D]")) {
            throw new IllegalArgumentException("Correct answer must be one of: A, B, C, D (Found: " + correctAnswer + ")");
        }
        this.correctAnswer = formatted;
    }

    public boolean isCorrect(String selectedAnswer) {
        if (selectedAnswer == null) return false;
        return this.correctAnswer.equalsIgnoreCase(selectedAnswer.trim());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        Question question = (Question) o;
        return id == question.id && quizId == question.quizId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quizId);
    }

    @Override
    public String toString() {
        return String.format("Question[id=%d, quizId=%d, text='%s', correct='%s']", 
                id, quizId, questionText.length() > 30 ? questionText.substring(0, 27) + "..." : questionText, correctAnswer);
    }
}

