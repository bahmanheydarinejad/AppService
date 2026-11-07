package ir.appservice.common.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import ir.appservice.common.context.TraceContextHolder;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Universal paged API envelope for all subject search endpoints.
 *
 * @param <T> Item entity type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedApiResponse<T> implements Serializable {

    private boolean success;
    private String traceId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant timestamp;

    private String subject;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private long executionTimeMs;
    private List<T> data;

    public PagedApiResponse() {
        this.success = true;
        this.traceId = TraceContextHolder.getTraceIdOrDefault();
        this.timestamp = Instant.now();
        this.data = Collections.emptyList();
    }

    public PagedApiResponse(String subject, List<T> data, int page, int size, long totalElements, long executionTimeMs) {
        this.success = true;
        this.traceId = TraceContextHolder.getTraceIdOrDefault();
        this.timestamp = Instant.now();
        this.subject = subject;
        this.data = data != null ? data : Collections.emptyList();
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        this.executionTimeMs = executionTimeMs;
    }

    public static <T> PagedApiResponse<T> of(String subject, List<T> data, int page, int size, long totalElements, long executionTimeMs) {
        return new PagedApiResponse<>(subject, data, page, size, totalElements, executionTimeMs);
    }

    public static <T> PagedApiResponse<T> empty(String subject, int page, int size) {
        return new PagedApiResponse<>(subject, Collections.emptyList(), page, size, 0L, 0L);
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
