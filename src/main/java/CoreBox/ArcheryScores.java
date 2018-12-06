package CoreBox;

import DatabaseBox.DatabaseTable;
import DatabaseBox.DatabaseWrapper;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.*;



/**
 * created 23/11/18
 * TODO archive bow rather than delete
 * TODO change bow name
 * TODO change archer name
 * TODO change round ref name
 * TODO delete round ref
 * TODO get double/triple round score
 * TODO what to do in archerRoundsDatabaseTable if for example a bow is deleted (can't do handicap calculations in
 * end round)
 */
public class ArcheryScores {
    /**
     * How arrows on a face are scored e.g. 10-zone metric or 5-zone imperial
     * Used for calculating average arrow scores for given handicaps
     */
    public enum ScoringType {
        IMPERIAL(9, 2, 1, 4, 10, 2, 1), METRIC(10, 1, 1, 10, 20, 0, 0),
        TRIPLE_VEGAS_FITAFIVE(10, 1, 1, 4, 20, 20.0 / 5.0, 6), WORCESTER(5, 1, 1, 5, 10, 0, 0),
        FITA_SIX_ZONE(10, 1, 1, 5, 20, 20.0 / 6.0, 5);

        // Parts of formula used to calculate average score per arrow
        int initial;
        int sumMultiplier;
        int sumStart;
        int sumEnd;
        double sumInternalDenominator;
        double subtractInternalDenominator;
        int subtractMultiplier;


        ScoringType(int initial, int sumMultiplier, int sumStart, int sumEnd, double sumInternalDenominator,
                    double subtractInternalDenominator, int subtractMultiplier) {
            this.initial = initial;
            this.sumMultiplier = sumMultiplier;
            this.sumStart = sumStart;
            this.sumEnd = sumEnd;
            this.sumInternalDenominator = sumInternalDenominator;
            this.subtractInternalDenominator = subtractInternalDenominator;
            this.subtractMultiplier = subtractMultiplier;
        }


        /**
         * Uses splines to calculate the HC
         *
         * @return the handicap to approx two decimal places (accuracy: 0.02)
         */
        @Deprecated
        public double getHandicap(double range, int faceSize, double averageScorePerArrow, boolean inner10Only) {
            final double[] xs = new double[]{0, 1, 2, 8, 10, 14, 20, 32, 41, 50, 57, 66, 71, 76, 84, 88, 92, 94, 98, 99,
                    100};
            final double[] ys = new double[xs.length];
            // The lowest index in ys that averageScorePerArrow is larger than
            int goalIntervalI = -1;
            for (int i = 0; i < xs.length; i++) {
                double x = xs[i];
                double scoreForHC = averageScorePerArrow(range, faceSize, (int) x, inner10Only);
                ys[i] = scoreForHC;
                if (scoreForHC <= averageScorePerArrow && goalIntervalI == -1) {
                    goalIntervalI = i - 1;
                }
            }
            final PolynomialFunction[] polynomials = new SplineInterpolator().interpolate(xs, ys).getPolynomials();
            // [3] x^3 + [2] x^2 + [1] x^1 + [0] for the part of the spline that the target is in the range of
            double[] splinePartCoefficients = getSplinePartCubicCoefficients(polynomials[goalIntervalI]);

            // +xs[goalIntervalI] to account for repeats in the polynomial, keeps answer in correct range
            double val = inverseValue(
                    averageScorePerArrow, splinePartCoefficients[3], splinePartCoefficients[2],
                    splinePartCoefficients[1], splinePartCoefficients[0]) + xs[goalIntervalI];
            // Make sure it worked
            if (val >= 0 && val <= 100) {
                return val;
            }
            else {
                // Try using a slightly different polynomial, could be the other side of the knot
                splinePartCoefficients = getSplinePartCubicCoefficients(polynomials[goalIntervalI - 1]);
                return inverseValue(
                        averageScorePerArrow, splinePartCoefficients[3], splinePartCoefficients[2],
                        splinePartCoefficients[1], splinePartCoefficients[0]) + xs[goalIntervalI - 1];
            }
        }


        /**
         * Formulas found in The Construction of the Graduated Handicap Tables for Target Archery by David Lane
         *
         * @param range in meters
         * @param faceSize in cm
         * @param inner10Only true if only the inner 10 should be counted as 10
         */
        public double averageScorePerArrow(double range, int faceSize, double handicap, boolean inner10Only) {
            double sigma = Math.pow(range * Math.pow(1.036, handicap + 12.9) * 0.05 * (1 + 0.000001429 * Math.pow(
                    1.07, handicap + 4.3) * Math.pow(range, 2)), 2);
            int sumStart = this.sumStart;
            if (inner10Only) {
                sumStart += 1;
            }
            double total = 0;
            for (int i = sumStart; i <= this.sumEnd; i++) {
                total += exponentialCalculation(faceSize, sigma, this.sumInternalDenominator / i);
            }
            total = this.initial - this.sumMultiplier * total - this.subtractMultiplier
                    * exponentialCalculation(faceSize, sigma, this.subtractInternalDenominator);
            if (inner10Only) {
                total -= exponentialCalculation(faceSize, sigma, 40);
            }
            return total;
        }


        /**
         * @return the polynomial coefficients of up to at least the x^3 coefficient (array of length at least 4)
         */
        @Deprecated
        private double[] getSplinePartCubicCoefficients(PolynomialFunction polynomialFunction) {
            double[] splinePartCoefficients = polynomialFunction.getCoefficients();
            // if it is less than cubic fill the higher coefficients with 0s
            if (splinePartCoefficients.length < 4) {
                int oldLength = splinePartCoefficients.length;
                splinePartCoefficients = Arrays.copyOf(splinePartCoefficients, 4);
                for (int i = oldLength; i < splinePartCoefficients.length; i++) {
                    splinePartCoefficients[i] = 0;
                }
            }
            return splinePartCoefficients;
        }


