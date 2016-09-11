import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Simone on 9/10/16.
 */
public class ModelReader {
    private static String coefficientsFilename = "coefficients.txt";

    public static String getCoefficientsFilename() {
        return coefficientsFilename;
    }

    public static double readParameterFromFile(String weightName) {
        File file = new File(getCoefficientsFilename());
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] nameAndValue = line.split(":");
                if (nameAndValue[0].equals(weightName)) {
                    return Double.parseDouble(nameAndValue[1]);
                }
            }
            scanner.close();
            System.out.println(String.format("Weight %s was not found in file %s", weightName, getCoefficientsFilename()));
        } catch (FileNotFoundException e) {
            System.out.println(String.format("Invalid filename %s. Perhaps you have not trained the model yet.", getCoefficientsFilename()));
        }
        return 0.0;
    }

    public static double getTotalScore(double twitterScore, double yelpScore, double alexaScore) {
        double intercept = readParameterFromFile(CoefficientNames.INTERCEPT);
        double totalScore = intercept + twitterScore + yelpScore + alexaScore;
        // Output must be in the range [0, 1].
        if (totalScore >= 0 && totalScore <= 1) {
            return totalScore;
        }
        if (totalScore < 0) {
            return 0;
        }
        return 1;
    }
}
