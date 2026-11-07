package ir.appservice.common.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import ir.appservice.common.context.TraceContextHolder;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Universal JSON response envelope for all AppService REST API endpoints.
 *
 * @param <T> Payload body type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    private boolean success;
    private String traceId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant timestamp;

    private int statusCode;
    private String message;
    private T data;
    private List<String> errors;

    public ApiResponse() {
        this.timestamp = Instant.now();
        this.traceId = TraceContextHolder.getTraceIdOrDefault();
    }

    public ApiResponse(boolean success, int statusCode, String message, T data, List<String> errors) {
        this.success = success;
        this.traceId = TraceContextHolder.getTraceIdOrDefault();
        this.timestamp = Instant.now();
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        this.errors = errors != null ? errors : Collections.emptyList();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, "Operation successful", data, Collections.emptyList());
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, 200, message, data, Collections.emptyList());
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(true, 201, message, data, Collections.emptyList());
    }

    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return new ApiResponse<>(false, statusCode, message, null, List.of(message));
    }

    public static <T> ApiResponse<T> error(String message, int statusCode, List<String> errors) {
        return new ApiResponse<>(false, statusCode, message, null, errors);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
