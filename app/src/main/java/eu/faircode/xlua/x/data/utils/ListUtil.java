package eu.faircode.xlua.x.data.utils;

import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.core.util.Predicate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import eu.faircode.xlua.x.Str;

@SuppressWarnings("all")
public class ListUtil {




    /**
     * Clears all elements from any Collection.
     * Works with any Collection type.
     *
     * @param collection The collection to clear
     * @return True if the operation succeeded, false otherwise
     */
    public static boolean clear(@Nullable Object collection) {
        if (!isCollection(collection)) {
            return false;
        }

        try {
            ((Collection<?>) collection).clear();
            return true;
        } catch (Exception ignored) {
            // Silent fail, might be unmodifiable collection
        }

        return false;
    }


    /**
     * Removes duplicate elements from a typed Collection.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the collection
     * @param collection The collection to process
     * @return True if duplicates were removed, false otherwise
     */
    public static <T> boolean removeDuplicates(@Nullable Collection<T> collection) {
        if (!isValid(collection)) {
            return false;
        }

        try {
            // Create a set of unique elements (preserves order with LinkedHashSet)
            Set<T> uniqueItems = new LinkedHashSet<>(collection);

            // If sizes are different, we had duplicates
            if (uniqueItems.size() != collection.size()) {
                // Clear the original collection and add unique items
                collection.clear();
                collection.addAll(uniqueItems);
                return true;
            }
        } catch (Exception ignored) {
            // Silent fail, might be unmodifiable collection
        }

        return false; // No duplicates found or operation failed
    }

    /**
     * Removes duplicate elements from a typed List.
     * Specialized version for List type.
     *
     * @param <T> The type of elements in the list
     * @param list The list to process
     * @return True if duplicates were removed, false otherwise
     */
    public static <T> boolean removeDuplicates(@Nullable List<T> list) {
        if (!isValid(list)) {
            return false;
        }

        try {
            // Keep track of elements we've seen
            Set<T> seen = new HashSet<>();
            int originalSize = list.size();

            // Iterate from the end to safely remove items
            for (int i = list.size() - 1; i >= 0; i--) {
                T current = list.get(i);

                // If we've seen this element before, remove it
                if (current != null && !seen.add(current)) {
                    list.remove(i);
                }
            }

            return list.size() < originalSize; // True if we removed any duplicates
        } catch (Exception ignored) {
            // Silent fail, might be unmodifiable list
        }

        return false; // No duplicates found or operation failed
    }

    /**
     * Creates a new collection with duplicates removed from the source collection.
     * Useful when you don't want to modify the original collection.
     *
     * @param <T> The type of elements in the collection
     * @param source The source collection to process
     * @return A new collection without duplicates, or null if operation fails
     */
    public static <T> Collection<T> createWithoutDuplicates(@Nullable Collection<T> source) {
        if (source == null) {
            return null;
        }

        try {
            // LinkedHashSet preserves order and removes duplicates
            return new ArrayList<>(new LinkedHashSet<>(source));
        } catch (Exception ignored) {
            // Silent fail
        }

        return null;
    }

    /**
     * Creates a new list with duplicates removed from the source list.
     * Useful when you don't want to modify the original list.
     *
     * @param <T> The type of elements in the list
     * @param source The source list to process
     * @return A new list without duplicates, or null if operation fails
     */
    public static <T> List<T> createWithoutDuplicates(@Nullable List<T> source) {
        if (source == null) {
            return null;
        }

        try {
            // LinkedHashSet preserves order and removes duplicates
            return new ArrayList<>(new LinkedHashSet<>(source));
        } catch (Exception ignored) {
            // Silent fail
        }

        return null;
    }

    /**
     * Removes null elements from any Collection.
     * Works with any Collection type.
     *
     * @param collection The collection to process as an Object
     * @return True if null elements were removed, false otherwise
     */
    public static boolean removeNulls(@Nullable Object collection) {
        if (!isCollection(collection)) {
            return false;
        }

        try {
            @SuppressWarnings("unchecked")
            Collection<Object> coll = (Collection<Object>) collection;
            int originalSize = coll.size();

            // Use Iterator pattern for compatibility with Java 7 and earlier
            Iterator<Object> iterator = coll.iterator();
            while (iterator.hasNext()) {
                Object item = iterator.next();
                if (item == null) {
                    iterator.remove();
                }
            }

            return coll.size() < originalSize; // True if we removed any nulls
        } catch (Exception ignored) {
            // Silent fail, might be unmodifiable collection
        }

        return false; // No nulls found or operation failed
    }

    /**
     * Creates a new set with duplicates removed from the source collection.
     * Sets automatically handle duplicates, so this effectively just converts to a set.
     *
     * @param <T> The type of elements in the collection
     * @param source The source collection to process
     * @param preserveOrder If true, uses LinkedHashSet to preserve order
     * @return A new set without duplicates, or null if operation fails
     */
    public static <T> Set<T> createSetWithoutDuplicates(@Nullable Collection<T> source, boolean preserveOrder) {
        if (source == null) {
            return null;
        }

        try {
            // Choose set implementation based on whether order should be preserved
            return preserveOrder ? new LinkedHashSet<>(source) : new HashSet<>(source);
        } catch (Exception ignored) {
            // Silent fail
        }

        return null;
    }

    /**
     * Removes null elements from a collection.
     * Compatible with Android before SDK 23 and Java 7.
     *
     * @param <T> The type of elements in the collection
     * @param collection The collection to process
     * @return True if null elements were removed, false otherwise
     */
    public static <T> boolean removeNulls(@Nullable Collection<T> collection) {
        if (!isValid(collection)) {
            return false;
        }

        try {
            int originalSize = collection.size();

            // Use Iterator pattern instead of removeIf (Java 8)
            Iterator<T> iterator = collection.iterator();
            while (iterator.hasNext()) {
                T item = iterator.next();
                if (item == null) {
                    iterator.remove();
                }
            }

            return collection.size() < originalSize; // True if we removed any nulls
        } catch (Exception ignored) {
            // Silent fail, might be unmodifiable collection
        }

        return false; // No nulls found or operation failed
    }

    /**
     * Removes duplicate elements from any Collection.
     * Works with any Collection type and compatible with pre-Java 8.
     *
     * @param collection The collection to process
     * @return True if duplicates were removed, false otherwise
     */
    public static boolean removeDuplicates(@Nullable Object collection) {
        if (!isCollection(collection)) {
            return false;
        }

        try {
            @SuppressWarnings("unchecked")
            Collection<Object> coll = (Collection<Object>) collection;
            if (coll.isEmpty()) {
                return false; // No duplicates in empty collection
            }

            // Create a set of unique elements
            Set<Object> uniqueItems = new LinkedHashSet<>();
            for (Object item : coll) {
                uniqueItems.add(item);
            }

            // If sizes are different, we had duplicates
            if (uniqueItems.size() != coll.size()) {
                // Clear the original collection
                coll.clear();

                // Add unique items one by one instead of using addAll
                for (Object item : uniqueItems) {
                    coll.add(item);
                }

                return true;
            }
        } catch (Exception ignored) {
            // Silent fail, might be unmodifiable collection
        }

        return false; // No duplicates found or operation failed
    }
    /**
     * Removes both duplicate and null elements from a collection.
     * Compatible with Android before SDK 23 and Java 7.
     *
     * @param <T> The type of elements in the collection
     * @param collection The collection to process
     * @return True if duplicates or nulls were removed, false otherwise
     */
    public static <T> boolean removeDuplicatesAndNulls(@Nullable Collection<T> collection) {
        if (!isValid(collection)) {
            return false;
        }

        try {
            // First remove nulls using iterator pattern
            int originalSize = collection.size();
            Iterator<T> iterator = collection.iterator();
            while (iterator.hasNext()) {
                T item = iterator.next();
                if (item == null) {
                    iterator.remove();
                }
            }

            // Then remove duplicates
            Set<T> uniqueItems = new LinkedHashSet<>(collection);
            if (uniqueItems.size() != collection.size()) {
                collection.clear();

                // Add items individually to ensure type compatibility
                for (T item : uniqueItems) {
                    collection.add(item);
                }
            }

            return collection.size() < originalSize; // True if we removed anything
        } catch (Exception ignored) {
            // Silent fail, might be unmodifiable collection
        }

        return false; // Nothing removed or operation failed
    }

