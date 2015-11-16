package com.qantium.data.pairwise;

import java.util.*;

public class TestDataSet {

    private final Scenario scenario;
    private final IInventory inventory;
    private final List<int[]> testSets = new ArrayList();

    public List<int[]> getRawTestSets() {
        return testSets;
    }

    private final Random random = new Random(2);

    public TestDataSet(IInventory inventory, Scenario scenario) {
        this.inventory = inventory;
        this.scenario = scenario;
    }

    public void buildTestCases() {
        int poolSize = 1; // number of candidate testSet arrays to generate before picking one to add to testSets List

        while (inventory.getUnusedMolecules().size() > 0) { //keep iterating until all pairs are used
            // as long as there are unused pairs to account for

            int[][] candidateSets = new int[poolSize][scenario.getParameterSetCount()]; // holds candidate testSets

            for (int candidate = 0; candidate < poolSize; ++candidate) {

                int[] testSet = getSingleTestSet();
                candidateSets[candidate] = testSet;  // add candidate testSet to candidateSets array
            } // for each candidate testSet
            int[] bestTestSet = determineBestCandidateSet(candidateSets);

            testSets.add(bestTestSet); // Add the best candidate to the main testSets List
            inventory.updateAllCounts(bestTestSet);
        } //while loop from hell
    }

    public List<Map<Object, Object>> getTestSets() {
        List<int[]> testSetIndexes = getRawTestSets();
        List<Map<Object, Object>> completeDataSet = new ArrayList();

        for (int[] testSetIndex : testSetIndexes) {
            Map<Object, Object> singleTestSet = new LinkedHashMap();

            for (int j = 0; j < scenario.getParameterSetCount(); j++) {
                Object value = scenario.getParameterValues()
                        .get(testSetIndex[j]);
                singleTestSet.put(scenario.getParameterSet(scenario.getParameterPositions()[testSetIndex[j]]).getName(), value);
            }
            completeDataSet.add(singleTestSet);
        }
        return completeDataSet;
    }

    //It's hard to figure out how to break this up into smaller chunks--everything in inter-dependent
    protected int[] getSingleTestSet() {
        int[] bestMolecule = inventory.getBestMolecule();

        int firstPos = scenario.getParameterPositions()[bestMolecule[0]];  // position of first parameter set from best unused pair
        int secondPos = scenario.getParameterPositions()[bestMolecule[1]]; // position of second parameter set from best unused pair

        // place two parameter values from best unused pair into candidate testSet
        int[] testSet = new int[scenario.getParameterSetCount()]; // make an empty candidate testSet
        testSet[firstPos] = bestMolecule[0];
        testSet[secondPos] = bestMolecule[1];

        int[] ordering = getParameterOrdering(firstPos, secondPos);

        // for remaining parameter positions in candidate testSet, try each possible legal value, picking the one which captures the most unused pair
        for (int i = 2; i < scenario.getParameterSetCount(); i++) {
            int currPos = ordering[i];
            int[] possibleValues = scenario.getLegalValues()[currPos];

            int highestCount = 0;
            int bestJ = 0;
            for (int j = 0; j < possibleValues.length; j++) {
                int currentCount = 0;
                for (int p = 0; p < i; ++p) {
                    int[] candidatePair = new int[]{possibleValues[j], testSet[ordering[p]]};
                    if (inventory.getUnusedMoleculesSearch()[candidatePair[0]][candidatePair[1]] == 1
                            || inventory.getUnusedMoleculesSearch()[candidatePair[1]][candidatePair[0]] == 1) {
                        ++currentCount;
                    }
                }
                if (currentCount > highestCount) {
                    highestCount = currentCount;
                    bestJ = j;
                }
            }
            testSet[currPos] = possibleValues[bestJ];
        } // i -- each testSet position 

        return testSet;
    }

    protected int[] determineBestCandidateSet(int[][] candidateSets) {
        // Iterate through candidateSets to determine the best candidate
        random.setSeed(random.nextLong());
        int indexOfBestCandidate = random.nextInt(candidateSets.length); // pick a random index as best
        int mostPairsCaptured = inventory.numberMoleculesCaptured(candidateSets[indexOfBestCandidate]);

        // Determine "best" candidate to use
        for (int i = 0; i < candidateSets.length; ++i) {
            int pairsCaptured = inventory.numberMoleculesCaptured(candidateSets[i]);

            if (pairsCaptured > mostPairsCaptured) {
                mostPairsCaptured = pairsCaptured;
                indexOfBestCandidate = i;
            }
        }

        return candidateSets[indexOfBestCandidate];
    }

    protected int[] getParameterOrdering(int firstPos, int secondPos) {
        // generate a random order to fill parameter positions
        int[] ordering = new int[scenario.getLegalValues().length];
        for (int i = 0; i < scenario.getLegalValues().length; i++) { // initially all in order
            ordering[i] = i;
        }

        // put firstPos at ordering[0] && secondPos at ordering[1]
        ordering[0] = firstPos;
        ordering[firstPos] = 0;

        int t = ordering[1];
        ordering[1] = secondPos;
        ordering[secondPos] = t;

        // shuffle ordering[2] thru ordering[last]
        for (int i = 2; i < ordering.length; i++) { // Knuth shuffle. start at i=2 because want first two slots left alone
            int j = random.nextInt(ordering.length - i) + i;
            int temp = ordering[j];
            ordering[j] = ordering[i];
            ordering[i] = temp;
        }
        return ordering;
    }
}
