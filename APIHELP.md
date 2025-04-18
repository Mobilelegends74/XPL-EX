# XParamExtra API Documentation

This document provides a comprehensive guide to the XParamExtra API, which allows LUA scripters to access various utility functions within their scripts for the XLua framework.

## Table of Contents

- [Logging Functions](#logging-functions)
- [String Manipulation](#string-manipulation)
- [Conversion Functions](#conversion-functions)
- [Array and Collection Operations](#array-and-collection-operations)
- [Random Data Generation](#random-data-generation)
- [Reflection Utilities](#reflection-utilities)
- [System Functions](#system-functions)

## Logging Functions

Functions that control logging behavior and provide debug information.

### Logging Controls

#### `setLoggingFlag(boolean shouldLog)`
Enables or disables logging functionality.
- **Parameters:** `shouldLog` - Whether logging should be enabled
- **Example:**
  ```lua
  param:setLoggingFlag(false) -- Disable logging
  ```

#### `getLoggingFlag()`
Gets the current logging state.
- **Returns:** Boolean indicating if logging is enabled
- **Example:**
  ```lua
  local isLoggingEnabled = param:getLoggingFlag()
  ```

### Settings Cache

#### `setLastSetting(String lastSetting)`
Stores a setting string in the settings cache.
- **Parameters:** `lastSetting` - The setting to store
- **Example:**
  ```lua
  param:setLastSetting("resolution=high")
  ```

#### `setLastSetting(String lastSetting, boolean flag)`
Conditionally stores a setting string in the settings cache.
- **Parameters:**
    - `lastSetting` - The setting to store
    - `flag` - Only store if this is true
- **Example:**
  ```lua
  param:setLastSetting("mode=silent", shouldBeSilent)
  ```

#### `getLastSetting()`
Retrieves the most recent setting from the cache.
- **Returns:** The last stored setting or empty string if none exists
- **Example:**
  ```lua
  local lastSetting = param:getLastSetting()
  ```

### Log Data Management

#### `setLogOld(String oldResult)`
Sets the old result value for logging.
- **Parameters:** `oldResult` - The old value to log
- **Example:**
  ```lua
  param:setLogOld(originalValue)
  ```

#### `getLogOld()`
Gets the stored old result value.
- **Returns:** The stored old result string
- **Example:**
  ```lua
  local oldValue = param:getLogOld()
  ```

#### `setLogNew(String newResult)`
Sets the new result value for logging.
- **Parameters:** `newResult` - The new value to log
- **Example:**
  ```lua
  param:setLogNew(modifiedValue)
  ```

#### `getLogNew()`
Gets the stored new result value.
- **Returns:** The stored new result string
- **Example:**
  ```lua
  local newValue = param:getLogNew()
  ```

#### `setLogExtra(String settingResult)`
Sets additional logging information.
- **Parameters:** `settingResult` - Extra information to log
- **Example:**
  ```lua
  param:setLogExtra("Modified by network filter")
  ```

#### `getLogExtra()`
Gets the stored extra log information.
- **Returns:** The stored extra information prefixed with "Setting:"
- **Example:**
  ```lua
  local extraInfo = param:getLogExtra()
  ```

### Debug Utilities

#### `safe(Object o)`
Safely converts any object to a string representation.
- **Parameters:** `o` - The object to convert
- **Returns:** String representation of the object or "null"
- **Example:**
  ```lua
  local safeString = param:safe(result)
  ```

#### `getStackTrace()`
Gets the current stack trace.
- **Returns:** Array of StackTraceElement objects
- **Example:**
  ```lua
  local stack = param:getStackTrace()
  ```

#### `getStackTrace(Throwable t)`
Gets the stack trace from a given throwable.
- **Parameters:** `t` - The throwable object
- **Returns:** Array of StackTraceElement objects
- **Example:**
  ```lua
  local exception = param:createException()
  local stack = param:getStackTrace(exception)
  ```

#### `getStackTraceString()`
Gets the current stack trace as a formatted string.
- **Returns:** String representation of the stack trace
- **Example:**
  ```lua
  local stackString = param:getStackTraceString()
  ```

#### `getStackTraceString(Throwable t)`
Gets the stack trace from a given throwable as a formatted string.
- **Parameters:** `t` - The throwable object
- **Returns:** String representation of the stack trace
- **Example:**
  ```lua
  local exception = param:createException("Failed to process")
  local stackString = param:getStackTraceString(exception)
  ```

#### `printStack()`
Prints the current stack trace to the log.
- **Example:**
  ```lua
  param:printStack()
  ```

#### `printFileContents(String filePath)`
Prints the contents of a file to the log.
- **Parameters:** `filePath` - Path to the file
- **Example:**
  ```lua
  param:printFileContents("/data/data/com.example.app/files/config.txt")
  ```

### Exception Handling

#### `createException()`
Creates a new Exception object.
- **Returns:** An Exception instance
- **Example:**
  ```lua
  local exception = param:createException()
  ```

#### `createException(String msg)`
Creates a new Exception object with a message.
- **Parameters:** `msg` - The exception message
- **Returns:** An Exception instance with the specified message
- **Example:**
  ```lua
  local exception = param:createException("Invalid operation")
  ```

#### `throwException()`
Throws an Exception.
- **Throws:** A new Exception
- **Example:**
  ```lua
  -- This will halt script execution with an exception
  param:throwException()
  ```

#### `throwException(String msg)`
Throws an Exception with a specified message.
- **Parameters:** `msg` - The exception message
- **Throws:** A new Exception with the specified message
- **Example:**
  ```lua
  -- This will halt script execution with an exception
  param:throwException("Invalid state detected")
  ```

## String Manipulation

Functions for working with strings, including validation, modification, and parsing.

### String Validation

#### `isNumericString(String s)`
Checks if a string contains only numeric characters.
- **Parameters:** `s` - The string to check
- **Returns:** true if the string is numeric
- **Example:**
  ```lua
  local isNumeric = param:isNumericString("12345")
  ```

#### `stringContains(String s, String containing)`
Checks if a string contains another string.
- **Parameters:**
    - `s` - The source string
    - `containing` - The substring to check for
- **Returns:** true if the source string contains the substring
- **Example:**
  ```lua
  local contains = param:stringContains("Hello World", "World")
  ```

#### `stringStartsWith(String s, String startsWith)`
Checks if a string starts with another string.
- **Parameters:**
    - `s` - The source string
    - `startsWith` - The prefix to check for
- **Returns:** true if the source string starts with the prefix
- **Example:**
  ```lua
  local startsWith = param:stringStartsWith("Hello World", "Hello")
  ```

#### `stringEndsWith(String s, String endsWith)`
Checks if a string ends with another string.
- **Parameters:**
    - `s` - The source string
    - `endsWith` - The suffix to check for
- **Returns:** true if the source string ends with the suffix
- **Example:**
  ```lua
  local endsWith = param:stringEndsWith("Hello World", "World")
  ```

#### `stringLength(String s)`
Gets the length of a string.
- **Parameters:** `s` - The string to measure
- **Returns:** The length of the string or -1 if the string is null
- **Example:**
  ```lua
  local length = param:stringLength("Hello")
  ```

#### `stringIsValid(String s)`
Checks if a string is not null and not empty.
- **Parameters:** `s` - The string to check
- **Returns:** true if the string is valid (not null and not empty)
- **Example:**
  ```lua
  local isValid = param:stringIsValid("Hello")
  ```

#### `stringIsNull(String s)`
Checks if a string is null.
- **Parameters:** `s` - The string to check
- **Returns:** true if the string is null
- **Example:**
  ```lua
  local isNull = param:stringIsNull(someString)
  ```

#### `stringIsEmpty(String s)`
Checks if a string is null or empty.
- **Parameters:** `s` - The string to check
- **Returns:** true if the string is null or empty
- **Example:**
  ```lua
  local isEmpty = param:stringIsEmpty(someString)
  ```

### String Modification

#### `stringReplaceAll(String s, String regex, String replaceWith)`
Replaces all occurrences of a pattern in a string.
- **Parameters:**
    - `s` - The source string
    - `regex` - The regular expression pattern to match
    - `replaceWith` - The replacement string
- **Returns:** The string with replacements
- **Example:**
  ```lua
  local replaced = param:stringReplaceAll("Hello World", "o", "0")
  ```

#### `stringTrim(String s)`
Removes leading and trailing whitespace from a string.
- **Parameters:** `s` - The string to trim
- **Returns:** The trimmed string
- **Example:**
  ```lua
  local trimmed = param:stringTrim("  Hello  ")
  ```

#### `stringSubString(String s, int startIndexInclusive, int endIndexExclusive)`
Gets a substring from the specified start index (inclusive) to the end index (exclusive).
- **Parameters:**
    - `s` - The source string
    - `startIndexInclusive` - The start index (inclusive)
    - `endIndexExclusive` - The end index (exclusive)
- **Returns:** The extracted substring
- **Example:**
  ```lua
  local subString = param:stringSubString("Hello World", 0, 5)
  ```

#### `stringSubString(String s, int startIndexInclusive)`
Gets a substring from the specified start index to the end of the string.
- **Parameters:**
    - `s` - The source string
    - `startIndexInclusive` - The start index (inclusive)
- **Returns:** The extracted substring
- **Example:**
  ```lua
  local subString = param:stringSubString("Hello World", 6)
  ```

#### `stringToLowerCase(String s)`
Converts a string to lowercase.
- **Parameters:** `s` - The string to convert
- **Returns:** The lowercase string
- **Example:**
  ```lua
  local lowercase = param:stringToLowerCase("Hello World")
  ```

#### `stringToUpperCase(String s)`
Converts a string to uppercase.
- **Parameters:** `s` - The string to convert
- **Returns:** The uppercase string
- **Example:**
  ```lua
  local uppercase = param:stringToUpperCase("Hello World")
  ```

### String Character Operations

#### `stringToCharArray(String s)`
Converts a string to a character array.
- **Parameters:** `s` - The string to convert
- **Returns:** An array of characters
- **Example:**
  ```lua
  local charArray = param:stringToCharArray("Hello")
  ```

#### `charArrayToString(char[] chars)`
Converts a character array to a string.
- **Parameters:** `chars` - The character array
- **Returns:** The resulting string
- **Example:**
  ```lua
  local str = param:charArrayToString(charArray)
  ```

#### `stringCharAt(String s, int index)`
Gets the character at the specified index.
- **Parameters:**
    - `s` - The source string
    - `index` - The index of the character
- **Returns:** The character at the specified index
- **Example:**
  ```lua
  local char = param:stringCharAt("Hello", 1)
  ```

#### `stringFirstChar(String s)`
Gets the first character of a string.
- **Parameters:** `s` - The source string
- **Returns:** The first character
- **Example:**
  ```lua
  local firstChar = param:stringFirstChar("Hello")
  ```

#### `stringLastChar(String s)`
Gets the last character of a string.
- **Parameters:** `s` - The source string
- **Returns:** The last character
- **Example:**
  ```lua
  local lastChar = param:stringLastChar("Hello")
  ```

#### `charToString(Character c)`
Converts a character to a string.
- **Parameters:** `c` - The character to convert
- **Returns:** The resulting string
- **Example:**
  ```lua
  local str = param:charToString('A')
  ```

#### `stringHasNumbersChars(String s)`
Checks if a string contains numeric characters.
- **Parameters:** `s` - The string to check
- **Returns:** true if the string contains numeric characters
- **Example:**
  ```lua
  local hasNumbers = param:stringHasNumbersChars("abc123")
  ```

#### `stringHasAlphabeticChars(String s)`
Checks if a string contains alphabetic characters.
- **Parameters:** `s` - The string to check
- **Returns:** true if the string contains alphabetic characters
- **Example:**
  ```lua
  local hasLetters = param:stringHasAlphabeticChars("abc123")
  ```

## Conversion Functions

Functions for converting between different data formats.

### String to Bytes Conversion

#### `stringToBytes(String s)`
Converts a string to a byte array using UTF-8 encoding.
- **Parameters:** `s` - The string to convert
- **Returns:** The resulting byte array
- **Example:**
  ```lua
  local bytes = param:stringToBytes("Hello")
  ```

#### `stringToBytesUnicode(String s)`
Converts a string to a byte array using UTF-16 encoding.
- **Parameters:** `s` - The string to convert
- **Returns:** The resulting byte array
- **Example:**
  ```lua
  local bytes = param:stringToBytesUnicode("Hello")
  ```

#### `stringToBytes(String s, String encoding)`
Converts a string to a byte array using the specified encoding.
- **Parameters:**
    - `s` - The string to convert
    - `encoding` - The character encoding to use
- **Returns:** The resulting byte array
- **Example:**
  ```lua
  local bytes = param:stringToBytes("Hello", "ISO-8859-1")
  ```

#### `stringBase64ToBytes(String s)`
Decodes a Base64 string to a byte array.
- **Parameters:** `s` - The Base64 string
- **Returns:** The decoded byte array
- **Example:**
  ```lua
  local bytes = param:stringBase64ToBytes("SGVsbG8=")
  ```

### Bytes to String Conversion

#### `bytesToString(byte[] bytes)`
Converts a byte array to a string using UTF-8 encoding.
- **Parameters:** `bytes` - The byte array
- **Returns:** The resulting string
- **Example:**
  ```lua
  local str = param:bytesToString(bytes)
  ```

#### `bytesToStringUnicode(byte[] bytes)`
Converts a byte array to a string using UTF-16 encoding.
- **Parameters:** `bytes` - The byte array
- **Returns:** The resulting string
- **Example:**
  ```lua
  local str = param:bytesToStringUnicode(bytes)
  ```

#### `bytesToString(byte[] bytes, String encoding)`
Converts a byte array to a string using the specified encoding.
- **Parameters:**
    - `bytes` - The byte array
    - `encoding` - The character encoding to use
- **Returns:** The resulting string
- **Example:**
  ```lua
  local str = param:bytesToString(bytes, "ISO-8859-1")
  ```

#### `bytesToHexString(byte[] bytes)`
Converts a byte array to a hex string representation with spaces.
- **Parameters:** `bytes` - The byte array
- **Returns:** The hex string
- **Example:**
  ```lua
  local hexStr = param:bytesToHexString(bytes)
  ```

#### `bytesToHexString(byte[] bytes, boolean addSpaces)`
Converts a byte array to a hex string representation.
- **Parameters:**
    - `bytes` - The byte array
    - `addSpaces` - Whether to add spaces between hex values
- **Returns:** The hex string
- **Example:**
  ```lua
  local hexStr = param:bytesToHexString(bytes, false)
  ```

#### `bytesToBase64String(byte[] bytes)`
Encodes a byte array to a Base64 string.
- **Parameters:** `bytes` - The byte array
- **Returns:** The Base64 encoded string
- **Example:**
  ```lua
  local base64Str = param:bytesToBase64String(bytes)
  ```

#### `stringToBase64String(String s)`
Encodes a string to Base64 using UTF-8 encoding.
- **Parameters:** `s` - The string to encode
- **Returns:** The Base64 encoded string
- **Example:**
  ```lua
  local base64Str = param:stringToBase64String("Hello")
  ```

#### `stringToBase64String(String s, String encoding)`
Encodes a string to Base64 using the specified encoding.
- **Parameters:**
    - `s` - The string to encode
    - `encoding` - The character encoding to use
- **Returns:** The Base64 encoded string
- **Example:**
  ```lua
  local base64Str = param:stringToBase64String("Hello", "ISO-8859-1")
  ```

### String Array and List Operations

#### `stringSplitToArray(String s)`
Splits a string into an array using comma as the delimiter.
- **Parameters:** `s` - The string to split
- **Returns:** An array of strings
- **Example:**
  ```lua
  local parts = param:stringSplitToArray("a,b,c")
  ```

#### `stringSplitToArray(String s, String delimiter)`
Splits a string into an array using the specified delimiter.
- **Parameters:**
    - `s` - The string to split
    - `delimiter` - The delimiter to use for splitting
- **Returns:** An array of strings
- **Example:**
  ```lua
  local parts = param:stringSplitToArray("a|b|c", "|")
  ```

#### `joinStringArray(String[] array)`
Joins an array of strings using comma as the delimiter.
- **Parameters:** `array` - The array to join
- **Returns:** The joined string
- **Example:**
  ```lua
  local joined = param:joinStringArray({"a", "b", "c"})
  ```

#### `joinStringArray(String[] array, String delimiter)`
Joins an array of strings using the specified delimiter.
- **Parameters:**
    - `array` - The array to join
    - `delimiter` - The delimiter to use for joining
- **Returns:** The joined string
- **Example:**
  ```lua
  local joined = param:joinStringArray({"a", "b", "c"}, "|")
  ```

#### `stringSplitToList(String s)`
Splits a string into a list using comma as the delimiter.
- **Parameters:** `s` - The string to split
- **Returns:** A list of strings
- **Example:**
  ```lua
  local list = param:stringSplitToList("a,b,c")
  ```

#### `stringSplitToList(String s, String delimiter)`
Splits a string into a list using the specified delimiter.
- **Parameters:**
    - `s` - The string to split
    - `delimiter` - The delimiter to use for splitting
- **Returns:** A list of strings
- **Example:**
  ```lua
  local list = param:stringSplitToList("a|b|c", "|")
  ```

#### `joinStringList(Collection<String> list)`
Joins a list of strings using comma as the delimiter.
- **Parameters:** `list` - The list to join
- **Returns:** The joined string
- **Example:**
  ```lua
  local joined = param:joinStringList(list)
  ```

#### `joinStringList(Collection<String> list, String delimiter)`
Joins a list of strings using the specified delimiter.
- **Parameters:**
    - `list` - The list to join
    - `delimiter` - The delimiter to use for joining
- **Returns:** The joined string
- **Example:**
  ```lua
  local joined = param:joinStringList(list, "|")
  ```

#### `getContainerSize(Object o)`
Gets the size of a container (array, collection, etc.).
- **Parameters:** `o` - The container object
- **Returns:** The size of the container
- **Example:**
  ```lua
  local size = param:getContainerSize(someArray)
  ```

### String to Primitive Conversion

#### `stringToBoolean(String s)`
Converts a string to a boolean value. Returns false if conversion fails.
- **Parameters:** `s` - The string to convert
- **Returns:** The boolean value
- **Example:**
  ```lua
  local bool = param:stringToBoolean("true")
  ```

#### `stringToBoolean(String s, boolean defaultValue)`
Converts a string to a boolean value with a fallback default.
- **Parameters:**
    - `s` - The string to convert
    - `defaultValue` - The default value if conversion fails
- **Returns:** The boolean value
- **Example:**
  ```lua
  local bool = param:stringToBoolean("invalid", true)
  ```

#### `stringToInt(String s)`
Converts a string to an integer value. Returns 0 if conversion fails.
- **Parameters:** `s` - The string to convert
- **Returns:** The integer value
- **Example:**
  ```lua
  local num = param:stringToInt("123")
  ```

#### `stringToInt(String s, int defaultValue)`
Converts a string to an integer value with a fallback default.
- **Parameters:**
    - `s` - The string to convert
    - `defaultValue` - The default value if conversion fails
- **Returns:** The integer value
- **Example:**
  ```lua
  local num = param:stringToInt("invalid", -1)
  ```

### Object Operations

#### `objectIsArray(Object o)`
Checks if an object is an array.
- **Parameters:** `o` - The object to check
- **Returns:** true if the object is an array
- **Example:**
  ```lua
  local isArray = param:objectIsArray(someObject)
  ```

#### `objectIsCollection(Object o)`
Checks if an object is a collection.
- **Parameters:** `o` - The object to check
- **Returns:** true if the object is a collection
- **Example:**
  ```lua
  local isCollection = param:objectIsCollection(someObject)
  ```

#### `objectToString(Object o)`
Converts an object to a string.
- **Parameters:** `o` - The object to convert
- **Returns:** The string representation of the object
- **Example:**
  ```lua
  local str = param:objectToString(someObject)
  ```

#### `objectToString(Object o, String defaultIfNull)`
Converts an object to a string with a fallback default if null.
- **Parameters:**
    - `o` - The object to convert
    - `defaultIfNull` - The default value if the object is null
- **Returns:** The string representation of the object
- **Example:**
  ```lua
  local str = param:objectToString(someObject, "N/A")
  ```

#### `objectTypeToString(Object o)`
Gets the class name of an object.
- **Parameters:** `o` - The object
- **Returns:** The class name of the object
- **Example:**
  ```lua
  local className = param:objectTypeToString(someObject)
  ```

#### `objectsAreEqual(Object a, Object b)`
Checks if two objects are equal.
- **Parameters:**
    - `a` - The first object
    - `b` - The second object
- **Returns:** true if the objects are equal
- **Example:**
  ```lua
  local areEqual = param:objectsAreEqual(obj1, obj2)
  ```

#### `objectsAreEqualDeep(Object a, Object b)`
Checks if two objects are deeply equal.
- **Parameters:**
    - `a` - The first object
    - `b` - The second object
- **Returns:** true if the objects are deeply equal
- **Example:**
  ```lua
  local areEqual = param:objectsAreEqualDeep(obj1, obj2)
  ```

#### `objectToHashCode(Object o)`
Gets the hash code of an object.
- **Parameters:** `o` - The object
- **Returns:** The hash code of the object
- **Example:**
  ```lua
  local hashCode = param:objectToHashCode(someObject)
  ```

## Array and Collection Operations

Functions for working with arrays and collections.

### Array Creation

#### `createArrayEmpty()`
Creates an empty array of objects.
- **Returns:** An empty array
- **Example:**
  ```lua
  local emptyArray = param:createArrayEmpty()
  ```

#### `createArray(int size)`
Creates an array of objects with the specified size.
- **Parameters:** `size` - The size of the array
- **Returns:** The array
- **Example:**
  ```lua
  local array = param:createArray(5)
  ```

#### `createArray(String elementKind, int size)`
Creates an array of a specific type with the specified size.
- **Parameters:**
    - `elementKind` - The fully qualified class name of the element type
    - `size` - The size of the array
- **Returns:** The array
- **Example:**
  ```lua
  local array = param:createArray("java.lang.String", 5)
  ```

#### `createArray(Class<?> elementKind, int size)`
Creates an array of a specific type with the specified size.
- **Parameters:**
    - `elementKind` - The Class object of the element type
    - `size` - The size of the array
- **Returns:** The array
- **Example:**
  ```lua
  local stringClass = param:classForString()
  local array = param:createArray(stringClass, 5)
  ```

#### `createArrayObject(int size)`
Creates an array of Objects with the specified size.
- **Parameters:** `size` - The size of the array
- **Returns:** The array
- **Example:**
  ```lua
  local array = param:createArrayObject(5)
  ```

#### `createArrayByte(int size)`
Creates a byte array with the specified size.
- **Parameters:** `size` - The size of the array
- **Returns:** The byte array
- **Example:**
  ```lua
  local byteArray = param:createArrayByte(10)
  ```

#### `createArrayChar(int size)`
Creates a char array with the specified size.
- **Parameters:** `size` - The size of the array
- **Returns:** The char array
- **Example:**
  ```lua
  local charArray = param:createArrayChar(10)
  ```

#### `createArrayShort(int size)`
Creates a short array with the specified size.
- **Parameters:** `size` - The size of the array
- **Returns:** The short array
- **Example:**
  ```lua
  local shortArray = param:createArrayShort(10)
  ```

#### `createArrayInt(int size)`
Creates an int array with the specified size.
- **Parameters:** `size` - The size of the array
- **Returns:** The int array
- **Example:**
  ```lua
  local intArray = param:createArrayInt(10)
  ```

#### `createArrayLong(int size)`
Creates a long array with the specified size.
- **Parameters:** `size` - The size of the array
- **Returns:** The long array
- **Example:**
  ```lua
  local longArray = param:createArrayLong(10)
  ```

#### `createArrayString(int size)`
Creates a String array with the specified size.
- **Parameters:** `size` - The size of the array
- **Returns:** The String array
- **Example:**
  ```lua
  local stringArray = param:createArrayString(5)
  ```

#### `createReflectArray(String className, int size)`
Creates an array of a specific type using reflection.
- **Parameters:**
    - `className` - The fully qualified class name of the element type
    - `size` - The size of the array
- **Returns:** The array
- **Example:**
  ```lua
  local array = param:createReflectArray("java.lang.Integer", 5)
  ```

#### `createReflectArray(Class<?> classType, int size)`
Creates an array of a specific type using reflection.
- **Parameters:**
    - `classType` - The Class object of the element type
    - `size` - The size of the array
- **Returns:** The array
- **Example:**
  ```lua
  local intClass = param:classForInt()
  local array = param:createReflectArray(intClass, 5)
  ```

### Array Operations

#### `reverseArray(Object o)`
Reverses the order of elements in an array.
- **Parameters:** `o` - The array to reverse
- **Returns:** The reversed array
- **Example:**
  ```lua
  local reversed = param:reverseArray(someArray)
  ```

#### `arraySize(Object o)`
Gets the length of an array.
- **Parameters:** `o` - The array
- **Returns:** The length of the array
- **Example:**
  ```lua
  local size = param:arraySize(someArray)
  ```

#### `arrayHasMinimumIndex(Object o, int minimumIndex)`
Checks if an array has at least the specified index.
- **Parameters:**
    - `o` - The array
    - `minimumIndex` - The minimum index to check
- **Returns:** true if the array has at least the specified index
- **Example:**
  ```lua
  local hasIndex = param:arrayHasMinimumIndex(someArray, 5)
  ```

#### `arrayHasMinimumSize(Object o, int minimumSize)`
Checks if an array has at least the specified size.
- **Parameters:**
    - `o` - The array
    - `minimumSize` - The minimum size to check
- **Returns:** true if the array has at least the specified size
- **Example:**
  ```lua
  local hasSize = param:arrayHasMinimumSize(someArray, 5)
  ```

#### `arrayElementAt(Object a, int index)`
Gets the element at the specified index in an array.
- **Parameters:**
    - `a` - The array
    - `index` - The index of the element
- **Returns:** The element at the specified index
- **Example:**
  ```lua
  local element = param:arrayElementAt(someArray, 2)
  ```

#### `arrayContains(Object a, Object c)`
Checks if an array contains a specific element.
- **Parameters:**
    - `a` - The array
    - `c` - The element to check for
- **Returns:** true if the array contains the element
- **Example:**
  ```lua
  local contains = param:arrayContains(someArray, "test")
  ```

#### `addElementToArray(Object a, Object e)`
Adds an element to an array.
- **Parameters:**
    - `a` - The array
    - `e` - The element to add
- **Returns:** The new array with the added element
- **Example:**
  ```lua
  local newArray = param:addElementToArray(someArray, "newElement")
  ```

#### `setArrayElementAtIndex(Object a, Object e, int index)`
Sets the element at the specified index in an array.
- **Parameters:**
    - `a` - The array
    - `e` - The element to set
    - `index` - The index to set the element at
- **Example:**
  ```lua
  param:setArrayElementAtIndex(someArray, "newValue", 2)
  ```

#### `removeArrayElementAtIndex(Object a, int index)`
Removes the element at the specified index from an array.
- **Parameters:**
    - `a` - The array
    - `index` - The index of the element to remove
- **Example:**
  ```lua
  param:removeArrayElementAtIndex(someArray, 2)
  ```

#### `elementIndexArray(Object a, Object e)`
Gets the index of an element in an array.
- **Parameters:**
    - `a` - The array
    - `e` - The element to find
- **Returns:** The index of the element, or -1 if not found
- **Example:**
  ```lua
  local index = param:elementIndexArray(someArray, "searchElement")
  ```

#### `clearArray(Object a)`
Clears all elements in an array by setting them to null/default values.
- **Parameters:** `a` - The array to clear
- **Returns:** true if the array was cleared successfully
- **Example:**
  ```lua
  param:clearArray(someArray)
  ```

#### `removeDuplicateArrayElements(Object a)`
Removes duplicate elements from an array.
- **Parameters:** `a` - The array
- **Returns:** A new array with duplicates removed
- **Example:**
  ```lua
  local uniqueArray = param:removeDuplicateArrayElements(someArray)
  ```

#### `removeNullArrayElements(Object a)`
Removes null elements from an array.
- **Parameters:** `a` - The array
- **Returns:** A new array with null elements removed
- **Example:**
  ```lua
  local nonNullArray = param:removeNullArrayElements(someArray)
  ```

#### `getFirstArrayElement(Object a)`
Gets the first element of an array.
- **Parameters:** `a` - The array
- **Returns:** The first element of the array
- **Example:**
  ```lua
  local first = param:getFirstArrayElement(someArray)
  ```

#### `getLastArrayElement(Object a)`
Gets the last element of an array.
- **Parameters:** `a` - The array
- **Returns:** The last element of the array
- **Example:**
  ```lua
  local last = param:getLastArrayElement(someArray)
  ```

### List Operations

#### `createListEmpty()`
Creates an empty list.
- **Returns:** An empty list
- **Example:**
  ```lua
  local emptyList = param:createListEmpty()
  ```

#### `createList(int size)`
Creates a list with an initial capacity.
- **Parameters:** `size` - The initial capacity of the list
- **Returns:** The list
- **Example:**
  ```lua
  local list = param:createList(10)
  ```

#### `arrayToList(Object e)`
Converts an array to a list.
- **Parameters:** `e` - The array to convert
- **Returns:** The resulting list
- **Example:**
  ```lua
  local list = param:arrayToList(someArray)
  ```

#### `listToArray(Object l)`
Converts a list to an array.
- **Parameters:** `l` - The list to convert
- **Returns:** The resulting array
- **Example:**
  ```lua
  local array = param:listToArray(someList)
  ```

#### `listToArray(Object l, String elementKind)`
Converts a list to an array of a specific type.
- **Parameters:**
    - `l` - The list to convert
    - `elementKind` - The fully qualified class name of the element type
- **Returns:** The resulting array
- **Example:**
  ```lua
  local array = param:listToArray(someList, "java.lang.String")
  ```

#### `listToArray(Object l, Class<?> elementKind)`
Converts a list to an array of a specific type.
- **Parameters:**
    - `l` - The list to convert
    - `elementKind` - The Class object of the element type
- **Returns:** The resulting array
- **Example:**
  ```lua
  local stringClass = param:classForString()
  local array = param:listToArray(someList, stringClass)
  ```

#### `sizeList(Object o)`
Gets the size of a list.
- **Parameters:** `o` - The list
- **Returns:** The size of the list
- **Example:**
  ```lua
  local size = param:sizeList(someList)
  ```

#### `listHasMinimumIndex(Object o, int minimumIndex)`
Checks if a list has at least the specified index.
- **Parameters:**
    - `o` - The list
    - `minimumIndex` - The minimum index to check
- **Returns:** true if the list has at least the specified index
- **Example:**
  ```lua
  local hasIndex = param:listHasMinimumIndex(someList, 5)
  ```

#### `listHasMinimumSize(Object o, int minimumSize)`
Checks if a list has at least the specified size.
- **Parameters:**
    - `o` - The list
    - `minimumSize` - The minimum size to check
- **Returns:** true if the list has at least the specified size
- **Example:**
  ```lua
  local hasSize = param:listHasMinimumSize(someList, 5)
  ```

#### `listElementAt(Object a, int index)`
Gets the element at the specified index in a list.
- **Parameters:**
    - `a` - The list
    - `index` - The index of the element
- **Returns:** The element at the specified index
- **Example:**
  ```lua
  local element = param:listElementAt(someList, 2)
  ```

#### `listContain(Object l, Object c)`
Checks if a list contains a specific element.
- **Parameters:**
    - `l` - The list
    - `c` - The element to check for
- **Returns:** true if the list contains the element
- **Example:**
  ```lua
  local contains = param:listContain(someList, "test")
  ```

#### `addElementToList(Object a, Object e)`
Adds an element to a list.
- **Parameters:**
    - `a` - The list
    - `e` - The element to add
- **Example:**
  ```lua
  param:addElementToList(someList, "newElement")
  ```

#### `setListElementAtIndex(Object l, Object e, int index)`
Sets the element at the specified index in a list.
- **Parameters:**
    - `l` - The list
    - `e` - The element to set
    - `index` - The index to set the element at
- **Example:**
  ```lua
  param:setListElementAtIndex(someList, "newValue", 2)
  ```

#### `removeListElementAtIndex(Object l, int index)`
Removes the element at the specified index from a list.
- **Parameters:**
    - `l` - The list
    - `index` - The index of the element to remove
- **Example:**
  ```lua
  param:removeListElementAtIndex(someList, 2)
  ```

#### `elementIndexList(Object l, Object e)`
Gets the index of an element in a list.
- **Parameters:**
    - `l` - The list
    - `e` - The element to find
- **Returns:** The index of the element, or -1 if not found
- **Example:**
  ```lua
  local index = param:elementIndexList(someList, "searchElement")
  ```

#### `clearList(Object l)`
Clears all elements from a list.
- **Parameters:** `l` - The list to clear
- **Returns:** true if the list was cleared successfully
- **Example:**
  ```lua
  param:clearList(someList)
  ```

#### `removeDuplicateListElements(Object l)`
Removes duplicate elements from a list.
- **Parameters:** `l` - The list
- **Returns:** true if duplicates were removed successfully
- **Example:**
  ```lua
  param:removeDuplicateListElements(someList)
  ```

#### `removeNullListElements(Object l)`
Removes null elements from a list.
- **Parameters:** `l` - The list
- **Returns:** true if null elements were removed successfully
- **Example:**
  ```lua
  param:removeNullListElements(someList)
  ```

#### `getFirstListElement(Object l)`
Gets the first element of a list.
- **Parameters:** `l` - The list
- **Returns:** The first element of the list
- **Example:**
  ```lua
  local first = param:getFirstListElement(someList)
  ```

#### `getLastListElement(Object l)`
Gets the last element of a list.
- **Parameters:** `l` - The list
- **Returns:** The last element of the list
- **Example:**
  ```lua
  local last = param:getLastListElement(someList)
  ```

## Random Data Generation

Functions for generating random data of various types.

### Random String Generation

#### `randomString()`
Generates a random string.
- **Returns:** A random string
- **Example:**
  ```lua
  local randomStr = param:randomString()
  ```

#### `randomString(int length)`
Generates a random string with the specified length.
- **Parameters:** `length` - The length of the string
- **Returns:** A random string
- **Example:**
  ```lua
  local randomStr = param:randomString(10)
  ```

#### `randomString(int origin, int bound)`
Generates a random string with a length between the specified bounds.
- **Parameters:**
    - `origin` - The minimum length (inclusive)
    - `bound` - The maximum length (exclusive)
- **Returns:** A random string
- **Example:**
  ```lua
  local randomStr = param:randomString(5, 10)
  ```

#### `randomHexString()`
Generates a random hexadecimal string.
- **Returns:** A random hexadecimal string
- **Example:**
  ```lua
  local randomHex = param:randomHexString()
  ```

#### `randomHexString(int length)`
Generates a random hexadecimal string with the specified length.
- **Parameters:** `length` - The length of the string
- **Returns:** A random hexadecimal string
- **Example:**
  ```lua
  local randomHex = param:randomHexString(8)
  ```

#### `randomHexString(int origin, int bound)`
Generates a random hexadecimal string with a length between the specified bounds.
- **Parameters:**
    - `origin` - The minimum length (inclusive)
    - `bound` - The maximum length (exclusive)
- **Returns:** A random hexadecimal string
- **Example:**
  ```lua
  local randomHex = param:randomHexString(5, 10)
  ```

#### `randomAlphabeticString()`
Generates a random alphabetic string.
- **Returns:** A random alphabetic string
- **Example:**
  ```lua
  local randomAlpha = param:randomAlphabeticString()
  ```

#### `randomAlphabeticString(int length)`
Generates a random alphabetic string with the specified length.
- **Parameters:** `length` - The length of the string
- **Returns:** A random alphabetic string
- **Example:**
  ```lua
  local randomAlpha = param:randomAlphabeticString(10)
  ```

#### `randomAlphabeticString(int origin, int bound)`
Generates a random alphabetic string with a length between the specified bounds.
- **Parameters:**
    - `origin` - The minimum length (inclusive)
    - `bound` - The maximum length (exclusive)
- **Returns:** A random alphabetic string
- **Example:**
  ```lua
  local randomAlpha = param:randomAlphabeticString(5, 10)
  ```

#### `randomNumericString()`
Generates a random numeric string.
- **Returns:** A random numeric string
- **Example:**
  ```lua
  local randomNum = param:randomNumericString()
  ```

#### `randomNumericString(int length)`
Generates a random numeric string with the specified length.
- **Parameters:** `length` - The length of the string
- **Returns:** A random numeric string
- **Example:**
  ```lua
  local randomNum = param:randomNumericString(6)
  ```

#### `randomNumericString(int origin, int bound)`
Generates a random numeric string with a length between the specified bounds.
- **Parameters:**
    - `origin` - The minimum length (inclusive)
    - `bound` - The maximum length (exclusive)
- **Returns:** A random numeric string
- **Example:**
  ```lua
  local randomNum = param:randomNumericString(4, 8)
  ```

#### `randomUuid()`
Generates a random UUID string.
- **Returns:** A random UUID string
- **Example:**
  ```lua
  local uuid = param:randomUuid()
  ```

### Random Number Generation

#### `randomShort()`
Generates a random short value.
- **Returns:** A random short value
- **Example:**
  ```lua
  local randomShort = param:randomShort()
  ```

#### `randomShort(short bound)`
Generates a random short value between 0 (inclusive) and the specified bound (exclusive).
- **Parameters:** `bound` - The upper bound (exclusive)
- **Returns:** A random short value
- **Example:**
  ```lua
  local randomShort = param:randomShort(100)
  ```

#### `randomShort(short origin, short bound)`
Generates a random short value between the specified bounds.
- **Parameters:**
    - `origin` - The lower bound (inclusive)
    - `bound` - The upper bound (exclusive)
- **Returns:** A random short value
- **Example:**
  ```lua
  local randomShort = param:randomShort(10, 100)
  ```

#### `randomInt()`
Generates a random integer value.
- **Returns:** A random integer value
- **Example:**
  ```lua
  local randomInt = param:randomInt()
  ```

#### `randomInt(int bound)`
Generates a random integer value between 0 (inclusive) and the specified bound (exclusive).
- **Parameters:** `bound` - The upper bound (exclusive)
- **Returns:** A random integer value
- **Example:**
  ```lua
  local randomInt = param:randomInt(100)
  ```

#### `randomInt(int origin, int bound)`
Generates a random integer value between the specified bounds.
- **Parameters:**
    - `origin` - The lower bound (inclusive)
    - `bound` - The upper bound (exclusive)
- **Returns:** A random integer value
- **Example:**
  ```lua
  local randomInt = param:randomInt(10, 100)
  ```

#### `randomFloat()`
Generates a random float value.
- **Returns:** A random float value
- **Example:**
  ```lua
  local randomFloat = param:randomFloat()
  ```

#### `randomFloat(float bound)`
Generates a random float value between 0 (inclusive) and the specified bound (exclusive).
- **Parameters:** `bound` - The upper bound (exclusive)
- **Returns:** A random float value
- **Example:**
  ```lua
  local randomFloat = param:randomFloat(10.0)
  ```

#### `randomFloat(float origin, float bound)`
Generates a random float value between the specified bounds.
- **Parameters:**
    - `origin` - The lower bound (inclusive)
    - `bound` - The upper bound (exclusive)
- **Returns:** A random float value
- **Example:**
  ```lua
  local randomFloat = param:randomFloat(1.0, 10.0)
  ```

#### `randomDouble()`
Generates a random double value.
- **Returns:** A random double value
- **Example:**
  ```lua
  local randomDouble = param:randomDouble()
  ```

#### `randomDouble(double bound)`
Generates a random double value between 0 (inclusive) and the specified bound (exclusive).
- **Parameters:** `bound` - The upper bound (exclusive)
- **Returns:** A random double value
- **Example:**
  ```lua
  local randomDouble = param:randomDouble(10.0)
  ```

#### `randomDouble(double origin, double bound)`
Generates a random double value between the specified bounds.
- **Parameters:**
    - `origin` - The lower bound (inclusive)
    - `bound` - The upper bound (exclusive)
- **Returns:** A random double value
- **Example:**
  ```lua
  local randomDouble = param:randomDouble(1.0, 10.0)
  ```

#### `randomByte()`
Generates a random byte value.
- **Returns:** A random byte value
- **Example:**
  ```lua
  local randomByte = param:randomByte()
  ```

### Random Boolean Generation

#### `randomBool()`
Generates a random boolean value.
- **Returns:** A random boolean value
- **Example:**
  ```lua
  local randomBool = param:randomBool()
  ```

#### `randomChance()`
Generates a random boolean with a 50% chance of being true.
- **Returns:** A random boolean value
- **Example:**
  ```lua
  local result = param:randomChance()
  ```

#### `randomChance(int chance)`
Generates a random boolean with the specified chance of being true.
- **Parameters:** `chance` - The percentage chance (0-100) of the result being true
- **Returns:** A random boolean value
- **Example:**
  ```lua
  local result = param:randomChance(75)  -- 75% chance of true
  ```

### Random Bytes Generation

#### `randomBytes()`
Generates a random byte array.
- **Returns:** A random byte array
- **Example:**
  ```lua
  local randomBytes = param:randomBytes()
  ```

#### `randomBytes(int bound)`
Generates a random byte array with a length between 0 (inclusive) and the specified bound (exclusive).
- **Parameters:** `bound` - The upper bound (exclusive) for the length
- **Returns:** A random byte array
- **Example:**
  ```lua
  local randomBytes = param:randomBytes(10)
  ```

#### `randomBytes(int origin, int bound)`
Generates a random byte array with a length between the specified bounds.
- **Parameters:**
    - `origin` - The lower bound (inclusive) for the length
    - `bound` - The upper bound (exclusive) for the length
- **Returns:** A random byte array
- **Example:**
  ```lua
  local randomBytes = param:randomBytes(5, 10)
  ```

## Reflection Utilities

Functions for working with Java reflection.

### Class Operations

#### `classForName(String className)`
Gets a Class object for a class with the specified name.
- **Parameters:** `className` - The fully qualified class name
- **Returns:** The Class object
- **Example:**
  ```lua
  local stringClass = param:classForName("java.lang.String")
  ```

#### `classFromObject(Object o)`
Gets the Class object for an object.
- **Parameters:** `o` - The object
- **Returns:** The Class object
- **Example:**
  ```lua
  local class = param:classFromObject(someObject)
  ```

#### `classForObject()`
Gets the Class object for java.lang.Object.
- **Returns:** The Class object for java.lang.Object
- **Example:**
  ```lua
  local objectClass = param:classForObject()
  ```

#### `classForByte()`
Gets the Class object for java.lang.Byte.
- **Returns:** The Class object for java.lang.Byte
- **Example:**
  ```lua
  local byteClass = param:classForByte()
  ```

#### `classForChar()`
Gets the Class object for java.lang.Character.
- **Returns:** The Class object for java.lang.Character
- **Example:**
  ```lua
  local charClass = param:classForChar()
  ```

#### `classForShort()`
Gets the Class object for java.lang.Short.
- **Returns:** The Class object for java.lang.Short
- **Example:**
  ```lua
  local shortClass = param:classForShort()
  ```

#### `classForInt()`
Gets the Class object for java.lang.Integer.
- **Returns:** The Class object for java.lang.Integer
- **Example:**
  ```lua
  local intClass = param:classForInt()
  ```

#### `classForLong()`
Gets the Class object for java.lang.Long.
- **Returns:** The Class object for java.lang.Long
- **Example:**
  ```lua
  local longClass = param:classForLong()
  ```

#### `classForString()`
Gets the Class object for java.lang.String.
- **Returns:** The Class object for java.lang.String
- **Example:**
  ```lua
  local stringClass = param:classForString()
  ```

### Type Checking

#### `isObjectObject(Object o)`
Checks if an object is of the exact type java.lang.Object.
- **Parameters:** `o` - The object to check
- **Returns:** true if the object is of the exact type java.lang.Object
- **Example:**
  ```lua
  local isObject = param:isObjectObject(someObject)
  ```

#### `isObjectByte(Object o)`
Checks if an object is a java.lang.Byte.
- **Parameters:** `o` - The object to check
- **Returns:** true if the object is a Byte
- **Example:**
  ```lua
  local isByte = param:isObjectByte(someObject)
  ```

#### `isObjectShort(Object o)`
Checks if an object is a java.lang.Short.
- **Parameters:** `o` - The object to check
- **Returns:** true if the object is a Short
- **Example:**
  ```lua
  local isShort = param:isObjectShort(someObject)
  ```

#### `isObjectInt(Object o)`
Checks if an object is a java.lang.Integer.
- **Parameters:** `o` - The object to check
- **Returns:** true if the object is an Integer
- **Example:**
  ```lua
  local isInt = param:isObjectInt(someObject)
  ```

#### `isObjectChar(Object o)`
Checks if an object is a java.lang.Character.
- **Parameters:** `o` - The object to check
- **Returns:** true if the object is a Character
- **Example:**
  ```lua
  local isChar = param:isObjectChar(someObject)
  ```

#### `isObjectCollection(Object o)`
Checks if an object is a Collection.
- **Parameters:** `o` - The object to check
- **Returns:** true if the object is a Collection
- **Example:**
  ```lua
  local isCollection = param:isObjectCollection(someObject)
  ```

#### `isObjectArray(Object o)`
Checks if an object is an Array.
- **Parameters:** `o` - The object to check
- **Returns:** true if the object is an Array
- **Example:**
  ```lua
  local isArray = param:isObjectArray(someObject)
  ```

#### `isObjectString(Object o)`
Checks if an object is a java.lang.String.
- **Parameters:** `o` - The object to check
- **Returns:** true if the object is a String
- **Example:**
  ```lua
  local isString = param:isObjectString(someObject)
  ```

#### `isAssignableFrom(Object o, String className)`
Checks if an object's class is assignable from the specified class name.
- **Parameters:**
    - `o` - The object
    - `className` - The fully qualified class name
- **Returns:** true if the object's class is assignable from the specified class
- **Example:**
  ```lua
  local isAssignable = param:isAssignableFrom(someObject, "java.lang.CharSequence")
  ```

#### `isAssignableFrom(Object o, Class<?> clazz)`
Checks if an object's class is assignable from the specified class.
- **Parameters:**
    - `o` - The object
    - `clazz` - The Class object
- **Returns:** true if the object's class is assignable from the specified class
- **Example:**
  ```lua
  local charSequenceClass = param:classForName("java.lang.CharSequence")
  local isAssignable = param:isAssignableFrom(someObject, charSequenceClass)
  ```

### Reflection Operations

#### `newInstance(String className, Object... args)`
Creates a new instance of a class with the specified name using the provided arguments.
- **Parameters:**
    - `className` - The fully qualified class name
    - `args` - The constructor arguments
- **Returns:** The new instance
- **Example:**
  ```lua
  local instance = param:newInstance("java.lang.StringBuilder", "Initial text")
  ```

#### `newInstance(Class<?> clazz, Object... args)`
Creates a new instance of a class using the provided arguments.
- **Parameters:**
    - `clazz` - The Class object
    - `args` - The constructor arguments
- **Returns:** The new instance
- **Example:**
  ```lua
  local stringBuilderClass = param:classForName("java.lang.StringBuilder")
  local instance = param:newInstance(stringBuilderClass, "Initial text")
  ```

#### `newInstance(String className)`
Creates a new instance of a class with the specified name using the default constructor.
- **Parameters:** `className` - The fully qualified class name
- **Returns:** The new instance
- **Example:**
  ```lua
  local instance = param:newInstance("java.util.ArrayList")
  ```

#### `newInstance(Class<?> clazz)`
Creates a new instance of a class using the default constructor.
- **Parameters:** `clazz` - The Class object
- **Returns:** The new instance
- **Example:**
  ```lua
  local arrayListClass = param:classForName("java.util.ArrayList")
  local instance = param:newInstance(arrayListClass)
  ```

#### `hasClass(String className)`
Checks if a class with the specified name exists.
- **Parameters:** `className` - The fully qualified class name
- **Returns:** true if the class exists
- **Example:**
  ```lua
  local exists = param:hasClass("java.util.HashMap")
  ```

#### `hasMethod(String className, String methodName)`
Checks if a class has a method with the specified name.
- **Parameters:**
    - `className` - The fully qualified class name
    - `methodName` - The name of the method
- **Returns:** true if the class has the method
- **Example:**
  ```lua
  local hasMethod = param:hasMethod("java.lang.String", "toUpperCase")
  ```

#### `hasMethod(Class<?> clazz, String methodName)`
Checks if a class has a method with the specified name.
- **Parameters:**
    - `clazz` - The Class object
    - `methodName` - The name of the method
- **Returns:** true if the class has the method
- **Example:**
  ```lua
  local stringClass = param:classForName("java.lang.String")
  local hasMethod = param:hasMethod(stringClass, "toUpperCase")
  ```

#### `hasMethod(String className, String methodName, int argCount)`
Checks if a class has a method with the specified name and argument count.
- **Parameters:**
    - `className` - The fully qualified class name
    - `methodName` - The name of the method
    - `argCount` - The number of arguments
- **Returns:** true if the class has the method with the specified argument count
- **Example:**
  ```lua
  local hasMethod = param:hasMethod("java.lang.String", "substring", 1)
  ```

#### `hasMethod(Class<?> clazz, String methodName, int argCount)`
Checks if a class has a method with the specified name and argument count.
- **Parameters:**
    - `clazz` - The Class object
    - `methodName` - The name of the method
    - `argCount` - The number of arguments
- **Returns:** true if the class has the method with the specified argument count
- **Example:**
  ```lua
  local stringClass = param:classForName("java.lang.String")
  local hasMethod = param:hasMethod(stringClass, "substring", 1)
  ```

#### `hasField(String className, String fieldName)`
Checks if a class has a field with the specified name.
- **Parameters:**
    - `className` - The fully qualified class name
    - `fieldName` - The name of the field
- **Returns:** true if the class has the field
- **Example:**
  ```lua
  local hasField = param:hasField("java.lang.System", "out")
  ```

#### `hasField(Class<?> clazz, String fieldName)`
Checks if a class has a field with the specified name.
- **Parameters:**
    - `clazz` - The Class object
    - `fieldName` - The name of the field
- **Returns:** true if the class has the field
- **Example:**
  ```lua
  local systemClass = param:classForName("java.lang.System")
  local hasField = param:hasField(systemClass, "out")
  ```

#### `methodForName(String className, String methodName)`
Gets a Method object for a method with the specified name in a class.
- **Parameters:**
    - `className` - The fully qualified class name
    - `methodName` - The name of the method
- **Returns:** The Method object
- **Example:**
  ```lua
  local method = param:methodForName("java.lang.String", "toUpperCase")
  ```

#### `methodForName(Class<?> clazz, String methodName)`
Gets a Method object for a method with the specified name in a class.
- **Parameters:**
    - `clazz` - The Class object
    - `methodName` - The name of the method
- **Returns:** The Method object
- **Example:**
  ```lua
  local stringClass = param:classForName("java.lang.String")
  local method = param:methodForName(stringClass, "toUpperCase")
  ```

#### `methodForName(Class<?> clazz, String methodName, int argCount)`
Gets a Method object for a method with the specified name and argument count in a class.
- **Parameters:**
    - `clazz` - The Class object
    - `methodName` - The name of the method
    - `argCount` - The number of arguments
- **Returns:** The Method object
- **Example:**
  ```lua
  local stringClass = param:classForName("java.lang.String")
  local method = param:methodForName(stringClass, "substring", 1)
  ```

#### `methodForName(String className, String methodName, int argCount)`
Gets a Method object for a method with the specified name and argument count in a class.
- **Parameters:**
    - `className` - The fully qualified class name
    - `methodName` - The name of the method
    - `argCount` - The number of arguments
- **Returns:** The Method object
- **Example:**
  ```lua
  local method = param:methodForName("java.lang.String", "substring", 1)
  ```

#### `fieldForName(String className, String fieldName)`
Gets a Field object for a field with the specified name in a class.
- **Parameters:**
    - `className` - The fully qualified class name
    - `fieldName` - The name of the field
- **Returns:** The Field object
- **Example:**
  ```lua
  local field = param:fieldForName("java.lang.System", "out")
  ```

#### `fieldForName(Class<?> clazz, String fieldName)`
Gets a Field object for a field with the specified name in a class.
- **Parameters:**
    - `clazz` - The Class object
    - `fieldName` - The name of the field
- **Returns:** The Field object
- **Example:**
  ```lua
  local systemClass = param:classForName("java.lang.System")
  local field = param:fieldForName(systemClass, "out")
  ```

#### `bypassHiddenApiRestrictions()`
Bypasses hidden API restrictions for the current application.
- **Returns:** true if the operation was successful
- **Example:**
  ```lua
  param:bypassHiddenApiRestrictions()
  ```

#### `bypassHiddenApiRestrictions(ClassLoader classLoader)`
Bypasses hidden API restrictions for a specific ClassLoader.
- **Parameters:** `classLoader` - The ClassLoader
- **Returns:** true if the operation was successful
- **Example:**
  ```lua
  local classLoader = someObject:getClass():getClassLoader()
  param:bypassHiddenApiRestrictions(classLoader)
  ```

## System Functions

Functions for interacting with the system and runtime environment.

### Process Management

#### `myPid()`
Gets the process ID of the current process.
- **Returns:** The process ID
- **Example:**
  ```lua
  local pid = param:myPid()
  ```

#### `myUid()`
Gets the user ID of the current process.
- **Returns:** The user ID
- **Example:**
  ```lua
  local uid = param:myUid()
  ```

#### `myTid()`
Gets the thread ID of the current thread.
- **Returns:** The thread ID
- **Example:**
  ```lua
  local tid = param:myTid()
  ```

#### `exit()`
Terminates the current process with status code 0.
- **Example:**
  ```lua
  param:exit()
  ```

#### `exit(int status)`
Terminates the current process with the specified status code.
- **Parameters:** `status` - The exit status code
- **Example:**
  ```lua
  param:exit(1)
  ```

#### `exitCurrentThread()`
Interrupts the current thread.
- **Example:**
  ```lua
  param:exitCurrentThread()
  ```

### Runtime Operations

#### `exec(String command)`
Executes a system command.
- **Parameters:** `command` - The command to execute
- **Returns:** A Process object representing the process
- **Example:**
  ```lua
  local process = param:exec("ls -la")
  ```

#### `execEcho(String msg)`
Executes an echo command with the specified message.
- **Parameters:** `msg` - The message to echo
- **Returns:** A Process object representing the process
- **Example:**
  ```lua
  local process = param:execEcho("Hello, World!")
  ```

#### `getRuntime()`
Gets the Runtime object.
- **Returns:** The Runtime object
- **Example:**
  ```lua
  local runtime = param:getRuntime()
  ```

### Thread Management

#### `getCurrentThread()`
Gets the current thread.
- **Returns:** The current Thread object
- **Example:**
  ```lua
  local thread = param:getCurrentThread()
  ```

#### `isCurrentThreadMain()`
Checks if the current thread is the main thread.
- **Returns:** true if the current thread is the main thread
- **Example:**
  ```lua
  local isMain = param:isCurrentThreadMain()
  ```

### System Utilities

#### `clearLogCatLogs()`
Clears the LogCat logs.
- **Example:**
  ```lua
  param:clearLogCatLogs()
  ```

#### `gigabytesToBytes(int gigabytes)`
Converts gigabytes to bytes.
- **Parameters:** `gigabytes` - The number of gigabytes
- **Returns:** The equivalent number of bytes
- **Example:**
  ```lua
  local bytes = param:gigabytesToBytes(2)
  ```

## Complete Example

Here's a complete example that demonstrates using various functions from the XParamExtra API to manipulate strings and arrays:

```lua
-- Create a hook function that modifies a string result
function after_method_call(param)
    -- Get the original result
    local originalResult = param:getResult()
    
    -- Log the original result
    param:setLogOld(originalResult)
    
    -- Only proceed if we have a valid string result
    if param:isObjectString(originalResult) and param:stringIsValid(originalResult) then
        -- Create a modified version by replacing specific text
        local modifiedResult = param:stringReplaceAll(originalResult, "password", "********")
        
        -- Convert to uppercase if not already
        if modifiedResult ~= param:stringToUpperCase(modifiedResult) then
            modifiedResult = param:stringToUpperCase(modifiedResult)
        end
        
        -- Set the new result
        param:setResult(modifiedResult)
        
        -- Log the new result
        param:setLogNew(modifiedResult)
        param:setLogExtra("String was modified and converted to uppercase")
        
        -- Print debug info to logcat
        log("Original: " .. param:safe(originalResult))
        log("Modified: " .. param:safe(modifiedResult))
    end
    
    return true
end
```

## Best Practices

When using the XParamExtra API in your LUA scripts, keep these best practices in mind:

1. **Error Handling**: Always check for null values before performing operations on objects.
   ```lua
   if param:stringIsValid(someString) then
       -- Proceed with string operations
   end
   ```

2. **Logging**: Use the logging functions to keep track of changes for debugging.
   ```lua
   param:setLogOld(originalValue)
   param:setLogNew(newValue)
   param:setLogExtra("Applied transformation X")
   ```

3. **Performance**: Be mindful of performance implications when working with large arrays or lists.
   ```lua
   -- Check array size before operating on it
   if param:arrayHasMinimumSize(someArray, 1000) then
       log("Warning: Processing large array")
   end
   ```

4. **Reflection**: Use reflection operations cautiously as they can impact performance.
   ```lua
   -- Cache reflection results
   local stringClass = param:classForName("java.lang.String")
   -- Reuse stringClass instead of calling classForName repeatedly
   ```

5. **Resource Management**: Clean up resources when no longer needed.
   ```lua
   -- Clear collections when done
   param:clearList(temporaryList)
   ```

## Troubleshooting

Here are some common issues and their solutions:

1. **NullPointerException**: Check if objects are null before operating on them.
   ```lua
   if param:objectToString(obj) ~= "null" then
       -- Safe to proceed
   end
   ```

2. **ClassCastException**: Verify object types before casting.
   ```lua
   if param:isObjectString(obj) then
       -- Safe to treat as string
   end
   ```

3. **IndexOutOfBoundsException**: Validate array/list indices before accessing elements.
   ```lua
   if param:arrayHasMinimumIndex(array, index) then
       -- Safe to access
   end
   ```

4. **Security exceptions**: Use bypass functions when necessary (with caution).
   ```lua
   param:bypassHiddenApiRestrictions()
   ```