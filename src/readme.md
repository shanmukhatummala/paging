Online Paging Problem with Predictions
================================

Instructions to compile and run:
----------------------------------

Steps to run tests:

- For compiling the code, use JDK 17
- Command to run from command-line to compile the source file: javac Paging.java
- Then you can see a .class file (Paging.class)
- Now to run the program use JDK 17 or JRE 17
- Command to run from command-line to run the compiled .class file: java Paging
- After you run the program, all the tests would be run (I have removed some logs to reduce the running time of PagingExperiments)
- Then you can see the test "Result"
- It tells whether all tests are passed or if there are any failures
- The expected result is "All tests passed" which means the code is working as expected
  
Steps to run experiments:

- For compiling the code, use JDK 17
- Please make sure both Paging.java and PagingExperiments.java are in the same location before running below commands
- Command to run from command-line to compile the source files: javac Paging.java PagingExperiments.java
- Then you can see .class files (PagingExperiments.class and Paging.class)
- Now to run the program, PagingExperiments, use JDK 17 or JRE 17
- Command to run from command-line to run the compiled .class file: java PagingExperiments
- After you run the program, all the experiments would be run
- It took 3 minutes in my laptop for these experiments to run
- Then you can see 8 .csv files at the location where you ran the code
- From these csv files, we can generate the plots
- I have used Python to generate plots from csv files, as I don't know about Java libraries for creating plots
- Please see https://colab.research.google.com/drive/182y2-bMROQm53lQSQzi5sH_NyICvhNaz to check my python program for generating plots
- (I do not have Python runtime environment in my laptop, so I used Google Colab. Please run the program in Google Colab, by using the csv files that have been generated, to get the plots)

Important Methods:
------------------

Details of each important method and some description about how they are implemented

1. generateRandomSequence(k, N, n, epsilon):
    - Generates a random request sequence
    - The first k requests are to pages 1, 2, . . . , k
    - Uses epsilon for deciding the amount of locality
    - Implementation details are explained within the code

2. generateH(pageRequests):
    - Generates the actual H sequence by looking at the pageRequests
    - Implementation details are explained within the code

3. addNoise(h, tow, omega):
    - Adds noise to the actual H sequence to make them look like predictions
    - Noise can be thought of something like how many values will be changed and how much the change will be when a value is changed
    - Tow is the probability with which a H value at an index will be updated
    - Omega helps decide what range of values would be used to replace a real value in H sequence
    - After this runs the H sequence looks like they are predictions
    - Implementation details are explained within the code

4. blindOracle(k, requestSequence, hPredictions):
    - Runs the BlindOracle algorithm and finds out the number of page faults
    - Used a Map to store the elements in the cache and their H prediction values
    - Used a SortedMap to store the H prediction values in sorted order. Key is the H prediction value and value is the list of all elements in the cache with that H prediction value. This List is ordered i.e., if an element comes to cache then it's added to the end of the list OR if an element's H prediction value is changed then that element is added to the end of the List that belongs to the new H prediction value. We always evict the first element in the list
    - That means when the cache is full, an element which got the highest H prediction value first, will be evicted
    - Methods like updateBlindOracleCache, updateOrdering and removeFromOrdering are used to implement blindOracle method 
    - Implementation details are explained within the code

5. LRU(k, pageRequests)
    - Runs the LRU algorithm and finds out the number of page faults
    - Used a doubly linked list which serves as a queue and a map that maps the keys to the nodes in the linked list, to implement the cache
    - When the cache is full we evict the element from the cache which has the lowest index (indicates it is the least recently used)
    - The updateLRUCache method is called to update the cache
    - Implementation details are explained within the code and chose variable names that explain what is happening

6. combinedAlg(k, requestSequence, hPredictions, threshold)
    - Runs the Combined algorithm and finds out the number of page faults
    - LRU and BlindOracle are implemented for each page request in the request sequence
    - Keeps track of page faults in LRU and page faults in BlindOracle as well as the CombinedAlg page faults
    - Implemented the logic of switching between both LRU and BlindOracle
    - This takes help of the same methods that LRU and BlindOracle use to implement those algorithms
    - Implementation details are explained within the code

7. main(args):
    - Runs all the tests and displays the results

Tests:
--------

If a test is successful, then it returns "Success". Else, it returns the reason for the failure

1. test1():
    - tests whether a valid request sequence is generated
    - checks if size of the request sequence is n
    - checks if all elements are in [1, N]
    - checks if first k elements are 1,2,....,k

