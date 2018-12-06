import CoreBox.ArcheryScores;
import DatabaseBox.DatabaseTable;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



/**
 * created 24/11/18
 */
public class ArcheryScoresTest {
    @Before
    public void setup() {
        DatabaseTable.setTestMode();
    }


    @After
    public void teardown() {
        ArcheryScores.getDatabaseWrapper().deleteAllTables();
    }


    @Test
    public void testAverageScore() {
        Assert.assertEquals(500, Math.round(ArcheryScores.ScoringType.METRIC.averageScorePerArrow(ArcheryScores.yardsToMeters(20), 60, 47, false) * 60));
        Assert.assertEquals(407, Math.round(ArcheryScores.ScoringType.METRIC.averageScorePerArrow(ArcheryScores.yardsToMeters(20), 60, 61, false) * 60));
    }

    @Test
    public void testApproxHandicap() {
        // TODO Test for other rounds
        double maxDifference = 0;
        for (double i = 0; i <= 100; i += 0.1) {
            double scorePerArrow = ArcheryScores.ScoringType.METRIC.averageScorePerArrow(ArcheryScores.yardsToMeters(20), 60, i, false);
            double handicap = ArcheryScores.ScoringType.METRIC.getHandicap(ArcheryScores.yardsToMeters(20), 60, scorePerArrow, false);
            double difference = Math.abs(handicap - i);
            if (difference > 0.01) {
                System.out.println(String.format("%.1f, %.3f (%.3f)", i, handicap, difference));
            }
            if (difference > maxDifference) {
                maxDifference = Math.abs(difference);
            }
            Assert.assertEquals(i, handicap, 0.05);
        }
        System.out.println(maxDifference);
    }

