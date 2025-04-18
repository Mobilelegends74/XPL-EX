package eu.faircode.xlua.x.data.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.runtime.reflect.DynType;

public class ArrayUtils {
    public static interface IOnInt {  int parseRange(int range); }

    public static boolean isArray(Object o) { return o instanceof Array || ( o != null && o.getClass().isArray()); }
    public static boolean isMinimumSize(Object o, int minimumSize) { return minimumSize > -1 && isArray(o) && safeLength(o) >= minimumSize;  }
    public static boolean isMinimumIndex(Object o, int minimumIndex) { return minimumIndex > -1 && isArray(o) && safeLength(o) > minimumIndex;  }
    public static Object getElementAtSafe(Object o, int index) { return isMinimumIndex(o, index) ? TryRun.get(() -> Array.get(o, index)) : null; }


    public static String[] emptyStringArray() { return new String[] { }; }

    /**
     * Removes null elements from any array type by creating a new array without nulls.
     * This method only works for object arrays, not primitive arrays (as primitives cannot be null).
     *
     * @param array The array to process
     * @return A new array with null elements removed, or the original array if operation fails or no nulls found
     */
    public static Object removeNulls(@Nullable Object array) {
        if (!isArray(array)) {
            return array;
        }

        try {
            int length = Array.getLength(array);

            // Check if array is primitive (can't have nulls)
            Class<?> componentType = array.getClass().getComponentType();
            if (componentType.isPrimitive()) {
                return array; // Primitives can't be null, so nothing to remove
            }

            // Count non-null elements
            int nonNullCount = 0;
            for (int i = 0; i < length; i++) {
                if (Array.get(array, i) != null) {
                    nonNullCount++;
                }
            }

            // If all elements are non-null, return original array
            if (nonNullCount == length) {
                return array;
            }

            // Create new array with non-null elements
            Object newArray = Array.newInstance(componentType, nonNullCount);

            // Copy non-null elements to new array
            int newIndex = 0;
            for (int i = 0; i < length; i++) {
                Object element = Array.get(array, i);
                if (element != null) {
                    Array.set(newArray, newIndex++, element);
                }
            }

            return newArray;
        } catch (Exception ignored) {
            // Silent fail, return original array
        }

        return array;
    }

