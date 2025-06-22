package app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class PageIndex implements Handler {
    public static final String URL = "/";

    public boolean compareChars(String s1, String s2, int pos) {
        return s1.charAt(pos) == s2.charAt(pos);
    }

    public String checkConflicts(HashMap<String, Relationship> relations) throws Exception {
        for (String s1 : relations.keySet()) {
            if (relations.get(s1) == null) {
                continue;
            }
            
            if (relations.get(s1).getNumChanges() > 1) {
                return "Error: Possible cell conflict(s) at cell " + s1 + "<br>";
            }
        }

        return "";
    }

    public String checkValidity(HashMap<String, Relationship> relations,
                                int numCategories, int numItems) {
        boolean success = true;
        int currNumTrues = 0;

        for (int i = numCategories - 1; i > 0; --i) {
            for (int j = 0; j < numItems; ++j) {
                for (int k = 0; k < i * numItems; ++k) {
                    int firstKey = (i == numCategories - 1) ? 1 : i + 2;
                    int secondKey = j + 1;
                    int thirdKey = (k / numItems) + 2;
                    int fourthKey = (k % numItems) + 1;
                    
                    String key = validateKey(firstKey + "." + secondKey, 
                                            thirdKey + "." + fourthKey);
                    
                    if (key.charAt(9) == '1') {
                        currNumTrues = 0;
                    }
                    currNumTrues += relations.get(key).getStatus().equalsIgnoreCase("True") ? 1 : 0;
                    
                    if ((currNumTrues != 1) && (key.charAt(9) == '4')) {
                        success = false;
                        break;
                    }
                }

                if (!success) {
                    break;
                }
            }

            if (!success) {
                break;
            }
        }

        return success ? "Success: Puzzle solved <br>" : "Error: Possible lack of clue information <br>";
    }

    public void crossEliminations (HashMap<String, Relationship> relations,
                                int numCategories, int numItems, int givenPos) {

        HashMap<String, String[]> subFieldStatuses = new HashMap<String, String[]>();
        String[] currSubField = new String[numItems];
        String hashMapKey;

        for (int i = numCategories - 1; i > 0; --i) {
            for (int j = 0; j < numItems; ++j) {
                for (int k = 0; k < i * numItems; ++k) {
                    int firstKey = (i == numCategories - 1) ? 1 : i + 2;
                    int secondKey = (givenPos == 9) ? (j + 1) : ((k % numItems) + 1);
                    int thirdKey = (k / numItems) + 2;
                    int fourthKey = (givenPos == 9) ? ((k % numItems) + 1) : (j + 1);
                    
                    String key = validateKey(firstKey + "." + secondKey, 
                                            thirdKey + "." + fourthKey);
                    currSubField[fourthKey - 1] = relations.get(key).getStatus();
                    
                    if (key.charAt(9) == '4') {
                        hashMapKey = key.substring(0, givenPos) + "x" + key.substring(givenPos + 1);
                        subFieldStatuses.put(hashMapKey, currSubField);
                    }
                }
            }
        }

        for (String s1 : subFieldStatuses.keySet()) {
            for (String s2 : subFieldStatuses.keySet()) {
                if (s1.equals(s2)) {
                    continue;
                }

                String[] array1 = subFieldStatuses.get(s1);
                String[] array2 = subFieldStatuses.get(s2);
                boolean[] array3 = new boolean[numItems];

                int numFalse = 0;
                String targetKey = "";

                for (int i = 0; i < numItems; ++i) {
                    array3[i] = !(array1[i].equalsIgnoreCase("FALSE") || array2[i].equalsIgnoreCase("FALSE"));
                    numFalse += array3[i] ? 0 : 1;
                }

                if (givenPos == 2) {
                    targetKey = validateKey(s1.substring(7), s2.substring(7));
                }
                else if (givenPos == 9) {
                    targetKey = validateKey(s1.substring(0, 3), s2.substring(0, 3));
                }

                if (numFalse == numItems) {
                    setTorF(relations, targetKey, "FALSE");
                }
            }
        }
    }

    public void equalItems(HashMap<String, Relationship> relations, String[] parameters, 
                            double loBound, double increment, int numItems, boolean equals) {
        double number = 0;
        if (isNumeric(parameters[2])) {
            number = Double.parseDouble(parameters[2]);
        }
        else {
            return;
        }
        
        int result = (int)((number - loBound) / increment);
        String key = validateKey("1." + (result + 1), parameters[1]);

        if (equals) {
            setTorF(relations, key, "TRUE");
        }
        else {
            setTorF(relations, key, "FALSE");
        }
    }
    
    public void fillSubFields (HashMap<String, Relationship> relations,
                            int numCategories, int numItems, int givenPos) {
        int numFalse = 0;
        int numTrue = 0;
        int numUnknown = 0;
        String lastUnknownKey = "";

        for (int i = numCategories - 1; i > 0; --i) {
            for (int j = 0; j < numItems; ++j) {
                for (int k = 0; k < i * numItems; ++k) {
                    int firstKey = (i == numCategories - 1) ? 1 : i + 2;
                    int secondKey = (givenPos == 9) ? (j + 1) : ((k % numItems) + 1);
                    int thirdKey = (k / numItems) + 2;
                    int fourthKey = (givenPos == 9) ? ((k % numItems) + 1) : (j + 1);
                    
                    String key = validateKey(firstKey + "." + secondKey, 
                                            thirdKey + "." + fourthKey);
                    
                    if (key.charAt(givenPos) == '1') {
                        numFalse = 0;
                        numTrue = 0;
                        numUnknown = 0;
                        lastUnknownKey = "";
                    }
                    
                    if (relations.get(key).getStatus().equalsIgnoreCase("FALSE")) {
                        numFalse++;
                    }
                    else if (relations.get(key).getStatus().equalsIgnoreCase("TRUE")) {
                        numTrue++;
                    }
                    else if (relations.get(key).getStatus().equalsIgnoreCase("Unknown")) {
                        numUnknown++;
                        lastUnknownKey = key;
                    }

                    if ((key.charAt(givenPos) == '4') &&
                        (numTrue == 0) &&
                        (numFalse == (numItems - 1)) &&
                        (numUnknown == 1)) {
                        setTorF(relations, lastUnknownKey, "TRUE");
                    }
                }
            }
        }
    }

    public String formGrid(HashMap<String, Relationship> relations,
                            int numCategories, int numItems) {
        String html = "<table>";
        
        for (int i = numCategories - 1; i > 0; --i) {
            for (int j = 0; j < numItems; ++j) {
                html += "<tr>";
                
                for (int k = 0; k < i * numItems; ++k) {
                    int firstKey = (i == numCategories - 1) ? 1 : i + 2;
                    int secondKey = j + 1;
                    int thirdKey = (k / numItems) + 2;
                    int fourthKey = (k % numItems) + 1;
                    
                    String key = validateKey(firstKey + "." + secondKey, 
                                            thirdKey + "." + fourthKey);
                    html += "<td>" + relations.get(key).getStatus() + "</td>";
                }
                
                html += "</tr>";
            }
        }   

        html += "</table> <br>";

        return html;
    }

    public HashMap<String, Relationship> formRelations(int numCategories, int numItems) {
        HashMap<String, Relationship> relations = new HashMap<String, Relationship>();

        for (int i = 0; i < numCategories; ++i) {
            for (int j = 0; j < numItems; ++j) {
                for (int k = 0; k < numCategories; ++k) {
                    for (int l = 0; l < numItems; ++l) {
                        if (i != k) {                            
                            String firstItem = (i + 1) + "." + (j + 1);
                            String secondItem = (k + 1) + "." + (l + 1);
                            
                            if (!relations.containsKey(firstItem + " vs " + secondItem)) {
                                relations.put(
                                validateKey(firstItem, secondItem), new Relationship("Unknown")
                                            );
                            }
                        }
                    }
                }        
            }
        }

        return relations;
    }
    
    public ArrayList<String> getClues (String cluesList) {
        Scanner scnr = new Scanner(cluesList);
        
        ArrayList<String> cluesIndex = new ArrayList<String>();
        while (scnr.hasNextLine()) {
            cluesIndex.add(scnr.nextLine());
        }
        
        scnr.close();

        return cluesIndex;
    }

    public String getPreliminaries() throws FileNotFoundException {
        String html = "";
        
        Scanner scnr = new Scanner(new FileInputStream("preliminaries.txt"));
        while (scnr.hasNextLine()) {
            html += scnr.nextLine();
        }

        scnr.close();
        return html;
    }

    public String getValues(HashMap<String, Relationship> oldOrNew) {
        String output = "";
        
        for (String s1 : oldOrNew.keySet()) {
            output += oldOrNew.get(s1).getStatus() + " ";
        }

        return output;
    }

    public static boolean isNumeric(String s1) {
        if (s1 == null) {
            return false;
        }

        try {
            double d1 = Double.parseDouble(s1);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }
    
    public boolean invalidInput (String s1, String s2, String s3, String s4) {       
        boolean invalidS1 = (s1 == null) || (s1.equals(""));
        boolean invalidS2 = (s2 == null) || (s2.equals(""));
        boolean invalidS3 = (s3 == null) || (s3.equals("")) || (Double.parseDouble(s3) == 0);
        boolean invalidS4 = (s4 == null) || (s4.equals(""));

        return invalidS1 || invalidS2 || invalidS3 || invalidS4;
    }

    public void lessOrMoreWithA1 (HashMap<String, Relationship> relations, String[] parameters, 
                                double increment, int numItems) {
        
        double number = 0;
        if (isNumeric(parameters[3])) {
            number = Double.parseDouble(parameters[3]);
        }
        else {
            return;
        }

        String direction = parameters[0];
        int offset = (int)(number / increment);
        
        for (int i = 1; i <= numItems; ++i) {    
            if ((offset <= 0) || (offset >= numItems)) {
                break;
            }

            String keyOne = "";
            String keyTwo = "";
            int keyTwoExpression = (i > offset) ? i - offset : numItems + 1 - i;

            if (direction.equalsIgnoreCase("LESS")) {
                keyOne = validateKey("1." + i, parameters[2]);
                keyTwo = validateKey("1." + keyTwoExpression, parameters[1]);
            }
            else if (direction.equalsIgnoreCase("MORE")) {
                keyOne = validateKey("1." + i, parameters[1]);
                keyTwo = validateKey("1." + keyTwoExpression, parameters[2]);
            }

            if ((relations.get(keyOne) == null) || (relations.get(keyTwo) == null)) {
                break;
            }

            if (i > offset) {
                if (relations.get(keyOne).getStatus().equalsIgnoreCase("TRUE")) {
                    setTorF(relations, keyTwo, "TRUE");
                    break;
                }
                else if (relations.get(keyTwo).getStatus().equalsIgnoreCase("TRUE")) {                    
                    setTorF(relations, keyOne, "TRUE");
                    break;
                }
            }
            else {
                if (relations.get(keyOne).getStatus().equalsIgnoreCase("FALSE") &&
                    relations.get(keyTwo).getStatus().equalsIgnoreCase("FALSE")) {
                    if (direction.equalsIgnoreCase("LESS")) {
                        keyOne = validateKey("1." + (++i), parameters[2]);;
                    }
                    else if (direction.equalsIgnoreCase("MORE")) {
                        keyTwoExpression--;
                        validateKey("1." + keyTwoExpression, parameters[2]);
                    }
                }
                System.out.println(keyOne + "; " + keyTwo);
                setTorF(relations, keyOne, "FALSE");
                setTorF(relations, keyTwo, "FALSE");
            }
        }

        setTorF(relations, parameters, "FALSE");
    }

    public void lessOrMoreNoA1 (HashMap<String, Relationship> relations, 
                                String[] parameters, int numItems) {
        String direction = parameters[0];
        String keyOne = "";
        String keyTwo = "";
        
        int loBound = 1;
        int numRepetitions = 1;
        boolean loBoundProtected = false;

        for (int i = 1; i <= numItems; ++i) {
            if (direction.equalsIgnoreCase("LESS")) {
                keyOne = validateKey("1." + i, parameters[1]);
                keyTwo = validateKey("1." + (numItems + 1 - i), parameters[2]);
            }
            else if (direction.equalsIgnoreCase("MORE")) {
                keyOne = validateKey("1." + i, parameters[2]);
                keyTwo = validateKey("1." + (numItems + 1 - i), parameters[1]);
            }

            if ((relations.get(keyOne) == null) || (relations.get(keyTwo) == null)) {
                break;
            }

            if (relations.get(keyOne).getStatus().equalsIgnoreCase("TRUE") ||
                relations.get(keyTwo).getStatus().equalsIgnoreCase("TRUE")) {
                numRepetitions = i;
                break;
            }
            else if (relations.get(keyOne).getStatus().equalsIgnoreCase("FALSE") &&
                    relations.get(keyTwo).getStatus().equalsIgnoreCase("FALSE")) {
                if (!loBoundProtected) {
                    System.out.println(keyOne + "; " + keyTwo);
                    
                    if (direction.equalsIgnoreCase("MORE")) {
                        numRepetitions--;
                    }
                    else if (direction.equalsIgnoreCase("LESS")) {
                        loBound = i;
                    }
                }
            }
            else {
                loBoundProtected = true;
            }
        }

        for (int i = loBound; i <= numRepetitions; ++i) {            
            if (direction.equalsIgnoreCase("LESS")) {
                keyOne = validateKey("1." + i, parameters[2]);
                keyTwo = validateKey("1." + (numItems + 1 - i), parameters[1]);
            }
            else if (direction.equalsIgnoreCase("MORE")) {
                keyOne = validateKey("1." + i, parameters[1]);
                keyTwo = validateKey("1." + (numItems + 1 - i), parameters[2]);
            }

            if ((relations.get(keyOne) == null) || (relations.get(keyTwo) == null)) {
                break;
            }

            if (relations.get(keyOne).getStatus().equalsIgnoreCase("Unknown")) {
                setTorF(relations, keyOne, "FALSE");
            }

            if (relations.get(keyTwo).getStatus().equalsIgnoreCase("Unknown")) {
                setTorF(relations, keyTwo, "FALSE");
            }
        }
        setTorF(relations, parameters, "FALSE");
    }

    public void multiElim (HashMap<String, Relationship> relations,
                                                    String[] parameters) {
        for (int i = 1; i < parameters.length; ++i) {
            for (int j = 1; j < parameters.length; ++j) {
                String targetKey = validateKey(parameters[i], parameters[j]);

                if (relations.containsKey(targetKey)) {
                    relations.get(targetKey).setStatus("FALSE");
                }
            }    
        }
    }

    public void notAllOf (HashMap<String, Relationship> relations, String[] parameters, 
                                                    int numItems, String type) {
        String targetAB = validateKey(parameters[1], parameters[2]);
        String targetAC = validateKey(parameters[1], parameters[3]);
        String targetBC = validateKey(parameters[2], parameters[3]);

        if (type.equalsIgnoreCase("EITHER")) {
            if (relations.get(targetAB).getStatus().equalsIgnoreCase("TRUE")) {
                setTorF(relations, targetAC, "FALSE");
            }
            else if (relations.get(targetAB).getStatus().equalsIgnoreCase("FALSE")) {
                setTorF(relations, targetAC, "TRUE");
            }

            if (relations.get(targetAC).getStatus().equalsIgnoreCase("TRUE")) {
                setTorF(relations, targetAB, "FALSE");
            }
            else if (relations.get(targetAC).getStatus().equalsIgnoreCase("FALSE")) {
                setTorF(relations, targetAB, "TRUE");
            }
        }   
        else if (type.equalsIgnoreCase("NEITHER")) {
            setTorF(relations, targetAB, "FALSE");
            setTorF(relations, targetAC, "FALSE");
        }

        setTorF(relations, targetBC, "FALSE");

        if (compareChars(parameters[2], parameters[3], 0)) {            
            int rowChar = parameters[2].charAt(0) - '0';

            for (int i = 1; i <= numItems; ++i) {
                String targetKey = validateKey(parameters[1], rowChar + "." + i);
                if (!(targetKey.equals(targetAB) || targetKey.equals(targetAC))) {
                    setTorF(relations, targetKey, "FALSE");
                }
            }
        }
    }

    public void setTorF (HashMap<String, Relationship> relations,
                                                    String[] parameters, String s1) {
        
        String targetKey = validateKey(parameters[1], parameters[2]);
        if (relations.containsKey(targetKey)) {
            for (String curr : relations.keySet()) {
                if (compareChars(curr, targetKey, 0)
                    && compareChars(curr, targetKey, 7)
                    && (compareChars(curr, targetKey, 2) || compareChars(curr, targetKey, 9))
                    && (!targetKey.equals(curr))
                    && s1.equalsIgnoreCase("TRUE")) {
                    relations.get(curr).setStatus("FALSE");
                }
            }

            relations.get(targetKey).setStatus(s1);
        }
    }

    public void setTorF (HashMap<String, Relationship> relations,
                                                    String targetKey, String s1) {
        if (relations.containsKey(targetKey)) {
            for (String curr : relations.keySet()) {
                if (compareChars(curr, targetKey, 0)
                    && compareChars(curr, targetKey, 7)
                    && (compareChars(curr, targetKey, 2) ^ compareChars(curr, targetKey, 9))
                    && s1.equalsIgnoreCase("TRUE")) {
                    relations.get(curr).setStatus("FALSE");
                }
            }

            relations.get(targetKey).setStatus(s1);
        }
    }

    public void solvePuzzle(HashMap<String, Relationship> relations, String cluesList, 
                                                    int numCategories, int numItems, double loBound, double increment) {
        ArrayList<String> cluesIndex = getClues(cluesList);
        for (String s1 : cluesIndex) {
            String[] parameters = s1.split(" ");
            parameters[0] = parameters[0].toUpperCase();
            
            switch (parameters[0]) {
                case "TRUE":
                    if (parameters.length == 3) {
                        setTorF(relations, parameters, "TRUE");
                    }
                    break;

                case "FALSE":
                    if (parameters.length == 3) {
                        setTorF(relations, parameters, "FALSE");
                    }
                    break;

                case "DIFF":
                    if (parameters.length >= 2) {
                        multiElim(relations, parameters);
                    }
                    break;

                case "EITHER":
                    if (parameters.length == 4) {
                        notAllOf(relations, parameters, numItems, "EITHER");
                    }
                    break;

                case "NEITHER":
                    if (parameters.length == 4) {
                        notAllOf(relations, parameters, numItems, "NEITHER");
                    }
                    break;

                case "LESS":
                case "MORE":
                    if (parameters.length == 4) {
                        lessOrMoreWithA1(relations, parameters, increment, numItems);
                    }
                    else if (parameters.length == 3) {
                        lessOrMoreNoA1(relations, parameters, numItems);
                    } 
                    break;

                case "EQUALS":
                    if (parameters.length == 3) {
                        equalItems(relations, parameters, loBound, increment, numItems, true);
                    }
                    break;

                case "NOTEQUALS":
                    if (parameters.length == 3) {
                        equalItems(relations, parameters, loBound, increment, numItems, false);
                    }
                    break;
                
                case "ALIGN":
                    if (parameters.length == 5) {
                        unalignedPairs(relations, parameters, numItems);
                    }
                    break;

                default:
                    break;
            }
        }

        fillSubFields(relations, numCategories, numItems, 2);
        fillSubFields(relations, numCategories, numItems, 9);
        
        transpositionsNorthWest(relations, numCategories, numItems);
        transpositionsNorthEast(relations, numCategories, numItems);
        transpositionsSouthWest(relations, numCategories, numItems);

        crossEliminations(relations, numCategories, numItems, 2);
        crossEliminations(relations, numCategories, numItems, 9);
    }

    public void transpositionsNorthWest (HashMap<String, Relationship> relations,
                                                                int numCategories, int numItems) {
        for (String s1 : relations.keySet()) {
            if (!relations.get(s1).getStatus().equalsIgnoreCase("TRUE")) {
                continue;
            }

            int a = s1.charAt(0) - '0';
            int b = s1.charAt(2) - '0';
            int c = s1.charAt(7) - '0';
            int d = s1.charAt(9) - '0';

            boolean threeCategories = (numCategories == 3) && (a == 1) && (c == 2);
            boolean fourCategories12 = (numCategories == 4) && (a == 1) && (c == 2);
            boolean fourCategories13 = (numCategories == 4) && (a == 1) && (c == 3);
            boolean fourCategories42 = (numCategories == 4) && (a == 4) && (c == 2);

            if (threeCategories || fourCategories12 || fourCategories13 || fourCategories42) {
                int hiBound = (fourCategories12 || fourCategories13) ? 4 : 3;
                
                for (int i = 3; i <= hiBound; ++i) {
                    for (int j = 1; j <= numItems; ++j) {
                        String key1 = validateKey(a + "." + b, i + "." + j);
                        String key2 = validateKey(key1.substring(7), c + "." + d);

                        String status1 = relations.get(key1).getStatus();
                        String status2 = relations.get(key2).getStatus();

                        if (!status1.equalsIgnoreCase("Unknown")) {
                            setTorF(relations, key2, status1);
                        }
                        else if (!status2.equalsIgnoreCase("Unknown")) {
                            setTorF(relations, key1, status2);
                        }
                    }
                }
            }
        }
    }

    public void transpositionsNorthEast (HashMap<String, Relationship> relations,
                                                                int numCategories, int numItems) {
        for (String s1 : relations.keySet()) {
            if (!relations.get(s1).getStatus().equalsIgnoreCase("TRUE")) {
                continue;
            }

            int a = s1.charAt(0) - '0';
            int b = s1.charAt(2) - '0';
            int c = s1.charAt(7) - '0';
            int d = s1.charAt(9) - '0';

            boolean threeCategories = (numCategories == 3) && (a == 1) && (c == 3);
            boolean fourCategories13 = (numCategories == 4) && (a == 1) && (c == 3);
            boolean fourCategories14 = (numCategories == 4) && (a == 1) && (c == 4);
            boolean fourCategories43 = (numCategories == 4) && (a == 4) && (c == 3);

            if (threeCategories || fourCategories13 || fourCategories14 || fourCategories43) {
                int hiBound = fourCategories14 ? 3 : 2;

                for (int i = 2; i <= hiBound; ++i) {
                    for (int j = 1; j <= numItems; ++j) {
                        String keyInitial = validateKey(a + "." + b, c + "." + d);
                        String keyPivot = validateKey(a + "." + b, i + "." + j);
                        String keyNew = validateKey(keyPivot.substring(7), c + "." + d);

                        for (int k = 0; k < 2; ++k) {
                            String tempKey = keyNew;
                            keyNew = keyPivot;
                            keyPivot = tempKey;

                            String statusPivot = relations.get(keyPivot).getStatus();
                            if (!statusPivot.equalsIgnoreCase("Unknown")) {
                                setTorF(relations, keyNew, statusPivot);
                            }
                        }
                    }
                }
            }
        }
    }

    public void transpositionsSouthWest (HashMap<String, Relationship> relations,
                                                                int numCategories, int numItems) {
        for (String s1 : relations.keySet()) {
            if (!relations.get(s1).getStatus().equalsIgnoreCase("TRUE")) {
                continue;
            }

            int a = s1.charAt(0) - '0';
            int b = s1.charAt(2) - '0';
            int c = s1.charAt(7) - '0';
            int d = s1.charAt(9) - '0';

            boolean threeCategories = (numCategories == 3) && (a == 3) && (c == 2);
            boolean fourCategories32 = (numCategories == 4) && (a == 3) && (c == 2);
            boolean fourCategories42 = (numCategories == 4) && (a == 4) && (c == 2);
            boolean fourCategories43 = (numCategories == 4) && (a == 4) && (c == 3);

            if (threeCategories || fourCategories32 || fourCategories42 || fourCategories43) {
                int hiBound = (fourCategories42 || fourCategories43) ? 4 : 1;

                for (int i = 1; i <= hiBound; ++i) {
                    for (int j = 1; j <= numItems; ++j) {
                        if ((i > 1) && (i < 4)) {
                            continue;
                        }

                        String keyInitial = validateKey(a + "." + b, c + "." + d);
                        String keyPivot = validateKey(a + "." + b, i + "." + j);
                        String keyNew = validateKey(keyPivot.substring(0, 3),
                                                    keyInitial.substring(7));
                        
                        for (int k = 0; k < 2; ++k) {
                            String tempKey = keyNew;
                            keyNew = keyPivot;
                            keyPivot = tempKey;

                            String statusPivot = relations.get(keyPivot).getStatus();
                            if (!statusPivot.equalsIgnoreCase("Unknown")) {
                                setTorF(relations, keyNew, statusPivot);
                            }
                        }
                    }
                }
            }
        }
    }

    public void unalignedPairs (HashMap<String, Relationship> relations,
                                                        String[] parameters, int numItems) {
        String targetAC = validateKey(parameters[1], parameters[3]);
        String targetAD = validateKey(parameters[1], parameters[4]);
        String targetBC = validateKey(parameters[2], parameters[3]);
        String targetBD = validateKey(parameters[2], parameters[4]);

        final String[] STATUSES = {"TRUE", "FALSE"};
        if (relations.get(targetAC).getStatus().equals(STATUSES[0])) {
            setTorF(relations, targetAD, STATUSES[1]);
            setTorF(relations, targetBC, STATUSES[1]);
            setTorF(relations, targetBD, STATUSES[0]);
        }
        else if (relations.get(targetAC).getStatus().equals(STATUSES[1])) {
            setTorF(relations, targetAD, STATUSES[0]);
            setTorF(relations, targetBC, STATUSES[0]);
            setTorF(relations, targetBD, STATUSES[1]);
        }
        else if (relations.get(targetAD).getStatus().equals(STATUSES[0])) {
            setTorF(relations, targetAC, STATUSES[1]);
            setTorF(relations, targetBC, STATUSES[0]);
            setTorF(relations, targetBD, STATUSES[1]);
        }
        else if (relations.get(targetAD).getStatus().equals(STATUSES[1])) {
            setTorF(relations, targetAC, STATUSES[0]);
            setTorF(relations, targetBC, STATUSES[1]);
            setTorF(relations, targetBD, STATUSES[0]);
        }
        else if (relations.get(targetBC).getStatus().equals(STATUSES[0])) {
            setTorF(relations, targetAC, STATUSES[1]);
            setTorF(relations, targetAD, STATUSES[0]);
            setTorF(relations, targetBD, STATUSES[1]);
        }
        else if (relations.get(targetBC).getStatus().equals(STATUSES[1])) {
            setTorF(relations, targetAC, STATUSES[0]);
            setTorF(relations, targetAD, STATUSES[1]);
            setTorF(relations, targetBD, STATUSES[0]);
        }
        else if (relations.get(targetBD).getStatus().equals(STATUSES[0])) {
            setTorF(relations, targetAC, STATUSES[1]);
            setTorF(relations, targetAD, STATUSES[0]);
            setTorF(relations, targetBC, STATUSES[0]);
        }
        else if (relations.get(targetBD).getStatus().equals(STATUSES[1])) {
            setTorF(relations, targetAC, STATUSES[0]);
            setTorF(relations, targetAD, STATUSES[1]);
            setTorF(relations, targetBC, STATUSES[1]);
        }

        setTorF(relations, validateKey(parameters[1], parameters[2]), "FALSE");
        setTorF(relations, validateKey(parameters[3], parameters[4]), "FALSE");

        boolean parameters12Category1 = compareChars(parameters[1], parameters[2], 0);
        boolean parameters34Category1 = compareChars(parameters[3], parameters[4], 0);

        if (parameters12Category1 || parameters34Category1) {            
            for (int i = 1; i <= numItems; ++i) {
                String keyOne = validateKey("1." + i, parameters12Category1 ? parameters[3]
                                                                            : parameters[1]);
                String keyTwo = validateKey("1." + i, parameters12Category1 ? parameters[4]
                                                                            : parameters[2]);

                if (unalignedPairConflict(keyOne, keyTwo, targetAC, targetAD, targetBC, targetBD)) {
                    continue;
                }

                if ((relations.get(keyOne) == null) || (relations.get(keyTwo) == null)) {
                    break;
                }   

                if (relations.get(keyOne).getStatus().equalsIgnoreCase("Unknown")) {
                    setTorF(relations, keyOne, "FALSE");
                }

                if (relations.get(keyTwo).getStatus().equalsIgnoreCase("Unknown")) {
                    setTorF(relations, keyTwo, "FALSE");
                }
            }
        }
    }

    public boolean unalignedPairConflict(String source1, String source2, String s1,
                                        String s2, String s3, String s4) {
        final String[] SOURCES = {source1, source2};
        final String[] TESTS = {s1, s2, s3, s4};

        for (String a : SOURCES) {
            for (String b : TESTS) {
                if (a.equals(b)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public String validateKey(String s1, String s2) {
        boolean twoOrOne = (s1.charAt(0) == '2') || (s2.charAt(0) == '1');
        boolean threeBeforeFour = (s1.charAt(0) == '3') && (s2.charAt(0) == '4');
        
        return (twoOrOne || threeBeforeFour) ? (s2 + " vs " + s1) : (s1 + " vs " + s2);
    }

    @Override
    public void handle(Context context) throws Exception {
        String html = getPreliminaries();
        
        String gridSize = context.formParam("gridSize");
        String loBoundString = context.formParam("loBound");
        String incrementString = context.formParam("increment");
        String cluesList = context.formParam("cluesList");

        if (!(invalidInput(gridSize, loBoundString, incrementString, cluesList) ||
            incrementString.equals("0"))) {
            
            int numCategories = Integer.parseInt(String.valueOf(gridSize.charAt(0)));
            int numItems = Integer.parseInt(String.valueOf(gridSize.charAt(2)));
            double loBound = Double.parseDouble(loBoundString);
            double increment = Double.parseDouble(incrementString);

            HashMap<String, Relationship> relations = formRelations(numCategories, numItems);
            final int MAX_ITERATIONS = 5; // If modifying, reset to 10000 afterwards

            for (int i = 0; i < MAX_ITERATIONS; ++i) {
                solvePuzzle(relations, cluesList, numCategories, numItems, loBound, increment);

                if (checkValidity(relations, numCategories, numItems).contains("Success") ||
                    checkConflicts(relations).contains("Error")) {
                    break;
                }
            }
            
            html += formGrid(relations, numCategories, numItems);
            html += checkConflicts(relations);
            html += checkValidity(relations, numCategories, numItems);
        }
        else {
            html += "Error: You have missing and/or invalid fields";
        }

        html += "</body>" + "</html>";
        context.html(html);
    }
}    