    @Test
    public void testAddArcher() {
        ArcheryScores.addArcher("Ella");
        ArcheryScores.addArcher("Dad");
        Assert.assertTrue(ArcheryScores.checkRowCounts(0, 0, 2, 0, 0, 0));

        boolean exceptionThrown = false;
        try {
            ArcheryScores.addArcher("Ella");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        Assert.assertTrue(ArcheryScores.checkRowCounts(0, 0, 2, 0, 0, 0));
    }

    @Test
    public void testAddBow() {
        ArcheryScores.addArcher("Ella");
        ArcheryScores.addBow("Ella", "main", ArcheryScores.Bowsyle.RECURVE, 43, 45);
        ArcheryScores.addBow("Ella", "field", ArcheryScores.Bowsyle.BAREBOW);
        ArcheryScores.addBow("Ella", "second", ArcheryScores.Bowsyle.RECURVE);
        Assert.assertTrue(ArcheryScores.checkRowCounts(0, 0, 1, 3, 0, 0));

        boolean exceptionThrown = false;
        try {
            ArcheryScores.addBow("Dad", "main", ArcheryScores.Bowsyle.RECURVE, 35, 38);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        exceptionThrown = false;
        try {
            ArcheryScores.addBow("Ella", "main", ArcheryScores.Bowsyle.RECURVE, 43, 45);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);Assert.assertTrue(ArcheryScores.checkRowCounts(0, 0, 1, 3, 0, 0));
    }

    @Test
    public void testAddRoundRef() {
        final double distanceInMeters = ArcheryScores.yardsToMeters(20);
        Map<Double, Integer> faceSizes = new HashMap<>();
        faceSizes.put(distanceInMeters, 60);
        Map<Double, Integer> arrowCounts = new HashMap<>();
        arrowCounts.put(distanceInMeters, 60);
        ArcheryScores.addRoundRef("Portsmouth", ArcheryScores.ScoringType.METRIC, false, faceSizes.keySet(), arrowCounts, faceSizes, true);
        Assert.assertTrue(ArcheryScores.checkRowCounts(1, 1, 0, 0, 0, 0));

        faceSizes = new HashMap<>();
        faceSizes.put(80.0, 122);
        faceSizes.put(60.0, 122);
        faceSizes.put(50.0, 80);
        faceSizes.put(30.0, 80);
        arrowCounts = new HashMap<>();
        arrowCounts.put(80.0, 36);
        arrowCounts.put(60.0, 36);
        arrowCounts.put(50.0, 36);
        arrowCounts.put(30.0, 36);
        ArcheryScores.addRoundRef("WA 1440", ArcheryScores.ScoringType.METRIC, true, faceSizes.keySet(), arrowCounts, faceSizes, false);
        Assert.assertTrue(ArcheryScores.checkRowCounts(2, 5, 0, 0, 0, 0));

        ArcheryScores.addRoundRef("WA 1440 TEST1", ArcheryScores.ScoringType.METRIC, true, faceSizes.keySet(), 36, faceSizes, false);
        ArcheryScores.addRoundRef("WA 1440 TEST2", ArcheryScores.ScoringType.METRIC, true, faceSizes.keySet(), arrowCounts, 122, false);
        ArcheryScores.addRoundRef("WA 1440 TEST3", ArcheryScores.ScoringType.METRIC, true, faceSizes.keySet(), 36, 122, false);

        boolean exceptionThrown = false;
        try {
            // Repeat name
            ArcheryScores.addRoundRef("Portsmouth", ArcheryScores.ScoringType.METRIC, false, faceSizes.keySet(), arrowCounts, faceSizes, true);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        exceptionThrown = false;
        try {
            // One extra arrow count
            arrowCounts.put(10.0, 20);
            ArcheryScores.addRoundRef("dgfhhgsh", ArcheryScores.ScoringType.METRIC, false, faceSizes.keySet(), arrowCounts, faceSizes, false);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        exceptionThrown = false;
        try {
            // One extra face size
            faceSizes.put(10.0, 20);
            faceSizes.put(20.0, 20);
            ArcheryScores.addRoundRef("dgfhhgsh", ArcheryScores.ScoringType.METRIC, false, faceSizes.keySet(), arrowCounts, faceSizes, false);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        exceptionThrown = false;
        try {
            // Mismatched distances in arrow counts and face sizes
            arrowCounts.put(40.0, 20);
            ArcheryScores.addRoundRef("dgfhhgsh", ArcheryScores.ScoringType.METRIC, false, faceSizes.keySet(), arrowCounts, faceSizes, false);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }

    @Test
    public void testStartNewRound() {
        final String archerName = "Ella";
        final String bowName = "main";
        final String roundName = "Portsmouth";
        final ZonedDateTime date = ZonedDateTime.now();

        ArcheryScores.addArcher(archerName);
        ArcheryScores.addBow(archerName, bowName, ArcheryScores.Bowsyle.RECURVE);
        ArcheryScores.addRoundRef(roundName, ArcheryScores.ScoringType.METRIC, false, Collections.singleton(ArcheryScores.yardsToMeters(20)), 60, 60, false);
        Assert.assertTrue(ArcheryScores.checkRowCounts(1, 1, 1, 1, 0, 0));

        ArcheryScores.startNewRound(archerName, null, roundName, date, null, null, null, null, true);
        Assert.assertTrue(ArcheryScores.checkRowCounts(1, 1, 1, 1, 1, 0));
        ArcheryScores.deleteArcherRoundsTableAndRemoveInProgressRound();

        ArcheryScores.startNewRound(archerName, bowName, roundName, date, ArcheryScores.ShootStatus.CLUB_SHOOT, 500, 1, "Good weather", false);
        Assert.assertTrue(ArcheryScores.checkRowCounts(1, 1, 1, 1, 1, 0));

        boolean exceptionThrown = false;
        try {
            ArcheryScores.startNewRound(
                    archerName, bowName, roundName, date, ArcheryScores.ShootStatus.CLUB_SHOOT, 500, 1, "Good weather",
                    false);
        } catch (BadStateException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }


    @Test
    public void testAddEnd() {
        final String archerName = "Ella";
        final String bowName = "main";
        final String roundName = "Portsmouth";
        final ZonedDateTime date = ZonedDateTime.now();

        ArcheryScores.addArcher(archerName);
        ArcheryScores.addBow(archerName, bowName, ArcheryScores.Bowsyle.RECURVE);
        ArcheryScores.addRoundRef(roundName, ArcheryScores.ScoringType.METRIC, false, Collections.singleton(ArcheryScores.yardsToMeters(20)), 60, 60, false);
        ArcheryScores.startNewRound(archerName, null, roundName, date, null, null, null, null, true);
        Assert.assertTrue(ArcheryScores.checkRowCounts(1, 1, 1, 1, 1, 0));

        int[] scores = new int[]{10, 10, 9, 9, 8, 0};
        boolean[] isX = new boolean[]{true, false, false, false, false, false};
        // Add 60 arrows
        for (int i = 0; i < 60 / scores.length; i++) {
            ArcheryScores.addEnd(archerName, scores, isX);
            Assert.assertTrue(ArcheryScores.checkRowCounts(1, 1, 1, 1, 1, (i+1) * 6));
        }
        // Add more arrows
        boolean exceptionThrown = false;
        try {
            ArcheryScores.addEnd(archerName, scores, isX);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        // isX on non-10s
        isX = new boolean[]{true, true, true, true, true, true};
        exceptionThrown = false;
        try {
            ArcheryScores.addEnd(archerName, scores, isX);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        // Too many isX
        isX = new boolean[]{true, false, false, false, false, false, false};
        exceptionThrown = false;
        try {
            ArcheryScores.addEnd(archerName, scores, isX);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        // Invalid scores
        scores = new int[]{10, 10, 0, -1, 8, 8};
        isX = new boolean[]{true, false, false, false, false, false};
        exceptionThrown = false;
        try {
            ArcheryScores.addEnd(archerName, scores, isX);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        scores = new int[]{10, 10, 0, 9, 30, 8};
        isX = new boolean[]{true, false, false, false, false, false};
        exceptionThrown = false;
        try {
            ArcheryScores.addEnd(archerName, scores, isX);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        Assert.assertTrue(ArcheryScores.checkRowCounts(1, 1, 1, 1, 1, 60));
    }

    
    @Test
    public void testSetRoundComplete() {
        final String archerName = "Ella";
        final String bowName = "main";
        final String roundName = "Portsmouth";
        ZonedDateTime date = ZonedDateTime.now().minusYears(2);

        ArcheryScores.addArcher(archerName);
        ArcheryScores.addBow(archerName, bowName, ArcheryScores.Bowsyle.RECURVE, 100, 100);
        ArcheryScores.addRoundRef(roundName, ArcheryScores.ScoringType.METRIC, false, Collections.singleton(ArcheryScores.yardsToMeters(20)), 60, 60, false);

        for (int i = 0; i < 4; i++) {
            ArcheryScores.startNewRound(archerName, null, roundName, date, null, null, null, null, true);
            int expectedTotalScore = (10 + 10 + 9 + 9 + 8 + 7) * 10;
            int[] scores = new int[]{10, 10, 9, 9, 8, 7};
            boolean[] isX = new boolean[]{true, false, false, false, false, false};
            for (int j = 0; j < 60 / scores.length; j++) {
                ArcheryScores.addEnd(archerName, scores, isX);
            }
            Assert.assertTrue(ArcheryScores.checkRowCounts(1, 1, 1, 1, i+1, 60 * (i+1)));
            ArcheryScores.setRoundComplete(archerName);
            switch (i) {
                case 0:
                    // Handicap update, later scores will be 'up to date' HCs
                    Assert.assertEquals(Math.floorDiv(140, 2), ArcheryScores.getBowIndoorHandicap(archerName, bowName));
                    date = ZonedDateTime.now();
                    ArcheryScores.setBowHandicap(archerName, bowName, null, null);
                    break;
                case 1:
                    // No handicap, doesn't have three scores
                    Assert.assertEquals(-1, ArcheryScores.getBowIndoorHandicap(archerName, bowName));
                    break;
                case 2:
                    // No handicap, doesn't have three scores (1 outdated)
                    Assert.assertEquals(-1, ArcheryScores.getBowIndoorHandicap(archerName, bowName));
                    break;
                case 3:
                    // No handicap, 3 scores (one out of date)
                    Assert.assertEquals(40, ArcheryScores.getBowIndoorHandicap(archerName, bowName));
            }
        }
    }


    @Test
    public void getHandicapForRound() {
        // Portsmouth normal scoring
        ArcheryScores.addRoundRef("Portsmouth", ArcheryScores.ScoringType.METRIC, false, Collections.singleton(ArcheryScores.yardsToMeters(20)), 60, 60, true);
        for (int i = 0; i <= 100; i++) {
            int score = ArcheryScores.getScoreForRound(1, i, false, null);
            int predictedHC = ArcheryScores.getHandicapForRound(1, score, false, null);
            int partialPredictedHC = ArcheryScores.getHandicapForRound(1, score / 2, false, 30);
            if (i >= 9) {
                Assert.assertEquals(i, predictedHC);
                Assert.assertTrue(Math.abs(partialPredictedHC - i) <= 1);
            }
            else {
                Assert.assertTrue(predictedHC >= i);
            }
            int handicapTablesScore = 0;
            switch (i) {
                case 0:
                    handicapTablesScore = 600;
                    break;
                case 10:
                    handicapTablesScore = 596;
                    break;
                case 50:
                    handicapTablesScore = 484;
                    break;
                case 100:
                    handicapTablesScore = 34;
                    break;
            }
            if (handicapTablesScore > 0) {
                Assert.assertEquals(handicapTablesScore, score);
            }
        }

        // Portsmouth inner 10
        for (int i = 0; i <= 100; i++) {
            int score = ArcheryScores.getScoreForRound(1, i, true, null);
            int predictedHC = ArcheryScores.getHandicapForRound(1, score, true, null);
            Assert.assertEquals(i, predictedHC);
            int partialPredictedHC = ArcheryScores.getHandicapForRound(1, score / 2, true, 30);
            Assert.assertTrue(Math.abs(partialPredictedHC - i) <= 1);
            int handicapTablesScore = 0;
            switch (i) {
                case 0:
                    handicapTablesScore = 589;
                    break;
                case 10:
                    handicapTablesScore = 573;
                    break;
                case 50:
                    handicapTablesScore = 478;
                    break;
                case 100:
                    handicapTablesScore = 34;
                    break;
            }
            if (handicapTablesScore > 0) {
                Assert.assertEquals(handicapTablesScore, score);
            }
        }

        // Ladies FITA
        Map<Double, Integer> faceSizes = new HashMap<>();
        Map<Double, Integer> arrowCounts = new HashMap<>();
        faceSizes.put(70.0, 122);
        arrowCounts.put(70.0, 36);
        ArcheryScores.addRoundRef("WA 1440 Pt1", ArcheryScores.ScoringType.METRIC, true, faceSizes.keySet(), arrowCounts, faceSizes, false);
        faceSizes.put(60.0, 122);
        arrowCounts.put(60.0, 36);
        ArcheryScores.addRoundRef("WA 1440 Pt2", ArcheryScores.ScoringType.METRIC, true, faceSizes.keySet(), arrowCounts, faceSizes, false);
        faceSizes.put(50.0, 80);
        arrowCounts.put(50.0, 36);
        ArcheryScores.addRoundRef("WA 1440 Pt3", ArcheryScores.ScoringType.METRIC, true, faceSizes.keySet(), arrowCounts, faceSizes, false);
        faceSizes.put(30.0, 80);
        arrowCounts.put(30.0, 36);
        ArcheryScores.addRoundRef("WA 1440", ArcheryScores.ScoringType.METRIC, true, faceSizes.keySet(), arrowCounts, faceSizes, false);
        for (int i = 0; i <= 100; i++) {
            int score = ArcheryScores.getScoreForRound(5, i, false, null);
            int predictedHC = ArcheryScores.getHandicapForRound(5, score, false, null);
            Assert.assertTrue(predictedHC >= i);
            int handicapTablesScore = 0;
            switch (i) {
                case 0:
                    handicapTablesScore = 1412;
                    break;
                case 10:
                    handicapTablesScore = 1371;
                    break;
                case 50:
                    handicapTablesScore = 817;
                    break;
                case 100:
                    handicapTablesScore = 7;
                    break;
            }
            if (handicapTablesScore > 0) {
                Assert.assertEquals(handicapTablesScore, score);
            }

            for (int j = 2; j < 5; j++) {
                int partialRoundScore = ArcheryScores.getScoreForRound(j, i, false, null);
                int partialScore = ArcheryScores.getScoreForRound(5, i, false, (j-1) * 36);
                Assert.assertEquals(partialRoundScore, partialScore);
            }
        }

        // Check all scores give the right HC
        for (int score = 34; score <= 600; score++) {
            int predictedHC = ArcheryScores.getHandicapForRound(1, score, false, null);
            int handicapTablesHc = 0;
            switch (score) {
                case 600:
                    handicapTablesHc = 1;
                    break;
                case 599:
                    handicapTablesHc = 5;
                    break;
                case 596:
                    handicapTablesHc = 10;
                    break;
                case 490:
                    handicapTablesHc = 49;
                    break;
                case 489:
                case 485:
                case 484:
                    handicapTablesHc = 50;
                    break;
                case 483:
                    handicapTablesHc = 51;
                    break;
                case 34:
                    handicapTablesHc = 100;
                    break;
            }
            if (handicapTablesHc > 0) {
                Assert.assertEquals(handicapTablesHc, predictedHC);
            }
        }
    }

    @Test
    public void testPartialHandicap() {

    }
}
