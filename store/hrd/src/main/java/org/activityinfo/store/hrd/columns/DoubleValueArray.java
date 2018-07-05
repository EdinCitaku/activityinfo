package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;

public class DoubleValueArray {

    public static final int BYTES = 8;

    public static boolean update(Entity blockEntity, String propertyName, int index, double value1, double value2) {

        Blob blob = (Blob) blockEntity.getProperty(propertyName);
        boolean missing = Double.isNaN(value1) && Double.isNaN(value2);

        // Values at the end are assumed to be NaN
        int capacity = ValueArrays.length(blob, BYTES);
        if(missing && (index + 1) >= capacity) {
            return false;
        }

        byte[] bytes = ensureCapacity(blob, index + 1);
        set(bytes, index, value1);
        set(bytes, index + 1, value2);

        return true;
    }

    /**
     * Allocates, if necessary, a larger array to hold up to the element index. Unused space is
     * initialized with NaN
     *
     * @param blob The existing values blob, or {@code null} if it is still uninitialized.
     * @param index the value index to update
     */
    public static byte[] ensureCapacity(Blob blob, int index) {
        int originalLength = ValueArrays.length(blob, BYTES);
        byte[] updatedArray = ValueArrays.ensureCapacity(blob, index, BYTES);

        // Fill empty spaces with NaN, which has the byte layout of [0, 0, 0, 0, 0, 0, -8, 127]
        int pos = originalLength * BYTES;
        while(pos < updatedArray.length) {
            updatedArray[pos + 6] = (byte) -8;
            updatedArray[pos + 7] = (byte) 127;
            pos += 8;
        }
        return updatedArray;
    }

    public static Blob update(Blob values, int index, double value) {
        byte[] bytes = ensureCapacity(values, index);
        set(bytes, index, value);
        return new Blob(bytes);
    }

    public static Blob update(Blob values, int index, double value1, double value2) {
        byte[] bytes = ensureCapacity(values, index + 1);
        set(bytes, index, value1);
        set(bytes, index, value2);
        return new Blob(bytes);
    }

    private static void set(byte[] bytes, int index, double value) {
        long longValue = Double.doubleToRawLongBits(value);
        int pos = index * BYTES;
        bytes[pos++] = (byte)longValue;
        bytes[pos++] = (byte)(longValue >> 8);
        bytes[pos++] = (byte)(longValue >> 16);
        bytes[pos++] = (byte)(longValue >> 24);
        bytes[pos++] = (byte)(longValue >> 32);
        bytes[pos++] = (byte)(longValue >> 40);
        bytes[pos++] = (byte)(longValue >> 48);
        bytes[pos  ] = (byte)(longValue >> 56);
    }

}
