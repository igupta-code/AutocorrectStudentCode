import jdk.jfr.Threshold;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 * @author Zach Blick
 * @author Isha Gupta
 */
public class Autocorrect {

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */
    private String[] dict;
    private static int THRESHOLD = 2;

    public static final int R = 27;
    public static final int MOD = 50021;
    public static final int N = 3;
    static char[] letters = new char[256];
    ArrayList<String>[] dictHash;
    public ArrayList<String> smallWords;

    // Constructor!
    public Autocorrect(String[] words, int threshold) {
        this.dict = words;

        // Set up the hash map for your dictionary
        dictHash = new ArrayList[MOD];
        smallWords = new ArrayList<String>();
        // Makes 'a' correspond with index 0, 'b' with 1 .... (dealing w/smaller numbers when hashing)
        for(int i = 0; i < R-1; i++){
            letters['a' + i] = 'i';
        }
        letters['\''] = R-1;

        // Go through the dictionary to add all dictionary n-grams to Hash map
        for(int i = 0; i < dict.length; i++){
            // Put all words with length less than 3 into an arraylist
            if(dict[i].length() <= N){
                smallWords.add(dict[i]);
            }
            else{
                // Find initial hash for the word
                int seqHash = hash(dict[i]);

                // Go through word to find all n-grams and add them to Hash map (below is modified from my DNA code)
                for(int j = N; j < dict[i].length(); j++) {
                    // Initialize arraylist if it hasn't been already
                    if (dictHash[seqHash] == null) dictHash[seqHash] = new ArrayList<String>();
                    dictHash[seqHash].add(dict[i]);

                    // Shift over the window by one letter by:
                    seqHash = shift(j, dict[i], seqHash);
                }
            }
        }
    }

    public static void main(String[] args){
        String dict[] = loadDictionary("large");

        System.out.println("How do you spell: ");
        Scanner scanner = new Scanner(System.in);
        String misspelt = scanner.nextLine();

        Autocorrect corrector = new Autocorrect(dict, THRESHOLD);
        // scanner.nextLine();
        while(!misspelt.isEmpty()){
            String[] toPrint = corrector.runTest(misspelt);
            if(toPrint.length == 0) System.out.println("No matches for this word.");
            else if(toPrint[0].equals(misspelt)) System.out.println("Your word is spelt correctly!");
            else{
                System.out.println("Did you mean: ");
                for(String word: toPrint){
                    System.out.println(word);
                }
            }
            System.out.println("How do you spell: ");
            misspelt = scanner.nextLine();
        }
    }


    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        ArrayList<String>[] correctedWords = new ArrayList[THRESHOLD+1];

        // Find your candidate words:
        ArrayList<String> candidates = new ArrayList<String>();
        for(int i = 0; i <= THRESHOLD; i++){
            correctedWords[i] = new ArrayList<String>();
        }

        // Create hashes for your word:
        int seqHash = hash(typed);
        for(int j = N; j < typed.length(); j++){
            // Search for the seqHash in dictHash, then shift the window to the next letter
            candidates.addAll(dictHash[seqHash]);
            seqHash = shift(j, typed, seqHash);
        }

        // Remove duplicates
        candidates = removeDuplicates(candidates);
        // Adds all the small words into candidates
        if(typed.length() <= N+THRESHOLD){
            candidates.addAll(smallWords);
        }

        // Filters out words any words in candidates not within edit distance
        for(int i = 0; i < candidates.size(); i++){
            int editD = editDistance(candidates.get(i), typed);
            if(editD <= THRESHOLD){
                correctedWords[editD].add(candidates.get(i));
            }
        }

        ArrayList<String> toReturn =  new ArrayList<>();
        // Alphabetically sorts each of the lists (which are already organized by edit distance)
        for(int i = 0; i <= THRESHOLD; i++) {
            Collections.sort(correctedWords[i]);
            toReturn.addAll(correctedWords[i]);
        }

        return toReturn.toArray(new String[0]);
    }

    // Returns the editDistance between the misspelled word and a potential suggestion (using tabulation)
    public int editDistance(String dictW, String word){
        int[][] table = new int[dictW.length()+1][word.length()+1];

        for(int i = 0; i < dictW.length()+1; i++){
            for(int j = 0; j < word.length()+1; j++){
                // If one string is empty, the difference is the length of the other string
                if(i==0) table[0][j] = j;
                else if(j==0) table[i][0] = i;

                // If the last letters are equal, your edit dist is the same as that of the words w/o the last letters
                else if(dictW.charAt(i-1) == word.charAt(j-1)) table[i][j] = table[i-1][j-1];

                // And 1 to the min length of the lengths of the last three strings we compared
                else{
                    int del = table[i][j-1];
                    int add = table[i-1][j];
                    int sub = table[i-1][j-1];

                    table[i][j] = 1+Math.min(Math.min(del, add), sub);
                }
            }
        }
        return table[dictW.length()][word.length()];
    }

    // Creates the initial hash for the first three letters (taken from DNA code)
    public static int hash(String s){
        int hash = 0;
        for (int i = 0; i < N; i++){
            hash = (hash*R + letters[s.charAt(i)]) % MOD;
        }
        return hash;
    }
    // Shifts the hash over by one
    public static int shift(int i, String word, int seqHash){
        // Subtract out the first letter, xR to shift, add next number, and then mod
        int firstL = letters[word.charAt(i - N)];
        seqHash = seqHash - firstL * (1 << 2*N - 2);
        seqHash *= R;
        seqHash += letters[word.charAt(i)];
        return (seqHash + MOD) % MOD;
    }

    // Method explained by Tony and chat.GPT
    public static ArrayList<String> removeDuplicates(ArrayList<String> list) {
        // Use LinkedHashSet to maintain order and remove duplicates
        return new ArrayList<>(new LinkedHashSet<>(list));
    }

    /**
     * Loads a dictionary of words from the provided textfiles in the dictionaries directory.
     * @param dictionary The name of the textfile, [dictionary].txt, in the dictionaries directory.
     * @return An array of Strings containing all words in alphabetical order.
     */
    private static String[] loadDictionary(String dictionary)  {
        try {
            String line;
            BufferedReader dictReader = new BufferedReader(new FileReader("dictionaries/" + dictionary + ".txt"));
            line = dictReader.readLine();

            // Update instance variables with test data
            int n = Integer.parseInt(line);
            String[] words = new String[n];

            for (int i = 0; i < n; i++) {
                line = dictReader.readLine();
                words[i] = line;
            }
            return words;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}