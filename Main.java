import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class Main {
    
    public static void main(String[] args) {
        try {
            // Read from separate JSON files
            String secret1 = solveTestCaseFromFile("testcase1.json");
            String secret2 = solveTestCaseFromFile("testcase2.json");
            
            // Output only the secrets
            System.out.println(secret1);
            System.out.println(secret2);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String solveTestCaseFromFile(String filename) throws Exception {
        String jsonContent = readFile(filename);
        return solveTestCase(jsonContent);
    }
    
    private static String solveTestCase(String jsonContent) throws Exception {
        // Clean the JSON content
        jsonContent = jsonContent.replaceAll("\\s+", "").replace("\"", "");
        
        // Extract n and k values
        int n = extractValue(jsonContent, "n:");
        int k = extractValue(jsonContent, "k:");
        
        List<BigInteger> xValues = new ArrayList<>();
        List<BigInteger> yValues = new ArrayList<>();
        
        // Find all points by scanning for base and value patterns
        int pos = 0;
        while (true) {
            // Find the next base entry
            int basePos = jsonContent.indexOf("base:", pos);
            if (basePos == -1) break;
            
            // Find the corresponding x value (the key before this object)
            int objectStart = jsonContent.lastIndexOf("{", basePos);
            int commaBefore = jsonContent.lastIndexOf(",", basePos);
            int keyStart = Math.max(objectStart, commaBefore);
            
            if (keyStart == -1) break;
            
            // Extract the point key (x value)
            String pointKey = jsonContent.substring(keyStart + 1, basePos);
            pointKey = pointKey.replace(":", "").replace("{", "").replace(",", "").trim();
            
            if (pointKey.isEmpty() || !pointKey.matches("\\d+")) {
                pos = basePos + 1;
                continue;
            }
            
            // Extract base value
            int baseEnd = jsonContent.indexOf(",", basePos);
            if (baseEnd == -1) baseEnd = jsonContent.indexOf("}", basePos);
            String baseStr = jsonContent.substring(basePos + 5, baseEnd);
            
            // Find and extract value
            int valuePos = jsonContent.indexOf("value:", basePos);
            if (valuePos == -1) {
                pos = basePos + 1;
                continue;
            }
            
            int valueEnd = jsonContent.indexOf(",", valuePos);
            if (valueEnd == -1) valueEnd = jsonContent.indexOf("}", valuePos);
            String valueStr = jsonContent.substring(valuePos + 6, valueEnd);
            
            // Parse and add the point
            try {
                BigInteger x = new BigInteger(pointKey);
                BigInteger y = decodeValue(valueStr, baseStr);
                
                xValues.add(x);
                yValues.add(y);
            } catch (Exception e) {
                System.err.println("Error parsing point: x=" + pointKey + ", base=" + baseStr + ", value=" + valueStr);
            }
            
            pos = valuePos + 1;
        }
        
        BigInteger secret = lagrangeInterpolation(xValues, yValues, BigInteger.ZERO);
        return secret.toString();
    }
    
    private static int extractValue(String json, String key) {
        int pos = json.indexOf(key);
        if (pos == -1) return 0;
        
        int start = pos + key.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        
        if (end != -1) {
            String value = json.substring(start, end);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    private static BigInteger decodeValue(String valueStr, String baseStr) {
        int base = Integer.parseInt(baseStr);
        return new BigInteger(valueStr, base);
    }
    
    private static BigInteger lagrangeInterpolation(List<BigInteger> xValues, List<BigInteger> yValues, BigInteger x) {
        BigInteger result = BigInteger.ZERO;
        int n = xValues.size();
        
        for (int i = 0; i < n; i++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    numerator = numerator.multiply(x.subtract(xValues.get(j)));
                    denominator = denominator.multiply(xValues.get(i).subtract(xValues.get(j)));
                }
            }
            
            BigInteger term = yValues.get(i).multiply(numerator).divide(denominator);
            result = result.add(term);
        }
        
        return result;
    }
    
    private static String readFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }
}