package com.quiz.model;

import com.quiz.interfaces.Reportable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a Quiz assessment.
 * Demonstrates:
 * - Collections (ArrayList of Questions)
 * - Iterators (Iterator<Question> implementation)
 * - Enhanced for-loop iteration
 * - Interface implementation (Reportable, Serializable)
 * - Encapsulation with defensive copying
 */
public class Quiz implements Reportable, Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String description;
    private int duration; // in minutes
    private int totalMarks;
    private List<Question> questions = new ArrayList<>();

    public Quiz() {
    }

    public Quiz(int id, String title, String description, int duration, int totalMarks) {
        setId(id);
        setTitle(title);
        setDescription(description);
        setDuration(duration);
        setTotalMarks(totalMarks);
    }

    public Quiz(String title, String description, int duration, int totalMarks) {
        this(0, title, description, duration, totalMarks);
    }

    // Question management using Collections
    public void addQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Cannot add a null question");
        }
        this.questions.add(question);
    }

    public void removeQuestion(int questionId) {
        // Using Java Iterator explicitly as requested
        Iterator<Question> iterator = this.questions.iterator();
        while (iterator.hasNext()) {
            Question q = iterator.next();
            if (q.getId() == questionId) {
                iterator.remove();
                break;
            }
        }
    }

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions != null ? new ArrayList<>(questions) : new ArrayList<>();
    }

    public int getQuestionCount() {
        return questions.size();
    }

    // Getters and Setters with Validation
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Quiz ID cannot be negative");
        }
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Quiz title cannot be blank");
        }
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0 minutes");
        }
        this.duration = duration;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        if (totalMarks <= 0) {
            throw new IllegalArgumentException("Total marks must be greater than 0");
        }
        this.totalMarks = totalMarks;
    }

    @Override
    public String getReportTitle() {
        return "Quiz Specification: " + title;
    }

    @Override
    public String generateReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append(String.format("QUIZ DETAILS: %s (ID: %d)\n", title, id));
        sb.append(String.format("Duration: %d Minutes | Total Marks: %d | Questions: %d\n", duration, totalMarks, questions.size()));
        sb.append("Description: ").append(description).append("\n");
        sb.append("------------------------------------------------------------\n");
        
        // Demonstrating Enhanced For-Loop
        int qIdx = 1;
        for (Question q : questions) {
            sb.append(String.format("Q%d: %s\n", qIdx++, q.getQuestionText()));
            sb.append(String.format("   A) %s\n   B) %s\n   C) %s\n   D) %s\n", 
                    q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()));
            sb.append(String.format("   Correct: [%s]\n", q.getCorrectAnswer()));
        }
        sb.append("============================================================\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quiz)) return false;
        Quiz quiz = (Quiz) o;
        return id == quiz.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Quiz[id=%d, title='%s', duration=%d mins, marks=%d, questions=%d]",
                id, title, duration, totalMarks, questions.size());
    }
}

