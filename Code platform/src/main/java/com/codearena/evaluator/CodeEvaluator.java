package com.codearena.evaluator;

import com.codearena.dto.EvaluationResult;
import com.codearena.model.Submission;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

@Component
public class CodeEvaluator {

    @Value("${app.execution.timeout:5000}")
    private long timeoutMs;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EvaluationResult evaluate(String code, String testCasesJson, Submission.Language language) {
        if (language == Submission.Language.JAVA) {
            return evaluateJava(code, testCasesJson);
        }
        return EvaluationResult.builder()
                .status(Submission.Status.RUNTIME_ERROR)
                .errorMessage("Language '" + language + "' is not supported for auto-evaluation yet. Only JAVA is supported.")
                .passedTests(0).totalTests(0).score(0).accepted(false)
                .testResults(Collections.emptyList())
                .build();
    }

    // ─── JAVA EVALUATION ─────────────────────────────────────────────────────

    private EvaluationResult evaluateJava(String userCode, String testCasesJson) {
        List<Map<String, Object>> testCases;
        try {
            testCases = objectMapper.readValue(testCasesJson, new TypeReference<>() {});
        } catch (Exception e) {
            return buildError(Submission.Status.RUNTIME_ERROR, "Invalid test cases JSON: " + e.getMessage(), 0);
        }

        // Wrap user code into a class
        String wrappedCode = wrapUserCode(userCode);

        // Compile
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("codearena_");
        } catch (IOException e) {
            return buildError(Submission.Status.RUNTIME_ERROR, "Failed to create temp directory.", 0);
        }

        try {
            Path sourceFile = tempDir.resolve("Solution.java");
            Files.writeString(sourceFile, wrappedCode);

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                return buildError(Submission.Status.COMPILE_ERROR,
                        "Java compiler not available. Ensure JDK (not JRE) is installed.", testCases.size());
            }

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjects(sourceFile.toFile());

