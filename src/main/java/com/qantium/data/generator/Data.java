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
package com.qantium.data.generator;

import com.qantium.data.parcers.DataParcer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author A.Solyankin
 */
public class Data {

    private Object[][] data;
    private boolean withHeader = true;
    private Object[] header;
    private Object nullValue = "";
    private boolean tabulation;
    private int[] tabulations;

    public Object[][] get() {
        return data;
    }
    
    public Data(Object[][] data) {

        if (ArrayUtils.isEmpty(data)) {
            throw new IllegalArgumentException("Data must have at least one string!");
        }
        
        this.data = data;
    }

    public Data parceBy(DataParcer... parcers) {

        List<DataParcer> parcersList = new ArrayList();

        if (ArrayUtils.isEmpty(parcers)) {
            return this;
        } else {

            int parcersCount = parcers.length;
            int dataColumnsCount = data[0].length;

            if (parcersCount > dataColumnsCount) {
                throw new IllegalArgumentException("Count of parcers must be equal or less than count of data columns!\n"
                        + "Count of data columns: " + dataColumnsCount + "\n"
                        + "Count of parcers: " + parcersCount + "\n");
            }

            for (DataParcer parcer : parcers) {
                parcersList.add(parcer);
            }

            if (parcersCount < dataColumnsCount) {
                DataParcer lastParcer = parcers[parcers.length - 1];

                for (int i = 0; i < dataColumnsCount - parcersCount; i++) {
                    parcersList.add(lastParcer);
                }
            }
            
            Object[][] parcedData = new Object[data.length][];
            
            for(int i = 0; i < data.length; i++) {
                
                List parcedRow = new ArrayList();
                
                for (int j = 0; j < dataColumnsCount; j++) {
                    DataParcer parcer = parcersList.get(j);
                    Object[] parcedCell = parcer.parce(data[i][j]);
                    parcedRow.addAll(Arrays.asList(parcedCell));
                }
                
                parcedData[i] = parcedRow.toArray();
            }
            
            return new Data(parcedData)
                    .withHeader(withHeader)
                    .withTabulation(tabulation)
                    .replaceNullBy(nullValue);
        }
    }
    
    public Data withTabulation(boolean tabulation) {
        this.tabulation = tabulation;

        if (tabulation && tabulations == null) {
            tabulations = new int[data[0].length];
            
            for (int i = 0; i < data.length; i++) {

                for (int j = 0; j < data[i].length; j++) {

                    int itemLength = data[i][j].toString().length();

                    if (itemLength > tabulations[j]) {
                        tabulations[j] = itemLength;
                    }
                }
            }
        }

        return this;
    }

    public boolean withTabulation() {
        return tabulation;
    }

    public boolean withHeader() {
        return withHeader;
    }

    public Data replaceNullBy(Object value) {
        nullValue = value;
        return this;
    }

    public Data withHeader(boolean withHeader) {
        
        if(this.withHeader != withHeader) {
            
            if(!withHeader && header == null) {
                header = data[0];
                data = ArrayUtils.remove(data, 0);
            } else if(withHeader && header != null) {
                ArrayUtils.reverse(data);
                data = ArrayUtils.add(data, header);
                ArrayUtils.reverse(data);
            }
            
            this.withHeader = withHeader;
        }
        return this;
    }

    public File toCSV(File file) throws IOException {
        return toCSV(file, "|");
    }

    public File toCSV(File file, String delimiter) throws IOException {
        Files.write(file.toPath(), toCSV(delimiter).getBytes());
        return file;
    }

    public String toCSV() {
        return toCSV("|");
    }

    public String toCSV(String delimiter) {

        StringBuilder table = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");

        for (int i = 0; i < data.length; i++) {

            Object[] row = data[i];

            for (int j = 0; j < row.length; j++) {

                Object item = row[j];

                if (item == null) {
                    item = nullValue;
                }

                if (withTabulation()) {
                    table.append(String.format("%" + tabulations[j] + "s", item.toString()));
                } else {
                    table.append(item);
                }

                if (j != row.length - 1) {
                    table.append(delimiter);
                }
            }

            if (i != data.length - 1) {
                table.append(lineSeparator);
            }
        }

        return table.toString();
    }

    public File toHTML(File file) throws IOException {
        Files.write(file.toPath(), toHTML().getBytes());
        return file;
    }

    public String toHTML() {

        StringBuilder table = new StringBuilder();
        table.append("<table border='1'>");

        int index = 0;

        if (withHeader()) {
            table.append(wrap("th", data[index++]));
        }

        for (; index < data.length; index++) {
            table.append(wrap("td", data[index]));
        }
        table.append("</table>");
        return table.toString();
    }

    protected StringBuilder wrap(String tag, Object[] row) {
        StringBuilder html = new StringBuilder();
        html.append("<tr>");

        for (Object cell : row) {

            html.append("<").append(tag).append(">");

            if (cell != null) {
                html.append(cell);
            } else {
                html.append(nullValue);
            }
            html.append("</").append(tag).append(">");
        }
        html.append("</tr>");
        return html;
    }

    @Override
    public String toString() {
        return toCSV();
    }
}
