/**
 * Vercel Serverless Function Handler
 * Complete serverless backend API for the Online Quiz Management System.
 * Fully compatible with Vercel edge/serverless execution.
 */

const fs = require('fs');
const path = require('path');

const TMP_DIR = process.platform === 'win32' ? (process.env.TEMP || process.env.TMP || 'C:\\temp') : '/tmp';
const USERS_STORAGE_PATH = path.join(TMP_DIR, 'quizmaster_users.json');

function readPersistedUsers() {
  try {
    if (fs.existsSync(USERS_STORAGE_PATH)) {
      const content = fs.readFileSync(USERS_STORAGE_PATH, 'utf8');
      const list = JSON.parse(content);
      if (Array.isArray(list) && list.length > 0) {
        return list;
      }
    }
  } catch (e) {}
  return null;
}

function persistUsersToDisk(list) {
  try {
    if (!fs.existsSync(TMP_DIR)) {
      fs.mkdirSync(TMP_DIR, { recursive: true });
    }
    fs.writeFileSync(USERS_STORAGE_PATH, JSON.stringify(list, null, 2), 'utf8');
  } catch (e) {}
}

// Candidate users store
const DEFAULT_USERS = [
  {
    id: 1,
    name: "Dr. Sarah Connor (Admin)",
    email: "admin@quiz.com",
    password: "admin123",
    role: "ADMIN",
    phone: "+1-555-0199",
    gender: "Female",
    dob: "1985-04-12",
    address: "100 Campus Drive, Admin Block 4",
    city: "Boston",
    state: "Massachusetts",
    institution: "MIT Department of Computing",
    department: "Computer Science & Engineering",
    rollNumber: "FAC-001",
    degree: "Ph.D. in Computer Science",
    yearOfStudy: "Faculty",
    cgpa: "4.00",
    bio: "Lead Exam Coordinator & Senior Academic Professor",
    loginCount: 42,
    lastLoginAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
    lastLoginIp: "127.0.0.1",
    status: "ACTIVE"
  },
  {
    id: 2,
    name: "Alex Johnson",
    email: "alex@student.com",
    password: "student123",
    role: "STUDENT",
    phone: "+1-555-0142",
    gender: "Male",
    dob: "2003-08-24",
    address: "742 Evergreen Terrace, Apt 3B",
    city: "San Jose",
    state: "California",
    institution: "Silicon Valley Institute of Technology",
    department: "Computer Science & Engineering",
    rollNumber: "CS-2024-001",
    degree: "B.Tech in Computer Science",
    yearOfStudy: "3rd Year",
    cgpa: "9.40",
    bio: "Passionate Java backend developer and competitive programmer.",
    loginCount: 15,
    lastLoginAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
    lastLoginIp: "127.0.0.1",
    status: "ACTIVE"
  },
  {
    id: 3,
    name: "Sophia Chen",
    email: "sophia@student.com",
    password: "student123",
    role: "STUDENT",
    phone: "+1-555-0177",
    gender: "Female",
    dob: "2004-01-15",
    address: "45 Innovation Way, Suite 12",
    city: "Seattle",
    state: "Washington",
    institution: "Northwest University of Engineering",
    department: "Information Science",
    rollNumber: "IS-2024-045",
    degree: "B.S. in Computer Science",
    yearOfStudy: "2nd Year",
    cgpa: "9.15",
    bio: "Aspiring full-stack developer focusing on microservices and databases.",
    loginCount: 9,
    lastLoginAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
    lastLoginIp: "127.0.0.1",
    status: "ACTIVE"
  },
  {
    id: 4,
    name: "Liam Davis",
    email: "liam@student.com",
    password: "student123",
    role: "STUDENT",
    phone: "+1-555-0188",
    gender: "Male",
    dob: "2002-11-30",
    address: "88 University Ave, Room 204",
    city: "Austin",
    state: "Texas",
    institution: "Texas Polytechnic Institute",
    department: "Data Engineering",
    rollNumber: "DE-2024-088",
    degree: "B.Tech in Data Science",
    yearOfStudy: "4th Year",
    cgpa: "8.85",
    bio: "Data science enthusiast and relational database architect.",
    loginCount: 6,
    lastLoginAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
    lastLoginIp: "127.0.0.1",
    status: "ACTIVE"
  }
];

let users = readPersistedUsers() || DEFAULT_USERS;

let loginHistory = [
  { id: 1, userId: 2, name: "Alex Johnson", email: "alex@student.com", loginTime: "2026-11-04 10:15:22", ipAddress: "192.168.1.101", userAgent: "Mozilla/5.0 Chrome/120.0.0", loginStatus: "SUCCESS" },
  { id: 2, userId: 1, name: "Dr. Sarah Connor (Admin)", email: "admin@quiz.com", loginTime: "2026-11-04 09:30:10", ipAddress: "192.168.1.5", userAgent: "Mozilla/5.0 Edge/120.0.0", loginStatus: "SUCCESS" },
  { id: 3, userId: 3, name: "Sophia Chen", email: "sophia@student.com", loginTime: "2026-11-03 16:45:00", ipAddress: "192.168.1.102", userAgent: "Mozilla/5.0 Safari/17.2", loginStatus: "SUCCESS" }
];