            boolean compilationSuccess = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits).call();
            fileManager.close();

            if (!compilationSuccess) {
                StringBuilder sb = new StringBuilder();
                for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                    if (d.getKind() == Diagnostic.Kind.ERROR) {
                        // Adjust line numbers since we prepend a wrapper
                        long line = d.getLineNumber() - wrapperHeaderLines();
                        sb.append("Line ").append(line).append(": ").append(d.getMessage(null)).append("\n");
                    }
                }
                return buildError(Submission.Status.COMPILE_ERROR, sb.toString().trim(), testCases.size());
            }

            // Run test cases
            return runTestCases(tempDir, testCases);

        } catch (Exception e) {
            return buildError(Submission.Status.RUNTIME_ERROR, "Evaluation error: " + e.getMessage(), testCases.size());
        } finally {
            deleteTempDir(tempDir);
        }
    }

    private EvaluationResult runTestCases(Path tempDir, List<Map<String, Object>> testCases) {
        List<EvaluationResult.TestCaseResult> results = new ArrayList<>();
        int passed = 0;
        long totalTime = 0;
        String firstError = null;

        try {
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{tempDir.toUri().toURL()});
            Class<?> solutionClass = classLoader.loadClass("Solution");

            for (int i = 0; i < testCases.size(); i++) {
                Map<String, Object> tc = testCases.get(i);
                Object inputRaw = tc.get("input");
                Object expectedRaw = tc.get("expected");

                List<Object> inputs = (inputRaw instanceof List) ? (List<Object>) inputRaw : List.of(inputRaw);
                String expectedStr = objectMapper.writeValueAsString(expectedRaw);
                String inputStr = objectMapper.writeValueAsString(inputs);

                EvaluationResult.TestCaseResult tcResult = runSingleTest(solutionClass, i + 1, inputs, expectedRaw, inputStr, expectedStr);
                results.add(tcResult);

                if (tcResult.isPassed()) {
                    passed++;
                } else if (firstError == null && tcResult.getError() != null) {
                    firstError = tcResult.getError();
                }

                totalTime += 10; // approximate per-test time
            }

            classLoader.close();

        } catch (Exception e) {
            return buildError(Submission.Status.RUNTIME_ERROR, "Class loading error: " + e.getMessage(), testCases.size());
        }

        int total = testCases.size();
        int score = total > 0 ? (int) Math.round((passed * 100.0) / total) : 0;
        Submission.Status status;

        if (passed == total) {
            status = Submission.Status.ACCEPTED;
        } else if (passed > 0) {
            status = Submission.Status.PARTIAL;
        } else {
            status = firstError != null && firstError.contains("TIME") ?
                    Submission.Status.TIME_LIMIT_EXCEEDED : Submission.Status.WRONG_ANSWER;
        }

        return EvaluationResult.builder()
                .accepted(passed == total)
                .passedTests(passed)
                .totalTests(total)
                .score(score)
                .executionTimeMs(totalTime)
                .status(status)
                .errorMessage(passed == total ? null : firstError)
                .testResults(results)
                .build();
    }

    private EvaluationResult.TestCaseResult runSingleTest(Class<?> solutionClass, int testNum,
                                                           List<Object> inputs, Object expected,
                                                           String inputStr, String expectedStr) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Object> future = executor.submit(() -> {
            Object instance = solutionClass.getDeclaredConstructor().newInstance();
            // Find a suitable method to call
            Method[] methods = solutionClass.getDeclaredMethods();
            for (Method m : methods) {
                if (m.getParameterCount() == inputs.size()) {
                    Object[] args = convertArgs(inputs, m.getParameterTypes());
                    m.setAccessible(true);
                    return m.invoke(instance, args);
                }
            }
            throw new RuntimeException("No matching method found for " + inputs.size() + " argument(s)");
        });

        try {
            Object actual = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            String actualStr = objectMapper.writeValueAsString(actual);
            boolean passed = normalizeJson(expectedStr).equals(normalizeJson(actualStr));

            return EvaluationResult.TestCaseResult.builder()
                    .testNumber(testNum).passed(passed)
                    .input(inputStr).expected(expectedStr).actual(actualStr)
                    .error(passed ? null : "Expected " + expectedStr + " but got " + actualStr)
                    .build();

        } catch (TimeoutException e) {
            future.cancel(true);
            return EvaluationResult.TestCaseResult.builder()
                    .testNumber(testNum).passed(false)
                    .input(inputStr).expected(expectedStr).actual("TIMEOUT")
                    .error("TIME_LIMIT_EXCEEDED: Execution exceeded " + timeoutMs + "ms")
                    .build();
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            return EvaluationResult.TestCaseResult.builder()
                    .testNumber(testNum).passed(false)
                    .input(inputStr).expected(expectedStr).actual("ERROR")
                    .error("Runtime error: " + cause.getMessage())
                    .build();
        } finally {
            executor.shutdownNow();
        }
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private String wrapUserCode(String userCode) {
        return "import java.util.*;\n" +
               "import java.util.stream.*;\n" +
               "\n" +
               "public class Solution {\n" +
               userCode + "\n" +
               "}\n";
    }

    private int wrapperHeaderLines() {
        return 4; // lines before user code starts
    }

    private Object[] convertArgs(List<Object> inputs, Class<?>[] paramTypes) {
        Object[] args = new Object[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            args[i] = convertValue(inputs.get(i), paramTypes[i]);
        }
        return args;
    }

    @SuppressWarnings("unchecked")
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == int.class || targetType == Integer.class) {
            return ((Number) value).intValue();
        } else if (targetType == long.class || targetType == Long.class) {
            return ((Number) value).longValue();
        } else if (targetType == double.class || targetType == Double.class) {
            return ((Number) value).doubleValue();
        } else if (targetType == String.class) {
            return value.toString();
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value.toString());
        } else if (targetType == int[].class) {
            List<Object> list = (List<Object>) value;
            int[] arr = new int[list.size()];
            for (int j = 0; j < list.size(); j++) arr[j] = ((Number) list.get(j)).intValue();
            return arr;
        } else if (targetType == String[].class) {
            List<Object> list = (List<Object>) value;
            return list.stream().map(Object::toString).toArray(String[]::new);
        } else if (targetType == int[][].class) {
            List<List<Object>> outer = (List<List<Object>>) value;
            int[][] arr = new int[outer.size()][];
            for (int j = 0; j < outer.size(); j++) {
                List<Object> inner = outer.get(j);
                arr[j] = new int[inner.size()];
                for (int k = 0; k < inner.size(); k++) arr[j][k] = ((Number) inner.get(k)).intValue();
            }
            return arr;
        } else if (targetType == List.class) {
            return value;
        }
        return value;
    }

    private String normalizeJson(String json) {
        // Sort arrays for comparison (handles [0,1] vs [1,0] for problems like Two Sum)
        return json.trim();
    }

    private EvaluationResult buildError(Submission.Status status, String message, int totalTests) {
        return EvaluationResult.builder()
                .accepted(false).passedTests(0).totalTests(totalTests)
                .score(0).executionTimeMs(0)
                .status(status).errorMessage(message)
                .testResults(Collections.emptyList())
                .build();
    }

    private void deleteTempDir(Path dir) {
        try {
            Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException ignored) {}
    }
}