        /**
         * @return x given score = a3*x^3 + a2*x^2 + a1*x + a0
         */
        @Deprecated
        private double inverseValue(double score, double a3, double a2, double a1, double a0) {
            // Equate to 0 and remove x^3 coefficient
            a0 = a0 - score;
            a2 /= a3;
            a1 /= a3;
            a0 /= a3;
            final double b = (2 * Math.pow(a2, 3) - 9 * a1 * a2 + 27 * a0) / 27.0;
            final double alphaBetaConstant = -b / 2.0;
            final double alphaBetaPlusMinus = (b * b) / 4.0 + Math.pow((3 * a1 - a2 * a2) / 3.0, 3) / 27;
            double y = 0;
            // check the square root will be real
            if (alphaBetaPlusMinus > 0) {
                final double alphaBetaPlusMinusSqrt = Math.sqrt(alphaBetaPlusMinus);
                y = Math.cbrt(alphaBetaConstant + alphaBetaPlusMinusSqrt)
                        + Math.cbrt(alphaBetaConstant - alphaBetaPlusMinusSqrt);
            }
            else {
                boolean yFound = false;
                final double alphaBetaPlusMinusImagCoef = Math.sqrt(-alphaBetaPlusMinus);
                final double r = Math.cbrt(Math.sqrt(alphaBetaConstant * alphaBetaConstant + alphaBetaPlusMinusImagCoef
                        * alphaBetaPlusMinusImagCoef));

                final double deg120inRadians = Math.toRadians(120);
                final double alphaTheta = Math.atan2(alphaBetaPlusMinusImagCoef, alphaBetaConstant) / 3;
                final double[] alphaThetas = {alphaTheta, alphaTheta + deg120inRadians, alphaTheta - deg120inRadians};
                final double betaTheta = Math.atan2(-alphaBetaPlusMinusImagCoef, alphaBetaConstant) / 3;
                final double[] betaThetas = {betaTheta, betaTheta + deg120inRadians, betaTheta - deg120inRadians};

                // Split the equation into real and imaginary parts
                final double[] alphaRealParts = new double[alphaThetas.length];
                final double[] alphaImagParts = new double[alphaThetas.length];
                final double[] betaRealParts = new double[alphaThetas.length];
                final double[] betaImagParts = new double[alphaThetas.length];
                for (int i = 0; i < alphaThetas.length; i++) {
                    alphaRealParts[i] = r * Math.cos(alphaThetas[i]);
                    alphaImagParts[i] = r * Math.sin(alphaThetas[i]);
                    betaRealParts[i] = r * Math.cos(betaThetas[i]);
                    betaImagParts[i] = r * Math.sin(betaThetas[i]);
                }

                final double[] realParts = new double[alphaThetas.length];
                final double[] imagParts = new double[alphaThetas.length];
                for (int i = 0; i < alphaThetas.length; i++) {
                    realParts[i] = (alphaImagParts[i] * Math.sqrt(3) - betaImagParts[i] * Math.sqrt(3)
                            - alphaRealParts[i] - betaRealParts[i]) / 2.0;
                    imagParts[i] = (alphaRealParts[i] * Math.sqrt(3) - betaRealParts[i] * Math.sqrt(3)
                            - alphaImagParts[i] - betaImagParts[i]) / 2.0;
                    if (imagParts[i] == 0) {
                        y = realParts[i];
                        yFound = true;
                        break;
                    }
                }
                if (!yFound) {
                    for (int i = 0; i < alphaThetas.length; i++) {
                        realParts[i] = (-alphaImagParts[i] * Math.sqrt(3) + betaImagParts[i] * Math.sqrt(3)
                                - alphaRealParts[i] - betaRealParts[i]) / 2.0;
                        imagParts[i] = (-alphaRealParts[i] * Math.sqrt(3) + betaRealParts[i] * Math.sqrt(3)
                                - alphaImagParts[i] - betaImagParts[i]) / 2.0;
                        if (imagParts[i] == 0) {
                            y = realParts[i];
                            yFound = true;
                            break;
                        }
                    }
                    if (!yFound) {
                        // However, cubics must always have at least one real root
                        throw new IllegalStateException("Couldn't find any real roots");
                    }
                }
            }
            return y - (a2 / 3.0);
        }


        /**
         * Helper for calculating average score per arrow
         */
        private static double exponentialCalculation(int faceSize, double sigma, double denominator) {
            return Math.exp(-Math.pow((double) faceSize / denominator + 0.357, 2) / sigma);
        }
    }



    public enum Bowsyle {
        RECURVE, COMPOUND(true), BAREBOW, LONGBOW;

        private boolean inner10Only;


        Bowsyle() {
            this.inner10Only = false;
        }


        Bowsyle(boolean inner10Only) {
            this.inner10Only = inner10Only;
        }
    }



    public enum ShootStatus {PRACTICE, CLUB_SHOOT, RECORD_STATUS, WORLD_RECORD_STATUS, TIER_1, TIER_2, TIER_3}



    private static final DatabaseTable roundsRefDatabaseTable = DatabaseTable.createDatabaseTable(
            "ArcheryRoundsRef", RoundsRefDatabaseFields.values());
    private static final DatabaseTable roundDistancesRefDatabaseTable = DatabaseTable.createDatabaseTable(
            "ArcheryRoundDistnacesRef", RoundDistancesRefDatabaseFields.values());
    private static final DatabaseTable archersDatabaseTable = DatabaseTable.createDatabaseTable(
            "ArcheryArchers", ArchersDatabaseFields.values());
    private static final DatabaseTable bowsDatabaseTable = DatabaseTable.createDatabaseTable(
            "ArcheryBows", BowsDatabaseFields.values());
    private static final DatabaseTable archerRoundsDatabaseTable = DatabaseTable.createDatabaseTable(
            "ArcheryArcherRounds", ArcherRoundsDatabaseFields.values());
    private static final DatabaseTable arrowValuesDatabaseTable = DatabaseTable.createDatabaseTable(
            "ArcheryArrowValues", ArrowValuesDatabaseFields.values());
    private static final DatabaseWrapper databaseWrapper = new DatabaseWrapper(
            new DatabaseTable[]{roundsRefDatabaseTable, roundDistancesRefDatabaseTable,
                    archersDatabaseTable, bowsDatabaseTable, archerRoundsDatabaseTable, arrowValuesDatabaseTable});


    public static double yardsToMeters(int yards) {
        return 0.9144 * yards;
    }


    /**
     * Helper method for testing
     */
    public static DatabaseWrapper getDatabaseWrapper() {
        DatabaseWrapper.checkDatabaseInTestMode();
        return databaseWrapper;
    }