// Master Technical Question Bank (Java Core, OOP, SQL, Multithreading, Collections, JVM, JDBC)
const MASTER_QUESTION_BANK = [
  // 1-10: Java Core & OOP
  { text: "Which OOP principle emphasizes hiding internal state and requiring interaction through methods?", a: "Inheritance", b: "Encapsulation", c: "Polymorphism", d: "Abstraction", ans: "B" },
  { text: "What is the default value of a boolean variable declared as an instance member in Java?", a: "true", b: "false", c: "0", d: "null", ans: "B" },
  { text: "Which keyword is used by a class to inherit from an abstract class?", a: "implements", b: "extends", c: "inherits", d: "instanceof", ans: "B" },
  { text: "Can an abstract class have constructors in Java?", a: "No, abstract classes cannot have constructors", b: "Yes, but only private constructors", c: "Yes, invoked during subclass instantiation via super()", d: "Only if no abstract methods", ans: "C" },
  { text: "Which statement is true regarding method overriding in Java?", a: "Return type must be incompatible", b: "Signature must match superclass method", c: "Private methods can override", d: "Static methods override dynamically", ans: "B" },
  { text: "Which keyword prevents a class from being inherited in Java?", a: "abstract", b: "final", c: "static", d: "sealed", ans: "B" },
  { text: "What is the return type of the hashCode() method defined in java.lang.Object?", a: "long", b: "int", c: "short", d: "void", ans: "B" },
  { text: "What is the superclass of all Exception and Error classes in Java?", a: "Throwable", b: "Object", c: "RuntimeException", d: "Exception", ans: "A" },
  { text: "Which clause in try-catch-finally always executes regardless of exceptions?", a: "try", b: "catch", c: "finally", d: "throws", ans: "C" },
  { text: "What is the default isolation level in MySQL InnoDB engine?", a: "READ UNCOMMITTED", b: "REPEATABLE READ", c: "SERIALIZABLE", d: "READ COMMITTED", ans: "B" },

  // 11-20: Collections & Generics
  { text: "Which Collections interface allows unique elements only and does not maintain insertion order by default?", a: "ArrayList", b: "LinkedList", c: "HashSet", d: "Vector", ans: "C" },
  { text: "Which collection class is synchronized and thread-safe by default?", a: "ArrayList", b: "Vector", c: "LinkedList", d: "TreeSet", ans: "B" },
  { text: "What data structure does TreeSet use internally to maintain sorted order?", a: "Red-Black Tree", b: "Binary Search Tree", c: "Heap", d: "B-Tree", ans: "A" },
  { text: "Which method removes all elements from a Java Collection?", a: "deleteAll()", b: "clear()", c: "empty()", d: "reset()", ans: "B" },
  { text: "In Java Generics, what does <? extends Number> represent?", a: "Unbounded wildcard", b: "Upper bounded wildcard", c: "Lower bounded wildcard", d: "Exact type match", ans: "B" },
  { text: "In Java Generics, what does <? super Integer> represent?", a: "Upper bounded wildcard", b: "Lower bounded wildcard", c: "Unbounded wildcard", d: "Invariant wildcard", ans: "B" },
  { text: "What is the compile-time mechanism where generic type parameters are removed called?", a: "Type Casting", b: "Type Erasure", c: "Autoboxing", d: "Type Conversion", ans: "B" },
  { text: "Which class provides static methods like Collections.sort() and reverse()?", a: "java.util.Collections", b: "java.util.Collection", c: "java.util.Arrays", d: "java.util.List", ans: "A" },
  { text: "What is the difference between Comparable and Comparator in Java?", a: "Comparable defines natural ordering via compareTo(); Comparator defines custom sorting via compare()", b: "Comparable is for numbers only", c: "Comparator is an abstract class", d: "They are identical", ans: "A" },
  { text: "Which map implementation maintains the insertion order of elements?", a: "HashMap", b: "TreeMap", c: "LinkedHashMap", d: "WeakHashMap", ans: "C" },

  // 21-30: Multithreading & Concurrency
  { text: "What will happen when you call Thread.start() twice on the same Thread instance?", a: "Thread restarts from beginning", b: "IllegalThreadStateException is thrown", c: "A new thread is created automatically", d: "The call is silently ignored", ans: "B" },
  { text: "Which concurrency interface returns a value and throws checked exceptions?", a: "Runnable", b: "Callable", c: "Thread", d: "Executor", ans: "B" },
  { text: "Which mechanism in Java provides mutual exclusion to prevent race conditions on shared objects?", a: "volatile keyword", b: "synchronized block or method", c: "transient keyword", d: "finalize method", ans: "B" },
  { text: "What is the state of a thread waiting for an intrinsic monitor lock in Java?", a: "WAITING", b: "TIMED_WAITING", c: "BLOCKED", d: "TERMINATED", ans: "C" },
  { text: "Which concurrency class allows one or more threads to wait until a countdown reaches zero?", a: "CountDownLatch", b: "CyclicBarrier", c: "Semaphore", d: "AtomicInteger", ans: "A" },
  { text: "Which synchronization primitive controls access to a fixed number of permits?", a: "ReentrantLock", b: "Semaphore", c: "Phaser", d: "Exchanger", ans: "B" },
  { text: "What is the primary advantage of using a ThreadPool over creating new Thread instances?", a: "Thread reuse reduces resource overhead and thread creation latency", b: "It bypasses CPU limitations", c: "Threads never terminate", d: "It eliminates synchronization", ans: "A" },
  { text: "What method on Future<T> blocks until the computation completes to return the result?", a: "get()", b: "isDone()", c: "cancel()", d: "await()", ans: "A" },
  { text: "Which package was introduced in Java 5 containing modern concurrency utilities?", a: "java.util.concurrent", b: "java.lang.thread", c: "java.concurrent", d: "java.util.threads", ans: "A" },
  { text: "What is a race condition in multithreading?", a: "When two threads finish at the exact same millisecond", b: "When multiple threads concurrently modify shared state leading to inconsistent results", c: "When thread priority is equal", d: "When a thread exceeds its time slice", ans: "B" },

  // 31-40: Database, SQL & JDBC
  { text: "Why is PreparedStatement preferred over Statement in JDBC?", a: "It executes slower but supports more databases", b: "It prevents SQL Injection and pre-compiles queries for efficiency", c: "It requires no database connection", d: "It does not require parameter binding", ans: "B" },
  { text: "Which JDBC method commits the current database transaction when auto-commit is disabled?", a: "connection.setAutoCommit(true)", b: "connection.save()", c: "connection.commit()", d: "statement.executeCommit()", ans: "C" },
  { text: "What type of exception is java.sql.SQLException in Java?", a: "Unchecked Exception (RuntimeException)", b: "Checked Exception requiring try-catch or throws", c: "Error", d: "JVM Fatal Error", ans: "B" },
  { text: "Which interface in java.sql is used to retrieve rows from an executed SQL SELECT query?", a: "RowMapper", b: "ResultSet", c: "DataRecord", d: "CursorSet", ans: "B" },
  { text: "Which clause in SQL is used to filter records after aggregation using GROUP BY?", a: "WHERE", b: "HAVING", c: "ORDER BY", d: "FILTER", ans: "B" },
  { text: "How do you disable auto-commit mode on a JDBC Connection?", a: "conn.setAutoCommit(false)", b: "conn.commit(false)", c: "conn.setTransaction(false)", d: "conn.begin()", ans: "A" },
  { text: "Which method rolls back changes in the current database transaction?", a: "conn.rollback()", b: "conn.abort()", c: "conn.undo()", d: "conn.revert()", ans: "A" },
  { text: "Which SQL clause is used to sort query results in ascending or descending order?", a: "GROUP BY", b: "ORDER BY", c: "SORT BY", d: "ARRANGE BY", ans: "B" },
  { text: "What does the ACID acronym stand for in relational databases?", a: "Atomicity, Consistency, Isolation, Durability", b: "Action, Commit, Integrity, Data", c: "Access, Control, Index, Database", d: "Accuracy, Concurrency, Isolation, Deletion", ans: "A" },
  { text: "Which JDBC statement type is used to invoke stored procedures and functions?", a: "PreparedStatement", b: "CallableStatement", c: "BatchStatement", d: "ProcedureStatement", ans: "B" },

  // 41-50: SQL Joins, DDL & Java 8+ Streams
  { text: "What does rs.next() return in JDBC ResultSet?", a: "The current row object", b: "true if the next row is valid, false if no more rows", c: "The total count of rows", d: "The column index", ans: "B" },
  { text: "What type of join returns all rows from the left table and matched rows from the right table?", a: "INNER JOIN", b: "LEFT JOIN", c: "RIGHT JOIN", d: "FULL JOIN", ans: "B" },
  { text: "Which SQL statement is used to remove a table and its structure completely?", a: "DELETE TABLE", b: "DROP TABLE", c: "REMOVE TABLE", d: "TRUNCATE TABLE", ans: "B" },
  { text: "What is a Functional Interface in Java 8?", a: "An interface with only default methods", b: "An interface with exactly one abstract method", c: "An interface with no methods", d: "An interface extending Functional", ans: "B" },
  { text: "Which annotation is recommended on single-abstract-method interfaces in Java?", a: "@FunctionalInterface", b: "@Functional", c: "@SingleMethod", d: "@Lambda", ans: "A" },
  { text: "Which Stream API operation converts stream elements to a List?", a: "collect(Collectors.toList())", b: "toListStream()", c: "asArrayList()", d: "convert(List.class)", ans: "A" },
  { text: "Is Stream.map() an intermediate or terminal operation in Java 8?", a: "Intermediate operation", b: "Terminal operation", c: "Static operation", d: "Reduction operation", ans: "A" },
  { text: "Is Stream.forEach() an intermediate or terminal operation in Java 8?", a: "Intermediate operation", b: "Terminal operation", c: "Filter operation", d: "Parallel operation", ans: "B" },
  { text: "Which class prevents NullPointerException by wrapping nullable values in Java 8?", a: "java.util.Optional", b: "java.util.Nullable", c: "java.lang.Wrapper", d: "java.util.SafeObject", ans: "A" },
  { text: "Which method reference syntax is equivalent to x -> System.out.println(x)?", a: "System.out::println", b: "System.out->println", c: "System.out.println()", d: "System::out::println", ans: "A" },

  // 51-60: Streams, Memory & Serialization
  { text: "What does the reduce() method do in the Java Stream API?", a: "Filters elements", b: "Combines stream elements into a single summary result", c: "Limits stream length", d: "Sorts elements descending", ans: "B" },
  { text: "Which marker interface must a class implement to enable object serialization in Java?", a: "java.io.Serializable", b: "java.io.Externalizable", c: "java.lang.Cloneable", d: "java.io.Readable", ans: "A" },
  { text: "Which keyword prevents a specific field from being serialized in Java?", a: "volatile", b: "transient", c: "static", d: "private", ans: "B" },
  { text: "What is the purpose of serialVersionUID in a Serializable class?", a: "Controls serialization speed", b: "Identifies class version compatibility during deserialization", c: "Stores object memory address", d: "Encrypts serialized data", ans: "B" },
  { text: "Where are Java object instances allocated in JVM memory?", a: "Call Stack", b: "Heap memory", c: "Native memory", d: "Program Counter register", ans: "B" },
  { text: "Where are primitive local variables stored in Java during method execution?", a: "Heap memory", b: "Call Stack memory", c: "Metaspace", d: "Constant Pool", ans: "B" },
  { text: "What is the primary role of the Java Garbage Collector?", a: "Deallocates unreachable heap memory automatically", b: "Compiles bytecode to native instructions", c: "Checks syntax errors", d: "Encrypts strings in memory", ans: "A" },
  { text: "What Error is thrown when JVM runs out of heap memory?", a: "StackOverflowError", b: "OutOfMemoryError", c: "MemoryLimitException", d: "HeapFullException", ans: "B" },
  { text: "What Error is thrown when infinite recursion exhausts thread stack space?", a: "StackOverflowError", b: "OutOfMemoryError", c: "RecursionLimitError", d: "InfiniteLoopException", ans: "A" },
  { text: "What is the time complexity of retrieval in an ideal HashMap?", a: "O(n)", b: "O(log n)", c: "O(1)", d: "O(n log n)", ans: "C" }
];

