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

import com.qantium.handlers.DataHandler;
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

    public Data normalize() {
        int columnsCount = 0;

        for (Object[] row : data) {

            if (row.length > columnsCount) {
                columnsCount = row.length;
            }
        }

        Object[][] normalizedData = new Object[data.length][];

        for (int i = 0; i < normalizedData.length; i++) {

            List row = new ArrayList();
            row.addAll(Arrays.asList(data[i]));

            for (int j = 0; j < columnsCount - row.size(); j++) {
                row.add(null);
            }
            normalizedData[i] = row.toArray();
        }

        return copy(normalizedData);
    }

    public Data handleBy(DataHandler... handlers) {

        List<DataHandler> handlerList = new ArrayList();
        handlerList.addAll(Arrays.asList(handlers));

        if (ArrayUtils.isEmpty(handlers)) {
            return this;
        } else {
            int handlersCount = handlers.length;
            int dataColumnsCount = data[0].length;

            if (handlersCount > dataColumnsCount) {
                throw new IllegalArgumentException("Count of handlers must be equal or less than count of data columns!\n"
                        + "Count of data columns: " + dataColumnsCount + "\n"
                        + "Count of handlers: " + handlersCount + "\n");
            }

            if (handlersCount < dataColumnsCount) {
                DataHandler lastHandle = handlers[handlers.length - 1];

                for (int i = 0; i < dataColumnsCount - handlersCount; i++) {
                    handlerList.add(lastHandle);
                }
            }

            Object[][] handledData = new Object[data.length][];

            for (int i = 0; i < data.length; i++) {

                List handledRow = new ArrayList();

                for (int j = 0; j < dataColumnsCount; j++) {
                    DataHandler handler = handlerList.get(j);
                    Object[] handledCell = handler.handle(data[i][j]);
                    handledRow.addAll(Arrays.asList(handledCell));
                }

                handledData[i] = handledRow.toArray();
            }

            return copy(handledData);
        }
    }

    protected Data copy(Object[][] data) {
        return new Data(data)
                .withHeader(withHeader)
                .withTabulation(tabulation)
                .replaceNullBy(nullValue);
    }

    public Data withTabulation(boolean tabulation) {
        this.tabulation = tabulation;

        if (tabulation && tabulations == null) {
            tabulations = new int[data[0].length];

            for (int i = 0; i < data.length; i++) {

                for (int j = 0; j < data[i].length; j++) {
                    Object cell = data[i][j];

                    if (cell != null) {
                        int itemLength = data[i][j].toString().length();

                        if (itemLength > tabulations[j]) {
                            tabulations[j] = itemLength;
                        }
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

        if (this.withHeader != withHeader) {

            if (!withHeader && header == null) {
                header = data[0];
                data = ArrayUtils.remove(data, 0);
            } else if (withHeader && header != null) {
                Object[][] newData = new Object[data.length + 1][];
                newData[0] = header;

                for (int i = 0; i < data.length; i++) {
                    newData[i + 1] = data[i];
                }
                data = newData;
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
