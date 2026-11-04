package com.quiz.concurrency;

import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.model.Result;
import com.quiz.model.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Concurrency Callable task for calculating quiz scores in parallel.
 * Demonstrates:
 * - Concurrency Utilities: implements Callable<Result>
 * - Enhanced for-loop & Stream operations
 * - Accurate mark weighting and question-level evaluation
 */
public class ScoreCalculationTask implements Callable<Result> {

    private final User student;
    private final Quiz quiz;
    private final List<Question> questions;
    private final Map<Integer, String> answers; // questionId -> selectedOption ("A","B","C","D")
    private final int timeTakenSeconds;

    public ScoreCalculationTask(User student, Quiz quiz, List<Question> questions, 
                                Map<Integer, String> answers, int timeTakenSeconds) {
        this.student = student;
        this.quiz = quiz;
        this.questions = questions;
        this.answers = answers;
        this.timeTakenSeconds = timeTakenSeconds;
    }

    @Override
    public Result call() throws Exception {
        int totalQuestions = questions.size();
        if (totalQuestions == 0) {
            throw new IllegalStateException("Cannot calculate score for a quiz with no questions");
        }

        int correctCount = 0;
        int wrongCount = 0;
        int unansweredCount = 0;

        Result result = new Result();
        result.setStudentId(student.getId());
        result.setStudentName(student.getName());
        result.setStudentEmail(student.getEmail());
        result.setQuizId(quiz.getId());
        result.setQuizTitle(quiz.getTitle());
        result.setTotalQuestions(totalQuestions);
        result.setTotalMarks(quiz.getTotalMarks());
        result.setTimeTakenSeconds(timeTakenSeconds);

        // Calculate marks per question
        double marksPerQuestion = (double) quiz.getTotalMarks() / totalQuestions;

        // Iterate through questions
        for (Question q : questions) {
            String selected = answers != null ? answers.get(q.getId()) : null;
            boolean isCorrect = false;

            if (selected == null || selected.trim().isEmpty()) {
                unansweredCount++;
                selected = null;
            } else if (q.isCorrect(selected)) {
                correctCount++;
                isCorrect = true;
            } else {
                wrongCount++;
            }

            result.addReview(new Result.QuestionReview(
                    q.getId(),
                    q.getQuestionText(),
                    q.getOptionA(),
                    q.getOptionB(),
                    q.getOptionC(),
                    q.getOptionD(),
                    selected,
                    q.getCorrectAnswer(),
                    isCorrect
            ));
        }

        int calculatedScore = (int) Math.round(correctCount * marksPerQuestion);
        double percentage = ((double) calculatedScore / quiz.getTotalMarks()) * 100.0;
        boolean passed = percentage >= 50.0; // Standard 50% passing threshold

        result.setCorrectAnswers(correctCount);
        result.setWrongAnswers(wrongCount);
        result.setUnansweredQuestions(unansweredCount);
        result.setScoreObtained(calculatedScore);
        result.setPercentage(Math.round(percentage * 100.0) / 100.0);
        result.setPassed(passed);

        return result;
    }
}