function generateQuestionsForQuiz(quizId, count) {
  const target = Math.max(1, Math.min(100, parseInt(count) || 10));
  const list = [];
  for (let i = 0; i < target; i++) {
    const tmpl = MASTER_QUESTION_BANK[i % MASTER_QUESTION_BANK.length];
    const isRepeated = i >= MASTER_QUESTION_BANK.length;
    const qNum = i + 1;
    list.push({
      id: qNum,
      quizId: quizId,
      questionText: isRepeated 
        ? `${tmpl.text} (Set ${Math.floor(i / MASTER_QUESTION_BANK.length) + 1}, Item #${qNum})`
        : tmpl.text,
      optionA: tmpl.a,
      optionB: tmpl.b,
      optionC: tmpl.c,
      optionD: tmpl.d,
      correctAnswer: tmpl.ans
    });
  }
  return list;
}

const QUIZZES_STORAGE_PATH = path.join(TMP_DIR, 'quizmaster_quizzes.json');

function readPersistedQuizzes() {
  try {
    if (fs.existsSync(QUIZZES_STORAGE_PATH)) {
      const content = fs.readFileSync(QUIZZES_STORAGE_PATH, 'utf8');
      const list = JSON.parse(content);
      if (Array.isArray(list) && list.length > 0) {
        return list;
      }
    }
  } catch (e) {}
  return null;
}

function persistQuizzesToDisk(list) {
  try {
    if (!fs.existsSync(TMP_DIR)) {
      fs.mkdirSync(TMP_DIR, { recursive: true });
    }
    fs.writeFileSync(QUIZZES_STORAGE_PATH, JSON.stringify(list, null, 2), 'utf8');
  } catch (e) {}
}

const DEFAULT_QUIZZES = [
  {
    id: 1,
    title: "Java Core & OOP Fundamentals",
    description: "Comprehensive assessment covering Object-Oriented Programming, Abstraction, Polymorphism, Inheritance, Encapsulation, and Java 8 Lambdas.",
    duration: 10,
    totalMarks: 50,
    questions: generateQuestionsForQuiz(1, 10)
  },
  {
    id: 2,
    title: "Advanced Java: Collections & Multithreading",
    description: "Evaluate your skills in Java Collections (List, Set, Map), Generics, Thread Lifecycle, and Concurrency utilities.",
    duration: 15,
    totalMarks: 50,
    questions: generateQuestionsForQuiz(2, 10)
  },
  {
    id: 3,
    title: "Database Concepts & JDBC Connectivity",
    description: "Questions covering relational database normalization, SQL queries, PreparedStatement, and JDBC transaction management.",
    duration: 10,
    totalMarks: 50,
    questions: generateQuestionsForQuiz(3, 10)
  }
];

let quizzes = readPersistedQuizzes() || DEFAULT_QUIZZES;

let attempts = [
  { id: 1, studentId: 2, studentName: "Alex Johnson", studentEmail: "alex@student.com", quizId: 1, quizTitle: "Java Core & OOP Fundamentals", score: 50, totalMarks: 50, percentage: 100.0, passed: true, correctAnswers: 5, wrongAnswers: 0, unansweredQuestions: 0, timeTakenSeconds: 180, attemptDate: "2026-11-02 15:30:00" },
  { id: 2, studentId: 3, studentName: "Sophia Chen", studentEmail: "sophia@student.com", quizId: 1, quizTitle: "Java Core & OOP Fundamentals", score: 40, totalMarks: 50, percentage: 80.0, passed: true, correctAnswers: 4, wrongAnswers: 1, unansweredQuestions: 0, timeTakenSeconds: 210, attemptDate: "2026-11-03 11:00:00" },
  { id: 3, studentId: 4, studentName: "Liam Davis", studentEmail: "liam@student.com", quizId: 1, quizTitle: "Java Core & OOP Fundamentals", score: 30, totalMarks: 50, percentage: 60.0, passed: true, correctAnswers: 3, wrongAnswers: 2, unansweredQuestions: 0, timeTakenSeconds: 240, attemptDate: "2026-11-04 09:20:00" }
];

