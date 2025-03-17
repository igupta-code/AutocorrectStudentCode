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
    public Autocorrect(String[] words, int threshold) {
        this.dict = words;
        this.threshold = threshold;
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        ArrayList<String> correctedWords = new ArrayList<String>();

        for(int i = 0; i < dict.length; i++){
            // THIS IS WRONG
            int editD = editDistance(dict[i], typed);
            if(editD <= threshold){
                correctedWords.add(dict[i]);
                System.out.println(dict[i] + ", " + editD);
            }
        }

        return new String[0];
        // return new correctedWords.toArray(new String[correctedWords.size()]);
    }

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