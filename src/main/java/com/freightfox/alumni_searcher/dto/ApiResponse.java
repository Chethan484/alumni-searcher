
package com.freightfox.alumni_searcher.dto;

public class ApiResponse<T> {

    private String status;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(String status, T data) {
        this.status = status;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }
}