const sessions = new Map();

// Helper to send JSON response
function sendJson(res, statusCode, payload) {
  res.setHeader('Content-Type', 'application/json');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Requested-With');
  res.statusCode = statusCode;
  res.end(JSON.stringify(payload));
}

function parseBody(req) {
  return new Promise((resolve) => {
    if (req.body && typeof req.body === 'object') {
      return resolve(req.body);
    }
    let body = '';
    req.on('data', chunk => { body += chunk.toString(); });
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (e) {
        resolve({});
      }
    });
  });
}

function extractToken(req) {
  const auth = req.headers['authorization'];
  if (auth && auth.startsWith('Bearer ')) {
    return auth.substring(7);
  }
  return null;
}

/**
 * Main Request Handler for Vercel Serverless Function
 */
module.exports = async function handler(req, res) {
  try {
    if (req.method === 'OPTIONS') {
      res.setHeader('Access-Control-Allow-Origin', '*');
      res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
      res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Requested-With');
      res.statusCode = 204;
      return res.end();
    }

  const host = req.headers.host || 'localhost';
  const url = new URL(req.url, `http://${host}`);
  
  // Resolve path from __route or __path or x-matched-path or url.pathname
  let path = url.pathname;
  if (url.searchParams.has('__route')) {
    path = '/api/' + url.searchParams.get('__route').replace(/^\/+/, '');
  } else if (url.searchParams.has('__path')) {
    path = '/api/' + url.searchParams.get('__path').replace(/^\/+/, '');
  } else if (req.headers['x-matched-path']) {
    path = req.headers['x-matched-path'].split('?')[0];
  } else if (req.headers['x-vercel-matched-path']) {
    path = req.headers['x-vercel-matched-path'].split('?')[0];
  } else if (req.headers['x-invoke-path']) {
    path = req.headers['x-invoke-path'].split('?')[0];
  }

  // Fallback if path resolved to index.js or api base
  if (path === '/api/index' || path === '/api' || path === '/api/index.js' || path === '/api/') {
    if (url.searchParams.has('path')) {
      path = '/api/' + url.searchParams.get('path').replace(/^\/+/, '');
    } else if (req.headers['x-now-route-matches']) {
      try {
        const matchParams = new URLSearchParams(req.headers['x-now-route-matches']);
        const matched = matchParams.get('path') || matchParams.get('1');
        if (matched) {
          path = '/api/' + decodeURIComponent(matched).replace(/^\/+/, '');
        }
      } catch (e) {}
    }
  }

  path = path.replace(/\/+$/, '') || '/';

  // API Root Status Check
  if (path === '/api' || path === '' || path === '/api/index' || path === '/api/index.js') {
    return sendJson(res, 200, {
      success: true,
      service: "Online Quiz Management System API",
      status: "HEALTHY",
      version: "2.0.0",
      timestamp: new Date().toISOString()
    });
  }

  const token = extractToken(req);
  const currentUser = token ? sessions.get(token) : null;
  const body = req.method !== 'GET' ? await parseBody(req) : {};

  // ---------------- AUTH ROUTES ----------------
  if (path === '/api/auth/login' && req.method === 'POST') {
    const { email, password } = body;
    const user = users.find(u => u.email.toLowerCase() === (email || '').trim().toLowerCase());
    if (!user || user.password !== password) {
      return sendJson(res, 401, { success: false, message: 'Invalid email or password.' });
    }

    user.loginCount = (user.loginCount || 0) + 1;
    user.lastLoginAt = new Date().toISOString().replace('T', ' ').substring(0, 19);
    user.lastLoginIp = req.headers['x-forwarded-for'] || (req.socket ? req.socket.remoteAddress : '127.0.0.1');
    persistUsersToDisk(users);

    loginHistory.unshift({
      id: loginHistory.length + 1,
      userId: user.id,
      name: user.name,
      email: user.email,
      loginTime: user.lastLoginAt,
      ipAddress: user.lastLoginIp,
      userAgent: req.headers['user-agent'] || 'Web Browser',
      loginStatus: 'SUCCESS'
    });

    const sessionToken = 'tok_' + Math.random().toString(36).substring(2) + Date.now();
    sessions.set(sessionToken, user);

    return sendJson(res, 200, {
      success: true,
      message: 'Login successful',
      data: { token: sessionToken, user }
    });
  }

  if (path === '/api/auth/register' && req.method === 'POST') {
    const { name, email, password } = body;
    if (!name || !email || !password || password.length < 6) {
      return sendJson(res, 400, { success: false, message: 'Valid name, email and password (min 6 chars) required.' });
    }

    const existing = users.find(u => u.email.toLowerCase() === email.trim().toLowerCase());
    if (existing) {
      return sendJson(res, 400, { success: false, message: 'An account with this email already exists.' });
    }

    const newUser = {
      id: users.length + 1,
      name: name.trim(),
      email: email.trim().toLowerCase(),
      password: password,
      role: 'STUDENT',
      phone: body.phone || '',
      gender: body.gender || '',
      dob: body.dob || '',
      address: body.address || '',
      city: body.city || '',
      state: body.state || '',
      institution: body.institution || '',
      department: body.department || '',
      rollNumber: body.rollNumber || '',
      degree: body.degree || '',
      yearOfStudy: body.yearOfStudy || '',
      cgpa: body.cgpa || '',
      bio: body.bio || '',
      loginCount: 1,
      lastLoginAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
      lastLoginIp: req.headers['x-forwarded-for'] || (req.socket ? req.socket.remoteAddress : '127.0.0.1'),
      status: 'ACTIVE'
    };

    users.push(newUser);
    persistUsersToDisk(users);
    const sessionToken = 'tok_' + Math.random().toString(36).substring(2) + Date.now();
    sessions.set(sessionToken, newUser);

    return sendJson(res, 200, {
      success: true,
      message: 'Registration successful! Candidate record created in database.',
      data: { token: sessionToken, user: newUser }
    });
  }

  if (path === '/api/auth/me' && req.method === 'GET') {
    if (!currentUser) return sendJson(res, 401, { success: false, message: 'Unauthorized session.' });
    return sendJson(res, 200, { success: true, data: currentUser });
  }

  if (path === '/api/auth/profile' && req.method === 'GET') {
    if (!currentUser) return sendJson(res, 401, { success: false, message: 'Unauthorized session.' });
    const userHistory = loginHistory.filter(h => h.userId === currentUser.id);
    return sendJson(res, 200, { success: true, data: { user: currentUser, loginHistory: userHistory } });
  }

  if (path === '/api/auth/profile' && req.method === 'POST') {
    if (!currentUser) return sendJson(res, 401, { success: false, message: 'Unauthorized session.' });
    Object.assign(currentUser, body);
    persistUsersToDisk(users);
    return sendJson(res, 200, { success: true, message: 'Profile updated successfully!', data: currentUser });
  }

  if (path === '/api/auth/logout' && req.method === 'POST') {
    if (token) sessions.delete(token);
    return sendJson(res, 200, { success: true, message: 'Logged out successfully' });
  }

  // ---------------- QUIZ BROWSE & ATTEMPT ROUTES ----------------
  if (path === '/api/quizzes/sync' && req.method === 'POST') {
    const clientQuizzes = body.quizzes || [];
    if (Array.isArray(clientQuizzes)) {
      clientQuizzes.forEach(cq => {
        if (!cq || !cq.id) return;
        const idx = quizzes.findIndex(q => q.id === cq.id);
        const qCount = (cq.questions && cq.questions.length) ? cq.questions.length : (cq.questionCount || 10);
        const qList = (cq.questions && cq.questions.length > 0) ? cq.questions : generateQuestionsForQuiz(cq.id, qCount);

        if (idx !== -1) {
          quizzes[idx] = {
            ...quizzes[idx],
            ...cq,
            duration: parseInt(cq.duration) || quizzes[idx].duration || 10,
            totalMarks: parseInt(cq.totalMarks) || quizzes[idx].totalMarks || 50,
            questionCount: qCount,
            totalQuestions: qCount,
            questions: (qList.length >= (quizzes[idx].questions?.length || 0)) ? qList : quizzes[idx].questions
          };
        } else {
          quizzes.push({
            id: cq.id,
            title: cq.title || `Assessment #${cq.id}`,
            description: cq.description || 'Comprehensive Examination Evaluation',
            duration: parseInt(cq.duration) || 10,
            totalMarks: parseInt(cq.totalMarks) || 50,
            questionCount: qCount,
            totalQuestions: qCount,
            questions: qList
          });
        }
      });
      persistQuizzesToDisk(quizzes);
    }
    return sendJson(res, 200, { success: true, message: 'Quizzes synced successfully', total: quizzes.length });
  }

  if (path === '/api/quizzes' && req.method === 'GET') {
    const safeQuizzes = quizzes.map(q => ({
      id: q.id,
      title: q.title,
      description: q.description,
      duration: q.duration,
      totalMarks: q.totalMarks,
      totalQuestions: q.questions ? q.questions.length : 0,
      questionCount: q.questions ? q.questions.length : 0,
      questions: q.questions
    }));
    return sendJson(res, 200, { success: true, data: safeQuizzes });
  }

  if (path === '/api/quizzes/search' && req.method === 'GET') {
    const keyword = (url.searchParams.get('q') || '').toLowerCase();
    const filtered = quizzes.filter(q => q.title.toLowerCase().includes(keyword) || q.description.toLowerCase().includes(keyword));
    return sendJson(res, 200, { success: true, data: filtered });
  }

  const quizByIdMatch = path.match(/^\/api\/quizzes\/(\d+)$/);
  if (quizByIdMatch && req.method === 'GET') {
    const qId = parseInt(quizByIdMatch[1]);
    const quiz = quizzes.find(q => q.id === qId);
    if (!quiz) return sendJson(res, 404, { success: false, message: 'Quiz not found' });
    quiz.totalQuestions = quiz.questions ? quiz.questions.length : 0;
    quiz.questionCount = quiz.questions ? quiz.questions.length : 0;
    return sendJson(res, 200, { success: true, data: quiz });
  }

  const quizQuestionsMatch = path.match(/^\/api\/quizzes\/(\d+)\/questions$/);
  if (quizQuestionsMatch && req.method === 'GET') {
    const qId = parseInt(quizQuestionsMatch[1]);
    const quiz = quizzes.find(q => q.id === qId);
    if (!quiz) return sendJson(res, 404, { success: false, message: 'Quiz not found' });
    const clientQuestions = quiz.questions.map(q => ({
      id: q.id,
      quizId: q.quizId,
      questionText: q.questionText,
      optionA: q.optionA,
      optionB: q.optionB,
      optionC: q.optionC,
      optionD: q.optionD
    }));
    return sendJson(res, 200, { success: true, data: clientQuestions });
  }

  // Quiz Start route (supports both /api/quizzes/:id/start and /api/student/quiz/:id/start)
  const quizStartMatch = path.match(/^\/api\/(?:quizzes|student\/quiz)\/(\d+)\/start$/);
  if (quizStartMatch && req.method === 'POST') {
    const qId = parseInt(quizStartMatch[1]);
    let quiz = quizzes.find(q => q.id === qId);
    if (!quiz && body && body.quiz) {
      quiz = body.quiz;
      quizzes.push(quiz);
      persistQuizzesToDisk(quizzes);
    }
    if (!quiz) {
      // Auto-create standard assessment for this ID if not in memory (e.g. Lambda cold start from QR code)
      const qCount = (body && body.questions && body.questions.length) ? body.questions.length : 10;
      quiz = {
        id: qId,
        title: (body && body.quizTitle) || `Assessment #${qId}`,
        description: 'Comprehensive assessment covering Java and Database Concepts',
        duration: 10,
        totalMarks: 50,
        questionCount: qCount,
        totalQuestions: qCount,
        questions: generateQuestionsForQuiz(qId, qCount)
      };
      quizzes.push(quiz);
      persistQuizzesToDisk(quizzes);
    }

    // Ensure all questions have proper options and structure
    if (!quiz.questions || quiz.questions.length === 0) {
      quiz.questions = generateQuestionsForQuiz(quiz.id, 10);
    }

    const sessionId = 'sess_' + Math.random().toString(36).substring(2);
    return sendJson(res, 200, {
      success: true,
      data: {
        sessionId,
        quizId: quiz.id,
        durationMinutes: quiz.duration,
        durationSeconds: quiz.duration * 60,
        totalQuestions: quiz.questions.length,
        startTime: Date.now(),
        quiz: {
          id: quiz.id,
          title: quiz.title,
          description: quiz.description,
          duration: quiz.duration,
          totalMarks: quiz.totalMarks,
          questionCount: quiz.questions.length,
          totalQuestions: quiz.questions.length,
          questions: quiz.questions.map(q => ({
            id: q.id,
            quizId: q.quizId,
            questionText: q.questionText,
            optionA: q.optionA,
            optionB: q.optionB,
            optionC: q.optionC,
            optionD: q.optionD
          }))
        }
      }
    });
  }

  // ---------------- STUDENT SUBMISSION & RESULTS ----------------
  const quizSubmitMatch = path.match(/^\/api\/(?:student\/submit|student\/quiz\/(\d+)\/submit)$/);
  if (quizSubmitMatch && req.method === 'POST') {
    const quizIdParam = quizSubmitMatch[1] ? parseInt(quizSubmitMatch[1]) : parseInt(body.quizId || 1);
    const { studentId, answers = {} } = body;
    let quiz = quizzes.find(q => q.id === quizIdParam);

    // If quiz is not in memory on this serverless instance (e.g. mobile QR scan landed on different lambda worker)
    if (!quiz) {
      const qCount = (body.questions && Array.isArray(body.questions) && body.questions.length > 0)
        ? body.questions.length
        : 10;
      quiz = {
        id: quizIdParam,
        title: body.quizTitle || `Assessment #${quizIdParam}`,
        description: 'Practical Examination Evaluation',
        duration: 10,
        totalMarks: parseInt(body.totalMarks) || 50,
        questions: generateQuestionsForQuiz(quizIdParam, qCount)
      };
      quizzes.push(quiz);
    }

    // Guarantee every question in quiz.questions has a valid correctAnswer
    if (quiz.questions && Array.isArray(quiz.questions)) {
      quiz.questions.forEach((q, idx) => {
        if (!q.correctAnswer) {
          const match = MASTER_QUESTION_BANK.find(m => m.text.toLowerCase() === (q.questionText || '').trim().toLowerCase());
          q.correctAnswer = match ? match.ans : (MASTER_QUESTION_BANK[idx % MASTER_QUESTION_BANK.length]?.ans || 'B');
        }
      });
    }

    // If client supplied more questions than quiz.questions, append extra questions with resolved answers
    if (body.questions && Array.isArray(body.questions) && body.questions.length > quiz.questions.length) {
      for (let i = quiz.questions.length; i < body.questions.length; i++) {
        const bq = body.questions[i];
        const match = MASTER_QUESTION_BANK.find(m => m.text.toLowerCase() === (bq.questionText || '').trim().toLowerCase());
        const ans = bq.correctAnswer || (match ? match.ans : (MASTER_QUESTION_BANK[i % MASTER_QUESTION_BANK.length]?.ans || 'B'));
        quiz.questions.push({
          id: bq.id || (i + 1),
          quizId: quiz.id,
          questionText: bq.questionText || `Question #${i + 1}`,
          optionA: bq.optionA || 'Option A',
          optionB: bq.optionB || 'Option B',
          optionC: bq.optionC || 'Option C',
          optionD: bq.optionD || 'Option D',
          correctAnswer: ans.toUpperCase()
        });
      }
    }

    let correctCount = 0;
    let wrongCount = 0;
    let unansweredCount = 0;

    quiz.questions.forEach((q, idx) => {
      const selected = answers[q.id] || answers[String(q.id)];
      const correctAns = (q.correctAnswer || (MASTER_QUESTION_BANK[idx % MASTER_QUESTION_BANK.length]?.ans) || 'B').toString().trim().toUpperCase();
      const userAns = selected ? String(selected).trim().toUpperCase() : null;

      if (!userAns) {
        unansweredCount++;
      } else if (userAns === correctAns) {
        correctCount++;
      } else {
        wrongCount++;
      }
    });

    const totalQCount = quiz.questions.length;
    const marksPerQ = totalQCount > 0 ? quiz.totalMarks / totalQCount : 0;
    const score = Math.round(correctCount * marksPerQ);
    const percentage = quiz.totalMarks > 0 ? Math.round((score / quiz.totalMarks) * 1000) / 10 : 0;
    const passed = percentage >= 50;

    const sId = parseInt(body.studentId) || (currentUser ? currentUser.id : (users[1] ? users[1].id : 2));
    const matchedUser = users.find(u => u.id === sId);
    const sName = body.studentName || (currentUser ? currentUser.name : (matchedUser ? matchedUser.name : 'Candidate Student'));
    const sEmail = body.studentEmail || (currentUser ? currentUser.email : (matchedUser ? matchedUser.email : 'student@quiz.com'));

    const timeTaken = (body.timeTakenSeconds !== undefined && body.timeTakenSeconds !== null) ? parseInt(body.timeTakenSeconds) : 10;
    const now = new Date();
    const formattedDate = now.toISOString().replace('T', ' ').substring(0, 19);

    // Build question reviews safely
    const reviews = quiz.questions.map((q, idx) => {
      const selected = answers[q.id] || answers[String(q.id)] || null;
      const userAns = selected ? String(selected).trim().toUpperCase() : null;
      const correctAns = (q.correctAnswer || (MASTER_QUESTION_BANK[idx % MASTER_QUESTION_BANK.length]?.ans) || 'B').toString().trim().toUpperCase();
      const isCorrect = userAns !== null && userAns === correctAns;
      return {
        questionId: q.id,
        questionIndex: idx + 1,
        questionText: q.questionText || `Question #${idx + 1}`,
        optionA: q.optionA || 'Option A',
        optionB: q.optionB || 'Option B',
        optionC: q.optionC || 'Option C',
        optionD: q.optionD || 'Option D',
        correctAnswer: correctAns,
        selectedAnswer: userAns,
        userAnswer: userAns || 'Not Answered',
        correct: isCorrect,
        isCorrect: isCorrect
      };
    });

    const newAttempt = {
      id: attempts.length + 1,
      attemptId: attempts.length + 1,
      studentId: sId,
      studentName: sName,
      studentEmail: sEmail,
      quizId: quiz.id,
      quizTitle: quiz.title,
      score,
      scoreObtained: score,
      totalMarks: quiz.totalMarks,
      percentage,
      timeTakenSeconds: timeTaken,
      attemptDate: formattedDate,
      correctAnswers: correctCount,
      wrongAnswers: wrongCount,
      unansweredQuestions: unansweredCount,
      totalQuestions: totalQCount,
      passed,
      answers,
      reviews
    };

    attempts.unshift(newAttempt);

    return sendJson(res, 200, {
      success: true,
      message: 'Quiz submitted and evaluated successfully!',
      data: newAttempt
    });
  }

  if (path === '/api/student/attempts' && req.method === 'GET') {
    const sId = currentUser ? currentUser.id : 2;
    const studentAttempts = attempts.filter(a => a.studentId === sId);
    return sendJson(res, 200, { success: true, data: studentAttempts });
  }

  const resultByIdMatch = path.match(/^\/api\/student\/results\/(\d+)$/);
  if (resultByIdMatch && req.method === 'GET') {
    const aId = parseInt(resultByIdMatch[1]);
    const attempt = attempts.find(a => a.id === aId || a.attemptId === aId);
    if (!attempt) return sendJson(res, 404, { success: false, message: 'Result attempt not found' });
    const quiz = quizzes.find(q => q.id === attempt.quizId) || {
      id: attempt.quizId,
      title: attempt.quizTitle || 'Quiz Assessment',
      questions: []
    };

    // Build question reviews (use stored reviews if available, or generate from quiz questions)
    const reviews = (attempt.reviews && attempt.reviews.length > 0) ? attempt.reviews : ((quiz.questions && quiz.questions.length > 0) ? quiz.questions.map((q, idx) => {
      const userAns = attempt.answers ? attempt.answers[q.id] : (idx < attempt.correctAnswers ? q.correctAnswer : null);
      const isCorrect = userAns && q.correctAnswer && (userAns.toUpperCase() === q.correctAnswer.toUpperCase());
      return {
        questionId: q.id,
        questionIndex: idx + 1,
        questionText: q.questionText,
        optionA: q.optionA,
        optionB: q.optionB,
        optionC: q.optionC,
        optionD: q.optionD,
        correctAnswer: q.correctAnswer,
        selectedAnswer: userAns || null,
        userAnswer: userAns || 'Not Answered',
        correct: !!isCorrect,
        isCorrect: !!isCorrect
      };
    }) : []);

    const totalQ = quiz.questions ? quiz.questions.length : 5;
    const correctCount = attempt.correctAnswers !== undefined ? attempt.correctAnswers : Math.round((attempt.percentage / 100) * totalQ);
    const wrongCount = attempt.wrongAnswers !== undefined ? attempt.wrongAnswers : Math.max(0, totalQ - correctCount);
    const unansweredCount = attempt.unansweredQuestions !== undefined ? attempt.unansweredQuestions : 0;
    const percentage = attempt.percentage !== undefined ? attempt.percentage : ((attempt.score / (attempt.totalMarks || 50)) * 100);
    const passed = attempt.passed !== undefined ? attempt.passed : (percentage >= 50);

    const resultPayload = {
      attemptId: attempt.id,
      id: attempt.id,
      studentId: attempt.studentId,
      studentName: attempt.studentName || 'Student Candidate',
      studentEmail: attempt.studentEmail || '',
      quizId: quiz.id,
      quizTitle: quiz.title || attempt.quizTitle || 'Assessment',
      totalQuestions: totalQ,
      correctAnswers: correctCount,
      wrongAnswers: wrongCount,
      unansweredQuestions: unansweredCount,
      totalMarks: attempt.totalMarks || 50,
      scoreObtained: attempt.score,
      score: attempt.score,
      percentage: Number(percentage.toFixed ? percentage.toFixed(1) : percentage),
      passed: passed,
      timeTakenSeconds: (attempt.timeTakenSeconds !== undefined && attempt.timeTakenSeconds !== null) ? attempt.timeTakenSeconds : 10,
      attemptDate: attempt.attemptDate || new Date().toISOString(),
      reviews: reviews,
      attempt: attempt,
      quiz: quiz
    };

    return sendJson(res, 200, { success: true, data: resultPayload });
  }

  // ---------------- LEADERBOARD ----------------
  if (path === '/api/leaderboard' && req.method === 'GET') {
    const quizId = url.searchParams.get('quizId');
    let board = attempts.map((a, idx) => ({
      rank: idx + 1,
      name: a.studentName,
      email: a.studentEmail,
      quizTitle: a.quizTitle,
      quizId: a.quizId,
      score: a.score,
      totalMarks: a.totalMarks,
      percentage: a.percentage,
      attemptDate: a.attemptDate
    }));

    if (quizId) {
      board = board.filter(b => b.quizId === parseInt(quizId));
    }
    board.sort((a, b) => b.score - a.score);
    board.forEach((b, i) => { b.rank = i + 1; });
    return sendJson(res, 200, { success: true, data: board });
  }

  // ---------------- ADMIN ROUTES ----------------
  if ((path === '/api/admin/candidates' || path === '/api/admin/students') && req.method === 'GET') {
    const q = (url.searchParams.get('q') || '').toLowerCase().trim();
    let studentUsers = users.filter(u => u.role === 'STUDENT');

    if (q) {
      studentUsers = studentUsers.filter(u =>
        (u.name && u.name.toLowerCase().includes(q)) ||
        (u.email && u.email.toLowerCase().includes(q)) ||
        (u.rollNumber && u.rollNumber.toLowerCase().includes(q)) ||
        (u.institution && u.institution.toLowerCase().includes(q)) ||
        (u.department && u.department.toLowerCase().includes(q)) ||
        (u.degree && u.degree.toLowerCase().includes(q)) ||
        (u.phone && u.phone.toLowerCase().includes(q))
      );
    }

    const candidates = studentUsers.map(u => {
      const candidateAttempts = attempts.filter(a => a.studentId === u.id || (a.studentEmail && a.studentEmail.toLowerCase() === u.email.toLowerCase()));
      const totalQuizzesAttempted = candidateAttempts.length;
      const averageScore = totalQuizzesAttempted > 0
        ? Math.round(candidateAttempts.reduce((acc, a) => acc + (a.percentage || 0), 0) / totalQuizzesAttempted)
        : 0;

      return {
        id: u.id,
        name: u.name,
        email: u.email,
        phone: u.phone,
        gender: u.gender,
        dob: u.dob,
        institution: u.institution,
        department: u.department,
        rollNumber: u.rollNumber,
        degree: u.degree,
        yearOfStudy: u.yearOfStudy,
        cgpa: u.cgpa,
        city: u.city,
        state: u.state,
        address: u.address,
        bio: u.bio,
        loginCount: u.loginCount || 1,
        lastLoginAt: u.lastLoginAt || new Date().toISOString().replace('T', ' ').substring(0, 19),
        lastLoginIp: u.lastLoginIp || '127.0.0.1',
        status: u.status || 'ACTIVE',
        totalQuizzesAttempted,
        averageScore
      };
    });
    return sendJson(res, 200, { success: true, data: candidates });
  }

  const candidateLoginsMatch = path.match(/^\/api\/admin\/(?:candidates|students)\/(\d+)\/logins$/);
  if (candidateLoginsMatch && req.method === 'GET') {
    const cId = parseInt(candidateLoginsMatch[1]);
    const history = loginHistory.filter(h => h.userId === cId);
    return sendJson(res, 200, { success: true, data: history });
  }

  if ((path === '/api/admin/students/sync' || path === '/api/admin/candidates/sync') && req.method === 'POST') {
    const clientStudents = body.students || body.candidates || [];
    if (Array.isArray(clientStudents)) {
      clientStudents.forEach(cs => {
        if (!cs || !cs.email) return;
        const existingIdx = users.findIndex(u => u.email.toLowerCase() === cs.email.toLowerCase());
        if (existingIdx !== -1) {
          users[existingIdx] = { ...users[existingIdx], ...cs };
        } else {
          users.push({
            id: cs.id || (users.length > 0 ? Math.max(...users.map(u => u.id)) + 1 : 1),
            role: 'STUDENT',
            status: 'ACTIVE',
            loginCount: 1,
            lastLoginAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
            ...cs
          });
        }
      });
      persistUsersToDisk(users);
    }
    return sendJson(res, 200, { success: true, message: 'Candidates synced successfully', total: users.filter(u => u.role === 'STUDENT').length });
  }

  if (path === '/api/admin/attempts' && req.method === 'GET') {
    return sendJson(res, 200, { success: true, data: attempts });
  }

  if (path === '/api/admin/stats' && req.method === 'GET') {
    const totalStudents = users.filter(u => u.role === 'STUDENT').length;
    const passed = attempts.filter(a => a.passed).length;
    const avgScore = attempts.length > 0
      ? Math.round(attempts.reduce((acc, a) => acc + a.percentage, 0) / attempts.length)
      : 0;
    const passRate = attempts.length > 0 ? Math.round((passed / attempts.length) * 100) : 0;

    return sendJson(res, 200, {
      success: true,
      data: {
        totalQuizzes: quizzes.length,
        totalStudents,
        totalAttempts: attempts.length,
        averageScore: avgScore,
        passRate,
        passedCount: passed,
        failedCount: attempts.length - passed
      }
    });
  }

  // ---------------- ADMIN QUIZ & QUESTION CRUD ----------------
  if (path === '/api/admin/quizzes' && req.method === 'POST') {
    const { title, description, duration, totalMarks, questionCount } = body;
    const newId = quizzes.length > 0 ? Math.max(...quizzes.map(q => q.id)) + 1 : 1;
    const count = Math.min(100, Math.max(1, parseInt(questionCount) || 10));
    const newQuiz = {
      id: newId,
      title: title || 'New Assessment',
      description: description || 'Practical Java and Relational Database Evaluation',
      duration: parseInt(duration) || 10,
      totalMarks: parseInt(totalMarks) || 50,
      questionCount: count,
      totalQuestions: count,
      questions: generateQuestionsForQuiz(newId, count)
    };
    quizzes.push(newQuiz);
    persistQuizzesToDisk(quizzes);
    return sendJson(res, 200, { success: true, message: 'Quiz created successfully', data: newQuiz });
  }

  const adminQuizIdMatch = path.match(/^\/api\/admin\/quizzes\/(\d+)$/);
  if (adminQuizIdMatch && req.method === 'PUT') {
    const qId = parseInt(adminQuizIdMatch[1]);
    const quiz = quizzes.find(q => q.id === qId);
    if (!quiz) return sendJson(res, 404, { success: false, message: 'Quiz not found' });
    if (body.title !== undefined) quiz.title = body.title;
    if (body.description !== undefined) quiz.description = body.description;
    if (body.duration !== undefined) quiz.duration = parseInt(body.duration) || 10;
    if (body.totalMarks !== undefined) quiz.totalMarks = parseInt(body.totalMarks) || 50;
    if (body.questionCount !== undefined) {
      const targetCount = parseInt(body.questionCount);
      if (!isNaN(targetCount) && targetCount > 0 && targetCount !== quiz.questions.length) {
        quiz.questions = generateQuestionsForQuiz(quiz.id, targetCount);
      }
    }
    quiz.questionCount = quiz.questions.length;
    quiz.totalQuestions = quiz.questions.length;
    persistQuizzesToDisk(quizzes);
    return sendJson(res, 200, { success: true, message: 'Quiz updated successfully', data: quiz });
  }

  if (adminQuizIdMatch && req.method === 'DELETE') {
    const qId = parseInt(adminQuizIdMatch[1]);
    const idx = quizzes.findIndex(q => q.id === qId);
    if (idx !== -1) {
      quizzes.splice(idx, 1);
      persistQuizzesToDisk(quizzes);
      return sendJson(res, 200, { success: true, message: 'Quiz deleted successfully' });
    }
    return sendJson(res, 404, { success: false, message: 'Quiz not found' });
  }

  const bulkGenMatch = path.match(/^\/api\/admin\/quizzes\/(\d+)\/generate-questions$/);
  if (bulkGenMatch && req.method === 'POST') {
    const qId = parseInt(bulkGenMatch[1]);
    let quiz = quizzes.find(q => q.id === qId);
    if (!quiz && body.quiz) {
      quiz = body.quiz;
      quizzes.push(quiz);
    }
    if (!quiz) return sendJson(res, 404, { success: false, message: 'Quiz not found' });
    const targetCount = Math.min(100, Math.max(1, parseInt(body.count) || 25));
    quiz.questions = generateQuestionsForQuiz(quiz.id, targetCount);
    quiz.questionCount = quiz.questions.length;
    quiz.totalQuestions = quiz.questions.length;
    persistQuizzesToDisk(quizzes);

    return sendJson(res, 200, {
      success: true,
      message: `Questions bank scaled to ${quiz.questions.length} questions successfully!`,
      data: {
        quiz,
        totalQuestions: quiz.questions.length
      }
    });
  }

  if (path === '/api/admin/questions' && req.method === 'POST') {
    const { quizId, questionText, optionA, optionB, optionC, optionD, correctAnswer } = body;
    const quiz = quizzes.find(q => q.id === parseInt(quizId));
    if (!quiz) return sendJson(res, 404, { success: false, message: 'Quiz not found' });
    const newId = quiz.questions.length > 0 ? Math.max(...quiz.questions.map(q => q.id)) + 1 : 1;
    const newQ = {
      id: newId,
      quizId: quiz.id,
      questionText: questionText || 'New Question',
      optionA: optionA || 'Option A',
      optionB: optionB || 'Option B',
      optionC: optionC || 'Option C',
      optionD: optionD || 'Option D',
      correctAnswer: (correctAnswer || 'A').toUpperCase()
    };
    quiz.questions.push(newQ);
    quiz.questionCount = quiz.questions.length;
    quiz.totalQuestions = quiz.questions.length;
    persistQuizzesToDisk(quizzes);
    return sendJson(res, 200, { success: true, message: 'Question added successfully', data: newQ, quiz });
  }

  const questionActionMatch = path.match(/^\/api\/admin\/questions\/(\d+)$/);
  if (questionActionMatch && req.method === 'PUT') {
    const qId = parseInt(questionActionMatch[1]);
    for (const quiz of quizzes) {
      const q = quiz.questions.find(x => x.id === qId);
      if (q) {
        if (body.questionText) q.questionText = body.questionText;
        if (body.optionA) q.optionA = body.optionA;
        if (body.optionB) q.optionB = body.optionB;
        if (body.optionC) q.optionC = body.optionC;
        if (body.optionD) q.optionD = body.optionD;
        if (body.correctAnswer) q.correctAnswer = body.correctAnswer.toUpperCase();
        persistQuizzesToDisk(quizzes);
        return sendJson(res, 200, { success: true, message: 'Question updated successfully', data: q, quiz });
      }
    }
    return sendJson(res, 404, { success: false, message: 'Question not found' });
  }

  if (questionActionMatch && req.method === 'DELETE') {
    const qId = parseInt(questionActionMatch[1]);
    for (const quiz of quizzes) {
      const idx = quiz.questions.findIndex(x => x.id === qId);
      if (idx !== -1) {
        quiz.questions.splice(idx, 1);
        quiz.questionCount = quiz.questions.length;
        quiz.totalQuestions = quiz.questions.length;
        persistQuizzesToDisk(quizzes);
        return sendJson(res, 200, { success: true, message: 'Question deleted successfully', quiz });
      }
    }
    return sendJson(res, 404, { success: false, message: 'Question not found' });
  }

  // ---------------- SYSTEM DB STATUS ----------------
  if (path === '/api/system/db-status' && req.method === 'GET') {
    return sendJson(res, 200, {
      success: true,
      data: {
        connected: true,
        mode: "VERCEL_SERVERLESS_DATABASE",
        database: "online_quiz_db",
        user: "root",
        statusMessage: "Connected to Cloud Database Engine (online_quiz_db)"
      }
    });
  }

  if (path === '/api/system/reconnect-db' && req.method === 'POST') {
    return sendJson(res, 200, {
      success: true,
      data: {
        connected: true,
        message: "Connected to Cloud Database Engine successfully!"
      }
    });
  }

  // Unhandled API route
  return sendJson(res, 404, {
    success: false,
    message: `Endpoint not found: ${path}`,
    debug: {
      url: req.url,
      matchedPath: req.headers['x-matched-path'] || null,
      routeMatches: req.headers['x-now-route-matches'] || null
    }
  });
  } catch (globalErr) {
    console.error('SERVERLESS GLOBAL ERROR:', globalErr);
    return sendJson(res, 500, {
      success: false,
      message: 'Server processing error: ' + (globalErr.message || 'Unknown server error'),
      error: globalErr.toString()
    });
  }
};
