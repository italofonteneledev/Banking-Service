package exception;

import java.util.Map;

public record ProblemDetail(
        String type,
        int status,
        String title,
        String detail,
        Map<String, Object> properties
) {

    private static final String DEFAULT_TYPE = "about:blank";

    public ProblemDetail(int status, String title, String detail) {
        this(DEFAULT_TYPE, status, title, detail, null);
    }

    public ProblemDetail(int status, String title, String detail, Map<String, Object> properties) {
        this(DEFAULT_TYPE, status, title, detail, properties);
    }

    public static ProblemDetail forStatus(int status) {
        return new ProblemDetail(status, null, null, null);
    }
}