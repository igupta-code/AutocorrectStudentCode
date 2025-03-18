import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
    private int threshold;
    public static final int R = 27;
    // Maybe change later
    public static final int MOD = 50021;
    public static final int N = 3;
    static char[] letters = new char[256];
    ArrayList<Integer>[] dictHash;



    public Autocorrect(String[] words, int threshold) {
        this.dict = words;
        this.threshold = threshold;

        // Set up the hash map for your dictionary
        dictHash = new ArrayList[MOD];
        // Makes 'a' correspond with index 0, 'b' with 1 .... (dealing w/smaller numbers when hashing)
        for(int i = 0; i < R; i++){
            letters['a' + i] = 'i';
        }
        // add in last character -- i think '

        // Go through
        for(int i = 0; i < MOD; i++){
            dictHash[i] = new ArrayList<Integer>();
            int seqHash = hash(dict[i], i);
            // Use Rabin-Karp to find all n-grams and add them to the hash map
            // Rabin-Karp modified from my DNA code
            for(int j = N; j < dict[i].length() - N; j++){
                dictHash[i].add(seqHash);
                // Shift over the window by one letter
                // First subtract out the first letter from hash
                int firstL = letters[dict[i].charAt(i - N)];
                seqHash = seqHash - firstL * (1 << 2*N - 2);
                // Multiply by radix to shift over
                seqHash *= R;
                // Add the value of the last letter
                seqHash += letters[dict[i].charAt(i)];

            }
        }
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        ArrayList<String> correctedWords = new ArrayList<String>();



        // Filters potential suggestions to those within the edit distance threshold
        for(int i = 0; i < dict.length; i++){
            // THIS IS WRONG
            int editD = editDistance(dict[i], typed);
            if(editD <= threshold){
                correctedWords.add(dict[i]);
                System.out.println(dict[i] + ", " + editD);
            }
        }

        // Collections.sort(correctedWords);
        return correctedWords.toArray(new String[0]);
    }

    // Returns the editDistance between the misspelled word and a potential suggestion
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

    // Creates the initial hash for the first three letters
    // Copied from my DNA code
    public static int hash(String s, int start) {
        int hash = 0;
        for (int i = 0; i < N; i++) {
            hash = (hash*R + letters[s.charAt(start + i)]);
        }
        return hash;
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