    /**
     * Creates a new collection with both duplicates and nulls removed.
     * Useful when you don't want to modify the original collection.
     *
     * @param <T> The type of elements in the collection
     * @param source The source collection to process
     * @return A new collection without duplicates or nulls, or null if operation fails
     */
    public static <T> Collection<T> createWithoutDuplicatesAndNulls(@Nullable Collection<T> source) {
        if (source == null) {
            return null;
        }

        try {
            // Filter nulls and collect unique items
            Set<T> uniqueNonNullItems = new LinkedHashSet<>();
            for (T item : source) {
                if (item != null) {
                    uniqueNonNullItems.add(item);
                }
            }

            return new ArrayList<>(uniqueNonNullItems);
        } catch (Exception ignored) {
            // Silent fail
        }

        return null;
    }

    /**
     * Checks if a list contains a specific element.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the collection
     * @param collection The collection to search in
     * @param element The element to search for
     * @return True if the element is found in the collection, false otherwise
     */
    public static <T> boolean contains(@Nullable Collection<T> collection, @Nullable T element) {
        if (!isValid(collection) || element == null) {
            return false;
        }

        try {
            return collection.contains(element);
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    public static <T> boolean stringListContains(@Nullable Collection<String> list, @Nullable String element) { return stringListContains(list, element, false, true); }
    public static <T> boolean stringListContains(@Nullable Collection<String> list, @Nullable String element, boolean ignoreCase) { return stringListContains(list, element, ignoreCase); }
    public static <T> boolean stringListContains(@Nullable Collection<String> list, @Nullable String element, boolean ignoreCase, boolean equalsCheckOnItems) {
        if(!isValid(list) || Str.isEmpty(element))
            return false;

        String comp = ignoreCase ? Str.toLowerCase(element) : element;
        for(String s : list) {
            if(Str.isEmpty(s))
               continue;
            String secondComp = ignoreCase ? Str.toLowerCase(s) : s;
            if(equalsCheckOnItems) {
                if(secondComp.equals(comp))
                    return true;
            } else {
                if(secondComp.contains(comp))
                    return true;
            }
        }

        return false;
    }

    /**
     * Specialized version for List type.
     *
     * @param <T> The type of elements in the list
     * @param list The list to search in
     * @param element The element to search for
     * @return True if the element is found in the list, false otherwise
     */
    public static <T> boolean contains(@Nullable List<T> list, @Nullable T element) {
        if (!isValid(list) || element == null)
            return false;

        try {
            return list.contains(element);
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Adds an element to a collection.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the collection
     * @param collection The collection to modify
     * @param element The element to add
     * @return True if the element was added successfully, false otherwise
     */
    public static <T> boolean addElement(@Nullable Collection<T> collection, @Nullable T element) {
        if (collection == null) {
            return false;
        }

        try {
            return collection.add(element);
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Specialized version for List type.
     *
     * @param <T> The type of elements in the list
     * @param list The list to modify
     * @param element The element to add
     * @return True if the element was added successfully, false otherwise
     */
    public static <T> boolean addElement(@Nullable List<T> list, @Nullable T element) {
        if (list == null) {
            return false;
        }

        try {
            return list.add(element);
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Sets an element at a specific index in a list.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the list
     * @param list The list to modify
     * @param element The element to set
     * @param index The index at which to set the element
     * @return True if the operation succeeded, false otherwise
     */
    public static <T> boolean setElementAt(@Nullable List<T> list, @Nullable T element, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return false;
        }

        try {
            list.set(index, element);
            return true;
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Removes an element at the specified index from a list.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the list
     * @param list The list to modify
     * @param index The index of the element to remove
     * @return True if the operation succeeded, false otherwise
     */
    public static <T> boolean removeElementAt(@Nullable List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return false;
        }

        try {
            list.remove(index);
            return true;
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Removes an element at the specified index from a collection by finding the element
     * at that position and removing it.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the collection
     * @param collection The collection to modify
     * @param index The index of the element to remove
     * @return True if the operation succeeded, false otherwise
     */
    public static <T> boolean removeElementAt(@Nullable Collection<T> collection, int index) {
        if (collection == null || index < 0 || collection.size() <= index) {
            return false;
        }

        try {
            if (collection instanceof List) {
                ((List<T>) collection).remove(index);
                return true;
            } else {
                // For non-List collections, find the element at that index and remove it
                int currentIndex = 0;
                T elementToRemove = null;

                for (T element : collection) {
                    if (currentIndex == index) {
                        elementToRemove = element;
                        break;
                    }
                    currentIndex++;
                }

                if (elementToRemove != null) {
                    return collection.remove(elementToRemove);
                }
            }
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Safely adds an element to a list if the element is not already present.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the list
     * @param list The list to modify
     * @param element The element to add
     * @return True if the element was added, false if it already exists or operation failed
     */
    public static <T> boolean addElementIfNotExists(@Nullable List<T> list, @Nullable T element) {
        if (list == null || element == null) {
            return false;
        }

        try {
            if (!list.contains(element)) {
                return list.add(element);
            }
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Safely adds an element to a collection if the element is not already present.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the collection
     * @param collection The collection to modify
     * @param element The element to add
     * @return True if the element was added, false if it already exists or operation failed
     */
    public static <T> boolean addElementIfNotExists(@Nullable Collection<T> collection, @Nullable T element) {
        if (collection == null || element == null) {
            return false;
        }

        try {
            if (!collection.contains(element)) {
                return collection.add(element);
            }
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Finds the index of an element in any Collection.
     * Returns -1 if the element is not found or if the collection is invalid.
     *
     * @param collection The collection to search in
     * @param element The element to search for
     * @return The index of the first occurrence of the element, or -1 if not found
     */
    public static int getIndexOfElement(@Nullable Object collection, @Nullable Object element) {
        if (!isCollection(collection) || element == null) {
            return -1;
        }

        try {
            Collection<?> c = (Collection<?>) collection;

            // Fast path for Lists
            if (collection instanceof List) {
                return ((List<?>) collection).indexOf(element);
            }

            // For other collection types, iterate and find the element
            int index = 0;
            for (Object item : c) {
                if (element.equals(item)) {
                    return index;
                }
                index++;
            }
        } catch (Exception ignored) {
            // Silent fail
        }

        return -1;
    }

    /**
     * Finds the index of an element in a Collection with generics.
     * Provides better type safety.
     *
     * @param <T> The type of elements in the collection
     * @param collection The collection to search in
     * @param element The element to search for
     * @return The index of the first occurrence of the element, or -1 if not found
     */
    public static <T> int getIndexOfElement(@Nullable Collection<T> collection, @Nullable T element) {
        if (!isValid(collection) || element == null) {
            return -1;
        }

        try {
            // Fast path for Lists
            if (collection instanceof List) {
                return ((List<T>) collection).indexOf(element);
            }

            // For other collection types, iterate and find the element
            int index = 0;
            for (T item : collection) {
                if (element.equals(item)) {
                    return index;
                }
                index++;
            }
        } catch (Exception ignored) {
            // Silent fail
        }

        return -1;
    }

    /**
     * Finds the index of an element in a List with generics.
     * Specialized version for List type.
     *
     * @param <T> The type of elements in the list
     * @param list The list to search in
     * @param element The element to search for
     * @return The index of the first occurrence of the element, or -1 if not found
     */
    public static <T> int getIndexOfElement(@Nullable List<T> list, @Nullable T element) {
        if (!isValid(list) || element == null) {
            return -1;
        }

        try {
            return list.indexOf(element);
        } catch (Exception ignored) {
            // Silent fail
        }

        return -1;
    }

    /**
     * Finds the index of an element in a List with generics, using a custom predicate.
     * Useful when you need to find an element based on complex criteria.
     *
     * @param <T> The type of elements in the list
     * @param list The list to search in
     * @param predicate The predicate to match elements
     * @return The index of the first occurrence of the matching element, or -1 if not found
     */
    public static <T> int getIndexOfElement(@Nullable List<T> list, @Nullable Predicate<T> predicate) {
        if (!isValid(list) || predicate == null) {
            return -1;
        }

        try {
            for (int i = 0; i < list.size(); i++) {
                T item = list.get(i);
                if (predicate.test(item)) {
                    return i;
                }
            }
        } catch (Exception ignored) {
            // Silent fail
        }

        return -1;
    }

    /**
     * Finds the index of an element in a Collection with generics, using a custom predicate.
     * Useful when you need to find an element based on complex criteria.
     *
     * @param <T> The type of elements in the collection
     * @param collection The collection to search in
     * @param predicate The predicate to match elements
     * @return The index of the first occurrence of the matching element, or -1 if not found
     */
    public static <T> int getIndexOfElement(@Nullable Collection<T> collection, @Nullable Predicate<T> predicate) {
        if (!isValid(collection) || predicate == null) {
            return -1;
        }

        try {
            int index = 0;
            for (T item : collection) {
                if (predicate.test(item)) {
                    return index;
                }
                index++;
            }
        } catch (Exception ignored) {
            // Silent fail
        }

        return -1;
    }

    /**
     * Finds the last index of an element in a List with generics.
     * Similar to getIndexOfElement but starts searching from the end.
     *
     * @param <T> The type of elements in the list
     * @param list The list to search in
     * @param element The element to search for
     * @return The last index of the element, or -1 if not found
     */
    public static <T> int getLastIndexOfElement(@Nullable List<T> list, @Nullable T element) {
        if (!isValid(list) || element == null) {
            return -1;
        }

        try {
            return list.lastIndexOf(element);
        } catch (Exception ignored) {
            // Silent fail
        }

        return -1;
    }

    /**
     * Finds the last index of an element in a Collection with generics.
     * For non-List collections, this iterates through the entire collection.
     *
     * @param <T> The type of elements in the collection
     * @param collection The collection to search in
     * @param element The element to search for
     * @return The last index of the element, or -1 if not found
     */
    public static <T> int getLastIndexOfElement(@Nullable Collection<T> collection, @Nullable T element) {
        if (!isValid(collection) || element == null) {
            return -1;
        }

        try {
            // Fast path for Lists
            if (collection instanceof List) {
                return ((List<T>) collection).lastIndexOf(element);
            }

            // For other collection types, we need to track the last found index
            int index = 0;
            int lastFoundIndex = -1;

            for (T item : collection) {
                if (element.equals(item)) {
                    lastFoundIndex = index;
                }
                index++;
            }

            return lastFoundIndex;
        } catch (Exception ignored) {
            // Silent fail
        }

        return -1;
    }

    /**
     * Gets an element at the specified index from a list.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the list
     * @param list The list to get from
     * @param index The index of the element to get
     * @return The element at the specified index, or null if not found
     */
    public static <T> T getElementAt(@Nullable List<T> list, int index) {
        return getElementAt(list, index, null);
    }

    /**
     * Gets an element at the specified index from a list with a default value.
     * Provides better type safety with generics.
     *
     * @param <T> The type of elements in the list
     * @param list The list to get from
     * @param index The index of the element to get
     * @param defaultValue The default value to return if element is not found
     * @return The element at the specified index, or defaultValue if not found
     */
    public static <T> T getElementAt(@Nullable List<T> list, int index, @Nullable T defaultValue) {
        if (list == null || index < 0 || index >= list.size()) {
            return defaultValue;
        }

        try {
            T element = list.get(index);
            return element != null ? element : defaultValue;
        } catch (Exception ignored) {
            // Silent fail
        }

        return defaultValue;
    }

    /**
     * Checks if a list contains a specific element.
     * Returns false if list is invalid or element is not found.
     *
     * @param list The list or collection to search in
     * @param element The element to search for
     * @return True if the element is found in the list, false otherwise
     */
    public static boolean contains(@Nullable Object list, @Nullable Object element) {
        if (!isCollection(list) || element == null) {
            return false;
        }

        try {
            Collection<?> collection = (Collection<?>) list;
            return collection.contains(element);
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Adds an element to a list or collection.
     * Safely handles invalid lists and null elements.
     *
     * @param list The list or collection to add to
     * @param element The element to add
     * @return True if the element was added successfully, false otherwise
     */
    public static boolean addElement(@Nullable Object list, @Nullable Object element) {
        if (!isCollection(list)) {
            return false;
        }

        try {
            Collection collection = (Collection) list;
            return collection.add(element);
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Sets an element at a specific index in a list.
     * Safely handles invalid indices and type mismatches.
     *
     * @param list The list to modify
     * @param element The element to set
     * @param index The index at which to set the element
     * @return True if the operation succeeded, false otherwise
     */
    public static boolean setElementAt(@Nullable Object list, @Nullable Object element, int index) {
        if (!isMinimumIndex(list, index)) {
            return false;
        }

        try {
            if (list instanceof List) {
                ((List) list).set(index, element);
                return true;
            }
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Removes an element at the specified index from a list.
     * Safely handles invalid lists and indices.
     *
     * @param list The list to modify
     * @param index The index of the element to remove
     * @return True if the operation succeeded, false otherwise
     */
    public static boolean removeElementAt(@Nullable Object list, int index) {
        if (!isMinimumIndex(list, index)) {
            return false;
        }

        try {
            if (list instanceof List) {
                ((List) list).remove(index);
                return true;
            } else if (list instanceof Collection) {
                // For non-List collections, find the element at that index and remove it
                Collection collection = (Collection) list;
                int currentIndex = 0;
                Object elementToRemove = null;

                for (Object element : collection) {
                    if (currentIndex == index) {
                        elementToRemove = element;
                        break;
                    }
                    currentIndex++;
                }

                if (elementToRemove != null) {
                    return collection.remove(elementToRemove);
                }
            }
        } catch (Exception ignored) {
            // Silent fail
        }

        return false;
    }

    /**
     * Checks if the object is a Collection.
     */
    public static boolean isCollection(Object o) {
        return o instanceof Collection;
    }

    /**
     * Safely gets the size of a Collection.
     * Returns the size or -1 if not a Collection.
     */
    public static int safeLength(Object o) {
        return isCollection(o) ? ((Collection<?>)o).size() : -1;
    }

    /**
     * Generic version that gets the size of a typed Collection.
     *
     * @param <T> The type of elements in the collection
     * @param collection The typed collection
     * @return The size of the collection or -1 if null
     */
    public static <T> int safeLength(Collection<T> collection) {
        return collection != null ? collection.size() : -1;
    }

    /**
     * Checks if the Collection has at least the minimum size.
     */
    public static boolean isMinimumSize(Object o, int minimumSize) {
        return minimumSize >= 0 && isCollection(o) && safeLength(o) >= minimumSize;
    }

    /**
     * Generic version that checks if the typed Collection has at least the minimum size.
     *
     * @param <T> The type of elements in the collection
     * @param collection The typed collection
     * @param minimumSize The minimum size required
     * @return True if collection has at least minimumSize elements
     */
    public static <T> boolean isMinimumSize(Collection<T> collection, int minimumSize) {
        return minimumSize >= 0 && collection != null && collection.size() >= minimumSize;
    }

    /**
     * Checks if the Collection has a valid index at the specified position.
     */
    public static boolean isMinimumIndex(Object o, int minimumIndex) {
        return minimumIndex >= 0 && isCollection(o) && safeLength(o) > minimumIndex;
    }

    /**
     * Generic version that checks if the typed Collection has a valid index.
     *
     * @param <T> The type of elements in the collection
     * @param collection The typed collection
     * @param minimumIndex The index to check
     * @return True if the index is valid for the collection
     */
    public static <T> boolean isMinimumIndex(Collection<T> collection, int minimumIndex) {
        return minimumIndex >= 0 && collection != null && collection.size() > minimumIndex;
    }

    /**
     * Safely gets an element at the specified index from any Collection with type casting.
     * Uses null as default value if element can't be retrieved.
     *
     * @param <T> The type to cast the result to
     * @param o The collection object
     * @param index The index to retrieve
     * @return The element cast to type T, or null if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T getElementAtSafe(Object o, int index) {
        return getElementAtSafe(o, index, null);
    }

    /**
     * Safely gets an element at the specified index from any Collection with type casting.
     *
     * @param <T> The type to cast the result to
     * @param o The collection object
     * @param index The index to retrieve
     * @param defaultValue Value to return if element can't be retrieved
     * @return The element cast to type T, or defaultValue if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T getElementAtSafe(Object o, int index, T defaultValue) {
        if (!isMinimumIndex(o, index)) return defaultValue;

        try {
            if (o instanceof List) {
                // Fast path for Lists that support random access
                Object result = ((List<?>)o).get(index);
                return result != null ? (T)result : defaultValue;
            } else if (isCollection(o)) {
                // Slower path for other Collections that don't support direct indexing
                Collection<?> c = (Collection<?>) o;
                int ix = 0;
                for (Object val : c) {
                    if (ix == index) return val != null ? (T)val : defaultValue;
                    ix++;
                }
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Generic version that safely gets an element from a typed Collection.
     * Uses null as default value if element can't be retrieved.
     *
     * @param <T> The type of elements in the collection
     * @param collection The typed collection
     * @param index The index to retrieve
     * @return The element, or null if not found
     */
    public static <T> T getElementAtSafe(Collection<T> collection, int index) {
        return getElementAtSafe(collection, index, null);
    }

    /**
     * Generic version that safely gets an element from a typed Collection.
     *
     * @param <T> The type of elements in the collection
     * @param collection The typed collection
     * @param index The index to retrieve
     * @param defaultValue Value to return if element can't be retrieved
     * @return The element, or defaultValue if not found
     */
    public static <T> T getElementAtSafe(Collection<T> collection, int index, T defaultValue) {
        if (collection == null || index < 0 || index >= collection.size()) {
            return defaultValue;
        }

        try {
            if (collection instanceof List) {
                // Fast path for Lists
                return ((List<T>)collection).get(index);
            } else {
                // Iterate for non-List collections
                int ix = 0;
                for (T val : collection) {
                    if (ix == index) return val;
                    ix++;
                }
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Generic version specifically for List type.
     * Uses null as default value if element can't be retrieved.
     *
     * @param <T> The type of elements in the list
     * @param list The typed list
     * @param index The index to retrieve
     * @return The element, or null if not found
     */
    public static <T> T getElementAtSafe(List<T> list, int index) {
        return getElementAtSafe(list, index, null);
    }

    /**
     * Generic version specifically for List type.
     *
     * @param <T> The type of elements in the list
     * @param list The typed list
     * @param index The index to retrieve
     * @param defaultValue Value to return if element can't be retrieved
     * @return The element, or defaultValue if not found
     */
    public static <T> T getElementAtSafe(List<T> list, int index, T defaultValue) {
        if (list == null || index < 0 || index >= list.size()) {
            return defaultValue;
        }

        try {
            return list.get(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Generic method to safely set an element in a List.
     *
     * @param <T> The type of elements in the list
     * @param list The list to modify
     * @param index The index to set
     * @param value The value to set
     * @return True if the operation succeeded
     */
    public static <T> boolean setElementAtSafe(List<T> list, int index, T value) {
        if (list == null || index < 0 || index >= list.size()) {
            return false;
        }

        try {
            list.set(index, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generic method to reverse any List.
     *
     * @param <T> The type of elements in the list
     * @param list The list to reverse
     * @return The reversed list (same instance)
     */
    public static <T> List<T> reverseList(List<T> list) {
        if (list == null || list.size() <= 1) {
            return list;
        }

        try {
            Collections.reverse(list);
            return list;
        } catch (Exception e) {
            return list;
        }
    }

    public static List<?> arrayToList(Object o) {
        if(!ArrayUtils.isArray(o))
            return new ArrayList<>();

        List lst = new ArrayList<>();
        try {
            for(int i = 0; i < ArrayUtils.safeLength(o); i++) {
                Object val = Array.get(o, i);
                if(val == null) continue;
                lst.add(val);
            }
        }catch (Exception ignored) { }
        return lst;
    }

    public static <T> List<T> arrayToList(T[] array) {
        List<T> items = new ArrayList<>();
        if(ArrayUtils.isValid(array)) {
            for(T item : array) {
                if(item != null && !items.contains(item))
                    items.add(item);
            }
        }

        return items;
    }


    //ToDO: move these into a linq class
    public interface IIndexable<T> {
        boolean isItem(T item, T itemToFind);
    }

    public interface IMapItem<TItem, TKey, TVal> {
        boolean isValid(TItem item);
        TKey getKey(TItem item);
        TVal getValue(TItem item);
    }

    public interface  IToStringItem<T> {
        String toStringItem(T o);
    }

    public interface IIterateVoid<T> {
        void onItem(T o, int index);
    }

    public interface IIteratePairCondition<TFrom, TTo> {
        boolean isFine(TFrom from);
        TTo get(TFrom from);
    }

    public interface IIterateCondition<T> {
        boolean isFine(T item);
    }

    public interface IIteratePairTo<TFrom, TTo> {
        TTo get(TFrom from);
    }

    public interface IIterateGet<T> {
         T onItem(T o);
    }


    public static <TFrom, TTo> List<TTo> forEachTo(Collection<TFrom> items, IIteratePairTo<TFrom, TTo> onItem) {
        List<TTo> list = new ArrayList<>();
        if(!isValid(items) || onItem == null)
            return list;

        for(TFrom item : items) {
            if(item != null) {
                TTo to = onItem.get(item);
                if(to instanceof String) {
                    if(!Str.isEmpty((String)to) && !list.contains(to))
                        list.add(to);
                } else {
                    if(to != null && !list.contains(to)) {
                        list.add(to);
                    }
                }
            }
        }

        return list;
    }

    public static <T> void filterCondition(List<T> items, IIterateCondition<T> onItem) {
        List<T> list = new ArrayList<>();
        if(!isValid(items) || onItem == null)
            return;

        for(T item : items) {
            if(item != null && onItem.isFine(item)) {
                list.add(item);
            }
        }

        items.clear();
        items.addAll(list);
    }

    public static <T> List<T> forEachCondition(Collection<T> items, IIterateCondition<T> onItem) {
        List<T> list = new ArrayList<>();
        if(!isValid(items) || onItem == null)
            return list;

        for(T item : items) {
            if(item != null && onItem.isFine(item)) {
                list.add(item);
            }
        }

        return list;
    }

    public static <TFrom, TTo> List<TTo> forEachConditionTo(List<TFrom> items, IIteratePairCondition<TFrom, TTo> onItem) {
        List<TTo> list = new ArrayList<>();
        if(!isValid(items) || onItem == null)
            return list;

        for(TFrom item : items) {
            if(item != null && onItem.isFine(item)) {
                TTo to = onItem.get(item);
                if(to != null && !list.contains(to)) {
                    list.add(to);
                }
            }
        }

        return list;
    }

    public static <T> List<T> forEach(List<T> items, IIterateGet<T> event) {
        List<T> list = new ArrayList<>();
        if(items == null || items.isEmpty() || event == null)
            return items;

        for(T item : items) {
            if(item == null)
                continue;

            T ret = event.onItem(item);
            if(!list.contains(ret))
                list.add(ret);
        }

        return list;
    }

    public static <T> void forEachVoid(List<T> items, IIterateVoid<T> event) {
        if(items == null || items.isEmpty() || event == null)
            return;

        int i = 0;
        for(T item : items) {
            event.onItem(item, i);
            i++;
        }
    }

    public static <T> void forEachVoidNonNull(List<T> items, IIterateVoid<T> event) {
        if(items == null || items.isEmpty() || event == null)
            return;

        int i = 0;
        for(T item : items) {
            if(item != null) {
                event.onItem(item, i);
                i++;
            }
        }
    }


    public static void clear(Map<?, ?> map) {
        try {
            if(map != null && !map.isEmpty())
                map.clear();
        }catch (Exception ignored) {
            //Make read only ?
        }
    }

    public static <T> T getLast(List<T> items) { return getLast(items, null); }
    public static <T> T getLast(List<T> items, T defaultValue) {
        if(items == null || items.isEmpty()) return defaultValue;
        return items.get(items.size() - 1);
    }

    public static <T> T getLast(Collection<T> c) { return getLast(c, null); }
    public static <T> T getLast(Collection<T> c, T defaultValue) {
        if(c != null || c.isEmpty()) return defaultValue;
        int last = c.size() -1;
        int ix = 0;
        for(T o : c) {
            if(ix == last)
                return o;
            ix++;
        }

        return defaultValue;
    }

    public static <T> T getFirst(List<T> items) { return getFirst(items, null); }
    public static <T> T getFirst(List<T> items, T defaultValue) {
        if(items == null || items.isEmpty()) return defaultValue;
        return items.get(0);
    }

    public static <T> T getFirst(Collection<T> items) { return getFirst(items, null); }
    public static <T> T getFirst(Collection<T> items, T defaultValue) {
        if(items == null || items.isEmpty()) return defaultValue;
        for(T o : items)
            return o;

        return null;
    }

    public static Object getFirst(Object c) {
        if(!isCollection(c))
            return null;

        Collection col = (Collection) c;
        for(Object o : col)
            return o;

        return null;
    }

    public static Object getLast(Object c) {
        if(!isCollection(c))
            return null;

        Collection col = (Collection) c;
        int last = col.size() -1;
        int ix = 0;
        for(Object o : col) {
            if(ix == last) return o;
            ix++;
        }

        return null;
    }

    public static void clear(Collection<?> collection) {
        try {
            if(collection != null && !collection.isEmpty())
                collection.clear();
        }catch (Exception ignored) {
            //Make read only ?
        }
    }

    public static boolean isListType(Object o) {
        return o != null && (o instanceof Collection || o instanceof List || o instanceof ArrayList);
    }

    public static <T> boolean isValid(LongSparseArray<T> sArray) { return sArray != null && !sArray.isEmpty(); }

    //Not all needed but fuck it I like it xD
    public static boolean isValid(ArrayList<?> collection) { return collection != null && !collection.isEmpty(); }
    public static boolean isValid(ArrayList<?> collection, int minSize) { return collection != null && collection.size() >= minSize; }
    public static boolean isValid(Set<?> collection) { return collection != null && !collection.isEmpty(); }
    public static boolean isValid(Set<?> collection, int minSize) { return collection != null && collection.size() >= minSize; }
    public static boolean isValid(List<?> collection) { return collection != null && !collection.isEmpty(); }
    public static boolean isValid(List<?> collection, int minSize) { return collection != null && collection.size() >= minSize; }
    public static boolean isValid(Collection<?> collection) { return collection != null && !collection.isEmpty(); }
    public static boolean isValid(Collection<?> collection, int minSize) { return collection != null && collection.size() >= minSize; }
    public static boolean isValid(HashSet<?> set) { return set != null && !set.isEmpty(); }
    public static boolean isValid(HashSet<?> set, int minSize) { return set != null && set.size() >= minSize; }
    public static boolean isValid(LinkedHashSet<?> set) { return set != null && !set.isEmpty(); }
    public static boolean isValid(LinkedHashSet<?> set, int minSize) { return set != null && set.size() >= minSize; }
    public static boolean isValid(LinkedList<?> collection) { return collection != null && !collection.isEmpty(); }
    public static boolean isValid(LinkedList<?> collection, int minSize) { return collection != null && collection.size() >= minSize; }
    public static boolean isValid(TreeSet<?> set) { return set != null && !set.isEmpty(); }
    public static boolean isValid(TreeSet<?> set, int minSize) { return set != null && set.size() >= minSize; }


    /**
     * Recursively clears a collection and its nested sub-collections.
     *
     * @param collection the collection to clear
     */
    public static void clearCollection(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return;
        }

        for (Object item : collection) {
            if (item instanceof Collection) {
                clearCollection((Collection<?>) item); // Recursively clear nested collections
            } else if (item instanceof Map) {
                clearMap((Map<?, ?>) item); // Handle maps if needed
            }
        }
        collection.clear(); // Clear the main collection
    }

    /**
     * Clears a map and its nested structures (collections or maps), assuming all values
     * are of the same type as the first value.
     *
     * @param map the map to clear. If null or empty, no action is performed.
     */
    public static void clearMap(Map<?, ?> map) { clearMap(map, false); }

    /**
     * Recursively clears a map and its nested structures (collections or maps).
     *
     * @param map the map to clear. If null or empty, no action is performed.
     * @param wildCardSubEntries if true, processes each value individually to allow for
     *                           mixed types (collections and maps). If false, assumes all
     *                           values are of the same type as the first value, for better
     *                           performance.
     */
    public static void clearMap(Map<?, ?> map, boolean wildCardSubEntries) {
        if (map == null || map.isEmpty()) {
            return;
        }

        Collection<?> vals = map.values();
        if(!wildCardSubEntries) {
            Object firstValue = vals.iterator().next();
            if (firstValue instanceof Collection) {
                for (Object value : vals) {
                    clearCollection((Collection<?>) value);
                }
            } else if (firstValue instanceof Map) {
                for (Object value : vals) {
                    clearMap((Map<?, ?>) value, false);
                }
            }
        } else {
            for (Object value : map.values()) {
                if (value instanceof Collection) {
                    clearCollection((Collection<?>) value);
                } else if (value instanceof Map) {
                    clearMap((Map<?, ?>) value, true); // Already determined to be a map
                }
            }
        }

        map.clear(); // Clear the main map
    }


    public static <TItem, TKey, TVal> Map<TKey, TVal> toMap(Collection<TItem> items, IMapItem<TItem, TKey, TVal> inv) {
        HashMap<TKey, TVal> map = new HashMap<>();
        for(TItem i : items) {
            if(i != null && inv.isValid(i)) {
                map.put(inv.getKey(i), inv.getValue(i));
            }
        }

        return map;
    }

    public static <T> List<T> nonNull(List<T> items) { return items == null ? emptyList() : items; }

    public static <T> void removeAt(List<T> items, int index) {
        if(index > -1 && items != null && items.size() > index) {
            items.remove(index);
        }
    }

    public static List<String> toLowerCase(List<String> items) {
        if(items == null || items.isEmpty())
            return items;

        List<String> new_list = new ArrayList<>(items.size());
        for(String s : items) {
            if(s != null) {
                new_list.add(s.toLowerCase());
            }
        }

        return new_list;
    }

    public static <T> int indexOf(List<T> items, T item) { return indexOf(items, item, null); }
    public static <T> int indexOf(List<T> items, T item, IIndexable<T> comparer) {
        if(items != null && !items.isEmpty() && item != null) {
            int possibleIndex = items.indexOf(item);
            if(possibleIndex > -1)
                return possibleIndex;

            if(comparer != null) {
                for(int i = 0; i < items.size(); i++) {
                    T o = items.get(i);
                    if(comparer.isItem(o, item))
                        return i;
                }
            } else {
                for(int i = 0; i < items.size(); i++) {
                    T o = items.get(i);
                    if(item.equals(o))
                        return i;
                }
            }
        }

        return -1;
    }

    public static <T> List<String> toStringList(List<T> items) { return toStringList(items, null); }
    public static <T> List<String> toStringList(List<T> items, IToStringItem<T> to) {
        List<String> list = new ArrayList<>();
        if(ListUtil.isValid(items)) {
            for(T item : items) {
                if(to == null)
                    list.add(Str.toStringOrNull(item));
                else
                    list.add(to.toStringItem(item));
            }
        }

        return list;
    }

    public static <T> T getAtIndex(List<T> items, int index) { return getAtIndex(items, index, null); }
    public static <T> T getAtIndex(List<T> items, int index, T defaultValue) { return items != null && items.size() > index ? items.get(index) : defaultValue; }

    public static <T> List<T> emptyList() { return new ArrayList<>(); }

    // List conversions
    public static <T> List<T> copyToList(Collection<T> collection) { return collection != null ? new ArrayList<>(collection) : new ArrayList<T>(); }
    public static <T> List<T> copyToList(Set<T> set) { return set != null ? new ArrayList<>(set) : new ArrayList<T>(); }
    public static <T> List<T> copyToList(ArrayList<T> arrayList) { return arrayList != null ? new ArrayList<>(arrayList) : new ArrayList<T>(); }

    // ArrayList conversions
    public static <T> ArrayList<T> copyToArrayList(Collection<T> collection) { return collection != null && !collection.isEmpty() ? new ArrayList<>(collection) : new ArrayList<T>(); }
    public static <T> ArrayList<T> copyToArrayList(List<T> list) { return list != null ? new ArrayList<>(list) : new ArrayList<T>(); }
    public static <T> ArrayList<T> copyToArrayList(Set<T> set) { return set != null ? new ArrayList<>(set) : new ArrayList<T>(); }

    // Set conversions
    public static <T> Set<T> copyToSet(Collection<T> collection) { return collection != null ? new HashSet<>(collection) : new HashSet<T>(); }
    public static <T> Set<T> copyToSet(List<T> list) { return list != null ? new HashSet<>(list) : new HashSet<T>(); }
    public static <T> Set<T> copyToSet(ArrayList<T> arrayList) { return arrayList != null ? new HashSet<>(arrayList) : new HashSet<T>(); }

    // HashSet conversions
    public static <T> HashSet<T> copyToHashSet(Collection<T> collection) { return collection != null ? new HashSet<>(collection) : new HashSet<T>(); }
    public static <T> HashSet<T> copyToHashSet(List<T> list) { return list != null ? new HashSet<>(list) : new HashSet<T>(); }
    public static <T> HashSet<T> copyToHashSet(ArrayList<T> arrayList) { return arrayList != null ? new HashSet<>(arrayList) : new HashSet<T>(); }

    // LinkedHashSet conversions
    public static <T> LinkedHashSet<T> copyToLinkedHashSet(Collection<T> collection) { return collection != null ? new LinkedHashSet<>(collection) : new LinkedHashSet<T>(); }
    public static <T> LinkedHashSet<T> copyToLinkedHashSet(List<T> list) { return list != null ? new LinkedHashSet<>(list) : new LinkedHashSet<T>(); }
    public static <T> LinkedHashSet<T> copyToLinkedHashSet(ArrayList<T> arrayList) { return arrayList != null ? new LinkedHashSet<>(arrayList) : new LinkedHashSet<T>(); }

    // LinkedList conversions
    public static <T> LinkedList<T> copyToLinkedList(Collection<T> collection) { return collection != null ? new LinkedList<>(collection) : new LinkedList<T>(); }
    public static <T> LinkedList<T> copyToLinkedList(List<T> list) { return list != null ? new LinkedList<>(list) : new LinkedList<T>(); }
    public static <T> LinkedList<T> copyToLinkedList(Set<T> set) { return set != null ? new LinkedList<>(set) : new LinkedList<T>(); }

    // TreeSet conversions
    public static <T extends Comparable<? super T>> TreeSet<T> copyToTreeSet(Collection<T> collection) { return collection != null ? new TreeSet<>(collection) : new TreeSet<T>(); }
    public static <T extends Comparable<? super T>> TreeSet<T> copyToTreeSet(List<T> list) { return list != null ? new TreeSet<>(list) : new TreeSet<T>(); }
    public static <T extends Comparable<? super T>> TreeSet<T> copyToTreeSet(Set<T> set) { return set != null ? new TreeSet<>(set) : new TreeSet<T>(); }

    // Collection conversions
    public static <T> Collection<T> copyToCollection(List<T> list) { return list != null ? new ArrayList<>(list) : new ArrayList<T>(); }
    public static <T> Collection<T> copyToCollection(Set<T> set) { return set != null ? new ArrayList<>(set) : new ArrayList<T>(); }
    public static <T> Collection<T> copyToCollection(ArrayList<T> arrayList) { return arrayList != null ? new ArrayList<>(arrayList) : new ArrayList<T>(); }

    // Immutable conversions
    public static <T> List<T> copyToImmutableList(Collection<T> collection) { return collection != null ? Collections.unmodifiableList(new ArrayList<>(collection)) : Collections.<T>emptyList(); }
    public static <T> Set<T> copyToImmutableSet(Collection<T> collection) { return collection != null ? Collections.unmodifiableSet(new HashSet<>(collection)) : Collections.<T>emptySet(); }

    public static boolean isValid(Hashtable<?, ?> table) { return table != null && !table.isEmpty(); }
    public static boolean isValid(Hashtable<?, ?> table, int minSize) { return table != null && table.size() >= minSize; }
    public static boolean isValid(HashMap<?, ?> map) { return map != null && !map.isEmpty(); }
    public static boolean isValid(HashMap<?, ?> map, int minSize) { return map != null && map.size() >= minSize; }
    public static boolean isValid(LinkedHashMap<?, ?> map) { return map != null && !map.isEmpty(); }
    public static boolean isValid(LinkedHashMap<?, ?> map, int minSize) { return map != null && map.size() >= minSize; }
    public static boolean isValid(Map<?, ?> map) { return map != null && !map.isEmpty(); }
    public static boolean isValid(Map<?, ?> map, int minSize) { return map != null && map.size() >= minSize; }



    public static int size(Map<?, ?> map) { return map != null ? map.size() : 0; }
    public static int size(Collection<?> collection) { return collection != null ? collection.size() : 0; }

    public static <T> List<T> ensureIsValidOrEmptyList(List<T> list) {
        return list == null ? emptyList() : list;
    }

    public static <T> List<T> toSingleList(T item) {
        List<T> list = new ArrayList<>();
        if(item != null)
            list.add(item);

        return list;
    }

    public static <K, V> boolean addAll(Map<K, V> baseMap, Map<? extends K, ? extends V> newElements)
    {
        return addAll(baseMap, newElements, false);
    }

    /**
     * Adds all elements from the newElements map to the baseMap if the newElements map is valid.
     * Optionally clears the original map before adding.
     *
     * @param baseMap       The map to which elements will be added.
     * @param newElements   The map containing elements to add.
     * @param clearOriginal If true, clears the baseMap before adding.
     * @param <K>           The type of keys in the map.
     * @param <V>           The type of values in the map.
     * @return True if elements were added successfully, false otherwise.
     */
    public static <K, V> boolean addAll(Map<K, V> baseMap, Map<? extends K, ? extends V> newElements, boolean clearOriginal) {
        if (baseMap == null) {
            return false; // Base map is null
        }

        if (clearOriginal) {
            baseMap.clear(); // Clear the base map if specified
        }

        if (!isValid(newElements)) {
            return false; // newElements is not valid
        }

        baseMap.putAll(newElements); // Add all elements from newElements to baseMap
        return !newElements.isEmpty(); // Return true if newElements had at least one entry
    }

    public static boolean addAllStrings(Collection<String> baseList, Collection<String> newElements) { return addAllStrings(baseList, newElements, false, true, true); }
    public static boolean addAllStrings(Collection<String> baseList, Collection<String> newElements, boolean clearOriginal) { return addAllStrings(baseList, newElements, clearOriginal, false, true); }
    public static boolean addAllStrings(Collection<String> baseList, Collection<String> newElements, boolean clearOriginal, boolean ensureNoCopies) { return addAllStrings(baseList, newElements, clearOriginal, ensureNoCopies, true); }

    public static boolean addAllStringsNoCopies(Collection<String> baseList, Collection<String> newElements) { return addAllStrings(baseList, newElements, false, true, true); }
    public static boolean addAllStringsNoCopies(Collection<String> baseList, Collection<String> newElements, boolean clearOriginal) { return addAllStrings(baseList, newElements, clearOriginal, true, true); }

    public static boolean addAllStrings(Collection<String> baseList, Collection<String> newElements, boolean clearOriginal, boolean ensureNoCopies, boolean checkIfValidStrings) {
        if(baseList == null)
            return false;
        //Be careful as this clears original EVEN if the NEW elements is not Valid! So make sure Callers are aware of this Logic
        if(clearOriginal)
            baseList.clear();

        if(!isValid(newElements))
            return false;

        if(!ensureNoCopies && !checkIfValidStrings) {
            return baseList.addAll(newElements);
        } else {
            boolean leastOneAdded = false;
            for(String e : newElements) {
                if(e == null || (checkIfValidStrings && Str.isEmpty(e)))
                    continue;
                if(ensureNoCopies && baseList.contains(e))
                    continue;

                baseList.add(e);
                leastOneAdded = true;
            }

            return leastOneAdded;
        }
    }


    public static <T> boolean addAllNoCopies(Collection<T> baseList, Collection<T> newElements) { return addAll(baseList, newElements, false, true); }
    public static <T> boolean addAllNoCopies(Collection<T> baseList, Collection<T> newElements, boolean clearOriginal) { return addAll(baseList, newElements, clearOriginal, true); }

    public static <T> boolean addAll(Collection<T> baseList, Collection<T> newElements) { return addAll(baseList, newElements, false, true); }
    public static <T> boolean addAll(Collection<T> baseList, Collection<T> newElements, boolean clearOriginal) { return addAll(baseList, newElements, clearOriginal, false); }
    public static <T> boolean addAll(Collection<T> baseList, Collection<T> newElements, boolean clearOriginal, boolean ensureNoCopies) {
        if(baseList == null)
            return false;

        //Be careful as this clears original EVEN if the NEW elements is not Valid! So make sure Callers are aware of this Logic
        if(clearOriginal)
            baseList.clear();

        if(!isValid(newElements))
            return false;

        if(!ensureNoCopies) {
            return baseList.addAll(newElements);
        } else {
            boolean leastOneAdded = false;
            for(T e : newElements ) {
                if(e != null && !baseList.contains(e)) {
                    baseList.add(e);
                    leastOneAdded = true;
                }
            }

            return leastOneAdded;
        }
    }

    public static <T> boolean addAll(Collection<T> baseList, T[] newElements)  { return addAll(baseList, newElements, false, false, false); }
    public static <T> boolean addAll(Collection<T> baseList, T[] newElements, boolean ensureNoCopies) { return addAll(baseList, newElements, ensureNoCopies, false, false); }
    public static <T> boolean addAll(Collection<T> baseList, T[] newElements, boolean ensureNoCopies, boolean clearOriginal) { return  addAll(baseList, newElements, ensureNoCopies, clearOriginal, false); }
    public static <T> boolean addAll(Collection<T> baseList, T[] newElements, boolean ensureNoCopies, boolean clearOriginal, boolean skipEmpty) {
        if(baseList == null || !ArrayUtils.isValid(newElements))
            return false;

        if(clearOriginal)
            baseList.clear();

        int originalSize = baseList.size();

        for(T item : newElements) {
            if(item != null) {
                if(skipEmpty && item instanceof String) {
                    if(((String) item).isEmpty())
                        continue;
                }

                if(ensureNoCopies && baseList.contains(item))
                    continue;

                baseList.add(item);
            }
        }

        return baseList.size() != originalSize;
    }


    public static <T> boolean addAllIfValidEx(List<T> baseList, List<T> newElements) {
        if(baseList == null || !isValid(newElements))
            return false;

        for(T item : newElements) {
            if(item != null && !baseList.contains(item))
                baseList.add(item);
        }

        //return baseList.addAll(newElements);
        return true;
    }


    public static <T> boolean addAll(List<T> baseList, List<T> newElements) {
        if(baseList == null || !isValid(newElements))
            return false;

        for(T el : newElements) {
            if(el != null && !baseList.contains(el)) {
                baseList.add(el);
            }
        }

        return isValid(baseList);
    }

    public static <T> Collection<T> combine(Collection<T> a, Collection<T> b) { return combine(a, b, true, true); }
    public static <T> Collection<T> combine(Collection<T> a, Collection<T> b, boolean ifTrueCondition) { return combine(a, b, ifTrueCondition, true); }
    public static <T> Collection<T> combine(Collection<T> a, Collection<T> b, boolean ifTrueCondition, boolean useAFirst) {
        Collection<T> items = new ArrayList<>();
        Collection<T> firstHalf = useAFirst ? a : b;
        Collection<T> secondHalf = useAFirst ? b : a;
        if(isValid(firstHalf)) items.addAll(firstHalf);
        if(isValid(secondHalf) && ifTrueCondition) items.addAll(secondHalf);
        return items;
    }

    public static <T> List<T> combine(List<T> a, List<T> b) { return combine(a, b, true, true); }
    public static <T> List<T> combine(List<T> a, List<T> b, boolean ifTrueCondition) { return combine(a, b, ifTrueCondition, true); }
    public static <T> List<T> combine(List<T> a, List<T> b, boolean ifTrueCondition, boolean useAFirst) {
        List<T> items = new ArrayList<>();
        List<T> firstHalf = useAFirst ? a : b;
        List<T> secondHalf = useAFirst ? b : a;
        if(isValid(firstHalf)) items.addAll(firstHalf);
        if(isValid(secondHalf) && ifTrueCondition) items.addAll(secondHalf);
        return items;
    }

    public static <K, V> Map<K, V> filterMap(Map<K, V> map, K badKey, V badValue) { return filterMap(map, badKey, badValue, false, false); }
    public static <K, V, BK> Map<K, V> filterMap(Map<K, V> map, BK badKey) { return filterMap(map, badKey, null, false, true); }
    public static <K, V, BV> Map<K, V> filterMapByValue(Map<K, V> map, BV badValue) { return filterMap(map, null, badValue, true, false); }
    public static <K, V, BK> Map<K, V> filterMap(Map<K, V> map, BK badKey, boolean allowNullKeys) { return filterMap(map, badKey, null, allowNullKeys, true); }
    public static <K, V, BV> Map<K, V> filterMapByValue(Map<K, V> map, BV badValue, boolean allowNullValues) { return filterMap(map, null, badValue, true, allowNullValues); }
    public static <K, V> Map<K, V> filterMap(Map<K, V> map, K badKey, V badValue, boolean allowNull) { return filterMap(map, badKey, badValue, allowNull, allowNull); }
    public static <K, V> Map<K, V> filterMap(Map<K, V> map, Collection<K> badKeys) { return filterMap(map, badKeys, null, false, true); }
    public static <K, V> Map<K, V> filterMapByValue(Map<K, V> map, Collection<V> badValues) { return filterMap(map, null, badValues, true, false); }
    public static <K, V> Map<K, V> filterMap(Map<K, V> map, K[] badKeys) { return filterMap(map, Arrays.asList(badKeys), null, false, true); }
    public static <K, V> Map<K, V> filterMapByValue(Map<K, V> map, V[] badValues) { return filterMap(map, null, Arrays.asList(badValues), true, false); }

    public static <K, V> Map<K, V> filterMapByPredicates(Map<K, V> map, Predicate<K> keyPredicate, Predicate<V> valuePredicate) {
        if (!isValid(map)) return new HashMap<>();
        Map<K, V> filteredMap = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (keyPredicate.test(entry.getKey()) && valuePredicate.test(entry.getValue())) {
                filteredMap.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredMap;
    }

    public static <K, V> Map<K, V> filterMap(Map<K, V> map, final Predicate<K> keyPredicate) {
        return filterMapByPredicates(map, keyPredicate, new Predicate<V>() {
            @Override public boolean test(V v) { return true; }
        });
    }

    public static <K, V> Map<K, V> filterMapByValue(Map<K, V> map, final Predicate<V> valuePredicate) {
        return filterMapByPredicates(map, new Predicate<K>() {
            @Override public boolean test(K k) { return true; }
        }, valuePredicate);
    }

    public static <K, V, BK, BV> Map<K, V> filterMap(Map<K, V> map, BK badKey, BV badValue, boolean allowNullKeys, boolean allowNullValues) {
        Map<K, V> filteredMap = new HashMap<>();
        if(isValid(map)) {
            for(Map.Entry<K, V> item : map.entrySet()) {
                K originalKey = item.getKey();
                V originalValue = item.getValue();
                if(isFine(originalKey, badKey, allowNullKeys) && isFine(originalValue, badValue, allowNullValues)) {
                    filteredMap.put(originalKey, originalValue);
                }
            }
        }

        return filteredMap;
    }

    @SuppressWarnings("unchecked")
    private static <T, BT> boolean isFine(T one, BT two, boolean allowNull) {
        if(one == null || two == null) return one == null && allowNull && two == null;
        // Handle arrays
        if (one.getClass().isArray()) {
            if (two.getClass().isArray()) {
                Object[] oneArr = ArrayUtils.toObjectArray(one);
                Object[] twoArr = ArrayUtils.toObjectArray(two);
                for (Object oneElem : oneArr) {
                    for (Object twoElem : twoArr) {
                        if (Objects.equals(oneElem, twoElem)) return false;
                    }
                }
                return true;
            }


            // Array contains single element
            Object[] arr = ArrayUtils.toObjectArray(one);
            for (Object elem : arr) {
                if (Objects.equals(elem, two)) return false;
            }
            return true;
        }

        // Handle collections
        if (one instanceof Collection) {
            Collection<?> oneCollection = (Collection<?>) one;
            if (two instanceof Collection) {
                Collection<?> twoCollection = (Collection<?>) two;
                for (Object oneElem : oneCollection) {
                    for (Object twoElem : twoCollection) {
                        if (Objects.equals(oneElem, twoElem)) return false;
                    }
                }
                return true;
            }
            // Collection contains single element
            return !oneCollection.contains(two);
        }

        // Handle Maps
        if (one instanceof Map) {
            Map<?, ?> oneMap = (Map<?, ?>) one;
            if (two instanceof Map) {
                Map<?, ?> twoMap = (Map<?, ?>) two;
                for (Map.Entry<?, ?> oneEntry : oneMap.entrySet()) {
                    for (Map.Entry<?, ?> twoEntry : twoMap.entrySet()) {
                        if (Objects.equals(oneEntry.getKey(), twoEntry.getKey()) &&
                                Objects.equals(oneEntry.getValue(), twoEntry.getValue())) return false;
                    }
                }
                return true;
            }
            // Map contains single key or value
            return !oneMap.containsKey(two) && !oneMap.containsValue(two);
        }

        // Handle strings (check if one string contains another)
        if (one instanceof String && two instanceof String) {
            return !((String) one).contains((String) two);
        }

        // Default case: simple equality check
        return !Objects.equals(one, two);
    }
}