    /**
     * Helper method for testing
     */
    public static boolean checkRowCounts(int roundsRef, int roundDistances, int archers, int bows, int archerRounds,
                                         int arrowValues) {
        DatabaseWrapper.checkDatabaseInTestMode();
        return databaseWrapper.checkRowCounts(
                new int[]{roundsRef, roundDistances, archers, bows, archerRounds, arrowValues});
    }


    /**
     * Helper method for testing
     *
     * @throws IllegalStateException if the database is not in test mode
     */
    public static void deleteArcherRoundsTableAndRemoveInProgressRound() {
        if (!DatabaseTable.isInTestMode()) {
            throw new IllegalStateException("This action can only be taken in test mode");
        }
        archerRoundsDatabaseTable.deleteTable();
        final Map<String, Object> setArgs = new HashMap<>();
        setArgs.put(ArchersDatabaseFields.IN_PROGRESS_ROUND_ID.fieldName, null);
        archersDatabaseTable.updateAND(setArgs, null);
    }


    /**
     * @throws NullPointerException for null arguments
     * @throws BadUserInputException if archer already exists
     */
    public static void addArcher(String name) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        final Map<String, Object> args = Map.of(ArchersDatabaseFields.NAME.fieldName, name);
        archersDatabaseTable.selectAND(args, rs -> {
            if (rs.next()) {
                throw new BadUserInputException("This archer name already exists");
            }
            return null;
        });
        archersDatabaseTable.insert(args);
    }


    /**
     * @see ArcheryScores#addBow(String, String, Bowsyle, Integer, Integer)
     */
    public static void addBow(String archerName, String bowName, Bowsyle bowStyle) {
        addBow(archerName, bowName, bowStyle, null, null);
    }


    /**
     * @throws NullPointerException for null arguments
     * @throws BadUserInputException if bow already exists
     */
    public static void addBow(String archerName, String bowName, Bowsyle bowStyle, @Nullable Integer indoorHandicap,
                              @Nullable Integer outdoorHandicap) {
        if (archerName == null || bowName == null || bowStyle == null) {
            throw new NullPointerException("Null argument");
        }
        final Map<String, Object> bowArgs = new HashMap<>();
        bowArgs.put(BowsDatabaseFields.ARCHER_ID.fieldName, getArcherID(archerName));
        bowArgs.put(BowsDatabaseFields.NAME.fieldName, bowName);
        bowsDatabaseTable.selectAND(bowArgs, rs -> {
            if (rs.next()) {
                throw new BadUserInputException("You already have a bow with this name");
            }
            return null;
        });
        indoorHandicap = getHandicapDbValue(indoorHandicap);
        outdoorHandicap = getHandicapDbValue(outdoorHandicap);
        bowArgs.put(BowsDatabaseFields.STYLE.fieldName, bowStyle.toString());
        bowArgs.put(BowsDatabaseFields.OUTDOOR_HANDICAP.fieldName, outdoorHandicap);
        bowArgs.put(BowsDatabaseFields.INDOOR_HANDICAP.fieldName, indoorHandicap);
        bowsDatabaseTable.insert(bowArgs);
    }


    /**
     * @throws NullPointerException for null arguments
     * @throws BadUserInputException if archer already exists
     */
    private static int getArcherID(String name) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }
        return (int) archersDatabaseTable.selectAND(
                Map.of(ArchersDatabaseFields.NAME.fieldName, name), rs -> {
                    if (rs.next()) {
                        return rs.getInt(archersDatabaseTable.getPrimaryKey());
                    }
                    else {
                        throw new BadUserInputException("This archer name doesn't exist");
                    }
                });
    }


    /**
     * @return -1 if handicap is null, or the handicap if it's valid
     * @throws BadUserInputException if the handicap is out of the range
     */
    private static int getHandicapDbValue(Integer handicap) {
        if (handicap == null) {
            return -1;
        }
        else if (handicap < 0 || handicap > 100) {
            throw new BadUserInputException("Handicap must be between 0 and 100");
        }
        else {
            return handicap;
        }
    }


    /**
     * @param distanceArrowCounts the number of arrows that will be shot at each distance
     * @param distanceFaceSizes Map<Distance, Face size>
     * @see ArcheryScores#addRoundRef(String, ScoringType, boolean, Set, Map, Map, boolean)
     */
    public static void addRoundRef(String name, ScoringType scoringType, boolean isOutdoor, Set<Double> distances,
                                   int distanceArrowCounts, Map<Double, Integer> distanceFaceSizes,
                                   boolean inner10Scoring) {
        addRoundRef(name, scoringType, isOutdoor, distances, getAsDistancesMap(distances, distanceArrowCounts),
                    distanceFaceSizes, inner10Scoring);
    }


    /**
     * @param distanceArrowCounts Map<Distance, Arrows to shoot>
     * @param distanceFaceSizes Map<Distance, Face size>
     * @param inner10Scoring whether compounds will use inner 10 scoring when doing this round
     * @throws NullPointerException for null arguments or if distances size is 0
     * @throws BadUserInputException if distances are invalid, if size of distances, arrow counts, and face sizes
     * don't match, or if a round with the name already exists
     */
    public static void addRoundRef(String name, ScoringType scoringType, boolean isOutdoor, Set<Double> distances,
                                   Map<Double, Integer> distanceArrowCounts, Map<Double, Integer> distanceFaceSizes,
                                   boolean inner10Scoring) {
        /*
         * Check inputs
         */
        if (name == null || scoringType == null || distances == null || distanceArrowCounts == null
                || distanceFaceSizes == null) {
            throw new NullPointerException("Null arguments");
        }
        else if (distances.size() == 0) {
            throw new NullPointerException("Distances size must be > 0");
        }
        for (double distance : distances) {
            if (distance <= 0) {
                throw new BadUserInputException("All distances must be > 0");
            }
        }
        if (isOutdoor && inner10Scoring) {
            throw new BadUserInputException("Inner 10 scoring is only used on some indoor rounds");
        }
        else if (!distances.equals(distanceArrowCounts.keySet())) {
            throw new BadUserInputException("Distances do not match arrow counts distances");
        }
        else if (!distances.equals(distanceFaceSizes.keySet())) {
            throw new BadUserInputException("Distances do not match face sizes distances");
        }
        final Map<String, Object> selectArgs = Map.of(RoundsRefDatabaseFields.NAME.fieldName, name);
        roundsRefDatabaseTable.selectAND(selectArgs, rs -> {
            if (rs.next()) {
                throw new BadUserInputException("A round with this name already exists");
            }
            return null;
        });

        /*
         * Add to the database
         */
        roundsRefDatabaseTable.insert(
                Map.of(RoundsRefDatabaseFields.NAME.fieldName, name,
                       RoundsRefDatabaseFields.IS_OUTDOOR.fieldName, isOutdoor,
                       RoundsRefDatabaseFields.INNER_TEN_SCORING.fieldName, inner10Scoring,
                       RoundsRefDatabaseFields.SCORING_TYPE.fieldName, scoringType.toString()));
        int roundRefID = (int) roundsRefDatabaseTable.selectAND(selectArgs, rs -> {
            rs.next();
            return rs.getInt(roundsRefDatabaseTable.getPrimaryKey());
        });
        for (double distance : distances) {
            roundDistancesRefDatabaseTable.insert(Map.of(
                    RoundDistancesRefDatabaseFields.ROUND_ID.fieldName, roundRefID,
                    RoundDistancesRefDatabaseFields.DISTANCE.fieldName, distance,
                    RoundDistancesRefDatabaseFields.ARROWS.fieldName, distanceArrowCounts.get(distance),
                    RoundDistancesRefDatabaseFields.FACE_SIZE.fieldName, distanceFaceSizes.get(distance)));
        }
    }


    /**
     * @return A map containing every distance mapped to i
     * @throws NullPointerException for null arguments
     */
    private static Map<Double, Integer> getAsDistancesMap(Set<Double> distances, int i) {
        if (distances == null) {
            throw new NullPointerException("Null distances");
        }
        final Map<Double, Integer> map = new HashMap<>();
        for (double distance : distances) {
            map.put(distance, i);
        }
        return map;
    }


    /**
     * @param distanceArrowCounts Map<Distance, Arrows to shoot>
     * @param distanceFaceSizes the face size to be used for every distance
     * @see ArcheryScores#addRoundRef(String, ScoringType, boolean, Set, Map, Map, boolean)
     */
    public static void addRoundRef(String name, ScoringType scoringType, boolean isOutdoor, Set<Double> distances,
                                   Map<Double, Integer> distanceArrowCounts, int distanceFaceSizes,
                                   boolean inner10Scoring) {
        addRoundRef(name, scoringType, isOutdoor, distances, distanceArrowCounts,
                    getAsDistancesMap(distances, distanceFaceSizes), inner10Scoring);
    }


    /**
     * @param distanceArrowCounts the number of arrows that will be shot at each distance
     * @param distanceFaceSizes the face size to be used for every distance
     * @see ArcheryScores#addRoundRef(String, ScoringType, boolean, Set, Map, Map, boolean)
     */
    public static void addRoundRef(String name, ScoringType scoringType, boolean isOutdoor, Set<Double> distances,
                                   int distanceArrowCounts, int distanceFaceSizes, boolean inner10Scoring) {
        addRoundRef(name, scoringType, isOutdoor, distances, getAsDistancesMap(distances, distanceArrowCounts),
                    getAsDistancesMap(distances, distanceFaceSizes), inner10Scoring);
    }


    /**
     * Creates a new archerRound in the database and sets the archer's in progress round to this newly created round
     *
     * @param bowName if null, the archer must only have one bow saved, this will be used
     * @param ordinal 1st round of this type of the day, etc. if null, rounds with the same archer, bow, name, and date
     * will be found and the next int (starting from 1) used
     * @throws NullPointerException for null arguments
     * @throws BadStateException if there's already a round in progress
     * @throws BadUserInputException if archer already exists
     */
    public static void startNewRound(String archerName, @Nullable String bowName, String roundName, ZonedDateTime date,
                                     @Nullable ShootStatus shootStatus, @Nullable Integer goalScore,
                                     @Nullable Integer ordinal, @Nullable String notes, boolean countsTowardsHandicap) {
        if (archerName == null || roundName == null || date == null) {
            throw new NullPointerException("Archer, round name, and date must be provided");
        }
        else if (ordinal != null && ordinal <= 0) {
            throw new IllegalStateException("Invalid ordinal");
        }
        else if (goalScore != null && goalScore <= 0) {
            throw new IllegalStateException("Invalid goal score");
        }
        // Check no current round in progress
        final Map<String, Object> archerWhereArgs = Map.of(ArchersDatabaseFields.NAME.fieldName, archerName);
        int archerID = (int) archersDatabaseTable.selectAND(archerWhereArgs, rs -> {
            if (rs.next()) {
                if (rs.getBoolean(ArchersDatabaseFields.IN_PROGRESS_ROUND_ID.fieldName)) {
                    throw new BadStateException("There is already a round in progress");
                }
                return rs.getInt(archersDatabaseTable.getPrimaryKey());
            }
            else {
                throw new BadUserInputException("Archer doesn't exist");
            }
        });

        // Add the round to the database
        final Map<String, Object> args = new HashMap<>();
        args.put(ArcherRoundsDatabaseFields.ARCHER_ID.fieldName, archerID);
        int bowID;
        if (bowName == null) {
            bowID = getArchersOnlyBowID(archerName);
        }
        else {
            bowID = getBowID(archerID, bowName);
        }

        args.put(ArcherRoundsDatabaseFields.BOW_ID.fieldName, bowID);
        args.put(ArcherRoundsDatabaseFields.ROUND_REF_ID.fieldName, roundsRefDatabaseTable.selectAND(
                Map.of(RoundsRefDatabaseFields.NAME.fieldName, roundName),
                rs -> {
                    if (rs.next()) {
                        return rs.getInt(roundsRefDatabaseTable.getPrimaryKey());
                    }
                    else {
                        throw new BadUserInputException("Could not find the round with that name");
                    }
                }));
        args.put(ArcherRoundsDatabaseFields.DATE.fieldName, date);
        if (ordinal == null) {
            ordinal = 1 + (int) archerRoundsDatabaseTable.selectAND(args, rs -> {
                int maxOrdinal = 0;
                while (rs.next()) {
                    int current = rs.getInt(ArcherRoundsDatabaseFields.ORDINAL.fieldName);
                    if (current > maxOrdinal) {
                        maxOrdinal = current;
                    }
                }
                return maxOrdinal;
            });
        }
        args.put(ArcherRoundsDatabaseFields.ORDINAL.fieldName, ordinal);
        args.put(ArcherRoundsDatabaseFields.COUNT_TOWARDS_HANDICAP.fieldName, countsTowardsHandicap);
        if (shootStatus != null) {
            args.put(ArcherRoundsDatabaseFields.SHOOT_STATUS.fieldName, shootStatus.toString());
        }
        if (goalScore != null) {
            args.put(ArcherRoundsDatabaseFields.GOAL_SCORE.fieldName, goalScore);
        }
        if (notes != null) {
            args.put(ArcherRoundsDatabaseFields.NOTES.fieldName, notes);
        }
        archerRoundsDatabaseTable.insert(args);

        // Set this round as in progress
        int roundID = (int) archerRoundsDatabaseTable.selectAND(args, rs -> {
            rs.next();
            return rs.getInt(archerRoundsDatabaseTable.getPrimaryKey());
        });
        archersDatabaseTable.updateAND(
                Map.of(ArchersDatabaseFields.IN_PROGRESS_ROUND_ID.fieldName, roundID), archerWhereArgs);
    }


    /**
     * @throws NullPointerException for null arguments
     * @throws BadUserInputException if the archer has more than one bow or no bows
     */
    private static int getArchersOnlyBowID(String archerName) {
        if (archerName == null) {
            throw new NullPointerException("Name cannot be null");
        }
        return (int) bowsDatabaseTable.selectAND(
                Map.of(BowsDatabaseFields.ARCHER_ID.fieldName, getArcherID(archerName)), rs -> {
                    int bowID;
                    if (rs.next()) {
                        bowID = rs.getInt(bowsDatabaseTable.getPrimaryKey());
                        if (rs.next()) {
                            throw new BadUserInputException("Archer has more than one bow, please specify");
                        }
                        else {
                            return bowID;
                        }
                    }
                    else {
                        throw new BadUserInputException("Archer does not have any bows");
                    }
                });
    }


    /**
     * @throws NullPointerException for null arguments
     * @throws BadUserInputException if archer doesn't exist
     */
    private static int getBowID(int archerID, String bowName) {
        if (bowName == null) {
            throw new NullPointerException("Null arguments");
        }
        return (int) bowsDatabaseTable.selectAND(
                Map.of(BowsDatabaseFields.ARCHER_ID.fieldName, archerID, BowsDatabaseFields.NAME.fieldName, bowName),
                rs -> {
                    if (rs.next()) {
                        return rs.getInt(bowsDatabaseTable.getPrimaryKey());
                    }
                    else {
                        throw new BadUserInputException("Archer doesn't exist");
                    }
                });
    }


    /**
     * @throws NullPointerException for null arguments
     * @throws BadUserInputException if scores and isX lengths don't match, for invalid scores, or if isXs on
     * non-10s, or if too many arrows are being added to the round
     */
    public static void addEnd(String archerName, int[] scores, boolean[] isX) {
        if (archerName == null || scores == null || isX == null) {
            throw new NullPointerException("Null arguments");
        }
        if (scores.length != isX.length) {
            throw new BadUserInputException("Scores and isX lengths don't match");
        }
        for (int i = 0; i < scores.length; i++) {
            // TODO Optimisation make this more precise, e.g. odd numbers for imperial
            if (scores[i] < 0 || scores[i] > 10) {
                throw new BadUserInputException("Score must be between 0 and 10");
            }
            if (isX[i] && scores[i] != 10) {
                throw new BadUserInputException("isX can only be set for scores of 10");
            }
        }

        int roundID = getRoundInProgressID(getArcherID(archerName));
        int arrowNumber = getArrowsCurrentlyShot(roundID);
        if (arrowNumber + scores.length > getTotalArrowCountForRound(roundID)) {
            throw new BadUserInputException("Adding this end will add too many arrow scores for the given round");
        }
        for (int i = 0; i < scores.length; i++) {
            final Map<String, Object> args = new HashMap<>();
            args.put(ArrowValuesDatabaseFields.ARCHER_ROUNDS_ID.fieldName, roundID);
            args.put(ArrowValuesDatabaseFields.ARROW_NUMBER.fieldName, ++arrowNumber);
            args.put(ArrowValuesDatabaseFields.SCORE.fieldName, scores[i]);
            args.put(ArrowValuesDatabaseFields.IS_X.fieldName, isX[i]);
            arrowValuesDatabaseTable.insert(args);
        }
    }


    /**
     * @throws BadStateException if there is no round in progress
     */
    private static int getRoundInProgressID(int archerID) {
        final Object roundID = archersDatabaseTable.selectAND(
                Map.of(archersDatabaseTable.getPrimaryKey(), archerID), rs -> {
                    rs.next();
                    return rs.getInt(ArchersDatabaseFields.IN_PROGRESS_ROUND_ID.fieldName);
                });
        if (roundID == null) {
            throw new BadStateException("There is no in-progress round");
        }
        else {
            return (int) roundID;
        }
    }


    private static int getArrowsCurrentlyShot(int archerRountID) {
        final Map<String, Object> startArgs = new HashMap<>();
        startArgs.put(ArrowValuesDatabaseFields.ARCHER_ROUNDS_ID.fieldName, archerRountID);
        return arrowValuesDatabaseTable.getFunctionOfIntColumn(
                DatabaseTable.ColumnFunction.MAX, ArrowValuesDatabaseFields.ARROW_NUMBER.fieldName, startArgs);
    }


    private static int getTotalArrowCountForRound(int archerRoundID) {
        int roundRefID = getRoundRefID(archerRoundID);
        return roundDistancesRefDatabaseTable.getFunctionOfIntColumn(
                DatabaseTable.ColumnFunction.SUM, RoundDistancesRefDatabaseFields.ARROWS.fieldName,
                Map.of(RoundDistancesRefDatabaseFields.ROUND_ID.fieldName, roundRefID));
    }


    private static int getRoundRefID(int archerRoundID) {
        return (int) archerRoundsDatabaseTable.selectAND(
                Map.of(archerRoundsDatabaseTable.getPrimaryKey(), archerRoundID), rs -> {
                    rs.next();
                    return rs.getInt(ArcherRoundsDatabaseFields.ROUND_REF_ID.fieldName);
                });
    }


    /**
     * Set the archer's in progress round as complete and update their handicap
     *
     * @throws NullPointerException for null arguments
     * @throws BadStateException if there aren't enough arrows added to the round
     */
    public static void setRoundComplete(String archerName) {
        if (archerName == null) {
            throw new NullPointerException("Null arguments");
        }
        int archerID = getArcherID(archerName);
        int archerRoundID = getRoundInProgressID(archerID);
        if (getArrowsCurrentlyShot(archerRoundID) != getTotalArrowCountForRound(archerRoundID)) {
            throw new BadStateException("Not enough arrows added to the round");
        }

        /*
         * Set no round in progress
         */
        final Map<String, Object> removeInProgressArgs = new HashMap<>();
        removeInProgressArgs.put(ArchersDatabaseFields.IN_PROGRESS_ROUND_ID.fieldName, null);
        archersDatabaseTable.updateAND(removeInProgressArgs, null);

        /*
         * Update handicap
         */
        archerRoundsDatabaseTable.selectAND(
                Map.of(archerRoundsDatabaseTable.getPrimaryKey(), archerRoundID), archerRoundRs -> {
                    archerRoundRs.next();
                    if (!archerRoundRs.getBoolean(ArcherRoundsDatabaseFields.COUNT_TOWARDS_HANDICAP.fieldName)) {
                        return null;
                    }

                    /*
                     * Indoor or outdoor
                     */
                    int roundRefID = archerRoundRs.getInt(ArcherRoundsDatabaseFields.ROUND_REF_ID.fieldName);
                    final boolean isOutdoor = (boolean) roundsRefDatabaseTable.selectAND(
                            Map.of(roundsRefDatabaseTable.getPrimaryKey(), roundRefID), rs -> {
                                rs.next();
                                return rs.getBoolean(RoundsRefDatabaseFields.IS_OUTDOOR.fieldName);
                            });
                    final String bowHandicapFieldName;
                    if (isOutdoor) {
                        bowHandicapFieldName = BowsDatabaseFields.OUTDOOR_HANDICAP.fieldName;
                    }
                    else {
                        bowHandicapFieldName = BowsDatabaseFields.INDOOR_HANDICAP.fieldName;
                    }

                    /*
                     * Find new handicap
                     */
                    int bowID = archerRoundRs.getInt(ArcherRoundsDatabaseFields.BOW_ID.fieldName);
                    final Map<String, Object> bowWhereArgs = Map.of(bowsDatabaseTable.getPrimaryKey(), bowID);
                    DatabaseTable.ResultsSetAction getNewHandicapRSA = findHcRs -> {
                        findHcRs.next();
                        int currentHandicap = findHcRs.getInt(bowHandicapFieldName);

                        // Update current handicap
                        if (currentHandicap >= 0) {
                            int achievedHC = getHandicap(archerRoundID);
                            if (achievedHC < currentHandicap - 1) {
                                return Optional.of(Math.floorDiv(achievedHC + currentHandicap, 2));
                            }
                            else {
                                return Optional.empty();
                            }
                        }
                        // Generate new handicap from three scores
                        else {

                            ZonedDateTime roundDate;
                            try {
                                roundDate = DatabaseTable.parseDateFromDatabase(
                                        archerRoundRs.getString(ArcherRoundsDatabaseFields.DATE.fieldName));
                            } catch (ParseException e) {
                                roundDate = ZonedDateTime.now();
                            }
                            final String dateFieldName = ArcherRoundsDatabaseFields.DATE.fieldName;
                            return archerRoundsDatabaseTable.selectAND(
                                    Map.of(ArcherRoundsDatabaseFields.ARCHER_ID.fieldName, archerID,
                                           ArcherRoundsDatabaseFields.BOW_ID.fieldName, bowID),
                                    String.format(
                                            " AND %s > datetime('now', '-1 years') ORDER BY %s",
                                            dateFieldName, dateFieldName),
                                    Map.of(dateFieldName, roundDate),
                                    threeScoresRs -> {
                                        int count = 0;
                                        int[] handicaps = new int[3];
                                        // Get the three most recent handicaps
                                        while (threeScoresRs.next() && count < 3) {
                                            handicaps[count] = getHandicap(
                                                    threeScoresRs.getInt(archerRoundsDatabaseTable.getPrimaryKey()));
                                            count++;
                                        }
                                        if (count < 3) {
                                            return Optional.empty();
                                        }
                                        else {
                                            return Optional.of(Math.floorDiv(
                                                    handicaps[0] + handicaps[1] + handicaps[2], 3));
                                        }
                                    });
                        }
                    };

                    /*
                     * Update handicap in database
                     */
                    //noinspection unchecked
                    ((Optional<Integer>) bowsDatabaseTable.selectAND(bowWhereArgs, getNewHandicapRSA)).ifPresent(
                            newHandicap -> bowsDatabaseTable.updateAND(
                                    Map.of(bowHandicapFieldName, newHandicap), bowWhereArgs));
                    return null;
                });
    }


    /**
     * @return the handicap achieved for the given round
     */
    private static int getHandicap(int archerRoundID) {
        Integer arrowCount = getArrowsCurrentlyShot(archerRoundID);
        if (arrowCount == getTotalArrowCountForRound(archerRoundID)) {
            arrowCount = null;
        }
        return getHandicapForRound(getRoundRefID(archerRoundID), getScoreForRound(archerRoundID),
                                   isBowInnerTen(archerRoundID), arrowCount);
    }


    /**
     * @param inner10Only whether or not the shooter uses inner 10 scoring (unused if the round doesn't use inner 10
     * scoring)
     * @param arrows get the handicap only for the first this many arrows (if null then for a full round)
     * @return the worst handicap for the given score and round (e.g. 599 portsmouth can be 3, 4, or 5 so return 5)
     */
    public static int getHandicapForRound(int roundRefID, int score, boolean inner10Only, @Nullable Integer arrows) {
        if (arrows != null && arrows <= 0) {
            throw new IllegalArgumentException("Arrows must be greater than 0");
        }

        int low = 0;
        int high = 100;
        // Binary search
        while (true) {
            int testHC = Math.floorDiv(high + low, 2);
            int testHCScore = getScoreForRound(roundRefID, testHC, inner10Only, arrows);
            if (testHCScore == score) {
                // Worst handicap for the given score
                while (getScoreForRound(roundRefID, testHC + 1, inner10Only, arrows) == score) {
                    testHC++;
                }
                return testHC;
            }
            else if (score < testHCScore) {
                if (high - low == 1) {
                    return high;
                }
                low = testHC;
            }
            else {
                if (high - low == 1) {
                    return low;
                }
                high = testHC;
            }
        }
    }


    /**
     * @return the current score for the given round
     */
    private static int getScoreForRound(int archerRoundID) {
        return arrowValuesDatabaseTable.getFunctionOfIntColumn(
                DatabaseTable.ColumnFunction.SUM, ArrowValuesDatabaseFields.SCORE.fieldName,
                Map.of(ArrowValuesDatabaseFields.ARCHER_ROUNDS_ID.fieldName, archerRoundID));
    }


    /**
     * @return whether the bow used in the given round uses the inner ten only
     */
    private static boolean isBowInnerTen(int archerRoundID) {
        final int bowID = (int) archerRoundsDatabaseTable.selectAND(
                Map.of(archerRoundsDatabaseTable.getPrimaryKey(), archerRoundID),
                rs -> rs.getInt(ArcherRoundsDatabaseFields.BOW_ID.fieldName));
        return (boolean) bowsDatabaseTable.selectAND(
                Map.of(bowsDatabaseTable.getPrimaryKey(), bowID),
                rs -> Bowsyle.valueOf(rs.getString(BowsDatabaseFields.STYLE.fieldName)).inner10Only);
    }


    /**
     * @param inner10Only whether or not the shooter uses inner 10 scoring
     * (unused if the round doesn't use inner 10 scoring)
     * @param arrows get the score only for the first this many arrows (if null then for a full round)
     * @return The score for the given handicap and round
     */
    public static int getScoreForRound(int roundRefID, int handicap, boolean inner10Only, @Nullable Integer arrows) {
        return (int) roundsRefDatabaseTable.selectAND(
                Map.of(roundsRefDatabaseTable.getPrimaryKey(), roundRefID),
                rs1 -> {
                    // Set inner 10 to false if the round doesn't use inner 10
                    boolean inner10OnlyLamda = inner10Only;
                    if (inner10OnlyLamda) {
                        if (!rs1.getBoolean(RoundsRefDatabaseFields.INNER_TEN_SCORING.fieldName)) {
                            inner10OnlyLamda = false;
                        }
                    }
                    final ScoringType scoringType = ScoringType.valueOf(rs1.getString(
                            RoundsRefDatabaseFields.SCORING_TYPE.fieldName));
                    final boolean inner10OnlyLamda2 = inner10OnlyLamda;

                    // Get score
                    return roundDistancesRefDatabaseTable.selectAND(
                            Map.of(RoundDistancesRefDatabaseFields.ROUND_ID.fieldName, roundRefID),
                            String.format(" ORDER BY %s DESC", RoundDistancesRefDatabaseFields.DISTANCE.fieldName),
                            new HashMap<>() {
                                {
                                    put(RoundDistancesRefDatabaseFields.DISTANCE.fieldName, null);
                                }
                            },
                            rs -> {
                                int currentArrowCount = 0;
                                double score = 0;
                                while (rs.next()) {
                                    double distance = rs.getDouble(RoundDistancesRefDatabaseFields.DISTANCE.fieldName);
                                    int arrowCount = rs.getInt(RoundDistancesRefDatabaseFields.ARROWS.fieldName);
                                    int faceSize = rs.getInt(RoundDistancesRefDatabaseFields.FACE_SIZE.fieldName);
                                    if (arrows == null || currentArrowCount < arrows) {
                                        if (arrows != null && currentArrowCount + arrowCount > arrows) {
                                            arrowCount = arrows - currentArrowCount;
                                        }
                                        score += arrowCount * scoringType.averageScorePerArrow(
                                                distance, faceSize, handicap, inner10OnlyLamda2);
                                        currentArrowCount += arrowCount;
                                    }
                                }
                                return (int) Math.round(score); // Truncate
                            });
                });
    }


    public static void setBowHandicap(String archerName, String bowName, @Nullable Integer indoorHandicap,
                                      @Nullable Integer outdoorHandicap) {
        if (archerName == null || bowName == null) {
            throw new NullPointerException("Null arguments");
        }
        indoorHandicap = getHandicapDbValue(indoorHandicap);
        outdoorHandicap = getHandicapDbValue(outdoorHandicap);

        bowsDatabaseTable.updateAND(
                Map.of(BowsDatabaseFields.INDOOR_HANDICAP.fieldName, indoorHandicap,
                       BowsDatabaseFields.OUTDOOR_HANDICAP.fieldName, outdoorHandicap),
                Map.of(bowsDatabaseTable.getPrimaryKey(), getBowID(getArcherID(archerName), bowName)));
    }


    public static int getBowIndoorHandicap(String archerName, String bowName) {
        return getBowHandicap(archerName, bowName, BowsDatabaseFields.INDOOR_HANDICAP.fieldName);
    }


    /**
     * @param fieldName BowsTable field name for the desired handicap (indoor or outdoor)
     */
    private static int getBowHandicap(String archerName, String bowName, String fieldName) {
        if (archerName == null || bowName == null || fieldName == null) {
            throw new NullPointerException("Null arguments");
        }

        return (int) bowsDatabaseTable.selectAND(
                Map.of(bowsDatabaseTable.getPrimaryKey(), getBowID(getArcherID(archerName), bowName)),
                rs -> {
                    rs.next();
                    return rs.getInt(fieldName);
                });
    }


    public static int getBowOutdoorHandicap(String archerName, String bowName) {
        return getBowHandicap(archerName, bowName, BowsDatabaseFields.OUTDOOR_HANDICAP.fieldName);
    }


    /**
     * @return the full predicted score based on their current score for a partially completed round
     */
    public static int getPredictedScore(int archerRoundID) {
        int handicap = getHandicap(archerRoundID);
        int roundRefID = getRoundRefID(archerRoundID);
        return getScoreForRound(roundRefID, handicap, isBowInnerTen(archerRoundID), null);
    }


    // Inner 10: whether compounds will use inner 10 scoring when doing this round
    private enum RoundsRefDatabaseFields implements DatabaseTable.DatabaseField {
        NAME("name", DatabaseTable.SQLType.TEXT, true), SCORING_TYPE("scoringType", DatabaseTable.SQLType.TEXT, true),
        IS_OUTDOOR("isOutdoor", DatabaseTable.SQLType.BIT, true),
        INNER_TEN_SCORING("innerTenScoring", DatabaseTable.SQLType.BIT, true);

        private String fieldName;
        private DatabaseTable.SQLType sqlType;
        private boolean required;


        RoundsRefDatabaseFields(String fieldName, DatabaseTable.SQLType sqlType, boolean required) {
            this.fieldName = fieldName;
            this.sqlType = sqlType;
            this.required = required;
        }


        @Override
        public String getFieldName() {
            return fieldName;
        }


        @Override
        public DatabaseTable.SQLType getSqlType() {
            return sqlType;
        }


        @Override
        public boolean isRequired() {
            return required;
        }
    }



    private enum RoundDistancesRefDatabaseFields implements DatabaseTable.DatabaseField {
        ROUND_ID("roundID", DatabaseTable.SQLType.INT, true), DISTANCE("distance", DatabaseTable.SQLType.DOUBLE, true),
        ARROWS("arrows", DatabaseTable.SQLType.INT, true), FACE_SIZE("faceSize", DatabaseTable.SQLType.INT, true);

        private String fieldName;
        private DatabaseTable.SQLType sqlType;
        private boolean required;


        RoundDistancesRefDatabaseFields(String fieldName, DatabaseTable.SQLType sqlType, boolean required) {
            this.fieldName = fieldName;
            this.sqlType = sqlType;
            this.required = required;
        }


        @Override
        public String getFieldName() {
            return fieldName;
        }


        @Override
        public DatabaseTable.SQLType getSqlType() {
            return sqlType;
        }


        @Override
        public boolean isRequired() {
            return required;
        }
    }



    private enum ArchersDatabaseFields implements DatabaseTable.DatabaseField {
        NAME("name", DatabaseTable.SQLType.TEXT, true),
        IN_PROGRESS_ROUND_ID("inProgressRoundID", DatabaseTable.SQLType.INT, false);

        private String fieldName;
        private DatabaseTable.SQLType sqlType;
        private boolean required;


        ArchersDatabaseFields(String fieldName, DatabaseTable.SQLType sqlType, boolean required) {
            this.fieldName = fieldName;
            this.sqlType = sqlType;
            this.required = required;
        }


        @Override
        public String getFieldName() {
            return fieldName;
        }


        @Override
        public DatabaseTable.SQLType getSqlType() {
            return sqlType;
        }


        @Override
        public boolean isRequired() {
            return required;
        }
    }



    // Handicaps -1 if not used
    private enum BowsDatabaseFields implements DatabaseTable.DatabaseField {
        ARCHER_ID("archerID", DatabaseTable.SQLType.INT, true), NAME("name", DatabaseTable.SQLType.TEXT, true),
        STYLE("style", DatabaseTable.SQLType.TEXT, true),
        INDOOR_HANDICAP("indoorHandicap", DatabaseTable.SQLType.INT, true),
        OUTDOOR_HANDICAP("outdoorHandicap", DatabaseTable.SQLType.INT, true);

        private String fieldName;
        private DatabaseTable.SQLType sqlType;
        private boolean required;


        BowsDatabaseFields(String fieldName, DatabaseTable.SQLType sqlType, boolean required) {
            this.fieldName = fieldName;
            this.sqlType = sqlType;
            this.required = required;
        }


        @Override
        public String getFieldName() {
            return fieldName;
        }


        @Override
        public DatabaseTable.SQLType getSqlType() {
            return sqlType;
        }


        @Override
        public boolean isRequired() {
            return required;
        }
    }



    // Ordinal: e.g. 1st round of this type of day
    // Notes: anything the archer wants to add e.g. rainy, windy
    private enum ArcherRoundsDatabaseFields implements DatabaseTable.DatabaseField {
        ARCHER_ID("archerID", DatabaseTable.SQLType.INT, true), DATE("shootDate", DatabaseTable.SQLType.DATE, true),
        BOW_ID("bowID", DatabaseTable.SQLType.INT, true), ROUND_REF_ID("roundRefID", DatabaseTable.SQLType.INT, true),
        ORDINAL("ordinal", DatabaseTable.SQLType.INT, true), NOTES("notes", DatabaseTable.SQLType.TEXT, false),
        GOAL_SCORE("goalScore", DatabaseTable.SQLType.INT, false),
        SHOOT_STATUS("shootStatus", DatabaseTable.SQLType.TEXT, false),
        COUNT_TOWARDS_HANDICAP("handicapped", DatabaseTable.SQLType.BIT, true);

        private String fieldName;
        private DatabaseTable.SQLType sqlType;
        private boolean required;


        ArcherRoundsDatabaseFields(String fieldName, DatabaseTable.SQLType sqlType, boolean required) {
            this.fieldName = fieldName;
            this.sqlType = sqlType;
            this.required = required;
        }


        @Override
        public String getFieldName() {
            return fieldName;
        }


        @Override
        public DatabaseTable.SQLType getSqlType() {
            return sqlType;
        }


        @Override
        public boolean isRequired() {
            return required;
        }
    }



    private enum ArrowValuesDatabaseFields implements DatabaseTable.DatabaseField {
        ARCHER_ROUNDS_ID("archerRoundsID", DatabaseTable.SQLType.INT, true),
        ARROW_NUMBER("arrowNumber", DatabaseTable.SQLType.INT, true), SCORE("score", DatabaseTable.SQLType.INT, true),
        IS_X("isX", DatabaseTable.SQLType.BIT, true);

        private String fieldName;
        private DatabaseTable.SQLType sqlType;
        private boolean required;


        ArrowValuesDatabaseFields(String fieldName, DatabaseTable.SQLType sqlType, boolean required) {
            this.fieldName = fieldName;
            this.sqlType = sqlType;
            this.required = required;
        }


        @Override
        public String getFieldName() {
            return fieldName;
        }


        @Override
        public DatabaseTable.SQLType getSqlType() {
            return sqlType;
        }


        @Override
        public boolean isRequired() {
            return required;
        }
    }
}
