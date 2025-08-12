import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class Paging {

    private static final String SUCCESS = "Success";

    // the below 2 variables are only for testing purpose, they do not impact the code logic
    private static int switchesFromLRUToBlindOracle = 0;
    private static int switchesFromBlindOracleToLRU = 0;

    /**
     * Generates a random input sequence
     * @param k capacity of the cache
     * @param N each request element belongs to [N]
     * @param n length of the request sequence
     * @param epsilon controls the amount of locality (large epsilon => more amount of locality)
     * @return the request sequence in the form of an array
     */
    int[] generateRandomSequence(int k, int N, int n, double epsilon) {

        int[] pageRequests = new int[n];
        Random random = new Random();

        // the first k requests are to pages 1, 2, . . . , k
        for (int i = 0; i < k; i ++) {
            pageRequests[i] = i + 1;
        }

        // l contains the k local pages and notL contains the remaining from [N]
        int[] l = new int[k];
        int[] notL = new int[N - k];

        // initially setting l = {1,2,...,k} and notL = {k+1,...,n}
        for (int i = 0; i < N; i ++) {
            if (i < k) {
                l[i] = i + 1;
            } else {
                notL[i - k] = i + 1;
            }
        }

        // generates the next N - k requests in the request sequence
        for (int i = k; i < n; i ++) {

            // choosing a random index from both l and notL and assigning the elements at these indices to x and y respectively
            int randomIndexInL = random.nextInt(k);
            int randomIndexInNotL = random.nextInt(N - k);
            int x = l[randomIndexInL];
            int y = notL[randomIndexInNotL];

            // with probability epsilon, set pageRequests[i] = x
            // and with probability 1 - epsilon, set pageRequests[i] = y, and update l <- (l \ {x}) ∪ {y} and notL <- (notL \ {y}) ∪ {x}
            if (Math.random() < epsilon) {
                pageRequests[i] = x;
            } else {
                pageRequests[i] = y;
                l[randomIndexInL] = y;
                notL[randomIndexInNotL] = x;
            }
        }


//        System.out.println("Request Sequence: " + Arrays.toString(pageRequests));

        return pageRequests;
    }

    /**
     * Generates the actual H sequence by looking at the pageRequests
     * @param pageRequests array of page requests
     * @return the H sequence generated
     */
    int[] generateH(int[] pageRequests) {

        int n = pageRequests.length;

        int[] h = new int[n];

        // traverses page requests starting from the end
        // uses a map to store the elements traversed and the left most index of each element till that point
        // using this map finds out the H sequence
        Map<Integer, Integer> elementIndexMap = new HashMap<>();
        for (int i = n - 1; i >= 0; i --) {
            if (!elementIndexMap.containsKey(pageRequests[i])) {
                h[i] = n + 1;
            } else {
                h[i] = elementIndexMap.get(pageRequests[i]);
            }
            elementIndexMap.put(pageRequests[i], i + 1);
        }

//        System.out.println("H Sequence (without noise): " + Arrays.toString(h));

        return h;
    }

    /**
     * Adds noise to the actual H sequence to make them look like predictions
     * @param h actual H sequence for the input sequence
     * @param tow this can help decide whether to change a value in H sequence
     * @param omega this can help decide what range of values would be used to replace a real value in H sequence
     */
    void addNoise(int[] h, double tow, int omega) {

        // traverse the actual h sequence
        // with probability tow update h[i]
        // and choose a number uniformly between l = max(i + 1, h[i] − floor(omega/2)) and l + omega (inclusive)
        for (int i = 1; i <= h.length; i ++) {
            if (Math.random() < tow) {
                int minPossibleValue =  Math.max(i + 1, h[i - 1] - (int) (omega / 2.0));
                int maxPossibleValue = minPossibleValue + omega;
                h[i - 1] = minPossibleValue + new Random().nextInt(maxPossibleValue + 1 - minPossibleValue);
            }
        }
//        System.out.println("H Predictions (after adding noise): " + Arrays.toString(h));
    }

    /**
     * runs the BlindOracle algorithm and finds out the number of page faults
     * @param k cache capacity
     * @param requestSequence sequence of page requests
     * @param hPredictions predicted H values
     * @return number of page faults
     */
    int blindOracle(int k, int[] requestSequence, int[] hPredictions) {

        int pageFaults = 0;

        // in the Map "cache", key is the page and value is the H prediction value of the page
        Map<Integer, Integer> cache = new HashMap<>();
        // in the SortedMap "orderingInCache", key is the H prediction value and value is the list of all elements in the cache with that H prediction value
        SortedMap<Integer, List<Integer>> orderingInCache = new TreeMap<>();

        // runs the BlindOracle algorithm using "cache" and "orderingInCache" and finds out the page faults
        for (int i = 0; i < requestSequence.length; i ++) {

            if (!cache.containsKey(requestSequence[i])) {
                pageFaults++;
            }
            updateBlindOracleCache(cache, orderingInCache, i, k, requestSequence, hPredictions);
//            System.out.println("State of BlindOracle cache after processing element at index, " + i + ", is: " + cache);
        }

        return pageFaults;
    }


    /**
     * Class that represents the node in the Doubly LinkedList of LRU Cache
     */
    public class Node {
        int key;
        Node prev;
        Node next;

        public Node() {
            this(0, null, null);
        }

        public Node(int key) {
            this(key, null, null);
        }

        public Node(int key, Node prev, Node next) {
            this.key = key;
            this.prev = prev;
            this.next = next;
        }
    }

    /**
     * runs the LRU algorithm and finds out the number of page faults
     * @param k cache capacity
     * @param pageRequests sequence of page requests
     * @return number of page faults
     */
    int LRU(int k, int[] pageRequests) {

        int pageFaults = 0;

        // LRU cache
        Node[] cacheBeginAndEnd = new Node[2];
        Map<Integer, Node> elementsInCache = new HashMap<>();

        for (int i = 0; i < pageRequests.length; i ++) {
            int pageRequest = pageRequests[i];
            if (!elementsInCache.containsKey(pageRequest)) {
                pageFaults ++;
            }
            updateLRUCache(cacheBeginAndEnd, elementsInCache, k, pageRequest);
//            System.out.println("State of LRU cache after processing element at index, " + i + ", is: " + cache);
        }

        return pageFaults;
    }

    /**
     * runs the Combined algorithm and finds out the number of page faults
     * @param k cache capacity
     * @param requestSequence sequence of page requests
     * @param hPredictions predicted H values
     * @param threshold helps in deciding when to switch between LRU and BlindOracle algorithms
     * @return number of page faults
     */
    int combinedAlg(int k, int[] requestSequence, int[] hPredictions, double threshold) {

        // page faults of combined algorithm
        int combinedAlgPageFaults = 0;

        // LRU page faults and cache to run LRU algorithm
        int lruPageFaults = 0;
        Node[] cacheBeginAndEnd = new Node[2];
        Map<Integer, Node> elementsInCache = new HashMap<>();

        // BlindOracle page faults and cache to run BlindOracle algorithm
        int blindOraclePageFaults = 0;
        Map<Integer, Integer> blindOracleCache = new HashMap<>();
        SortedMap<Integer, List<Integer>> orderingInBlindOracleCache = new TreeMap<>();

        // starting with LRU initially
        boolean usingLRU = true;

        for (int i = 0; i < requestSequence.length; i ++) {

            // running LRU algorithm
            if (!elementsInCache.containsKey(requestSequence[i])) {
                lruPageFaults ++;
                if (usingLRU) {
                    combinedAlgPageFaults ++;
                }
            }
            updateLRUCache(cacheBeginAndEnd, elementsInCache, k, requestSequence[i]);

            // running BlindOracle algorithm
            if (!blindOracleCache.containsKey(requestSequence[i])) {
                blindOraclePageFaults ++;
                if (!usingLRU) {
                    combinedAlgPageFaults ++;
                }
            }
            updateBlindOracleCache(blindOracleCache, orderingInBlindOracleCache, i, k, requestSequence, hPredictions);

            // checking if it has to switch and then switch if necessary
            // add k to the page faults of Combined Algorithm when there is a switch
            // after switching update switch count which can be useful when testing
            if (usingLRU && ((double) lruPageFaults) > (1 + threshold) * ((double) blindOraclePageFaults)) {
                combinedAlgPageFaults = combinedAlgPageFaults + k;
                usingLRU = false;
                switchesFromLRUToBlindOracle ++;
//                System.out.println("Switched from using LRU cache to BlindOracle cache at index, " + i);
            } else if (!usingLRU && ((double) blindOraclePageFaults) > (1 + threshold) * ((double) lruPageFaults)) {
                combinedAlgPageFaults = combinedAlgPageFaults + k;
                usingLRU = true;
                switchesFromBlindOracleToLRU ++;
//                System.out.println("Switched from using BlindOracle cache to LRU cache at index, " + i);
            }
        }

        return combinedAlgPageFaults;
    }

    /**
     * Updates the LRU cache after getting a new page request
     * @param cacheBeginAndEnd contains the beginning and end nodes of the the doubly linked list implementation of the cache
     * @param elementsInCache maps the keys to the nodes of the linked list
     * @param k cache capacity
     * @param pageRequest page requested
     */
    private void updateLRUCache(Node[] cacheBeginAndEnd, Map<Integer, Node> elementsInCache, int k, int pageRequest) {

        if (!elementsInCache.containsKey(pageRequest)) {
            if (elementsInCache.size() == k) {
                elementsInCache.remove(cacheBeginAndEnd[0].key);
                cacheBeginAndEnd[0] = cacheBeginAndEnd[0].next;
                if (cacheBeginAndEnd[0] != null) {
                    cacheBeginAndEnd[0].prev = null;
                } else {
                    cacheBeginAndEnd[1] = null;
                }
            }
            Node newNode = new Node(pageRequest);
            if (cacheBeginAndEnd[0] == null) {
                cacheBeginAndEnd[0] = newNode;
                cacheBeginAndEnd[1] = newNode;
            } else {
                cacheBeginAndEnd[1].next = newNode;
                newNode.prev = cacheBeginAndEnd[1];
                cacheBeginAndEnd[1] = newNode;
            }
            elementsInCache.put(pageRequest, newNode);
        } else {
            Node oldNode = elementsInCache.get(pageRequest);
            Node oldNext = oldNode.next;
            Node oldPrev = oldNode.prev;
            if (oldNext != null) {
                oldNext.prev = oldPrev;
            } else {
                cacheBeginAndEnd[1] = oldPrev;
            }
            if (oldPrev != null) {
                oldPrev.next = oldNext;
            } else {
                cacheBeginAndEnd[0] = oldNext;
            }

            oldNode.next = null;
            oldNode.prev = null;
            if (cacheBeginAndEnd[0] == null) {
                cacheBeginAndEnd[0] = oldNode;
                cacheBeginAndEnd[1] = oldNode;
            } else {
                cacheBeginAndEnd[1].next = oldNode;
                oldNode.prev = cacheBeginAndEnd[1];
                cacheBeginAndEnd[1] = oldNode;
            }
        }
    }

    /**
     * Updates the BlindOracle cache after getting a new page request
     * @param cache BlindOracle cache
     * @param orderingInCache ordering according to H prediction value in the BlindOracle cache
     * @param i index of new page request in the request sequence
     * @param k cache capacity
     * @param requestSequence request sequence
     * @param hPredictions H prediction values that will be used by BlindOracle algorithm
     */
    private void updateBlindOracleCache(Map<Integer, Integer> cache, SortedMap<Integer, List<Integer>> orderingInCache, int i, int k, int[] requestSequence, int[] hPredictions) {
        if (cache.containsKey(requestSequence[i])) {
            updateOrdering(orderingInCache, requestSequence[i], cache.get(requestSequence[i]), hPredictions[i]);
        } else {
            if (i >= k) {
                int furthestHPrediction = orderingInCache.lastKey();
                int elementToEvict = orderingInCache.get(furthestHPrediction).get(0);
                removeFromOrdering(orderingInCache, elementToEvict, furthestHPrediction);
                cache.remove(elementToEvict);
            }
            updateOrdering(orderingInCache, requestSequence[i], null, hPredictions[i]);
        }
        cache.put(requestSequence[i], hPredictions[i]);

    }

    /**
     * Updates orderingInCache when a new page has to be added into the cache or the H prediction value of an existing page has to be updated
     * @param orderingInCache the sorted map that maintains the ordering according to H prediction values
     * @param request the page request
     * @param oldHPrediction the old H prediction value, if the request is present in cache. Else, it will be null
     * @param newHPrediction the new H prediction value of the page
     */
    private void updateOrdering(SortedMap<Integer, List<Integer>> orderingInCache, Integer request, Integer oldHPrediction, Integer newHPrediction) {

        if (oldHPrediction != null) {
            removeFromOrdering(orderingInCache, request, oldHPrediction);
        }

        if (orderingInCache.containsKey(newHPrediction)) {
            orderingInCache.get(newHPrediction).add(request);
        } else {
            orderingInCache.put(newHPrediction, new ArrayList<>(singletonList(request)));
        }
    }

    /**
     * method to remove a page request and its H prediction value from orderingInCache
     * @param orderingInCache the sorted map that maintains the ordering according to H prediction values
     * @param request the page request
     * @param hPrediction the hPrediction value of the request
     */
    private void removeFromOrdering(SortedMap<Integer, List<Integer>> orderingInCache, Integer request, Integer hPrediction) {

        orderingInCache.get(hPrediction).remove(request);
        if (orderingInCache.get(hPrediction).isEmpty()) {
            orderingInCache.remove(hPrediction);
        }
    }

    /**
     * Runs all the tests and prints the result of the tests
     * @param args we do not pass any arguments when running this class, so args will be empty
     */
    public static void main(String[] args) {

        System.out.println("Starting tests");

        List<String> results = new ArrayList<>();

        results.add(test1());
        results.add(test2());
        results.add(test3());
        results.add(test4());
        results.add(test5());
        results.add(test6());
        results.add(test7());
        results.add(test8());
        results.add(test9());
        results.add(test10());
        results.add(test11());
        results.add(test12());
        results.add(test13());
        results.add(test14());
        results.add(test15());
        results.add(test16());
        results.add(test17());
        results.add(test18());
        results.add(test19());

        System.out.println("\nResult: ");

        boolean allTestsPassed = true;
        for (int i = 1; i <= 19; i ++) {
            String result = results.get(i - 1);
            if (!result.equals(SUCCESS)) {
                allTestsPassed = false;
                System.out.println("Test " + i + " failed. Failure: " + result);
            }
        }

        if (allTestsPassed) {
            System.out.println("All tests passed");
        }
    }

    /* Below are the tests
       -> If a test is successful, then it returns "Success"
       -> Else, it returns the reason for the failure */

    // tests whether a valid request sequence is generated
    private static String test1() {
        System.out.println("\n---------Running test1---------");
        Paging paging = new Paging();

        int N = 10;
        int n = 20;
        int k = 3;
        double epsilon = 0.7;

        int[] requestSequence = paging.generateRandomSequence(k, N, n, epsilon);

        // size of the request sequence should be n
        if (requestSequence.length != n) {
            return "Request sequence size is not n";
        }

        // all elements should be in [1, N]
        for (int i = 0; i < n; i ++) {
            if (requestSequence[i] < 1 || requestSequence[i] > N) {
                return "All elements in request sequence should be in [1, N]";
            }
        }

        // first k elements should be 1,2,....,k
        for (int i = 0; i < k; i ++) {
            if (requestSequence[i] != i + 1) {
                return "First k elements should be 1,2,....,k";
            }
        }

        System.out.println("---------Finished test1---------");
        return SUCCESS;
    }

    // tests if the request sequence generated is a random one
    private static String test2() {
        System.out.println("\n---------Running test2---------");
        Paging paging = new Paging();

        int N = 1000;
        int n = 2000;
        int k = 5;
        double epsilon = 0.7;

        int[][] requestSequences = new int[5][n];

        // generates 5 request sequences by calling generateRandomSequence with the same arguments and stores them in requestSequences
        int iteration = 0;
        while (iteration < 5) {
            int[] requestSequence = paging.generateRandomSequence(k, N, n, epsilon);
            for (int i = 0; i < n; i ++) {
                requestSequences[iteration][i] = requestSequence[i];
            }

            iteration ++;
        }

        // checks if any 2 request sequences out of the 5 are the same
        // (because n and N are considerably large, there is really a very minute chance to get 2 same sequences in 5 when Java uses randomness)
        for (int i = 0; i < 5; i ++) {
            for (int j = i + 1; j < 5; j ++) {
                if (Arrays.equals(requestSequences[i], requestSequences[j])) {
                    return "Request sequences " + (i + 1) + ", " + (j + 1) + " are identical";
                }
            }
        }

        System.out.println("---------Finished test2---------");
        return SUCCESS;
    }

    // tests if the request sequence has the amount of locality depending on epsilon
    private static String test3() {
        System.out.println("\n---------Running test3---------");
        Paging paging = new Paging();

        int N = 1000;
        int n = 2000;
        int k = 5;

        // testing the same 10 times to gain confidence, because we use randomness in methods
        int iterations = 10;
        while (iterations > 0) {
            int[] requestSequenceWithHighEpsilon = paging.generateRandomSequence(k, N, n, 0.7);
            int[] requestSequenceWithLowEpsilon = paging.generateRandomSequence(k, N, n, 0.2);

            Set<Integer> distinctElementsInSequenceWithHighEpsilon = new HashSet<>();
            Set<Integer> distinctElementsInSequenceWithLowEpsilon = new HashSet<>();

            for (int i = 0; i < n; i++) {
                distinctElementsInSequenceWithHighEpsilon.add(requestSequenceWithHighEpsilon[i]);
                distinctElementsInSequenceWithLowEpsilon.add(requestSequenceWithLowEpsilon[i]);
            }

            // when we use high epsilon, this should increase the amount of locality
            // increasing amount of locality means choosing the element that is already in the last k distinct elements
            // so, in general, the distinct elements in case of high amount of locality should be lesser than distinct elements when less amount of locality
            if (distinctElementsInSequenceWithHighEpsilon.size() > distinctElementsInSequenceWithLowEpsilon.size()) {
                return "Amount of locality is not as expected";
            }

            iterations --;
        }

        System.out.println("---------Finished test3---------");
        return SUCCESS;
    }

    // tests if Hseq is as expected for a request sequence
    private static String test4() {
        System.out.println("\n---------Running test4---------");
        Paging paging = new Paging();

        int[] requestSequence = new int[] {1, 2, 3, 2, 1, 5, 4, 1, 5, 2};
        // this is the H sequence that I find out using paper for the above request sequence
        int[] expectedHSeq = new int[] {5, 4, 11, 10, 8, 9, 11, 11, 11, 11};

        int[] generatedHSeq = paging.generateH(requestSequence);

        if (!Arrays.equals(generatedHSeq, expectedHSeq)) {
            return "Generated Hseq is not as expected";
        }

        System.out.println("---------Finished test4---------");
        return SUCCESS;
    }

    // tests if each element at index i in Hseq is in [i + 1, n + 1]
    private static String test5() {
        System.out.println("\n---------Running test5---------");
        Paging paging = new Paging();

        int N = 1000;
        int n = 2000;
        int k = 5;
        double epsilon = 0.7;

        int[] requestSequence = paging.generateRandomSequence(k, N, n, epsilon);

        int[] generatedHSeq = paging.generateH(requestSequence);

        // checks if the length of the H sequence is the same as the request sequence
        if (generatedHSeq.length != n) {
            return "Hsequence length is not equal to n which is wrong";
        }

        // checks if every element in the H sequence is in [i + 1, n + 1]
        for (int i = 1; i <= generatedHSeq.length; i ++) {
            if (generatedHSeq[i - 1] < i + 1 || generatedHSeq[i - 1] > n + 1) {
                return "Element at index i in Hseq is not in [i + 1, n + 1]";
            }
        }

        System.out.println("---------Finished test5---------");
        return SUCCESS;
    }

    // tests if each predicted h[i] is in [l = max(i + 1, actual h[i] − floor(omega/2)), l + omega]
    private static String test6() {
        System.out.println("\n---------Running test6---------");
        Paging paging = new Paging();

        int N = 1000;
        int n = 2000;
        int k = 5;
        double epsilon = 0.7;
        double tow = 0.6;
        int omega = 50;

        int[] requestSequence = paging.generateRandomSequence(k, N, n, epsilon);
        int[] hSeq = paging.generateH(requestSequence);

        int[] hPredictions = Arrays.copyOf(hSeq, n);
        paging.addNoise(hPredictions, tow, omega);

        // checks if the length of the H sequence after adding noise remains the same (i.e. n)
        if (hSeq.length != n) {
            return "Predictions length is not equal to n which is wrong";
        }

        // checks if each predicted h[i] is in [l = max(i + 1, actual h[i] − floor(omega/2)), l + omega]
        for (int i = 1; i <= n; i ++) {
            int l = Math.max(i + 1, hSeq[i - 1] - (omega / 2));
            if ((hPredictions[i - 1] < l) || (hPredictions[i - 1] > l + omega)) {
                return "Element at index i in predicted h values is not in [l = max(i + 1, h[i] − floor(omega/2)), l + omega]";
            }
        }

        System.out.println("---------Finished test6---------");
        return SUCCESS;
    }

    // tests if high value of tow adds more noise to Hseq
    private static String test7() {
        System.out.println("\n---------Running test7---------");
        Paging paging = new Paging();

        int N = 1000;
        int n = 2000;
        int k = 5;
        double epsilon = 0.7;
        int omega = 50;

        // testing the same 10 times to gain more confidence as we are using randomness in the methods
        int iterations = 10;
        while (iterations > 0) {
            int[] requestSequence = paging.generateRandomSequence(k, N, n, epsilon);
            int[] hSeq = paging.generateH(requestSequence);

            int[] hPredictionsForHighTow = Arrays.copyOf(hSeq, n);
            int[] hPredictionsForLowTow = Arrays.copyOf(hSeq, n);

            paging.addNoise(hPredictionsForHighTow, 0.7, omega);
            paging.addNoise(hPredictionsForLowTow, 0.2, omega);

            int numOfValuesSameAsOriginalHSeqAfterAddingNoiseWithHighTow = 0;
            int numOfValuesSameAsOriginalHSeqAfterAddingNoiseWithLowTow = 0;
            for (int i = 0; i < n; i ++) {
                if (hSeq[i] == hPredictionsForHighTow[i]) {
                    numOfValuesSameAsOriginalHSeqAfterAddingNoiseWithHighTow ++;
                }
                if (hSeq[i] == hPredictionsForLowTow[i]) {
                    numOfValuesSameAsOriginalHSeqAfterAddingNoiseWithLowTow ++;
                }
            }

            // if we use high value of tow (keeping omega constant), that should change more elements in the actual H sequence
            // which means high tow is increasing the noise
            // Note: We can create cases to fail this by using lower values of N and n and choosing randomness to behave in some way
            // I am taking high values for N and n. Also assuming Java uses randomness which is close to true randomness
            if (numOfValuesSameAsOriginalHSeqAfterAddingNoiseWithHighTow > numOfValuesSameAsOriginalHSeqAfterAddingNoiseWithLowTow) {
                return "High tow did not add more noise";
            }

            iterations --;
        }

        System.out.println("---------Finished test7---------");
        return SUCCESS;
    }

    // tests if high value of omega adds more noise to Hseq -- omega suggests how much the predicted value is deviated from the actual
    private static String test8() {
        System.out.println("\n---------Running test8---------");
        Paging paging = new Paging();

        int N = 1000;
        int n = 2000;
        int k = 5;
        double epsilon = 0.7;
        double tow = 0.6;

        int iterations = 10;
        while (iterations > 0) {
            int[] requestSequence = paging.generateRandomSequence(k, N, n, epsilon);
            int[] hSeq = paging.generateH(requestSequence);

            int[] hPredictionsForHighOmega = Arrays.copyOf(hSeq, n);
            int[] hPredictionsForLowOmega = Arrays.copyOf(hSeq, n);

            paging.addNoise(hPredictionsForHighOmega, tow, 100);
            paging.addNoise(hPredictionsForLowOmega, tow, 20);

            int deviationForHighOmega = 0;
            int deviationForLowOmega = 0;
            for (int i = 0; i < n; i ++) {
                deviationForHighOmega += Math.abs(hSeq[i] - hPredictionsForHighOmega[i]);
                deviationForLowOmega += Math.abs(hSeq[i] - hPredictionsForLowOmega[i]);
            }

            // increasing omega means increasing the range to choose a value from, to replace the actual H value
            // so when using high omega (keeping tow constant), the average deviation of the sequence should be more than when using low omega
            // Note: We can create cases to fail this by using lower values of N and n and choosing randomness to behave in some way
            // I am taking high values for N and n. Also assuming Java uses randomness which is close to true randomness
            if (deviationForLowOmega > deviationForHighOmega) {
                return "High omega did not add more deviation (a parameter to measure noise)";
            }

            iterations --;
        }

        System.out.println("---------Finished test8---------");
        return SUCCESS;
    }

    // tests if BlindOracle works as expected on a given input
    private static String test9() {
        System.out.println("\n---------Running test9---------");
        Paging paging = new Paging();

        int[] pageRequests = new int[] {1, 2, 3, 6, 10, 4, 8, 9, 7, 5, 5, 9, 5, 9, 4, 3, 4, 5, 8, 3};
        int[] hPredictions = new int[] {23, 21, 14, 22, 20, 17, 18, 11, 21, 11, 13, 14, 18, 21, 19, 24, 20, 21, 21, 18};
        int k = 3;

        // I have calculated expected page faults manually on paper
        int expectedPageFaults = 13;

        int pageFaults = paging.blindOracle(k, pageRequests, hPredictions);

        if (pageFaults != expectedPageFaults) {
            return "Page faults not the same as expected";
        }

        System.out.println("---------Finished test9---------");
        return SUCCESS;
    }

    // tests BlindOracle when N = k + 1
    private static String test10() {
        System.out.println("\n---------Running test10---------");
        Paging paging = new Paging();

        int k = 4;

        // N is 5 and n is 20
        int[] pageRequests = new int[] {1, 2, 3, 4, 2, 1, 5, 3, 4, 3, 4, 5, 5, 1, 2, 2, 2, 3, 2, 1};
        int[] hPredictions = new int[] {6, 5, 9, 9, 14, 13, 12, 10, 11, 17, 21, 15, 19, 19, 17, 20, 22, 22, 21, 24};

        // I have calculated expected page faults manually on paper
        int expectedPageFaults = 6;

        int pageFaults = paging.blindOracle(k, pageRequests, hPredictions);

        if (pageFaults != expectedPageFaults) {
            return "Page faults not the same as expected";
        }

        System.out.println("---------Finished test10---------");
        return SUCCESS;
    }

    // tests BlindOracle when N >> k
    private static String test11() {
        System.out.println("\n---------Running test11---------");
        Paging paging = new Paging();

        int k = 4;

        // N is 1000 and n is 20
        int[] pageRequests = new int[] {1, 2, 3, 1, 255, 2, 2, 1, 255, 255, 1, 298, 95, 2, 2, 2, 2, 851, 95, 95};
        int[] hPredictions = new int[] {4, 6, 21, 8, 9, 7, 14, 11, 10, 21, 21, 21, 22, 19, 16, 26, 21, 21, 20, 21};

        // I have calculated expected page faults manually on paper
        int expectedPageFaults = 8;

        int pageFaults = paging.blindOracle(k, pageRequests, hPredictions);

        if (pageFaults != expectedPageFaults) {
            return "Page faults not the same as expected";
        }

        System.out.println("---------Finished test11---------");
        return SUCCESS;
    }

    // generate request sequence, predictions and run BlindOracle on them to test if the page faults are a valid number
    private static String test12() {
        System.out.println("\n---------Running test12---------");
        Paging paging = new Paging();

        int N = 10;
        int n = 20;
        int k = 3;
        double epsilon = 0.7;
        double tow = 0.4;
        int omega = 5;

        int[] pageRequests = paging.generateRandomSequence(k, N, n, epsilon);
        int[] hSequence = paging.generateH(pageRequests);
        paging.addNoise(hSequence, tow, omega);

        int pageFaults = paging.blindOracle(k, pageRequests, hSequence);

        // tests if page faults are in [k, n]
        if (pageFaults < 3 || pageFaults > 20) {
            return "Page faults should be in between k and n (both inclusive)";
        }

        System.out.println("---------Finished test12---------");
        return SUCCESS;
    }

    // tests if LRU works as expected on a given input
    private static String test13() {
        System.out.println("\n---------Running test13---------");
        Paging paging = new Paging();

        int[] pageRequests = new int[] {1, 2, 3, 6, 10, 4, 8, 9, 7, 5, 5, 9, 5, 9, 4, 3, 4, 5, 8, 3};
        int k = 3;

        // I have calculated expected page faults manually on paper
        int expectedPageFaults = 15;

        int pageFaults = paging.LRU(k, pageRequests);

        if (pageFaults != expectedPageFaults) {
            return "Page faults not the same as expected";
        }

        System.out.println("---------Finished test13---------");
        return SUCCESS;
    }

    // tests LRU when N = k + 1
    private static String test14() {
        System.out.println("\n---------Running test14---------");
        Paging paging = new Paging();

        int k = 4;

        // N is 5 and n is 20
        int[] pageRequests = new int[] {1, 2, 3, 4, 2, 1, 5, 3, 4, 3, 4, 5, 5, 1, 2, 2, 2, 3, 2, 1};

        // I have calculated expected page faults manually on paper
        int expectedPageFaults = 9;

        int pageFaults = paging.LRU(k, pageRequests);

        if (pageFaults != expectedPageFaults) {
            return "Page faults not the same as expected";
        }

        System.out.println("---------Finished test14---------");
        return SUCCESS;
    }

    // tests LRU when N >> k
    private static String test15() {
        System.out.println("\n---------Running test15---------");
        Paging paging = new Paging();

        int k = 4;

        // N is 1000 and n is 20
        int[] pageRequests = new int[] {1, 2, 3, 1, 255, 2, 2, 1, 255, 255, 1, 298, 95, 2, 2, 2, 2, 851, 95, 95};

        // I have calculated expected page faults manually on paper
        int expectedPageFaults = 8;

        int pageFaults = paging.LRU(k, pageRequests);

        if (pageFaults != expectedPageFaults) {
            return "Page faults not the same as expected";
        }

        System.out.println("---------Finished test15---------");
        return SUCCESS;
    }

    // generate request sequence, predictions and run CombinedAlg on them to test if the number of page faults is a valid number
    private static String test16() {
        System.out.println("\n---------Running test16---------");
        Paging paging = new Paging();
        resetSwitchesCount();

        int N = 100;
        int n = 2000;
        int k = 4;
        double epsilon = 0.7;
        double tow = 0.3;
        int omega = 5;
        double threshold = 0.3;

        int[] pageRequests = paging.generateRandomSequence(k, N, n, epsilon);
        int[] hSequence = paging.generateH(pageRequests);
        paging.addNoise(hSequence, tow, omega);

        int pageFaults = paging.combinedAlg(k, pageRequests, hSequence, threshold);

        // tests if page faults are >= k
        if (pageFaults < k) {
            return "Page faults should be greater than or equal to k";
        }

        System.out.println("---------Finished test16---------");
        return SUCCESS;
    }

    // tests if CombinedAlg works as expected on a given request sequence and H predictions
    private static String test17() {
        System.out.println("\n---------Running test17---------");
        Paging paging = new Paging();
        resetSwitchesCount();

        int[] pageRequests = new int[] {1, 2, 3, 6, 10, 6, 4, 8, 10, 3, 4, 4, 5, 1, 5, 2, 3, 2, 1, 1};
        int[] hPredictions = new int[] {14, 16, 10, 6, 9, 21, 11, 9, 15, 21, 21, 13, 21, 21, 17, 18, 21, 20, 20, 21};

        int k = 4;
        double threshold = 0.09;

        // I have calculated expected page faults manually on paper
        int expectedPageFaults = 23;

        int pageFaults = paging.combinedAlg(k, pageRequests, hPredictions, threshold);

        if (pageFaults != expectedPageFaults) {
            return "Page faults not the same as expected";
        }

        // As I calculated on paper, there should be 1 swap from LRU to BlindOracle and 1 from BlindOracle to LRU
        if (switchesFromLRUToBlindOracle != 1 && switchesFromBlindOracleToLRU != 1) {
            return "There should be one switch from LRU to BlindOracle and one from BlindOracle to LRU";
        }

        System.out.println("---------Finished test17---------");
        return SUCCESS;
    }

    // more threshold for CombinedAlg should imply fewer switches
    private static String test18() {
        System.out.println("\n---------Running test18---------");
        Paging paging = new Paging();

        int N = 100;
        int n = 10000;
        int k = 4;
        double epsilon = 0.5;
        double tow = 0.7;
        int omega = 50;

        // testing the same 10 times to gain more confidence as we are using randomness in the methods
        int iterations = 10;
        while (iterations > 0) {
            double highThreshold = 0.4;
            double lowThreshold = 0.2;

            int[] pageRequests = paging.generateRandomSequence(k, N, n, epsilon);
            int[] hSequence = paging.generateH(pageRequests);
            paging.addNoise(hSequence, tow, omega);

            resetSwitchesCount();
            int pageFaultsForHighThreshold = paging.combinedAlg(k, pageRequests, hSequence, highThreshold);
            int switchesForHighThreshold = switchesFromLRUToBlindOracle + switchesFromBlindOracleToLRU;

            resetSwitchesCount();
            int pageFaultsForLowThreshold = paging.combinedAlg(k, pageRequests, hSequence, lowThreshold);
            int switchesForLowThreshold = switchesFromLRUToBlindOracle + switchesFromBlindOracleToLRU;

            if (switchesForHighThreshold > switchesForLowThreshold) {
                return "Number of switches with higher threshold should not be more than number of switches with lower threshold";
            }

            iterations --;
        }

        System.out.println("---------Finished test18---------");
        return SUCCESS;
    }

    // more noise in predictions implies fewer switches from LRU to BlindOracle and higher switches from BlindOracle to LRU
    private static String test19() {
        System.out.println("\n---------Running test19---------");
        Paging paging = new Paging();

        int N = 100;
        int n = 10000;
        int k = 4;
        double epsilon = 0.6;
        double threshold = 0.1;

        // testing the same 10 times to gain more confidence as we are using randomness in the methods
        int iterations = 10;
        while (iterations > 0) {

            double highTow = 0.8;
            int highOmega = 250;

            double lowTow = 0.3;
            int lowOmega = 50;

            int[] pageRequests = paging.generateRandomSequence(k, N, n, epsilon);
            int[] hSequence = paging.generateH(pageRequests);
            paging.addNoise(hSequence, highTow, highOmega);

            resetSwitchesCount();
            int pageFaultsForHighNoise = paging.combinedAlg(k, pageRequests, hSequence, threshold);
            int netSwitchesFromLRUToBlindOracleForHighNoise = switchesFromLRUToBlindOracle - switchesFromBlindOracleToLRU;

            hSequence = paging.generateH(pageRequests);
            paging.addNoise(hSequence, lowTow, lowOmega);

            resetSwitchesCount();
            int pageFaultsForLowNoise = paging.combinedAlg(k, pageRequests, hSequence, threshold);
            int netSwitchesFromLRUToBlindOracleForLowNoise = switchesFromLRUToBlindOracle - switchesFromBlindOracleToLRU;

            if (netSwitchesFromLRUToBlindOracleForHighNoise > netSwitchesFromLRUToBlindOracleForLowNoise) {
                return "Net number of switches from LRU to BlindOracle with higher noise should not be more than the net number of switches with lower noise";
            }

            iterations --;
        }

        System.out.println("---------Finished test19---------");
        return SUCCESS;
    }

    // resets the switch count to 0 before running a test for combinedAlg
    private static void resetSwitchesCount() {
        switchesFromLRUToBlindOracle = 0;
        switchesFromBlindOracleToLRU = 0;
    }
}