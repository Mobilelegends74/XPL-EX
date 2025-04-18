package eu.faircode.xlua.x.data.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility class for executing code in try-catch blocks with various error handling options.
 * Compatible with Android SDK 23+.
 */
public class TryRun {
    private static final String TAG = "XLua.TryRun";

    /**
     * Interface for operations with no parameters and no return value
     */
    public interface Action {
        void run() throws Exception;
    }

    /**
     * Interface for operations with no parameters that return a value
     */
    public interface ActionResult<R> {
        R run() throws Exception;
    }

    /**
     * Interface for operations with one parameter and no return value
     */
    public interface ActionParam<T> {
        void run(T param) throws Exception;
    }

    /**
     * Interface for operations with one parameter that return a value
     */
    public interface ActionParamResult<T, R> {
        R run(T param) throws Exception;
    }

    /**
     * Interface for handling exceptions
     */
    public interface ExceptionHandler {
        void handle(Exception e);
    }

    /**
     * Executes a void action in a try-catch block and ignores any exceptions.
     * @param action The action to execute
     */
    public static void silent(Action action) {
        try {
            action.run();
        } catch (Exception e) {
            // Silently ignore exception
        }
    }

    /**
     * Executes a void action in a try-catch block and logs any exceptions.
     * @param action The action to execute
     * @param logTag The tag to use for logging errors
     */
    public static void log(Action action, String logTag) {
        try {
            action.run();
        } catch (Exception e) {
            Log.e(logTag, "Exception in try run: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a void action in a try-catch block and logs any exceptions with default tag.
     * @param action The action to execute
     */
    public static void log(Action action) {
        log(action, TAG);
    }

    /**
     * Executes a void action in a try-catch block and passes any exceptions to a handler.
     * @param action The action to execute
     * @param exceptionHandler The handler for any exceptions
     */
    public static void handle(Action action, ExceptionHandler exceptionHandler) {
        try {
            action.run();
        } catch (Exception e) {
            exceptionHandler.handle(e);
        }
    }

    public interface IGetCollection {

    }

    /**
     * Executes a supplier function in a try-catch block and returns null if an exception occurs.
     * @param supplier The supplier function
     * @param <T> The return type
     * @return The result or null if an exception occurred
     */
    public static <T> T get(ActionResult<T> supplier) {
        try {
            return supplier.run();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Executes a supplier function in a try-catch block and returns a default value if an exception occurs.
     * @param supplier The supplier function
     * @param defaultValue The default value to return if an exception occurs
     * @param <T> The return type
     * @return The result or the default value if an exception occurred
     */
    public static <T> T getOrDefault(ActionResult<T> supplier, T defaultValue) {
        try {
            return supplier.run();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Executes a supplier function in a try-catch block, logs any exceptions, and returns null.
     * @param supplier The supplier function
     * @param logTag The tag to use for logging errors
     * @param <T> The return type
     * @return The result or null if an exception occurred
     */
    public static <T> T getAndLog(ActionResult<T> supplier, String logTag) {
        try {
            return supplier.run();
        } catch (Exception e) {
            Log.e(logTag, "Exception in try run: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Executes a supplier function in a try-catch block, logs any exceptions with default tag, and returns null.
     * @param supplier The supplier function
     * @param <T> The return type
     * @return The result or null if an exception occurred
     */
    public static <T> T getAndLog(ActionResult<T> supplier) {
        return getAndLog(supplier, TAG);
    }

    /**
     * Executes a function with input in a try-catch block and returns null if an exception occurs.
     * @param function The function to execute
     * @param input The input parameter
     * @param <T> The input type
     * @param <R> The return type
     * @return The result or null if an exception occurred
     */
    public static <T, R> R apply(ActionParamResult<T, R> function, T input) {
        try {
            return function.run(input);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Executes a function with input in a try-catch block and returns a default value if an exception occurs.
     * @param function The function to execute
     * @param input The input parameter
     * @param defaultValue The default value to return if an exception occurs
     * @param <T> The input type
     * @param <R> The return type
     * @return The result or the default value if an exception occurred
     */
    public static <T, R> R applyOrDefault(ActionParamResult<T, R> function, T input, R defaultValue) {
        try {
            return function.run(input);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Executes a function with input in a try-catch block, passes any exceptions to a handler, and returns null.
     * @param function The function to execute
     * @param input The input parameter
     * @param exceptionHandler The handler for any exceptions
     * @param <T> The input type
     * @param <R> The return type
     * @return The result or null if an exception occurred
     */
    public static <T, R> R applyWithHandler(ActionParamResult<T, R> function, T input, ExceptionHandler exceptionHandler) {
        try {
            return function.run(input);
        } catch (Exception e) {
            exceptionHandler.handle(e);
            return null;
        }
    }

    /**
     * Executes a consumer with input in a try-catch block and ignores any exceptions.
     * @param consumer The consumer to execute
     * @param input The input parameter
     * @param <T> The input type
     */
    public static <T> void accept(ActionParam<T> consumer, T input) {
        try {
            consumer.run(input);
        } catch (Exception e) {
            // Silently ignore exception
        }
    }

    /**
     * Executes a consumer with input in a try-catch block and logs any exceptions.
     * @param consumer The consumer to execute
     * @param input The input parameter
     * @param logTag The tag to use for logging errors
     * @param <T> The input type
     */
    public static <T> void acceptAndLog(ActionParam<T> consumer, T input, String logTag) {
        try {
            consumer.run(input);
        } catch (Exception e) {
            Log.e(logTag, "Exception in try run: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a consumer with input in a try-catch block and logs any exceptions with default tag.
     * @param consumer The consumer to execute
     * @param input The input parameter
     * @param <T> The input type
     */
    public static <T> void acceptAndLog(ActionParam<T> consumer, T input) {
        acceptAndLog(consumer, input, TAG);
    }

    /**
     * Executes an action on the main UI thread. If called from the main thread,
     * executes immediately; otherwise posts to the main thread handler.
     * @param action The action to execute
     */
    public static void onMain(Action action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            silent(action);
        } else {
            new Handler(Looper.getMainLooper()).post(() -> silent(action));
        }
    }

    /**
     * Executes an action on the main UI thread with logging for exceptions.
     * @param action The action to execute
     */
    public static void onMainLog(Action action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            log(action);
        } else {
            new Handler(Looper.getMainLooper()).post(() -> log(action));
        }
    }

    /**
     * Executes an action with a parameter on the main UI thread.
     * @param consumer The action to execute
     * @param input The input parameter
     * @param <T> The input type
     */
    public static <T> void onMainAccept(ActionParam<T> consumer, T input) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            accept(consumer, input);
        } else {
            new Handler(Looper.getMainLooper()).post(() -> accept(consumer, input));
        }
    }

    /**
     * Executes an action with a parameter on the main UI thread with logging.
     * @param consumer The action to execute
     * @param input The input parameter
     * @param <T> The input type
     */
    public static <T> void onMainAcceptLog(ActionParam<T> consumer, T input) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            acceptAndLog(consumer, input);
        } else {
            new Handler(Looper.getMainLooper()).post(() -> acceptAndLog(consumer, input));
        }
    }

    /**
     * Executes an action with handler for UI updates and with exception handling.
     * @param backgroundAction The action to execute in background
     * @param uiAction The UI update action to run on main thread
     * @param exceptionHandler Handler for any exceptions
     */
    public static void background(Action backgroundAction, Action uiAction, ExceptionHandler exceptionHandler) {
        new Thread(() -> {
            try {
                backgroundAction.run();
                if (uiAction != null) {
                    onMain(uiAction);
                }
            } catch (Exception e) {
                if (exceptionHandler != null) {
                    onMain(() -> exceptionHandler.handle(e));
                }
            }
        }).start();
    }

    /**
     * Executes an action in background with UI update on completion.
     * @param backgroundAction The action to execute in background
     * @param uiAction The UI update action to run on main thread after successful completion
     */
    public static void background(Action backgroundAction, Action uiAction) {
        background(backgroundAction, uiAction, null);
    }

    /**
     * Executes an action in background.
     * @param backgroundAction The action to execute in background
     */
    public static void background(Action backgroundAction) {
        background(backgroundAction, null, null);
    }
}