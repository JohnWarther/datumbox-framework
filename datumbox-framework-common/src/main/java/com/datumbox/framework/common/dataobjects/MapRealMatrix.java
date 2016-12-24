/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datumbox.framework.common.dataobjects;

import com.datumbox.framework.common.persistentstorage.interfaces.StorageConnector;
import com.datumbox.framework.common.persistentstorage.interfaces.StorageConnector.MapType;
import com.datumbox.framework.common.persistentstorage.interfaces.StorageConnector.StorageHint;
import com.datumbox.framework.common.utilities.RandomGenerator;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SparseRealMatrix;

import java.util.Map;

/**
 * The MapRealMatrix class is a RealMatrix implementation which stores the data in a Map.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MapRealMatrix extends AbstractRealMatrix implements SparseRealMatrix {

    /**
     * The number of rows of the matrix.
     */
    private final int rowDimension;

    /**
     * The number of columns of the matrix.
     */
    private final int columnDimension;

    /**
     * The map that stores the internal data.
     */
    private final Map<Long, Double> entries;

    /**
     * The storage connector.
     */
    private final StorageConnector sc;

    /**
     * Protected constructor with the provided the dimension arguments.
     *
     * @param rowDimension
     * @param columnDimension
     * @throws NotStrictlyPositiveException
     */
    protected MapRealMatrix(int rowDimension, int columnDimension) throws NotStrictlyPositiveException {
        super(rowDimension, columnDimension);

        this.rowDimension = rowDimension;
        this.columnDimension = columnDimension;

        String storageName = "mrm" + RandomGenerator.getThreadLocalRandomUnseeded().nextLong();
        sc = MatrixDataframe.configuration.getStorageConfiguration().getStorageConnector(storageName);
        entries = sc.getBigMap("tmp_entries", Long.class, Double.class, MapType.HASHMAP, StorageHint.IN_DISK, false, true);
    }

    /**
     * When we perform matrix operations, we often lose the reference to the original matrix and we are unable to
     * close its storage. Even though the JVM will close the storage before shutdown, by adding a close method in the finalize
     * we ensure that if the object is gc, we will release the connection sooner.
     * @throws java.lang.Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            sc.close();
        }
        finally {
            super.finalize();
        }
    }

    /** {@inheritDoc} */
    @Override
    public RealMatrix createMatrix(int rowDimension, int columnDimension) throws NotStrictlyPositiveException {
        return new MapRealMatrix(rowDimension, columnDimension);
    }

    /** {@inheritDoc} */
    @Override
    public int getRowDimension() {
        return rowDimension;
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnDimension() {
        return columnDimension;
    }

    /** {@inheritDoc} */
    @Override
    public RealMatrix copy() {
        MapRealMatrix copy = new MapRealMatrix(rowDimension, columnDimension);
        copy.entries.putAll(entries);
        return copy;
    }

    /** {@inheritDoc} */
    @Override
    public double getEntry(int row, int column) throws OutOfRangeException {
        MatrixUtils.checkRowIndex(this, row);
        MatrixUtils.checkColumnIndex(this, column);
        return entries.getOrDefault(computeKey(row, column), 0.0);
    }

    /** {@inheritDoc} */
    @Override
    public void setEntry(int row, int column, double value) throws OutOfRangeException {
        MatrixUtils.checkRowIndex(this, row);
        MatrixUtils.checkColumnIndex(this, column);
        if(value == 0.0) {
            entries.remove(computeKey(row, column)); //if it is exactly 0.0 don't store it. Also make sure you remove any previous key.
        }
        else {
            entries.put(computeKey(row, column), value);
        }
    }

    /**
     * Compute the map key of the element of the matrix.
     *
     * @param row
     * @param column
     * @return
     */
    private long computeKey(int row, int column) {
        return (long)row * columnDimension + column;
    }
}
