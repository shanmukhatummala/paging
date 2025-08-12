import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Contains the experiments for all the 4 trends
 */
public class PagingExperiments {

    private static final double NUM_OF_TRAILS = 100.00;

    /**
     * Runs a couple of example that show how values are chosen for regime1 and regime2
     * @param args no arguments need to be passed to run this program
     */
    public static void main(String[] args) {
        example1();
        example2();

        trend1();
        trend2();
        trend3();
        trend4();
    }

    // Note: epsilon, tow, omega, threshold etc., carry the usual meanings which are explained in Paging class.
    // I have shortly explained them again in the description of the method "runTrailsAndComputeAveragePageFaults" for further reference

    /**
     * identifying (k, N, n, epsilon, tow, omega) such that
     * OPT is significantly better than BlindOracle which is significantly better than LRU
     */
    private static void example1() {

        System.out.println("Identifying (k, N, n, epsilon, tow, omega) such that " +
                "OPT is significantly better than BlindOracle which is significantly better than LRU:");

        int k = 10;
        int N = 100;
        int n = 10000;
        double threshold = 0.1;

        double epsilon = 0.3;
        double tow = 0.5;
        int omega = 180;

        Paging paging = new Paging();

        int[] randomInputSequence = paging.generateRandomSequence(k, N, n, epsilon);
        int[] hSeq = paging.generateH(randomInputSequence);
        int[] trueHSeq = Arrays.copyOf(hSeq, hSeq.length);
        paging.addNoise(hSeq, tow, omega);

        int pageFaultsOfOpt = paging.blindOracle(k, randomInputSequence, trueHSeq);
        int pageFaultsOfBlindOracle = paging.blindOracle(k, randomInputSequence, hSeq);
        int pageFaultsOfLRU = paging.LRU(k, randomInputSequence);
        int pageFaultsOfCombinedAlg = paging.combinedAlg(k, randomInputSequence, hSeq, threshold);

        System.out.println("pageFaultsOfOpt: " + pageFaultsOfOpt);
        System.out.println("pageFaultsOfBlindOracle: " + pageFaultsOfBlindOracle);
        System.out.println("pageFaultsOfLRU: " + pageFaultsOfLRU);
        System.out.println("pageFaultsOfCombinedAlg: " + pageFaultsOfCombinedAlg);
        System.out.println("------------------");
    }

    /**
     * identifying (k, N, n, epsilon, tow, omega) such that
     * OPT is significantly better than LRU which is significantly better than BlindOracle
     */
    private static void example2() {

        System.out.println("identifying (k, N, n, epsilon, tow, omega) such that " +
                "OPT is significantly better than LRU which is significantly better than BlindOracle");

        int k = 10;
        int N = 100;
        int n = 10000;
        double threshold = 0.1;

        double epsilon = 0.8;
        double tow = 0.9;
        int omega = 1000;

        Paging paging = new Paging();

        int[] randomInputSequence = paging.generateRandomSequence(k, N, n, epsilon);
        int[] hSeq = paging.generateH(randomInputSequence);
        int[] trueHSeq = Arrays.copyOf(hSeq, hSeq.length);
        paging.addNoise(hSeq, tow, omega);

        int pageFaultsOfOpt = paging.blindOracle(k, randomInputSequence, trueHSeq);
        int pageFaultsOfBlindOracle = paging.blindOracle(k, randomInputSequence, hSeq);
        int pageFaultsOfLRU = paging.LRU(k, randomInputSequence);
        int pageFaultsOfCombinedAlg = paging.combinedAlg(k, randomInputSequence, hSeq, threshold);

        System.out.println("pageFaultsOfOpt: " + pageFaultsOfOpt);
        System.out.println("pageFaultsOfBlindOracle: " + pageFaultsOfBlindOracle);
        System.out.println("pageFaultsOfLRU: " + pageFaultsOfLRU);
        System.out.println("pageFaultsOfCombinedAlg: " + pageFaultsOfCombinedAlg);
        System.out.println("------------------");
    }

