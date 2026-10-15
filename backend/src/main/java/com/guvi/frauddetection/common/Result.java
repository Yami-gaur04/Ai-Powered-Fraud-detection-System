package com.guvi.frauddetection.common;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Generic wrapper for operation results, encapsulating either success with data
 * or failure with an error message and code.
 *
 * @param <T> The data type wrapped by this result
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final T data;
    private final String message;
    private final String errorCode;

    private Result(boolean success, T data, String message, String errorCode) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
    }

    /**
     * Creates a successful result containing payload data.
     *
     * @param data the result value
     * @param <T>  the value type
     * @return successful {@link Result} instance
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, "Success", null);
    }

    /**
     * Creates a successful result with custom message.
     *
     * @param data    the result value
     * @param message description of success
     * @param <T>     the value type
     * @return successful {@link Result} instance
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(true, data, message, null);
    }

    /**
     * Creates a failure result with an error message.
     *
     * @param message the failure message
     * @param <T>     the expected value type
     * @return failed {@link Result} instance
     */
    public static <T> Result<T> failure(String message) {
        return new Result<>(false, null, message, "ERROR_GENERAL");
    }

    /**
     * Creates a failure result with custom error code and message.
     *
     * @param errorCode the error identifier
     * @param message   the failure message
     * @param <T>       the expected value type
     * @return failed {@link Result} instance
     */
    public static <T> Result<T> failure(String errorCode, String message) {
        return new Result<>(false, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Transforms the payload data if the result is successful.
     *
     * @param mapper transformation function
     * @param <U>    target type
     * @return new {@link Result} of target type
     */
    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isSuccess()) {
            return Result.success(mapper.apply(data), message);
        }
        return Result.failure(errorCode, message);
    }

    /**
     * Executes the given action if the result is successful.
     *
     * @param action the consumer action
     */
    public void ifSuccess(Consumer<? super T> action) {
        if (isSuccess()) {
            action.accept(data);
        }
    }
}
