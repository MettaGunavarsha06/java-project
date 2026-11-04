package com.quiz.service;

import com.quiz.dao.QuestionDao;
import com.quiz.dao.QuizDao;
import com.quiz.exception.DatabaseException;
import com.quiz.exception.ResourceNotFoundException;
import com.quiz.model.Question;
import com.quiz.model.Quiz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing Quiz creation, question bank maintenance, searching, and filtering.
 * Demonstrates:
 * - CRUD operations
 * - Iterators: Iterator<Question> usage
 * - Searching and filtering via Java Stream API
 * - Validation and custom checked exceptions
 */
public class QuizService {

    private final QuizDao quizDao;
    private final QuestionDao questionDao;

    public QuizService(QuizDao quizDao, QuestionDao questionDao) {
        this.quizDao = quizDao;
        this.questionDao = questionDao;
    }

    public List<Quiz> getAllQuizzes() throws DatabaseException {
        List<Quiz> quizzes = quizDao.findAll();
        // Attach questions to each quiz
        for (Quiz q : quizzes) {
            List<Question> questions = questionDao.findByQuizId(q.getId());
            q.setQuestions(questions);
        }
        return quizzes;
    }

    public Quiz getQuizWithQuestions(int quizId) throws DatabaseException, ResourceNotFoundException {
        Quiz quiz = quizDao.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + quizId));
        List<Question> questions = questionDao.findByQuizId(quizId);
        quiz.setQuestions(questions);
        return quiz;
    }

    public Quiz createQuiz(String title, String description, int duration, int totalMarks) throws DatabaseException {
        Quiz quiz = new Quiz(title, description, duration, totalMarks);
        return quizDao.save(quiz);
    }

    public boolean updateQuiz(int id, String title, String description, int duration, int totalMarks) throws DatabaseException, ResourceNotFoundException {
        Quiz quiz = getQuizWithQuestions(id);
        quiz.setTitle(title);
        quiz.setDescription(description);
        quiz.setDuration(duration);
        quiz.setTotalMarks(totalMarks);
        return quizDao.update(quiz);
    }

    public boolean deleteQuiz(int id) throws DatabaseException {
        return quizDao.deleteById(id);
    }

    // Question Management
    public Question addQuestionToQuiz(int quizId, String text, String optA, String optB, String optC, String optD, String correct) throws DatabaseException, ResourceNotFoundException {
        // Ensure quiz exists
        quizDao.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Cannot add question: Quiz ID " + quizId + " does not exist"));
        Question q = new Question(quizId, text, optA, optB, optC, optD, correct);
        return questionDao.save(q);
    }

    public boolean updateQuestion(int questionId, String text, String optA, String optB, String optC, String optD, String correct) throws DatabaseException, ResourceNotFoundException {
        Question q = questionDao.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + questionId));
        q.setQuestionText(text);
        q.setOptionA(optA);
        q.setOptionB(optB);
        q.setOptionC(optC);
        q.setOptionD(optD);
        q.setCorrectAnswer(correct);
        return questionDao.update(q);
    }

    public boolean deleteQuestion(int questionId) throws DatabaseException {
        return questionDao.deleteById(questionId);
    }

    /**
     * Search and filter quizzes by keyword using Java Stream API.
     */
    public List<Quiz> searchQuizzes(String keyword) throws DatabaseException {
        List<Quiz> all = getAllQuizzes();
        if (keyword == null || keyword.trim().isEmpty()) {
            return all;
        }
        String term = keyword.trim().toLowerCase();
        return all.stream()
                .filter(q -> q.getTitle().toLowerCase().contains(term) || q.getDescription().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates explicit use of Java Iterator to safely inspect and filter questions.
     */
    public List<Question> filterValidQuestions(int quizId) throws DatabaseException {
        List<Question> questions = new ArrayList<>(questionDao.findByQuizId(quizId));
        Iterator<Question> iterator = questions.iterator();
        while (iterator.hasNext()) {
            Question q = iterator.next();
            if (q.getQuestionText() == null || q.getCorrectAnswer() == null) {
                iterator.remove();
            }
        }
        return questions;
    }
}

