package com.quiz.controller;

import com.quiz.annotations.Author;
import com.quiz.exception.DatabaseException;
import com.quiz.exception.ResourceNotFoundException;
import com.quiz.generics.ApiResponse;
import com.quiz.model.Quiz;
import com.quiz.service.QuizService;

import java.util.List;

/**
 * Controller handling public and student quiz browsing, searching, and details.
 */
@Author(name = "Antigravity Team", date = "2026-11-03", version = "1.0.0")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    public ApiResponse<List<Quiz>> getAllQuizzes() {
        try {
            List<Quiz> quizzes = quizService.getAllQuizzes();
            return ApiResponse.success("Retrieved quizzes", quizzes);
        } catch (DatabaseException e) {
            return ApiResponse.error("Failed to load quizzes: " + e.getMessage());
        }
    }

    public ApiResponse<Quiz> getQuizById(int id) {
        try {
            Quiz quiz = quizService.getQuizWithQuestions(id);
            return ApiResponse.success(quiz);
        } catch (ResourceNotFoundException e) {
            return ApiResponse.error(e.getMessage());
        } catch (DatabaseException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        }
    }

    public ApiResponse<List<Quiz>> searchQuizzes(String keyword) {
        try {
            List<Quiz> matches = quizService.searchQuizzes(keyword);
            return ApiResponse.success(matches);
        } catch (DatabaseException e) {
            return ApiResponse.error("Error searching quizzes: " + e.getMessage());
        }
    }
}