    /**
     * Varies cache size and provides the csv files with the output data for each regime
     */
    private static void trend1() {

        System.out.println("Trend 1 started");
        int k, N, n, omega;
        double epsilon, tow, threshold;
        Paging paging = new Paging();
        n = 10000;
        threshold = 0.1;

        // starting regime1
        // the below values are specific to regime 1
        epsilon = 0.3;
        tow = 0.5;
        omega = 180;

        List<String> kValues = new ArrayList<>();
        List<String> pageFaultsOfOpt = new ArrayList<>();
        List<String> pageFaultsOfBlindOracle = new ArrayList<>();
        List<String> pageFaultsOfLRU = new ArrayList<>();
        List<String> pageFaultsOfCombined = new ArrayList<>();

        for (k = 3; k <= 50; k = k + 2) {
            N = k * 10;
            kValues.add(String.valueOf(k));
            runTrailsAndComputeAveragePageFaults(paging, k, N, n, epsilon, tow, omega, threshold,
                    pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        }
        putValuesInCsv("trend1-regime1", "k", kValues,
                pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        // finished regime1

        kValues.clear();
        pageFaultsOfOpt.clear();
        pageFaultsOfBlindOracle.clear();
        pageFaultsOfLRU.clear();
        pageFaultsOfCombined.clear();

        // starting regime2
        // the below values are specific to regime 2
        epsilon = 0.8;
        tow = 0.9;
        omega = 1000;

        for (k = 3; k <= 50; k = k + 2) {
            N = k * 10;
            kValues.add(String.valueOf(k));
            runTrailsAndComputeAveragePageFaults(paging, k, N, n, epsilon, tow, omega, threshold,
                    pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        }
        putValuesInCsv("trend1-regime2", "k", kValues,
                pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        // finished regime2
        System.out.println("Trend 1 finished");
    }

    /**
     * Varies omega and provides the csv files with the output data for each regime
     */
    private static void trend2() {

        System.out.println("Trend 2 started");
        int k, N, n, omega;
        double epsilon, tow, threshold;
        Paging paging = new Paging();
        k = 10;
        N = 100;
        n = 10000;
        threshold = 0.1;

        // starting regime1
        // the below values are specific to regime 1
        epsilon = 0.3;
        tow = 0.5;

        List<String> omegaValues = new ArrayList<>();
        List<String> pageFaultsOfOpt = new ArrayList<>();
        List<String> pageFaultsOfBlindOracle = new ArrayList<>();
        List<String> pageFaultsOfLRU = new ArrayList<>();
        List<String> pageFaultsOfCombined = new ArrayList<>();

        for (omega = 0; omega < 2000; omega = omega + 50) {
            omegaValues.add(String.valueOf(omega));
            runTrailsAndComputeAveragePageFaults(paging, k, N, n, epsilon, tow, omega, threshold,
                    pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        }
        putValuesInCsv("trend2-regime1", "omega", omegaValues,
                pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        // finished regime1

        omegaValues.clear();
        pageFaultsOfOpt.clear();
        pageFaultsOfBlindOracle.clear();
        pageFaultsOfLRU.clear();
        pageFaultsOfCombined.clear();

        // starting regime2
        // the below values are specific to regime 2
        epsilon = 0.8;
        tow = 0.9;

        for (omega = 0; omega < 2000; omega = omega + 50) {
            omegaValues.add(String.valueOf(omega));
            runTrailsAndComputeAveragePageFaults(paging, k, N, n, epsilon, tow, omega, threshold,
                    pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        }
        putValuesInCsv("trend2-regime2", "omega", omegaValues,
                pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        // finished regime2

        System.out.println("Trend 2 finished");
    }

    /**
     * Varies epsilon and provides the csv files with the output data for each regime
     */
    private static void trend3() {

        System.out.println("Trend 3 started");
        int k, N, n, omega;
        double epsilon, tow, threshold;
        Paging paging = new Paging();
        k = 10;
        N = 100;
        n = 10000;
        threshold = 0.1;

        // starting regime1
        // the below values are specific to regime 1
        tow = 0.5;
        omega = 180;

        List<String> epsilonValues = new ArrayList<>();
        List<String> pageFaultsOfOpt = new ArrayList<>();
        List<String> pageFaultsOfBlindOracle = new ArrayList<>();
        List<String> pageFaultsOfLRU = new ArrayList<>();
        List<String> pageFaultsOfCombined = new ArrayList<>();

        for (epsilon = 0.00; epsilon <= 1.00; epsilon = epsilon + 0.05) {
            epsilon = Math.floor(epsilon * 100) / 100;
            epsilonValues.add(String.valueOf(epsilon));
            runTrailsAndComputeAveragePageFaults(paging, k, N, n, epsilon, tow, omega, threshold,
                    pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        }
        putValuesInCsv("trend3-regime1", "epsilon", epsilonValues,
                pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        // finished regime1

        epsilonValues.clear();
        pageFaultsOfOpt.clear();
        pageFaultsOfBlindOracle.clear();
        pageFaultsOfLRU.clear();
        pageFaultsOfCombined.clear();

        // starting regime2
        // the below values are specific to regime 2
        tow = 0.9;
        omega = 1000;

        for (epsilon = 0.00; epsilon < 1.00; epsilon = epsilon + 0.05) {
            epsilon = Math.floor(epsilon * 100) / 100;
            epsilonValues.add(String.valueOf(epsilon));
            runTrailsAndComputeAveragePageFaults(paging, k, N, n, epsilon, tow, omega, threshold,
                    pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        }
        putValuesInCsv("trend3-regime2", "epsilon", epsilonValues,
                pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        // finished regime2
        System.out.println("Trend 3 finished");
    }

    /**
     * Varies tow and provides the csv files with the output data for each regime
     */
    private static void trend4() {

        System.out.println("Trend 4 started");
        int k, N, n, omega;
        double epsilon, tow, threshold;
        Paging paging = new Paging();
        k = 10;
        N = 100;
        n = 10000;
        threshold = 0.1;

        // starting regime1
        // the below values are specific to regime 1
        epsilon = 0.3;
        omega = 180;

        List<String> towValues = new ArrayList<>();
        List<String> pageFaultsOfOpt = new ArrayList<>();
        List<String> pageFaultsOfBlindOracle = new ArrayList<>();
        List<String> pageFaultsOfLRU = new ArrayList<>();
        List<String> pageFaultsOfCombined = new ArrayList<>();

        for (tow = 0.00; tow < 1.00; tow = tow + 0.05) {
            tow = Math.floor(tow * 100) / 100;
            towValues.add(String.valueOf(tow));
            runTrailsAndComputeAveragePageFaults(paging, k, N, n, epsilon, tow, omega, threshold,
                    pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        }
        putValuesInCsv("trend4-regime1", "tow", towValues,
                pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        // finished regime1

        towValues.clear();
        pageFaultsOfOpt.clear();
        pageFaultsOfBlindOracle.clear();
        pageFaultsOfLRU.clear();
        pageFaultsOfCombined.clear();

        // starting regime2
        // the below values are specific to regime 2
        epsilon = 0.8;
        omega = 1000;

        for (tow = 0.00; tow < 1.00; tow = tow + 0.05) {
            tow = Math.floor(tow * 100) / 100;
            towValues.add(String.valueOf(tow));
            runTrailsAndComputeAveragePageFaults(paging, k, N, n, epsilon, tow, omega, threshold,
                    pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        }
        putValuesInCsv("trend4-regime2", "tow", towValues,
                pageFaultsOfOpt, pageFaultsOfBlindOracle, pageFaultsOfLRU, pageFaultsOfCombined);
        // finished regime2
        System.out.println("Trend 4 finished");
    }

    /**
     * Runs a batch of trails
     * @param paging reference to call the methods in Paging
     * @param k cache size
     * @param N every page request belongs to [N]
     * @param n number of page requests
     * @param epsilon controls the amount of locality
     * @param tow controls noise (how many values in true H sequence are to be changed)
     * @param omega controls noise (by how much values in true H sequence are to be changed)
     * @param threshold determines when a Combined algorithm can switch between LRU and BlindOracle
     * @param pageFaultsOfOpt list that stores the average page faults of OPT for each value of the varying parameter
     * @param pageFaultsOfBlindOracle list that stores the average page faults of BlindOracle for each value of the varying parameter
     * @param pageFaultsOfLRU list that stores the average page faults of LRU for each value of the varying parameter
     * @param pageFaultsOfCombined list that stores the average page faults of Combined for each value of the varying parameter
     */
    private static void runTrailsAndComputeAveragePageFaults(
            Paging paging, int k, int N, int n, double epsilon, double tow, int omega, double threshold,
            List<String> pageFaultsOfOpt, List<String> pageFaultsOfBlindOracle,
            List<String> pageFaultsOfLRU, List<String> pageFaultsOfCombined) {

        AtomicInteger totalPageFaultsOfOptInAllTrails = new AtomicInteger();
        AtomicInteger totalPageFaultsOfBlindOracleInAllTrails = new AtomicInteger();
        AtomicInteger totalPageFaultsOfLRUInAllTrails = new AtomicInteger();
        AtomicInteger totalPageFaultsOfCombinedInAllTrails = new AtomicInteger();

        // Used parallel streams to reduce the time taken for a run
        IntStream.range(0, 100).parallel().forEach(trail -> {
            int[] randomInputSequence = paging.generateRandomSequence(k, N, n, epsilon);
            int[] hSeq = paging.generateH(randomInputSequence);
            int[] trueHSeq = Arrays.copyOf(hSeq, hSeq.length);
            paging.addNoise(hSeq, tow, omega);

            totalPageFaultsOfOptInAllTrails.addAndGet(paging.blindOracle(k, randomInputSequence, trueHSeq));
            totalPageFaultsOfBlindOracleInAllTrails.addAndGet(paging.blindOracle(k, randomInputSequence, hSeq));
            totalPageFaultsOfLRUInAllTrails.addAndGet(paging.LRU(k, randomInputSequence));
            totalPageFaultsOfCombinedInAllTrails.addAndGet(paging.combinedAlg(k, randomInputSequence, hSeq, threshold));
        });

        pageFaultsOfOpt.add(String.valueOf(totalPageFaultsOfOptInAllTrails.get() / NUM_OF_TRAILS));
        pageFaultsOfBlindOracle.add(String.valueOf(totalPageFaultsOfBlindOracleInAllTrails.get() / NUM_OF_TRAILS));
        pageFaultsOfLRU.add(String.valueOf(totalPageFaultsOfLRUInAllTrails.get() / NUM_OF_TRAILS));
        pageFaultsOfCombined.add(String.valueOf(totalPageFaultsOfCombinedInAllTrails.get() / NUM_OF_TRAILS));
    }

    /**
     * Helps in putting the data into csv
     * @param fileName name of the csv file to put the data into
     * @param varyingParameterName the name of the parameter that is varying
     * @param varyingParameterValues the values taken by the varying parameter
     * @param pageFaultsOfOpt list that stores the average page faults of OPT for each value of the varying parameter
     * @param pageFaultsOfBlindOracle list that stores the average page faults of BlindOracle for each value of the varying parameter
     * @param pageFaultsOfLRU list that stores the average page faults of LRU for each value of the varying parameter
     * @param pageFaultsOfCombined list that stores the average page faults of Combined for each value of the varying parameter
     */
    private static void putValuesInCsv(
            String fileName, String varyingParameterName, List<String> varyingParameterValues,
            List<String> pageFaultsOfOpt, List<String> pageFaultsOfBlindOracle,
            List<String> pageFaultsOfLRU, List<String> pageFaultsOfCombined) {

        String filePath = System.getProperty("user.dir") + File.separator + fileName + ".csv";
        createFile(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(createCsvRow(varyingParameterName,
                    "pageFaultsOfOpt", "pageFaultsOfBlindOracle", "pageFaultsOfLRU", "pageFaultsOfCombined"));
            for (int i = 0; i < varyingParameterValues.size(); i ++) {
                writer.write(createCsvRow(varyingParameterValues.get(i),
                        pageFaultsOfOpt.get(i), pageFaultsOfBlindOracle.get(i),
                        pageFaultsOfLRU.get(i), pageFaultsOfCombined.get(i)));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error when writing to csv file");
        }
    }

    /**
     * Helps in creating the csv files
     * @param path path to the file to be created
     */
    private static void createFile(String path) {
        File file = new File(path);
        if (!file.exists() && !file.isDirectory()) {
            file.delete();
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("path:" + path);
            throw new RuntimeException(e);
        }
    }

    /**
     * convert the values to a row in csv file
     * @param values values that should be present in a row in the csv file
     * @return the row of the csv file in the form of a string
     */
    private static String createCsvRow(String... values) {
        return String.join(",", values).concat("\n");
    }
}
