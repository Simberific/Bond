import java.util.logging.Logger;

/**
 * Created by Simone on 9/5/16.
 */
public class AlexaDataSource implements CreditScoreDataSource {

    private AlexaAPI alexaAPI;
    private Logger logger;

    public AlexaDataSource() {
        alexaAPI = new AlexaAPI(Config.getAlexaAccessKeyId(), Config.getAlexaSecretAccessKey());
        this.logger = Logger.getLogger(AlexaDataSource.class.getName());
    }

    public double getWeightedScore(Business business) {
        return getRawScores(business)[0] * ModelReader.readParameterFromFile(CoefficientNames.ALEXA_RANK);
    }

    public double[] getRawScores(Business business) {
        int rank = alexaAPI.getRank(business.getUrl());
        logger.info(String.format("%s is the # %d ranked website according to Alexa.", business.getUrl(), rank));
        double rawScore = rank == -1 ? 0.0 : rank;
        return new double[]{rawScore};
    }
}
