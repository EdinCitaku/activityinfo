package org.activityinfo.indexedb;

import org.activityinfo.promise.Promise;

/**
 * Interface to an Indexed DB object store.
 */
public interface IDBObjectStore<T> {

    void put(T object);

    void put(int key, T object);

    void put(String key, T object);

    void put(String[] key, T object);

    /**
     * Retrieves the given object from the ObjectStore, encoded as a JSON string, or {@code null} if no
     * such object exists.
     */
    Promise<T> get(String key);

    /**
     * Retrieves the given object from the ObjectStore, encoded as a JSON string, or {@code null} if no
     * such object exists.
     */
    Promise<T> get(String[] keys);


    Promise<T> get(int key);

    /**
     * Opens a cursor over the objects in the store with keys in the range [lower, bound]
     *
     * See <a href="https://w3c.github.io/IndexedDB/#key-construct">MDN</a> for the way in which keys are ordered.
     *  @param lowerBound the lower bound of the key, inclusive
     * @param upperBound the upper bound of the key, inclusive
     * @param callback
     */
    void openCursor(String lowerBound, String upperBound, IDBCursorCallback<T> callback);

    void openCursor(IDBCursorCallback<T> callback);

    void delete(String key);

    /**
     * Deletes all the objects in the store with keys in the range [lower, bound]
     *
     * See <a href="https://w3c.github.io/IndexedDB/#key-construct">MDN</a> for the way in which keys are ordered.
     *  @param lowerBound the lower bound of the key, inclusive
     * @param upperBound the upper bound of the key, inclusive
     * @param callback
     */
    void delete(String lowerBound, String upperBound);

    void delete(int key);
}