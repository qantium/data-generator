package com.qantium.pairwise;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class PairwiseInventoryFactory {

    public static IInventory generateMatrixInventory(Object[][] parameters) {
        return generateTableInventory(null, parameters);
    }

    public static IInventory generateRotatedMatrixInventory(Object[][] data) {
        Object[][] table = getRotatedTable(data);
        return generateMatrixInventory(table);
    }

    public static IInventory generateTableInventory(Object[][] table) {

        if (table.length < 2) {
            throw new IllegalArgumentException("Table with data: " + Arrays.toString(table) + "  must have at least two strings!");
        }

        Object[] names = table[0];
        Object[][] parameters = Arrays.copyOfRange(table, 1, table.length);

        return generateTableInventory(names, parameters);
    }

    public static IInventory generateRotatedTableInventory(Object[][] data) {
        Object[][] table = getRotatedTable(data);
        return generateTableInventory(table);
    }

    public static IInventory generateTableInventory(Object[] names, Object[][] parameters) {

        if (ArrayUtils.isEmpty(parameters)) {
            throw new IllegalArgumentException("Parameters table must have at least one string!");
        }

        IInventory inventory = new PairwiseInventory();
        Scenario scenario = generateScenario(names, parameters);
        inventory.setScenario(scenario);
        inventory.buildMolecules();
        return inventory;
    }

    public static IInventory generateRotatedTableInventory(Object[] names, Object[][] data) {
        Object[][] table = getRotatedTable(data);
        return generateTableInventory(names, table);
    }

    public static Object[][] getRotatedTable(Object[][] data) {
        int maxParameterCount = 0;

        for (Object[] parameters : data) {

            if (parameters.length > maxParameterCount) {
                maxParameterCount = parameters.length;
            }
        }

        Object[][] table = new Object[maxParameterCount][];

        for (int i = 0; i < maxParameterCount; i++) {
            List row = new ArrayList();

            for (int j = 0; j < data.length; j++) {

                Object[] parameters = data[j];

                if (i < parameters.length) {
                    row.add(parameters[i]);
                }
            }
            table[i] = row.toArray();
        }
        return table;
    }

    /**
     * Parses a String representing the contents of the Scenario, and returns
     * the Scenario
     *
     * @param contents The contents of the Scenario you're testing
     * @return the Scenario
     */
    public static IInventory generateParameterInventory(String contents) {
        IInventory inventory = new PairwiseInventory();
        Scenario scenario = generateScenario(contents);
        inventory.setScenario(scenario);
        inventory.buildMolecules();
        return inventory;
    }

    public static Scenario generateScenario(String contents) {
        Scenario scenario = new Scenario();

        for (String line : StringUtils.split(contents, System.getProperty("line.separator"))) {
            scenario.addParameterSet(processOneLine(line));
        }
        return scenario;
    }

    public static Scenario generateScenario(Object[] names, Object[][] parameters) {

        int parametersCount = 0;

        for (Object[] line : parameters) {

            if (parametersCount < line.length) {
                parametersCount = line.length;
            }
        }

        int index;

        if (names != null) {
            index = names.length;
        } else {
            names = new Object[parametersCount];
            index = 0;
        }

        if (names.length > parametersCount) {
            throw new IllegalArgumentException("List of names: " + Arrays.toString(names) + "  must not be larger than list of parameters!");

        }

        if (index < parametersCount) {

            if (index != 0) {
                Object[] newNames = new Object[parametersCount];

                for (int i = 0; i < index; i++) {
                    newNames[i] = names[i];
                }
                names = newNames;
            }

            while (index < parametersCount) {
                names[index] = "[" + (index++) + "]";
            }
        }

        Scenario scenario = new Scenario();

        for (int colIndex = 0; colIndex < parametersCount; colIndex++) {
            String name = names[colIndex].toString();
            List parametersSet = new ArrayList();

            for (int rowIndex = 0; rowIndex < parameters.length; rowIndex++) {

                Object[] row = parameters[rowIndex];
                Object parameter;

                if (colIndex < row.length) {
                    parameter = row[colIndex];
                    parametersSet.add(parameter);
                }
            }
            scenario.addParameterSet(process(name, parametersSet));
        }
        return scenario;
    }

    /**
     * Processes a single line of inputs
     *
     * @param line One line, containing one parameter space (e.g. "Title:
     * Value1, Value2, Value3")
     * @return The ParameterSet representing the line
     */
    public static ParameterSet<String> processOneLine(String line) {
        String[] lineTokens = line.split(":", 2);
        List<String> strValues = splitAndTrim(",", lineTokens[1]);
        ParameterSet<String> parameterSet = new ParameterSet(strValues);
        parameterSet.setName(lineTokens[0]);
        return parameterSet;
    }

    public static ParameterSet process(String name, List parameters) {
        ParameterSet parameterSet = new ParameterSet(parameters);
        parameterSet.setName(name);
        return parameterSet;
    }

    public static IInventory generateParameterInventory(InputStream stream) throws IOException {
        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(isr);

        Scenario scenario = new Scenario();
        String line;
        while ((line = br.readLine()) != null) {
            scenario.addParameterSet(processOneLine(line));
        }

        IInventory inventory = new PairwiseInventory();
        inventory.setScenario(scenario);
        inventory.buildMolecules();
        return inventory;
    }

    private static List<String> splitAndTrim(String regex, String lineTokens) {
        String[] rawTokens = lineTokens.split(regex);
        String[] processedTokens = new String[rawTokens.length];
        for (int i = 0; i < rawTokens.length; i++) {
            processedTokens[i] = StringUtils.trim(rawTokens[i]);
        }
        return Arrays.asList(processedTokens);
    }
}
