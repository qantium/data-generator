/*
 * Copyright 2015 A.Solyankin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qantium.data;

import com.qantium.pairwise.IInventory;
import com.qantium.pairwise.PairwiseInventoryFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author A.Solyankin
 */
public class DataGenerator {

    public static Data generatePairwise(IInventory inventory) {
        List<Map<Object, Object>> dataSets = inventory.getTestDataSet().getTestSets();
        Object[][] table = new Object[dataSets.size() + 1][];
        Map<Object, Object> firstDataSet = dataSets.get(0);
        int index = 0;
        Object[] header = new String[firstDataSet.size()];

        for (Object key : firstDataSet.keySet()) {
            header[index++] = key;
        }
        table[0] = header;
        index = 1;

        for (Map<Object, Object> dataSet : dataSets) {

            Object[] row = new Object[dataSet.size()];

            for (int i = 0; i < header.length; i++) {
                row[i] = dataSet.get(header[i]);
            }

            table[index++] = row;
        }
        return new Data(table);
    }

    public static Data generatePairwiseMatrix(Object[][] data) {
        IInventory inventory = PairwiseInventoryFactory.generateMatrixInventory(data);
        return generatePairwise(inventory).withHeader(false);
    }

    public static Data generatePairwiseRotatedMatrix(Object[][] data) {
        IInventory inventory = PairwiseInventoryFactory.generateRotatedMatrixInventory(data);
        return generatePairwise(inventory).withHeader(false);
    }

    public static Data geberatePairwiseTable(InputStream data) throws IOException {
        IInventory inventory = PairwiseInventoryFactory.generateParameterInventory(data);
        return generatePairwise(inventory);
    }

    public static Data generatePairwiseTable(String data) {
        IInventory inventory = PairwiseInventoryFactory.generateParameterInventory(data);
        return generatePairwise(inventory);
    }

    public static Data generatePairwiseTable(Object[][] data) {
        IInventory inventory = PairwiseInventoryFactory.generateTableInventory(data);
        return generatePairwise(inventory);
    }

    public static Data generatePairwiseTable(Object[] names, Object[][] data) {
        IInventory inventory = PairwiseInventoryFactory.generateTableInventory(names, data);
        return generatePairwise(inventory);
    }

    public static Data generatePairwiseRotatedTable(Object[][] data) {
        IInventory inventory = PairwiseInventoryFactory.generateRotatedTableInventory(data);
        return generatePairwise(inventory);
    }

    public static Data generatePairwiseRotatedTable(Object[] names, Object[][] data) {
        IInventory inventory = PairwiseInventoryFactory.generateRotatedTableInventory(names, data);
        return generatePairwise(inventory);
    }

    public static Data generatePairwiseTable(File csv) throws IOException {
        return DataGenerator.generatePairwiseTable(csv, "\\|");
    }

    public static Data generatePairwiseTable(File csv, String delimiter) throws IOException {

        List<String[]> data = new ArrayList();

        for (String line : Files.readAllLines(csv.toPath())) {
            data.add(line.split(delimiter));
        }

        String[][] table = new String[data.size()][];

        for (int i = 0; i < data.size(); i++) {
            table[i] = data.get(i);
        }

        return DataGenerator.generatePairwiseTable(table);
    }

}