    /**
     * Clears all elements in an array by setting them to null or default values.
     * Works with any array type.
     *
     * @param array The array to clear
     * @return True if the operation succeeded, false otherwise
     */
    public static boolean clear(@Nullable Object array) {
        if (!isArray(array)) {
            return false;
        }

        try {
            int length = Array.getLength(array);
            Class<?> componentType = array.getClass().getComponentType();

            // Set each element to null for object arrays or default value for primitives
            for (int i = 0; i < length; i++) {
                if (componentType.isPrimitive()) {
                    // Set primitive values to their defaults (0, false, etc.)
                    if (componentType == boolean.class) {
                        Array.setBoolean(array, i, false);
                    } else if (componentType == byte.class) {
                        Array.setByte(array, i, (byte) 0);
                    } else if (componentType == char.class) {
                        Array.setChar(array, i, (char) 0);
                    } else if (componentType == short.class) {
                        Array.setShort(array, i, (short) 0);
                    } else if (componentType == int.class) {
                        Array.setInt(array, i, 0);
                    } else if (componentType == long.class) {
                        Array.setLong(array, i, 0L);
                    } else if (componentType == float.class) {
                        Array.setFloat(array, i, 0.0f);
                    } else if (componentType == double.class) {
                        Array.setDouble(array, i, 0.0);
                    }
                } else {
                    // For object arrays, set to null
                    Array.set(array, i, null);
                }
            }
            return true;
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Clears all elements in a typed array by setting them to null.
     *
     * @param <T> The type of elements in the array
     * @param array The array to clear
     * @return True if the operation succeeded, false otherwise
     */
    public static <T> boolean clear(@Nullable T[] array) {
        if (array == null) {
            return false;
        }

        try {
            Arrays.fill(array, null);
            return true;
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Clears all elements in an int array by setting them to 0.
     *
     * @param array The array to clear
     * @return True if the operation succeeded, false otherwise
     */
    public static boolean clear(@Nullable int[] array) {
        if (array == null) {
            return false;
        }

        try {
            Arrays.fill(array, 0);
            return true;
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Clears all elements in a long array by setting them to 0.
     *
     * @param array The array to clear
     * @return True if the operation succeeded, false otherwise
     */
    public static boolean clear(@Nullable long[] array) {
        if (array == null) {
            return false;
        }

        try {
            Arrays.fill(array, 0L);
            return true;
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Clears all elements in a double array by setting them to 0.0.
     *
     * @param array The array to clear
     * @return True if the operation succeeded, false otherwise
     */
    public static boolean clear(@Nullable double[] array) {
        if (array == null) {
            return false;
        }

        try {
            Arrays.fill(array, 0.0);
            return true;
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Clears all elements in a boolean array by setting them to false.
     *
     * @param array The array to clear
     * @return True if the operation succeeded, false otherwise
     */
    public static boolean clear(@Nullable boolean[] array) {
        if (array == null) {
            return false;
        }

        try {
            Arrays.fill(array, false);
            return true;
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Removes duplicate elements from any array type, creating a new array without duplicates.
     *
     * @param array The array to process
     * @return A new array with duplicates removed, or the original array if operation fails
     */
    public static Object removeDuplicates(@Nullable Object array) {
        if (!isArray(array)) {
            return array;
        }

        try {
            int length = Array.getLength(array);
            if (length <= 1) {
                return array; // No duplicates possible with 0 or 1 elements
            }

            Class<?> componentType = array.getClass().getComponentType();
            List<Object> uniqueItems = new ArrayList<>();

            // Collect unique elements
            for (int i = 0; i < length; i++) {
                Object current = Array.get(array, i);

                // Skip null values for object arrays
                if (current == null && !componentType.isPrimitive()) {
                    continue;
                }

                // Add if not already in our unique list
                boolean exists = false;
                for (Object item : uniqueItems) {
                    if ((current == null && item == null) ||
                            (current != null && current.equals(item))) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    uniqueItems.add(current);
                }
            }

            // Create new array with unique items
            Object newArray = Array.newInstance(componentType, uniqueItems.size());
            for (int i = 0; i < uniqueItems.size(); i++) {
                Array.set(newArray, i, uniqueItems.get(i));
            }

            return newArray;
        } catch (Exception ignored) {
            // Silent fail, return original array
        }

        return array;
    }

    /**
     * Removes duplicate elements from a typed array, creating a new array without duplicates.
     *
     * @param <T> The type of elements in the array
     * @param array The array to process
     * @return A new array with duplicates removed, or the original array if operation fails
     */
    public static <T> T[] removeDuplicates(@Nullable T[] array) {
        if (array == null || array.length <= 1) {
            return array; // No duplicates possible with 0 or 1 elements
        }

        try {
            // Use LinkedHashSet to maintain order and eliminate duplicates
            Set<T> uniqueItems = new LinkedHashSet<>();

            // Add all non-null items
            for (T item : array) {
                if (item != null) {
                    uniqueItems.add(item);
                }
            }

            // Create result array
            @SuppressWarnings("unchecked")
            T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), uniqueItems.size());
            return uniqueItems.toArray(result);
        } catch (Exception ignored) {
            // Silent fail, return original array
        }

        return array;
    }

    /**
     * Removes duplicate elements from an int array.
     *
     * @param array The array to process
     * @return A new array with duplicates removed, or the original array if operation fails
     */
    public static int[] removeDuplicates(@Nullable int[] array) {
        if (array == null || array.length <= 1) {
            return array; // No duplicates possible with 0 or 1 elements
        }

        try {
            // Use a set to track unique values
            Set<Integer> uniqueValues = new LinkedHashSet<>();
            for (int value : array) {
                uniqueValues.add(value);
            }

            // Create result array
            int[] result = new int[uniqueValues.size()];
            int index = 0;
            for (int value : uniqueValues) {
                result[index++] = value;
            }

            return result;
        } catch (Exception ignored) {
            // Silent fail, return original array
        }

        return array;
    }

    /**
     * Removes duplicate elements from a long array.
     *
     * @param array The array to process
     * @return A new array with duplicates removed, or the original array if operation fails
     */
    public static long[] removeDuplicates(@Nullable long[] array) {
        if (array == null || array.length <= 1) {
            return array; // No duplicates possible with 0 or 1 elements
        }

        try {
            // Use a set to track unique values
            Set<Long> uniqueValues = new LinkedHashSet<>();
            for (long value : array) {
                uniqueValues.add(value);
            }

            // Create result array
            long[] result = new long[uniqueValues.size()];
            int index = 0;
            for (long value : uniqueValues) {
                result[index++] = value;
            }

            return result;
        } catch (Exception ignored) {
            // Silent fail, return original array
        }

        return array;
    }

    /**
     * Removes duplicate elements from a double array.
     *
     * @param array The array to process
     * @return A new array with duplicates removed, or the original array if operation fails
     */
    public static double[] removeDuplicates(@Nullable double[] array) {
        if (array == null || array.length <= 1) {
            return array; // No duplicates possible with 0 or 1 elements
        }

        try {
            // Use a set to track unique values
            Set<Double> uniqueValues = new LinkedHashSet<>();
            for (double value : array) {
                uniqueValues.add(value);
            }

            // Create result array
            double[] result = new double[uniqueValues.size()];
            int index = 0;
            for (double value : uniqueValues) {
                result[index++] = value;
            }

            return result;
        } catch (Exception ignored) {
            // Silent fail, return original array
        }

        return array;
    }

    /**
     * Removes duplicate elements from a boolean array.
     * Note that a boolean array can only have two unique values (true/false).
     *
     * @param array The array to process
     * @return A new array with duplicates removed, or the original array if operation fails
     */
    public static boolean[] removeDuplicates(@Nullable boolean[] array) {
        if (array == null || array.length <= 1) {
            return array; // No duplicates possible with 0 or 1 elements
        }

        try {
            // Track if we've seen true and false
            boolean seenTrue = false;
            boolean seenFalse = false;

            for (boolean value : array) {
                if (value) {
                    seenTrue = true;
                } else {
                    seenFalse = true;
                }

                // If we've seen both, we can stop
                if (seenTrue && seenFalse) {
                    break;
                }
            }

            // Count unique values
            int uniqueCount = 0;
            if (seenTrue) uniqueCount++;
            if (seenFalse) uniqueCount++;

            // Create result array with unique values
            boolean[] result = new boolean[uniqueCount];
            int index = 0;

            if (seenFalse) {
                result[index++] = false;
            }

            if (seenTrue) {
                result[index] = true;
            }

            return result;
        } catch (Exception ignored) {
            // Silent fail, return original array
        }

        return array;
    }

    /**
     * Finds the index of an element in any array type.
     * Returns -1 if the element is not found or if the array is invalid.
     *
     * @param array The array to search in (can be any array type)
     * @param element The element to search for
     * @return The index of the first occurrence of the element, or -1 if not found
     */
    public static int getIndexOfElement(@Nullable Object array, @Nullable Object element) {
        if (!isArray(array) || element == null) {
            return -1;
        }

        try {
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                Object current = Array.get(array, i);
                if (element.equals(current)) {
                    return i;
                }
            }
        } catch (Exception ignored) {
            // Silent fail
        }

        return -1;
    }

    /**
     * Finds the index of an element in a typed array.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the array
     * @param array The array to search in
     * @param element The element to search for
     * @return The index of the first occurrence of the element, or -1 if not found
     */
    public static <T> int getIndexOfElement(@Nullable T[] array, @Nullable T element) {
        if (!isValid(array) || element == null) {
            return -1;
        }

        for (int i = 0; i < array.length; i++) {
            if (element.equals(array[i])) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds the index of an int value in an int array.
     * Specialized version for primitive int arrays.
     *
     * @param array The array to search in
     * @param value The value to search for
     * @return The index of the first occurrence of the value, or -1 if not found
     */
    public static int getIndexOfElement(@Nullable int[] array, int value) {
        if (!isValid(array)) {
            return -1;
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds the index of a long value in a long array.
     * Specialized version for primitive long arrays.
     *
     * @param array The array to search in
     * @param value The value to search for
     * @return The index of the first occurrence of the value, or -1 if not found
     */
    public static int getIndexOfElement(@Nullable long[] array, long value) {
        if (!isValid(array)) {
            return -1;
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds the index of a double value in a double array.
     * Specialized version for primitive double arrays.
     *
     * @param array The array to search in
     * @param value The value to search for
     * @return The index of the first occurrence of the value, or -1 if not found
     */
    public static int getIndexOfElement(@Nullable double[] array, double value) {
        if (!isValid(array)) {
            return -1;
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds the index of a boolean value in a boolean array.
     * Specialized version for primitive boolean arrays.
     *
     * @param array The array to search in
     * @param value The value to search for
     * @return The index of the first occurrence of the value, or -1 if not found
     */
    public static int getIndexOfElement(@Nullable boolean[] array, boolean value) {
        if (!isValid(array)) {
            return -1;
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds the index of a char value in a char array.
     * Specialized version for primitive char arrays.
     *
     * @param array The array to search in
     * @param value The value to search for
     * @return The index of the first occurrence of the value, or -1 if not found
     */
    public static int getIndexOfElement(@Nullable char[] array, char value) {
        if (!isValid(array)) {
            return -1;
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Checks if an array contains a specific element.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the array
     * @param array The array to search in
     * @param element The element to search for
     * @return True if the element is found in the array, false otherwise
     */
    public static <T> boolean contains(@Nullable T[] array, @Nullable T element) {
        if (!isValid(array) || element == null) {
            return false;
        }

        for (T item : array) {
            if (element.equals(item)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds an element to an array by creating a new array with increased length.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the array
     * @param array The original array
     * @param element The element to add
     * @return A new array containing all original elements plus the new element, or the original array if operation fails
     */
    public static <T> T[] addElement(@Nullable T[] array, @Nullable T element) {
        if (array == null) {
            return array;
        }

        try {
            Class<?> componentType = array.getClass().getComponentType();
            @SuppressWarnings("unchecked")
            T[] newArray = (T[]) Array.newInstance(componentType, array.length + 1);

            // Copy existing elements
            System.arraycopy(array, 0, newArray, 0, array.length);

            // Add new element at the end
            newArray[array.length] = element;

            return newArray;
        } catch (Exception ignored) {
            // Silent fail, return original array
        }

        return array;
    }

    /**
     * Sets an element at a specific index in an array.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the array
     * @param array The array to modify
     * @param element The element to set
     * @param index The index at which to set the element
     * @return True if the operation succeeded, false otherwise
     */
    public static <T> boolean setElementAt(@Nullable T[] array, @Nullable T element, int index) {
        if (array == null || index < 0 || index >= array.length) {
            return false;
        }

        try {
            array[index] = element;
            return true;
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Removes an element at the specified index from an array by creating a new array without that element.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the array
     * @param array The original array
     * @param index The index of the element to remove
     * @return A new array with the element removed, or the original array if operation fails
     */
    public static <T> T[] removeElementAt(@Nullable T[] array, int index) {
        if (array == null || index < 0 || index >= array.length) {
            return array;
        }

        try {
            @SuppressWarnings("unchecked")
            T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);

            // Copy elements before the index
            if (index > 0) {
                System.arraycopy(array, 0, newArray, 0, index);
            }

            // Copy elements after the index
            if (index < array.length - 1) {
                System.arraycopy(array, index + 1, newArray, index, array.length - index - 1);
            }

            return newArray;
        } catch (Exception ignored) {
            // Silent fail, return original array
        }

        return array;
    }

// === Primitive array specializations for common types ===

    /**
     * Checks if an int array contains a specific value.
     *
     * @param array The array to search in
     * @param value The value to search for
     * @return True if the value is found in the array, false otherwise
     */
    public static boolean contains(@Nullable int[] array, int value) {
        if (!isValid(array)) {
            return false;
        }

        for (int item : array) {
            if (item == value) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a value to an int array by creating a new array with increased length.
     *
     * @param array The original array
     * @param value The value to add
     * @return A new array containing all original values plus the new value, or the original array if operation fails
     */
    public static int[] addElement(@Nullable int[] array, int value) {
        if (array == null) {
            return new int[] { value };
        }

        int[] newArray = new int[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = value;
        return newArray;
    }

    /**
     * Sets a value at a specific index in an int array.
     *
     * @param array The array to modify
     * @param value The value to set
     * @param index The index at which to set the value
     * @return True if the operation succeeded, false otherwise
     */
    public static boolean setElementAt(@Nullable int[] array, int value, int index) {
        if (array == null || index < 0 || index >= array.length) {
            return false;
        }

        try {
            array[index] = value;
            return true;
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Removes a value at the specified index from an int array.
     *
     * @param array The original array
     * @param index The index of the value to remove
     * @return A new array with the value removed, or the original array if operation fails
     */
    public static int[] removeElementAt(@Nullable int[] array, int index) {
        if (array == null || index < 0 || index >= array.length) {
            return array;
        }

        int[] newArray = new int[array.length - 1];

        // Copy elements before the index
        if (index > 0) {
            System.arraycopy(array, 0, newArray, 0, index);
        }

        // Copy elements after the index
        if (index < array.length - 1) {
            System.arraycopy(array, index + 1, newArray, index, array.length - index - 1);
        }

        return newArray;
    }

// Additional primitive specializations can be added for long, boolean, double, etc.
// following the same pattern as the int array specializations above

    /**
     * Safely reverses any generic array without throwing exceptions.
     * Returns the reversed array or the original input if it cannot be reversed.
     *
     * @param array The array to reverse
     * @param <T> The type of elements in the array
     * @return The reversed array, original array, or null depending on input
     */
    public static <T> T[] safeReverseArray(T[] array) {
        // Handle null or empty arrays
        if (array == null || array.length <= 1) {
            return array;
        }

        int left = 0;
        int right = array.length - 1;

        while (left < right) {
            // Swap elements
            T temp = array[left];
            array[left] = array[right];
            array[right] = temp;

            left++;
            right--;
        }

        return array;
    }

    /**
     * Reverses any Object if it's an array of any type.
     * Handles object arrays, primitive arrays, null inputs, etc.
     *
     * @param obj The object to reverse if it's an array
     * @return The reversed array object or the original object if not an array
     */
    public static Object reverseAnyArray(Object obj) {
        // Handle null
        if (obj == null) {
            return null;
        }

        // Check if the object is an array
        if (!obj.getClass().isArray()) {
            return obj; // Not an array, return unchanged
        }

        // Get array length using reflection
        int length = Array.getLength(obj);

        // Handle empty or single-element arrays
        if (length <= 1) {
            return obj;
        }

        // Handle reversing based on array type
        if (obj instanceof Object[]) {
            // Object array
            return safeReverseArray((Object[]) obj);
        } else {
            // Primitive array - use reflection
            int left = 0;
            int right = length - 1;

            while (left < right) {
                // Get values at indices
                Object temp = Array.get(obj, left);

                // Swap using reflection
                Array.set(obj, left, Array.get(obj, right));
                Array.set(obj, right, temp);

                left++;
                right--;
            }

            return obj;
        }
    }

    public static int[] generate(int start, int end) { return generate(start, end, null); }
    public static int[] generate(int start, int end, IOnInt onInt) {
        List<Integer> ints = new ArrayList<>();
        start = Math.max(start, 0);
        end = Math.max(end, 0);
        if(start != end) {
            if(start > end) {
                int o = start;
                start = end;
                end = o;
            }

            for(int i = start; i < end + 1; i++) {
                if(onInt != null) {
                    int resolved = onInt.parseRange(i);
                    if(!ints.contains(resolved))
                        ints.add(resolved);
                } else {
                    if(!ints.contains(i))
                        ints.add(i);
                }
            }
        }

        int[] values = new int[ints.size()];
        for(int i = 0; i < ints.size(); i++) {
            int val = ints.get(i);
            values[i] = ints.get(i);
        }

        return values;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] reverse(@Nullable T[] array) {
        if(array == null || array.length < 2)
            return array;

        Class<?> clazz = DynType.getFirstNonNullElementClass(array);
        if(clazz == null)
            return array;

        T[] reversed =  (T[]) Array.newInstance(clazz, array.length);
        for (int i = 0, j = array.length - 1; i < array.length; i++, j--) {
            reversed[i] = array[j];
        }

        return reversed;
    }


    public static <T> T[] ensureNoDuplicates(@Nullable T[] array, Class<T> clazz) {
        if(isValid(array))
            return array;

        List<T> filtered = new ArrayList<>();
        for(T item : array) {
            if(item != null) {
                if(!filtered.contains(item))
                    filtered.add(item);
            }
        }

        if(filtered.size() == array.length)
            return array;

        return toArray(filtered, clazz);
    }

    /**
     * Creates a copy of the given array
     * @param array The array to copy
     * @return A new array containing all elements from the source array
     */
    @Nullable
    public static <T> T[] copy(@Nullable T[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    public static boolean isValid(JSONArray array) {
        return array != null && array.length() > 0;
    }

    /**
     * Creates a copy of the given array with a new size
     * @param array The array to copy
     * @param newSize The size of the new array
     * @return A new array containing elements from the source array, truncated or padded with nulls as needed
     */
    @Nullable
    public static <T> T[] copy(@Nullable T[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    // Primitive array copies

    @Nullable
    public static boolean[] copy(@Nullable boolean[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static boolean[] copy(@Nullable boolean[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static byte[] copy(@Nullable byte[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static byte[] copy(@Nullable byte[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static char[] copy(@Nullable char[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static char[] copy(@Nullable char[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static short[] copy(@Nullable short[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static short[] copy(@Nullable short[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static int[] copy(@Nullable int[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static int[] copy(@Nullable int[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static long[] copy(@Nullable long[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static long[] copy(@Nullable long[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static float[] copy(@Nullable float[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static float[] copy(@Nullable float[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static double[] copy(@Nullable double[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static double[] copy(@Nullable double[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    /**
     * Converts any array type to an Object array.
     * @param array The source array (can be primitive or object array)
     * @return Object array containing all elements from the source array
     * @throws IllegalArgumentException if parameter is not an array
     */
    public static Object[] toObjectArray(Object array) {
        if (array instanceof Object[]) {
            return (Object[]) array;
        }

        int length = Array.getLength(array);
        Object[] result = new Object[length];
        for (int i = 0; i < length; i++) {
            result[i] = Array.get(array, i);
        }
        return result;
    }

    /**
     * Combines two arrays into a new array. If one array is null, returns the non-null array.
     * If both are null, returns null.
     * @param first First array
     * @param second Second array
     * @return Combined array, or single non-null array, or null if both are null
     */
    @Nullable
    public static <T> T[] combine(@Nullable T[] first, @Nullable T[] second) {
        if (first == null) return second;
        if (second == null) return first;

        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(first.getClass().getComponentType(),
                first.length + second.length);
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines multiple arrays into a new array. Skips null arrays.
     * If all arrays are null, returns null.
     * @param arrays Arrays to combine
     * @return Combined array containing all elements from non-null arrays, or null if all arrays are null
     */
    @Nullable
    @SafeVarargs
    public static <T> T[] combine(@Nullable T[]... arrays) {
        if (arrays == null || arrays.length == 0) return null;

        // Count total length and find first non-null array
        int totalLength = 0;
        Class<?> componentType = null;
        for (T[] array : arrays) {
            if (array != null) {
                totalLength += array.length;
                if (componentType == null) {
                    componentType = array.getClass().getComponentType();
                }
            }
        }

        // If no non-null arrays found
        if (componentType == null) return null;

        // Create result array
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(componentType, totalLength);

        // Copy all non-null arrays
        int destinationPos = 0;
        for (T[] array : arrays) {
            if (array != null) {
                System.arraycopy(array, 0, result, destinationPos, array.length);
                destinationPos += array.length;
            }
        }

        return result;
    }

    // Primitive array combinations

    /**
     * Combines two boolean arrays, handling null inputs
     */
    @Nullable
    public static boolean[] combine(@Nullable boolean[] first, @Nullable boolean[] second) {
        if (first == null) return second;
        if (second == null) return first;

        boolean[] result = new boolean[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines two byte arrays, handling null inputs
     */
    @Nullable
    public static byte[] combine(@Nullable byte[] first, @Nullable byte[] second) {
        if (first == null) return second;
        if (second == null) return first;

        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines two char arrays, handling null inputs
     */
    @Nullable
    public static char[] combine(@Nullable char[] first, @Nullable char[] second) {
        if (first == null) return second;
        if (second == null) return first;

        char[] result = new char[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines two int arrays, handling null inputs
     */
    @Nullable
    public static int[] combine(@Nullable int[] first, @Nullable int[] second) {
        if (first == null) return second;
        if (second == null) return first;

        int[] result = new int[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines two long arrays, handling null inputs
     */
    @Nullable
    public static long[] combine(@Nullable long[] first, @Nullable long[] second) {
        if (first == null) return second;
        if (second == null) return first;

        long[] result = new long[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines two float arrays, handling null inputs
     */
    @Nullable
    public static float[] combine(@Nullable float[] first, @Nullable float[] second) {
        if (first == null) return second;
        if (second == null) return first;

        float[] result = new float[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines two double arrays, handling null inputs
     */
    @Nullable
    public static double[] combine(@Nullable double[] first, @Nullable double[] second) {
        if (first == null) return second;
        if (second == null) return first;

        double[] result = new double[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Checks if an array contains a specific element.
     * Returns false if array is invalid or element is not found.
     *
     * @param array The array to search in (can be any array type)
     * @param element The element to search for
     * @return True if the element is found in the array, false otherwise
     */
    public static boolean contains(@Nullable Object array, @Nullable Object element) {
        if (!isArray(array) || element == null) {
            return false;
        }

        try {
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                Object current = Array.get(array, i);
                if (element.equals(current)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Adds an element to an array by creating a new array with increased length.
     * Returns the original array if it's not a valid array or the element can't be added.
     *
     * @param array The original array
     * @param element The element to add
     * @return A new array containing all original elements plus the new element, or the original array if operation fails
     */
    public static Object addElement(@Nullable Object array, @Nullable Object element) {
        if (!isArray(array)) {
            return array;
        }

        try {
            int length = Array.getLength(array);
            Class<?> componentType = array.getClass().getComponentType();

            // Create new array with increased length
            Object newArray = Array.newInstance(componentType, length + 1);

            // Copy existing elements
            System.arraycopy(array, 0, newArray, 0, length);

            // Add new element at the end
            Array.set(newArray, length, element);

            return newArray;
        } catch (Exception ignored) {
            // Silent fail, return original array
        }

        return array;
    }

    /**
     * Sets an element at a specific index in an array.
     * Safely handles invalid indices and type mismatches.
     *
     * @param array The array to modify
     * @param element The element to set
     * @param index The index at which to set the element
     * @return True if the operation succeeded, false otherwise
     */
    public static boolean setElementAt(@Nullable Object array, @Nullable Object element, int index) {
        if (!isMinimumIndex(array, index)) {
            return false;
        }

        try {
            Array.set(array, index, element);
            return true;
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Removes an element at the specified index from an array by creating a new array without that element.
     * Returns the original array if it's not a valid array or the index is invalid.
     *
     * @param array The original array
     * @param index The index of the element to remove
     * @return A new array with the element removed, or the original array if operation fails
     */
    public static Object removeElementAt(@Nullable Object array, int index) {
        if (!isMinimumIndex(array, index)) {
            return array;
        }

        try {
            int length = Array.getLength(array);
            Class<?> componentType = array.getClass().getComponentType();

            // Create new array with decreased length
            Object newArray = Array.newInstance(componentType, length - 1);

            // Copy elements before the index
            if (index > 0) {
                System.arraycopy(array, 0, newArray, 0, index);
            }

            // Copy elements after the index
            if (index < length - 1) {
                System.arraycopy(array, index + 1, newArray, index, length - index - 1);
            }

            return newArray;
        } catch (Exception ignored) {
            // Silent fail, return original array
        }

        return array;
    }

    @Nullable
    public static <T> T[] toArrayNoDuplicates(@Nullable List<T> list, @Nullable Class<T> clazz) {
        if(list == null || clazz == null)
            return null;
        List<T> copy = new ArrayList<>();
        for(T item : list) {
            if(item != null && !copy.contains(item)) {
                copy.add(item);
            }
        }

        return toArray(list, clazz);
    }

    /**
     * Gets the first element from an array.
     *
     * @param array The array object
     * @return The first element or null if the array is empty/invalid
     */
    public static Object getFirst(@Nullable Object array) {
        if(!isArray(array))
            return null;

        try {
            int length = Array.getLength(array);
            if(length > 0) {
                return Array.get(array, 0);
            }
        } catch(Exception e) {
            // Silently fail
        }

        return null;
    }

    /**
     * Gets the first element from an array with a default value.
     *
     * @param array The array object
     * @param defaultValue Value to return if element can't be retrieved
     * @return The first element or defaultValue if the array is empty/invalid
     */
    public static Object getFirst(@Nullable Object array, Object defaultValue) {
        if(!isArray(array))
            return defaultValue;

        try {
            int length = Array.getLength(array);
            if(length > 0) {
                return Array.get(array, 0);
            }
        } catch(Exception e) {
            // Silently fail
        }

        return defaultValue;
    }

    /**
     * Gets the last element from an array.
     *
     * @param array The array object
     * @return The last element or null if the array is empty/invalid
     */
    public static Object getLast(@Nullable Object array) {
        if(!isArray(array))
            return null;

        try {
            int length = Array.getLength(array);
            if(length > 0) {
                return Array.get(array, length - 1);
            }
        } catch(Exception e) {
            // Silently fail
        }

        return null;
    }

    /**
     * Gets the last element from an array with a default value.
     *
     * @param array The array object
     * @param defaultValue Value to return if element can't be retrieved
     * @return The last element or defaultValue if the array is empty/invalid
     */
    public static Object getLast(@Nullable Object array, Object defaultValue) {
        if(!isArray(array))
            return defaultValue;

        try {
            int length = Array.getLength(array);
            if(length > 0) {
                return Array.get(array, length - 1);
            }
        } catch(Exception e) {
            // Silently fail
        }

        return defaultValue;
    }

    /**
     * Gets the first element from a typed array.
     *
     * @param <T> The type of elements in the array
     * @param array The typed array
     * @return The first element or null if the array is empty/invalid
     */
    public static <T> T getFirst(@Nullable T[] array) {
        if(array == null || array.length == 0)
            return null;

        return array[0];
    }

    /**
     * Gets the first element from a typed array with a default value.
     *
     * @param <T> The type of elements in the array
     * @param array The typed array
     * @param defaultValue Value to return if element can't be retrieved
     * @return The first element or defaultValue if the array is empty/invalid
     */
    public static <T> T getFirst(@Nullable T[] array, T defaultValue) {
        if(array == null || array.length == 0)
            return defaultValue;

        return array[0];
    }

    /**
     * Gets the last element from a typed array.
     *
     * @param <T> The type of elements in the array
     * @param array The typed array
     * @return The last element or null if the array is empty/invalid
     */
    public static <T> T getLast(@Nullable T[] array) {
        if(array == null || array.length == 0)
            return null;

        return array[array.length - 1];
    }

    /**
     * Gets the last element from a typed array with a default value.
     *
     * @param <T> The type of elements in the array
     * @param array The typed array
     * @param defaultValue Value to return if element can't be retrieved
     * @return The last element or defaultValue if the array is empty/invalid
     */
    public static <T> T getLast(@Nullable T[] array, T defaultValue) {
        if(array == null || array.length == 0)
            return defaultValue;

        return array[array.length - 1];
    }

    /**
     * Converts a Collection to an array of the appropriate type.
     * The array type is determined by the first non-null element in the collection.
     *
     * @param list The Collection to convert to an array
     * @return A typed array containing the collection elements, or null if conversion fails
     */
    public static Object toArray(@Nullable Object list) {
        if(!ListUtil.isCollection(list))
            return null;

        Class<?> wantedType = null;
        Collection c = (Collection)list;

        // Find the first non-null element to determine array type
        for(Object o : c) {
            if(o != null) {
                wantedType = o.getClass();
                break;
            }
        }

        // If all elements are null or collection is empty, return Object array
        if(wantedType == null) {
            return c.toArray(new Object[0]);
        }

        try {
            // Create array of the appropriate type with the right size
            Object array = Array.newInstance(wantedType, c.size());

            // Fill the array with collection elements
            int index = 0;
            for(Object element : c) {
                // Skip incompatible types (or leave as null)
                if(element == null || wantedType.isAssignableFrom(element.getClass())) {
                    Array.set(array, index, element);
                }
                index++;
            }

            return array;
        } catch(Exception e) {
            // Fallback to Object array if any issues occur
            return c.toArray();
        }
    }

    /**
     * Converts a Collection to an array of the specified type.
     * If class type is provided, uses that type; otherwise determines type from collection contents.
     *
     * @param list The Collection to convert to an array
     * @param clazz The desired class type of the array (can be null to auto-detect)
     * @return A typed array containing the collection elements, or null if conversion fails
     */
    public static Object toArrayObject(@Nullable Object list, @Nullable Class<?> clazz) {
        if (!ListUtil.isCollection(list))
            return null;

        Collection<?> c = (Collection<?>) list;

        // If collection is empty, return empty array of specified type or Object[]
        if (c.isEmpty()) {
            return clazz != null ? Array.newInstance(clazz, 0) : new Object[0];
        }

        // Determine array component type if not specified
        Class<?> componentType = clazz;
        if (componentType == null) {
            // Find first non-null element to determine type
            for (Object o : c) {
                if (o != null) {
                    componentType = o.getClass();
                    break;
                }
            }

            // If all elements are null, use Object class
            if (componentType == null) {
                componentType = Object.class;
            }
        }

        try {
            // Create array of the appropriate type
            Object array = Array.newInstance(componentType, c.size());

            // Fill the array with collection elements
            int index = 0;
            for (Object element : c) {
                // Skip incompatible types or convert if possible
                if (element == null || componentType.isAssignableFrom(element.getClass())) {
                    Array.set(array, index, element);
                } else {
                    // Attempt conversion for primitive types and their wrappers
                    if (isPrimitiveOrWrapper(componentType) && isPrimitiveOrWrapper(element.getClass())) {
                        try {
                            Object converted = convertPrimitiveType(element, componentType);
                            Array.set(array, index, converted);
                        } catch (Exception e) {
                            // Leave as null if conversion fails
                        }
                    }
                }
                index++;
            }

            return array;
        } catch (Exception e) {
            // Fallback to Object array if any issues occur with the specified type
            return c.toArray();
        }
    }

    /**
     * Checks if a class is a primitive type or its wrapper.
     */
    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Boolean.class ||
                clazz == Byte.class ||
                clazz == Character.class ||
                clazz == Short.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Float.class ||
                clazz == Double.class;
    }


    public static <T> T[] toArray(@Nullable Object list, @Nullable Class<T> clazz) {
        if(clazz == null || !ListUtil.isCollection(list))
            return null;

        Collection c = (Collection)list;
        final T[] array = createArray(clazz, c.size());
        if(c.isEmpty()) return array;
        TryRun.silent(() -> {
            int ix = 0;
            for(Object o : c) {
                array[ix] = (T)o;
                ix++;
            }
        });

        return array;
    }

    @Nullable
    public static <T> T[] toArrayNullIfEmpty(@Nullable Collection<T> list, @Nullable Class<T> clazz) { return toArray(list, clazz, true, true, true); }

    @Nullable
    public static <T> T[] toArray(@Nullable Collection<T> list, @Nullable Class<T> clazz) { return toArray(list, clazz, true, true, false); }
    @Nullable
    public static <T> T[] toArray(@Nullable Collection<T> list, @Nullable Class<T> clazz, boolean keepDuplicates) { return toArray(list, clazz, keepDuplicates, true, false); }
    @Nullable
    public static <T> T[] toArray(@Nullable Collection<T> list, @Nullable Class<T> clazz, boolean keepDuplicates, boolean removeNull) { return toArray(list, clazz, keepDuplicates, removeNull, false); }

    public static <T> T[] toArray(@Nullable Collection<T> list, @Nullable Class<T> clazz, boolean keepDuplicates, boolean removeNull, boolean returnNullIfEmptyOrNullList) {
        if(clazz == null || list == null || (returnNullIfEmptyOrNullList && list.isEmpty()))
            return null;

        if(!removeNull && keepDuplicates) {
            T[] array = createArray(clazz, list.size());
            return list.toArray(array);
        }

        List<T> newList = new ArrayList<>();
        for(T item : list) {
            if(removeNull && item == null)
                continue;

            if(!keepDuplicates && newList.contains(item))
                continue;

            newList.add(item);
        }

        T[] array = createArray(clazz, newList.size());
        return list.toArray(array);
    }

    public static <T> T[] createArray(Class<T> clazz, int size) {
        if(size < 0) size = 0;
        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(clazz, size);
        return array;
    }

    /**
     * Converts a List of Integer to a primitive int array
     * @param list The list to convert
     * @return A primitive int array containing all elements from the list
     * @throws NullPointerException if list is null
     */
    @NonNull
    public static int[] toIntArray(@NonNull List<Integer> list) {
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i) != null ? list.get(i) : 0;
        }
        return result;
    }

    /**
     * Converts a List of Long to a primitive long array
     * @param list The list to convert
     * @return A primitive long array containing all elements from the list
     * @throws NullPointerException if list is null
     */
    @NonNull
    public static long[] toLongArray(@NonNull List<Long> list) {
        long[] result = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i) != null ? list.get(i) : 0L;
        }
        return result;
    }

    /**
     * Converts a List of Double to a primitive double array
     * @param list The list to convert
     * @return A primitive double array containing all elements from the list
     * @throws NullPointerException if list is null
     */
    @NonNull
    public static double[] toDoubleArray(@NonNull List<Double> list) {
        double[] result = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i) != null ? list.get(i) : 0.0;
        }
        return result;
    }

    /**
     * Converts a List of Float to a primitive float array
     * @param list The list to convert
     * @return A primitive float array containing all elements from the list
     * @throws NullPointerException if list is null
     */
    @NonNull
    public static float[] toFloatArray(@NonNull List<Float> list) {
        float[] result = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i) != null ? list.get(i) : 0.0f;
        }
        return result;
    }

    /**
     * Converts a List of Boolean to a primitive boolean array
     * @param list The list to convert
     * @return A primitive boolean array containing all elements from the list
     * @throws NullPointerException if list is null
     */
    @NonNull
    public static boolean[] toBooleanArray(@NonNull List<Boolean> list) {
        boolean[] result = new boolean[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = Boolean.TRUE.equals(list.get(i));
        }
        return result;
    }

    public static <T> boolean isValid(LongSparseArray<T> sArray) {
        return sArray != null && !sArray.isEmpty();
    }

    /**
     * Checks if an array is valid (not null and has elements)
     * @param array Array to check
     * @return true if array is not null and has at least one element
     */
    public static <T> boolean isValid(@Nullable T[] array) {
        return array != null && array.length > 0;
    }

    /**
     * Checks if an array is valid (not null and has elements)
     * @param array Array to check
     * @param minSize Minimum Size the Array can be
     * @return true if array is not null and has at least one element
     */
    public static <T> boolean isValid(@Nullable T[] array, int minSize) {
        return array != null && array.length >= minSize;
    }


    public static int getFromStringArray(String[] array, int index, int defaultValue) {
        String x = getElementAtIndexOrDefault(array, index, null);
        return x == null ? defaultValue : Str.tryParseInt(x, defaultValue);
    }

    public static <T> T getElementAtIndexOrDefault(@Nullable T[] array, int index, T defaultValue) {
        return array == null || array.length <= index ?
                defaultValue : array[index];
    }

    /**
     * Checks if an array is valid (not null and has elements) and all elements are non-null
     * @param array Array to check
     * @return true if array is not null, has at least one element, and all elements are non-null
     */
    public static <T> boolean isValidStrict(@Nullable T[] array) {
        if (!isValid(array)) return false;

        for (T element : array) {
            if (element == null) return false;
        }
        return true;
    }

    /**
     * Checks if a primitive array is valid (not null and has elements)
     */
    public static boolean isValid(@Nullable boolean[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable byte[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable char[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable short[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable int[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable long[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable float[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable double[] array) {
        return array != null && array.length > 0;
    }

    public static int safeLength(JSONArray array) {
        return array == null ? -1 : array.length();
    }

    public static int safeLength(@Nullable Object o) {
        return o != null && o.getClass().isArray() ? Array.getLength(o) : -1;
    }

    /**
     * Gets the size of an array, safely handling null arrays
     * @param array Array to check
     * @return Size of array, or 0 if array is null
     */
    public static <T> int safeLength(@Nullable T[] array) {
        return array != null ? array.length : 0;
    }

    // Safe length checks for primitive arrays
    public static int safeLength(@Nullable boolean[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable byte[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable char[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable short[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable int[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable long[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable float[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable double[] array) {
        return array != null ? array.length : 0;
    }

    /**
     * Attempts to convert between primitive types and their wrappers.
     */
    private static Object convertPrimitiveType(Object value, Class<?> targetType) {
        // String to primitive conversions
        if (value instanceof String) {
            String strValue = (String) value;
            if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(strValue);
            } else if (targetType == byte.class || targetType == Byte.class) {
                return Byte.parseByte(strValue);
            } else if (targetType == char.class || targetType == Character.class && strValue.length() > 0) {
                return strValue.charAt(0);
            } else if (targetType == short.class || targetType == Short.class) {
                return Short.parseShort(strValue);
            } else if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(strValue);
            } else if (targetType == long.class || targetType == Long.class) {
                return Long.parseLong(strValue);
            } else if (targetType == float.class || targetType == Float.class) {
                return Float.parseFloat(strValue);
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(strValue);
            }
        }

        // Number to primitive conversions
        if (value instanceof Number) {
            Number numValue = (Number) value;
            if (targetType == byte.class || targetType == Byte.class) {
                return numValue.byteValue();
            } else if (targetType == short.class || targetType == Short.class) {
                return numValue.shortValue();
            } else if (targetType == int.class || targetType == Integer.class) {
                return numValue.intValue();
            } else if (targetType == long.class || targetType == Long.class) {
                return numValue.longValue();
            } else if (targetType == float.class || targetType == Float.class) {
                return numValue.floatValue();
            } else if (targetType == double.class || targetType == Double.class) {
                return numValue.doubleValue();
            }
        }

        // Fallback - return original if no conversion found
        return value;
    }
}
