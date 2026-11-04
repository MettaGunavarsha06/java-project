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
 * Service managing Quiz creation, question bank maintenance (up to 100 questions), searching, and filtering.
 * Demonstrates:
 * - CRUD operations
 * - Iterators: Iterator<Question> usage
 * - Searching and filtering via Java Stream API
 * - Bulk question population scaling up to 100 questions
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
        List<Question> existing = questionDao.findByQuizId(quizId);
        if (existing.size() >= 100) {
            throw new IllegalArgumentException("Cannot exceed maximum limit of 100 questions per quiz.");
        }
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

    /**
     * Generates and bulk populates high-quality technical MCQs for a quiz up to targetTotal (maximum 100 questions).
     */
    public int generateQuestionsUpTo(int quizId, int targetTotal) throws DatabaseException, ResourceNotFoundException {
        Quiz quiz = getQuizWithQuestions(quizId);
        int currentCount = quiz.getQuestions().size();
        int maxTarget = Math.min(100, targetTotal);
        if (currentCount >= maxTarget) {
            return currentCount;
        }

        List<QuestionTemplate> templates = getQuestionBankTemplates();
        int added = 0;
        int tIdx = currentCount % templates.size();

        while (currentCount < maxTarget) {
            QuestionTemplate t = templates.get(tIdx % templates.size());
            String text = (currentCount >= templates.size())
                    ? t.text + " (Set " + ((currentCount / templates.size()) + 1) + ", Item #" + (currentCount + 1) + ")"
                    : t.text;
            Question q = new Question(quizId, text, t.a, t.b, t.c, t.d, t.ans);
            questionDao.save(q);
            currentCount++;
            added++;
            tIdx++;
        }

        return added;
    }

    private static class QuestionTemplate {
        String text, a, b, c, d, ans;
        QuestionTemplate(String text, String a, String b, String c, String d, String ans) {
            this.text = text; this.a = a; this.b = b; this.c = c; this.d = d; this.ans = ans;
        }
    }

    private static List<QuestionTemplate> getQuestionBankTemplates() {
        List<QuestionTemplate> list = new ArrayList<>();
        // Java Core & OOP
        list.add(new QuestionTemplate("Which keyword prevents a class from being inherited in Java?", "abstract", "final", "static", "sealed", "B"));
        list.add(new QuestionTemplate("What is the return type of the hashCode() method defined in java.lang.Object?", "long", "int", "short", "void", "B"));
        list.add(new QuestionTemplate("Which of these interfaces provides key-value pair mapping without duplicate keys?", "java.util.List", "java.util.Set", "java.util.Map", "java.util.Collection", "C"));
        list.add(new QuestionTemplate("What is the time complexity of retrieval in an ideal HashMap?", "O(n)", "O(log n)", "O(1)", "O(n log n)", "C"));
        list.add(new QuestionTemplate("Which method is called to begin execution of a Thread?", "run()", "start()", "init()", "execute()", "B"));
        list.add(new QuestionTemplate("Which keyword is used to ensure visibility of changes to variables across threads?", "volatile", "transient", "synchronized", "native", "A"));
        list.add(new QuestionTemplate("Which interface must be implemented to create a task for ExecutorService that returns a result?", "Runnable", "Callable", "Thread", "Future", "B"));
        list.add(new QuestionTemplate("What is the superclass of all Exception and Error classes in Java?", "Throwable", "Object", "RuntimeException", "Exception", "A"));
        list.add(new QuestionTemplate("Which clause in try-catch-finally always executes regardless of exceptions?", "try", "catch", "finally", "throws", "C"));
        list.add(new QuestionTemplate("What does JDBC stand for?", "Java Database Connectivity", "Java Data Binding Controller", "Java Direct Byte Connection", "Java Database Compiler", "A"));

        // Collections & Generics
        list.add(new QuestionTemplate("Which collection class is synchronized and thread-safe by default?", "ArrayList", "Vector", "LinkedList", "TreeSet", "B"));
        list.add(new QuestionTemplate("What does TreeSet use internally to maintain sorted order?", "Red-Black Tree", "Binary Search Tree", "Heap", "B-Tree", "A"));
        list.add(new QuestionTemplate("Which method removes all elements from a Java Collection?", "deleteAll()", "clear()", "empty()", "reset()", "B"));
        list.add(new QuestionTemplate("In Java Generics, what does <? extends Number> represent?", "Unbounded wildcard", "Upper bounded wildcard", "Lower bounded wildcard", "Exact type match", "B"));
        list.add(new QuestionTemplate("In Java Generics, what does <? super Integer> represent?", "Upper bounded wildcard", "Lower bounded wildcard", "Unbounded wildcard", "Invariant wildcard", "B"));
        list.add(new QuestionTemplate("What is the process of compiler removing generic type parameters at compile-time called?", "Type Casting", "Type Erasure", "Autoboxing", "Type Conversion", "B"));
        list.add(new QuestionTemplate("Which class provides static factory methods like Collections.sort() and Collections.reverse()?", "java.util.Collections", "java.util.Collection", "java.util.Arrays", "java.util.List", "A"));
        list.add(new QuestionTemplate("What is the difference between Comparable and Comparator in Java?", "Comparable defines natural ordering via compareTo(); Comparator defines custom sorting via compare()", "Comparable is for numbers only", "Comparator is an abstract class", "They are identical", "A"));
        list.add(new QuestionTemplate("Which map implementation maintains the insertion order of elements?", "HashMap", "TreeMap", "LinkedHashMap", "WeakHashMap", "C"));
        list.add(new QuestionTemplate("What happens if duplicate elements are added to a HashSet?", "Compilation error", "Runtime exception", "The duplicate is silently ignored", "Existing element is removed", "C"));

        // Multithreading & Concurrency
        list.add(new QuestionTemplate("What is the state of a thread waiting for a monitor lock?", "WAITING", "TIMED_WAITING", "BLOCKED", "TERMINATED", "C"));
        list.add(new QuestionTemplate("Which concurrency class allows one or more threads to wait until a set of operations completes?", "CountDownLatch", "CyclicBarrier", "Semaphore", "AtomicInteger", "A"));
        list.add(new QuestionTemplate("Which synchronization primitive controls access to a fixed number of concurrent permits?", "ReentrantLock", "Semaphore", "Phaser", "Exchanger", "B"));
        list.add(new QuestionTemplate("What is the advantage of using a ThreadPool over creating new Thread instances manually?", "Thread reuse reduces resource overhead and thread creation latency", "It bypasses CPU limitations", "Threads never terminate", "It eliminates synchronization", "A"));
        list.add(new QuestionTemplate("What method on Future<T> blocks until the computation completes to return the result?", "get()", "isDone()", "cancel()", "await()", "A"));
        list.add(new QuestionTemplate("Which package was introduced in Java 5 containing modern concurrency utilities?", "java.util.concurrent", "java.lang.thread", "java.concurrent", "java.util.threads", "A"));
        list.add(new QuestionTemplate("What is the purpose of Thread.sleep()?", "Permanently stops a thread", "Temporarily pauses execution for specified milliseconds", "Kills the JVM", "Yields processor to priority threads", "B"));
        list.add(new QuestionTemplate("What exception is thrown when a sleeping thread is interrupted?", "ThreadDeathException", "InterruptedException", "IllegalThreadStateException", "TimeoutException", "B"));
        list.add(new QuestionTemplate("Which keyword in a method signature restricts simultaneous execution to one thread per instance?", "synchronized", "volatile", "atomic", "locked", "A"));
        list.add(new QuestionTemplate("What is a race condition in multithreading?", "When two threads finish at the exact same millisecond", "When multiple threads concurrently modify shared state leading to inconsistent results", "When thread priority is equal", "When a thread exceeds its time slice", "B"));

        // Database & JDBC
        list.add(new QuestionTemplate("Which JDBC interface executes parameterized SQL statements safely?", "Statement", "PreparedStatement", "CallableStatement", "ResultSet", "B"));
        list.add(new QuestionTemplate("How do you disable auto-commit mode on a JDBC Connection?", "conn.setAutoCommit(false)", "conn.commit(false)", "conn.setTransaction(false)", "conn.begin()", "A"));
        list.add(new QuestionTemplate("Which method rolls back changes in the current transaction?", "conn.rollback()", "conn.abort()", "conn.undo()", "conn.revert()", "A"));
        list.add(new QuestionTemplate("Which SQL clause is used to sort query results?", "GROUP BY", "ORDER BY", "SORT BY", "ARRANGE BY", "B"));
        list.add(new QuestionTemplate("What does the ACID acronym stand for in relational databases?", "Atomicity, Consistency, Isolation, Durability", "Action, Commit, Integrity, Data", "Access, Control, Index, Database", "Accuracy, Concurrency, Isolation, Deletion", "A"));
        list.add(new QuestionTemplate("Which JDBC statement type is used to invoke stored procedures?", "PreparedStatement", "CallableStatement", "BatchStatement", "ProcedureStatement", "B"));
        list.add(new QuestionTemplate("What does rs.next() return in JDBC ResultSet?", "The current row object", "true if the next row is valid, false if no more rows", "The total count of rows", "The column index", "B"));
        list.add(new QuestionTemplate("Which standard HTTP status code signifies successful resource creation?", "200 OK", "201 Created", "204 No Content", "301 Moved Permanently", "B"));
        list.add(new QuestionTemplate("What type of join returns all rows from the left table and matched rows from the right table?", "INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL JOIN", "B"));
        list.add(new QuestionTemplate("Which SQL statement is used to remove a table and its structure completely?", "DELETE TABLE", "DROP TABLE", "REMOVE TABLE", "TRUNCATE TABLE", "B"));

        // Java 8+, Streams & Lambdas
        list.add(new QuestionTemplate("What is a Functional Interface in Java?", "An interface with only default methods", "An interface with exactly one abstract method", "An interface with no methods", "An interface extending Functional", "B"));
        list.add(new QuestionTemplate("Which annotation is recommended on single-abstract-method interfaces?", "@FunctionalInterface", "@Functional", "@SingleMethod", "@Lambda", "A"));
        list.add(new QuestionTemplate("Which Stream API operation converts stream elements to a List?", "collect(Collectors.toList())", "toListStream()", "asArrayList()", "convert(List.class)", "A"));
        list.add(new QuestionTemplate("Is Stream.map() an intermediate or terminal operation?", "Intermediate operation", "Terminal operation", "Static operation", "Reduction operation", "A"));
        list.add(new QuestionTemplate("Is Stream.forEach() an intermediate or terminal operation?", "Intermediate operation", "Terminal operation", "Filter operation", "Parallel operation", "B"));
        list.add(new QuestionTemplate("Which class prevents NullPointerException by wrapping nullable values in Java 8?", "java.util.Optional", "java.util.Nullable", "java.lang.Wrapper", "java.util.SafeObject", "A"));
        list.add(new QuestionTemplate("Which method reference syntax is equivalent to x -> System.out.println(x)?", "System.out::println", "System.out->println", "System.out.println()", "System::out::println", "A"));
        list.add(new QuestionTemplate("What does the reduce() method do in Stream API?", "Filters elements", "Combines stream elements into a single summary result", "Limits stream length", "Sorts elements descending", "B"));
        list.add(new QuestionTemplate("Which default method on Iterable allows functional iteration with a Consumer?", "forEach()", "iterate()", "consume()", "loop()", "A"));
        list.add(new QuestionTemplate("What is the time complexity of searching by key in a TreeMap?", "O(1)", "O(log n)", "O(n)", "O(n^2)", "B"));

        // Serialization, File I/O & JVM
        list.add(new QuestionTemplate("Which marker interface must a class implement to enable object serialization in Java?", "java.io.Serializable", "java.io.Externalizable", "java.lang.Cloneable", "java.io.Readable", "A"));
        list.add(new QuestionTemplate("Which keyword prevents a specific field from being serialized?", "volatile", "transient", "static", "private", "B"));
        list.add(new QuestionTemplate("What is the purpose of serialVersionUID in a Serializable class?", "Controls serialization performance", "Identifies class version compatibility during deserialization", "Stores object memory address", "Encrypts serialized data", "B"));
        list.add(new QuestionTemplate("Which class is used to write serialized objects to an output stream?", "ObjectOutputStream", "DataOutputStream", "FileOutputStream", "BufferedOutputStream", "A"));
        list.add(new QuestionTemplate("Where are Java objects allocated in JVM memory?", "Stack memory", "Heap memory", "Native memory", "Program Counter register", "B"));
        list.add(new QuestionTemplate("Where are primitive local variables stored in Java?", "Heap memory", "Call Stack", "Metaspace", "Constant Pool", "B"));
        list.add(new QuestionTemplate("What is the main role of the Java Garbage Collector?", "Deallocates unused heap memory automatically", "Compiles bytecode to native instructions", "Checks syntax errors", "Encrypts strings in memory", "A"));
        list.add(new QuestionTemplate("What error is thrown when JVM runs out of heap memory?", "StackOverflowError", "OutOfMemoryError", "MemoryLimitException", "HeapFullException", "B"));
        list.add(new QuestionTemplate("What error is thrown when infinite recursion exhausts thread stack space?", "StackOverflowError", "OutOfMemoryError", "RecursionLimitError", "InfiniteLoopException", "A"));
        list.add(new QuestionTemplate("Which Java tool compiles .java files into .class bytecode files?", "java", "javac", "javap", "javadoc", "B"));

        return list;
    }
}