2. test2():
    - tests if the request sequence generated is a random one

3. test3():
    - tests if the request sequence has the amount of locality depending on epsilon

The above 3 tests confirm that generateRandomSequence is working as expected

4. test4():
    - tests if Hseq is as expected for a request sequence

5. test5():
    - tests if each element at index i in Hseq is in [i + 1, n + 1]

The above 2 tests confirm that generateH is working as expected

6. test6():
    - tests if each predicted h[i] is in [l = max(i + 1, actual h[i] − floor(omega/2)), l + omega]

7. test7():
    - tests if high value of tow adds more noise to Hseq
    - tested multiple times using iterations
    - more details are written in the test in the form of comments

8. test8():
    - tests if high value of omega adds more noise to Hseq -- omega suggests how much the predicted value is deviated from the actual
    - tested multiple times using iterations
    - more details are written in the test in the form of comments

The above 3 tests confirm that addNoise is working as expected

9. test9():
    - tests if BlindOracle works as expected on a given input
    - this covers cases like:
        - adding elements to cache when it is not full
        - updating H value of an element already in the cache
        - evicting the element with highest H prediction value when the cache is full and adding the new request into the cache
        - when 2 elements have the same and highest H prediction value, evict the element which got the highest H-prediction value first

10. test10():
    - tests BlindOracle when N = k + 1

11. test11():
    - tests BlindOracle when N >> k
    -  this covers a corner case not covered before:
        1. an element is present in the cache with some H-value and the cache is full
        2. a new element came with highest H prediction value
        3. the H prediction value of the element in point 1 is now updated, and it equals the H prediction value of the element in point 2 which is the highest
        4. now a new request comes and one has to be evicted
        5. the element which got the highest H prediction value first will be evicted, which means the element we discussed in point 2 will be evicted

12. test12():
    - runs all the methods in order -- generateRandomSequence, generateH, addNoise and blindOracle
    - checks if the page faults is a valid number

The above 4 tests confirm that blindOracle is working as expected

13. test13():
    - tests if LRU works as expected on a given input
    - this covers cache hit, cache miss, and evicting the least recently used element when the cache is full

14. test14():
    - tests LRU when N = k + 1

15. test15():
    - tests LRU when N >> k

The above 3 tests confirm that LRU is working as expected

16. test16():
    - tests if the number of page faults given by the Combined Algorithm is a valid number
    - generates the request sequence, and finds out the actual H values and add noise to them
    - then calls Combined Algorithm to get the number of page faults
    - basically tests if the whole flow is okay and the output is a valid number

17. test17():
    - tests if CombinedAlg works as expected on a given request sequence and H prediction values
    - i have designed the request sequence and H predictions in such a way that there will be 2 swaps, one from LRU to BlindOracle and one from BlindOracle to LRU
    - this tests if the swapping is done as expected
    - also checks if the page faults for the give input as expected

18. test18():
    - this tests if the threshold that we give to CombinedAlg is working as expected
    - for the same request sequence and H prediction values, using high threshold should result in less swaps and using less threshold should result in more swaps

19. test19():
    - tests if more noise in predictions implies fewer switches from LRU to BlindOracle and higher switches from BlindOracle to LRU
    - when we run the Combined algorithm for the same request sequence with different H prediction values:
        1. we can define the net number of switches from LRU to BlindOracle as (num of switches from LRU to BlindOracle - num of switches from BlindOracle to LRU)
        2. so, net number of switches from LRU to BlindOracle with higher noise should not be more than net number of switches from LRU to BlindOracle with lower noise. 

The above 4 tests confirm that CombinedAlg is working as expected

Experiments:
-------------

- I have varied cache size, omega, epsilon and tow, one at a time, over a range of values and ran a batch of 100 trails for each value
- Each trend is divided into 2 regimes as given the project description
- After obtaining the average page faults for the batch of trails at each point for OPT, BlindOracle, LRU and Combined, I have put them in csv files
- The code is present in PagingExperiments. I have followed the same process as given in the project description and explained the implementation details within the code
- Used those csv files to create the plots. Used Python to generate plots from csv files, as I don't know how to create plots in Java
- Please see https://colab.research.google.com/drive/182y2-bMROQm53lQSQzi5sH_NyICvhNaz to check my python program for generating plots


More details about implementation are added in the form of comments and JavaDoc within the code. Please see them for further information.
