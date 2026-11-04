package com.quiz.service;

import com.quiz.dao.QuizAttemptDao;
import com.quiz.dao.UserDao;
import com.quiz.exception.DatabaseException;
import com.quiz.exception.ResourceNotFoundException;
import com.quiz.model.CandidateLoginHistory;
import com.quiz.model.QuizAttempt;
import com.quiz.model.Student;
import com.quiz.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing user retrieval, student profiles, and search operations.
 * Demonstrates:
 * - Java Collections & Stream API searching and filtering
 * - Custom Exception handling
 * - Candidate profile and login tracking enrichment
 */
public class UserService {

    private final UserDao userDao;
    private final QuizAttemptDao quizAttemptDao;

    public UserService(UserDao userDao, QuizAttemptDao quizAttemptDao) {
        this.userDao = userDao;
        this.quizAttemptDao = quizAttemptDao;
    }

    public List<Student> getAllStudents() throws DatabaseException {
        List<Student> students = userDao.findAllStudents();
        // Enrich students with summary statistics
        for (Student s : students) {
            List<QuizAttempt> attempts = quizAttemptDao.findByStudentId(s.getId());
            s.setTotalQuizzesAttempted(attempts.size());
            double avg = attempts.stream()
                    .mapToDouble(QuizAttempt::getPercentage)
                    .average()
                    .orElse(0.0);
            s.setAverageScore(Math.round(avg * 100.0) / 100.0);
        }
        return students;
    }

    public User getUserById(int id) throws DatabaseException, ResourceNotFoundException {
        return userDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    public List<CandidateLoginHistory> getCandidateLoginHistory(int userId) throws DatabaseException {
        return userDao.getCandidateLoginHistory(userId);
    }

    /**
     * Search candidate students using Stream API filter across name, email, roll number, college, and branch.
     */
    public List<Student> searchStudents(String keyword) throws DatabaseException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStudents();
        }
        String lower = keyword.trim().toLowerCase();
        return getAllStudents().stream()
                .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(lower)) ||
                             (s.getEmail() != null && s.getEmail().toLowerCase().contains(lower)) ||
                             (s.getRollNumber() != null && s.getRollNumber().toLowerCase().contains(lower)) ||
                             (s.getInstitution() != null && s.getInstitution().toLowerCase().contains(lower)) ||
                             (s.getDepartment() != null && s.getDepartment().toLowerCase().contains(lower)) ||
                             (s.getCity() != null && s.getCity().toLowerCase().contains(lower)) ||
                             (s.getPhone() != null && s.getPhone().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
    }
}
