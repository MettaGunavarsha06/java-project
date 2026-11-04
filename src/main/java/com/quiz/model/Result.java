package com.quiz.model;

import com.quiz.interfaces.Reportable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Result evaluation model capturing detailed performance statistics.
 * Demonstrates:
 * - Complex domain object modeling
 * - Implementation of Serializable for serialization and file persistence
 * - Implementation of Reportable for text report generation
 * - Formatting and business calculations
 */
public class Result implements Reportable, Serializable {
    private static final long serialVersionUID = 1L;

    private int attemptId;
    private int studentId;
    private String studentName;
    private String studentEmail;
    private int quizId;
    private String quizTitle;

    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private int unansweredQuestions;

    private int totalMarks;
    private int scoreObtained;
    private double percentage;
    private boolean passed;
    private int timeTakenSeconds;
    private Date attemptDate;

    // Detailed question reviews
    private List<QuestionReview> reviews = new ArrayList<>();

    public Result() {
        this.attemptDate = new Date();
    }

    public Result(int attemptId, int studentId, String studentName, int quizId, String quizTitle,
                  int totalQuestions, int correctAnswers, int wrongAnswers, int unansweredQuestions,
                  int totalMarks, int scoreObtained, double percentage, boolean passed, int timeTakenSeconds) {
        this.attemptId = attemptId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.unansweredQuestions = unansweredQuestions;
        this.totalMarks = totalMarks;
        this.scoreObtained = scoreObtained;
        this.percentage = percentage;
        this.passed = passed;
        this.timeTakenSeconds = timeTakenSeconds;
        this.attemptDate = new Date();
    }

    public void addReview(QuestionReview review) {
        this.reviews.add(review);
    }

    @Override
    public String getReportTitle() {
        return "Student Quiz Performance Report - " + studentName + " (" + quizTitle + ")";
    }

    @Override
    public String generateReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================================\n");
        sb.append("                 ONLINE QUIZ SYSTEM - RESULT REPORT                 \n");
        sb.append("====================================================================\n");
        sb.append(String.format("Student Name   : %s (ID: %d)\n", studentName, studentId));
        sb.append(String.format("Email Address  : %s\n", studentEmail != null ? studentEmail : "N/A"));
        sb.append(String.format("Quiz Title     : %s (ID: %d)\n", quizTitle, quizId));
        sb.append(String.format("Date Taken     : %s\n", attemptDate));
        sb.append(String.format("Time Taken     : %d minutes %d seconds\n", timeTakenSeconds / 60, timeTakenSeconds % 60));
        sb.append("--------------------------------------------------------------------\n");
        sb.append(String.format("Total Questions: %d\n", totalQuestions));
        sb.append(String.format("Correct Answers: %d\n", correctAnswers));
        sb.append(String.format("Wrong Answers  : %d\n", wrongAnswers));
        sb.append(String.format("Unanswered     : %d\n", unansweredQuestions));
        sb.append("--------------------------------------------------------------------\n");
        sb.append(String.format("Marks Obtained : %d / %d\n", scoreObtained, totalMarks));
        sb.append(String.format("Percentage     : %.2f%%\n", percentage));
        sb.append(String.format("Status         : %s\n", passed ? "PASSED (HONORS)" : "FAILED (RETAKE RECOMMENDED)"));
        sb.append("====================================================================\n");
        return sb.toString();
    }

    // Getters and Setters
    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
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

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(int wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    public int getUnansweredQuestions() {
        return unansweredQuestions;
    }

    public void setUnansweredQuestions(int unansweredQuestions) {
        this.unansweredQuestions = unansweredQuestions;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public int getScoreObtained() {
        return scoreObtained;
    }

    public void setScoreObtained(int scoreObtained) {
        this.scoreObtained = scoreObtained;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public int getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setTimeTakenSeconds(int timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public Date getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(Date attemptDate) {
        this.attemptDate = attemptDate;
    }

    public List<QuestionReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<QuestionReview> reviews) {
        this.reviews = reviews;
    }

    /**
     * Inner static class to capture detailed review of each question
     */
    public static class QuestionReview implements Serializable {
        private static final long serialVersionUID = 1L;

        private int questionId;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String selectedAnswer;
        private String correctAnswer;
        private boolean correct;

        public QuestionReview() {
        }

        public QuestionReview(int questionId, String questionText, String optionA, String optionB,
                              String optionC, String optionD, String selectedAnswer, String correctAnswer, boolean correct) {
            this.questionId = questionId;
            this.questionText = questionText;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.selectedAnswer = selectedAnswer;
            this.correctAnswer = correctAnswer;
            this.correct = correct;
        }

        public int getQuestionId() { return questionId; }
        public String getQuestionText() { return questionText; }
        public String getOptionA() { return optionA; }
        public String getOptionB() { return optionB; }
        public String getOptionC() { return optionC; }
        public String getOptionD() { return optionD; }
        public String getSelectedAnswer() { return selectedAnswer; }
        public String getCorrectAnswer() { return correctAnswer; }
        public boolean isCorrect() { return correct; }
    }
}

