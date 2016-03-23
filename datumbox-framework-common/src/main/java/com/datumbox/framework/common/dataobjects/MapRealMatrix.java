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

import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SparseRealMatrix;

import java.util.Arrays;
import java.util.List;
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
    private final Map<List<Integer>, Double> entries;

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

        String dbName = "mrm_"+System.nanoTime();
        DatabaseConnector dbc = MatrixDataframe.conf.getDbConfig().getConnector(dbName);
        entries = dbc.getBigMap("tmp_entries", MapType.HASHMAP, StorageHint.IN_DISK, false, true);
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
        return entries.getOrDefault(Arrays.asList(row, column), 0.0);
    }

    /** {@inheritDoc} */
    @Override
    public void setEntry(int row, int column, double value) throws OutOfRangeException {
        MatrixUtils.checkRowIndex(this, row);
        MatrixUtils.checkColumnIndex(this, column);
        if(value == 0.0) {
            entries.remove(Arrays.asList(row, column)); //if it is exactly 0.0 don't store it. Also make sure you remove any previous key.
        }
        else {
            entries.put(Arrays.asList(row, column), value);
        }
    }
}
