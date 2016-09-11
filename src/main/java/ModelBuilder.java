/**
 * Created by Simone on 9/7/16.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.math3.stat.regression.*;

public class ModelBuilder {
    private String trainingSetFilename = "SmallBizDataset.txt";
    private TwitterDataSource twitterDataSource;
    private YelpDataSource yelpDataSource;
    private AlexaDataSource alexaDataSource;

    public ModelBuilder(TwitterDataSource twitterDataSource, YelpDataSource yelpDataSource, AlexaDataSource alexaDataSource) {
        this.twitterDataSource = twitterDataSource;
        this.yelpDataSource = yelpDataSource;
        this.alexaDataSource = alexaDataSource;
    }

    public void trainModel() {
        BusinessesAndScores businessesAndScores = readFile(trainingSetFilename);
        System.out.println(String.format("Training model on %d observations: ", businessesAndScores.getBusinesses().size()));
        RegressionInput regressionInput = getFeatureValues(businessesAndScores);
        double[] parameters = runRegression(regressionInput);
        System.out.println("Size of parameters array: " + parameters.length);
        printParametersToFile(parameters);
    }

    public void printParametersToFile(double[] parameters) {
        try {
            PrintWriter writer = new PrintWriter(ModelReader.getCoefficientsFilename(), "UTF-8");
            for (int i = 0; i < parameters.length; i++) {
                String paramName = CoefficientNames.PARAMETERS_LIST[i];
                double paramValue = parameters[i];
                writer.println(writeParameterToFile(paramName, paramValue));
            }
            writer.close();
            System.out.println("Coefficients written to file " + ModelReader.getCoefficientsFilename());
        } catch (FileNotFoundException e) {
            System.out.println("Invalid filename. Coefficients not written. " + e.getStackTrace());
        } catch (UnsupportedEncodingException e) {
            System.out.println("Invalid encoding type. Coefficients not written. " + e.getStackTrace());
        }
    }

    public String writeParameterToFile(String weightName, double weightValue) {
        return String.format("%s:%f", weightName, weightValue);
    }

    public double[] runRegression(RegressionInput regressionInput) {
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(regressionInput.getRegressand(), regressionInput.getRegressors());
        return regression.estimateRegressionParameters();
    }

    private RegressionInput getFeatureValues(BusinessesAndScores businessesAndScores) {
        int length = businessesAndScores.getRegressands().size();
        double[][] regressors = new double[length][];

        // For each business, calculate the raw feature values from each data source.
        for (int i = 0; i < length; i++) {
            List<Double> rawValues = new ArrayList<Double>();
            Business business = businessesAndScores.getBusinesses().get(i);
            double[] twitterScores = twitterDataSource.getRawScores(business);
            for (double twitterScore : twitterScores) {
                rawValues.add(twitterScore);
            }
            double[] yelpScores = yelpDataSource.getRawScores(business);
            for (double yelpScore : yelpScores) {
                rawValues.add(yelpScore);
            }
            double[] alexaScores = alexaDataSource.getRawScores(business);
            for (double alexaScore : alexaScores) {
                rawValues.add(alexaScore);
            }
            regressors[i] = doubleListToArray(rawValues);
            System.out.println(String.format("Feature values calculated for business # %d of %d:  %s",
                    i, length - 1, business.getBusinessName()));
        }

        double[] regressandArray = doubleListToArray(businessesAndScores.getRegressands());

        return new RegressionInput(regressandArray, regressors);
    }

    private BusinessesAndScores readFile(String filename) {
        File file = new File(filename);
        List<Business> businesses = new ArrayList<Business>();
        List<Double> regressands = new ArrayList<Double>();

        try {
            Scanner scanner = new Scanner(file);
            System.out.println(scanner.nextLine());
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] fieldsArray = line.split("\t");
                boolean hasMissingFields = false;
                // Clean up entries
                for (int i = 0; i < fieldsArray.length; i++) {
                    fieldsArray[i] = fieldsArray[i].replace("\"", "").trim();
                    if (fieldsArray[i].isEmpty()) {
                        hasMissingFields = true;
                        break;
                    }
                }
                if (!hasMissingFields) {
                    // CSV is in the form: Contact \t City, State \t Url \t Name of Firm \t Did not default
                    Business business = new Business(
                            fieldsArray[3],
                            fieldsArray[0],
                            fieldsArray[2],
                            fieldsArray[1]
                    );
                    businesses.add(business);
                    regressands.add(Double.parseDouble(fieldsArray[4]));
                }
            }
            scanner.close();
            return new BusinessesAndScores(businesses, regressands);
        } catch (FileNotFoundException e) {
            System.out.println(String.format("Invalid filename %s - try a new one.", filename));
            return new BusinessesAndScores(new ArrayList<Business>(), new ArrayList<Double>());
        }
    }

    private double[] doubleListToArray(List<Double> doubles) {
        double[] target = new double[doubles.size()];
        for (int i = 0; i < target.length; i++) {
            target[i] = doubles.get(i);
        }
        return target;
    }
}
