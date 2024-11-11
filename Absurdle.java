import java.util.*;
import java.io.*;

public class Absurdle  {
    public static final String GREEN = "🟩";
    public static final String YELLOW = "🟨";
    public static final String GRAY = "⬜";

    // [[ ALL OF MAIN PROVIDED ]]
    public static void main(String[] args) throws FileNotFoundException {
        Scanner console = new Scanner(System.in);
        System.out.println("Welcome to the game of Absurdle.");

        System.out.print("What dictionary would you like to use? ");
        String dictName = console.next();

        System.out.print("What length word would you like to guess? ");
        int wordLength = console.nextInt();

        List<String> contents = loadFile(new Scanner(new File(dictName)));
        Set<String> words = pruneDictionary(contents, wordLength);

        List<String> guessedPatterns = new ArrayList<>();
        while (!isFinished(guessedPatterns)) {
            System.out.print("> ");
            String guess = console.next();
            String pattern = recordGuess(guess, words, wordLength);
            guessedPatterns.add(pattern);
            System.out.println(": " + pattern);
            System.out.println();
        }
        System.out.println("Absurdle " + guessedPatterns.size() + "/∞");
        System.out.println();
        printPatterns(guessedPatterns);
    }


    // [[ PROVIDED ]]
    // Prints out the given list of patterns.
    // - List<String> patterns: list of patterns from the game
    public static void printPatterns(List<String> patterns) {
        for (String pattern : patterns) {
            System.out.println(pattern);
        }
    }


    // [[ PROVIDED ]]
    // Returns true if the game is finished, meaning the user guessed the word. Returns
    // false otherwise.
    // - List<String> patterns: list of patterns from the game
    public static boolean isFinished(List<String> patterns) {
        if (patterns.isEmpty()) {
            return false;
        }
        String lastPattern = patterns.get(patterns.size() - 1);
        return !lastPattern.contains("⬜") && !lastPattern.contains("🟨");
    }


    // [[ PROVIDED ]]
    // Loads the contents of a given file Scanner into a List<String> and returns it.
    // - Scanner dictScan: contains file contents
    public static List<String> loadFile(Scanner dictScan) {
        List<String> contents = new ArrayList<>();
        while (dictScan.hasNext()) {
            contents.add(dictScan.next());
        }
        return contents;
    }


    //Behavior:
    //  - this method prunes the dictionary so that only words that are of a user-inputted
    //    length are used for the game
    //Exceptions:
    //  - IllegalArgumentException: thrown if the user-inputted word length is less than 1
    //Returns:
    //  - Set<String>: returns a set of strings of words from the dictionary that are equal
    //    to the user-inputted word length
    //Parameters:
    //  - contents: a list of all the words from the dictionary file
    //  - wordLength: a user-inputted number for the desired length of words to be used for 
    //    the game
    public static Set<String> pruneDictionary(List<String> contents, int wordLength) {
        if(wordLength < 1) {
           throw new IllegalArgumentException(); 
        }
        
        Set<String> result = new HashSet<>();
        for(String word : contents) {
            if(word.length() == wordLength) {
                result.add(word);
            }
        }
        return result;
    }


    //Behavior:
    //  - groups the possible target words with their corresponding patterns and returns the
    //    pattern which appears most frequently
    //Exceptions:
    //  - IllegalArgumentException: thrown if there are no words left in the possible words list
    //    or if the guessed word's length is not equal to the word length being used for the game
    //Returns:
    //  - String: returns the pattern associated with the most target words
    //Parameters:
    //  - guess: a user-inputted string representing their guess for what the target word is
    //  - words: a set of words which are possible target words
    //  - wordLength: a user-inputted number for the desired length of words to be used for 
    //    the game
    public static String recordGuess(String guess, Set<String> words, int wordLength) {
        if(words.isEmpty() || guess.length() != wordLength) {
            throw new IllegalArgumentException();
        }

        Map<String, Set<String>> patternGroups = new TreeMap<>();

        //adding the words to the map like so: pattern, words corresponding to pattern
        for (String word : words) {
            String pattern = patternFor(word, guess);
            if(!patternGroups.containsKey(pattern)) {
                patternGroups.put(pattern, new TreeSet<>());
            }
            patternGroups.get(pattern).add(word);
        }

        String bestPattern = mostCommonPattern(patternGroups);

        //narrows choices to only words that correspond to the most common pattern for
        //future rounds
        words.clear();
        for(String word : patternGroups.get(bestPattern)) {
            words.add(word);
        }

        return bestPattern;
    }


    //Behavior:
    //  - finds which pattern in the map corresponds to the most words in the set
    //Returns:
    //  - String: returns the pattern associated with the most words
    //Parameters:
    //  - patternGroups: a map with a key representing an emoji pattern and a value
    //    which represents the strings which would make that pattern
    public static String mostCommonPattern(Map<String, Set<String>> patternGroups) {
        String bestPattern = "";
        int maxSize = 0;
        for (String pattern : patternGroups.keySet()) {
            int size = patternGroups.get(pattern).size();
            if (size > maxSize) {
                bestPattern = pattern;
                maxSize = size;
            }
        } 
        return bestPattern;
    }


    //Behavior:
    //  - this method finds the pattern of a guess compared to the target word (target word is
    //    decided by recordGuess method)
    //Returns:
    //  - String: returns the emoji pattern of the guess corresponding to the number of 
    //    exact matches and approximate matches in the guess to the target word
    //Parameters:
    //  - word: a string representing the target word which the user is trying to guess
    //  - guess: a user-inputted string representing their guess for what the target word is
    public static String patternFor(String word, String guess) {
        List<String> pattern = new ArrayList<>();
        Map<Character, Integer> count = new TreeMap<>();
        
        for(int i = 0; i < guess.length(); i++) {
            pattern.add(GRAY);

            char letter = word.charAt(i);
            if(!count.containsKey(letter)) {
                count.put(letter, 1);
            } else {
                count.put(letter, count.get(letter) + 1);
            }

        }

        for(int i = 0; i < guess.length(); i++) {
            char letter = guess.charAt(i);
            if(word.charAt(i) == letter) {
                pattern.set(i, GREEN);
                count.put(letter, count.get(letter) - 1);
                if (count.get(letter) == 0) {
                    count.remove(letter);
                }
            }
        }

        for(int i = 0; i < guess.length(); i++) {
            char letter = guess.charAt(i);
            if(!pattern.get(i).equals(GREEN)) {
                if (count.containsKey(letter) && count.get(letter) > 0) {
                    pattern.set(i, YELLOW);
                    count.put(letter, count.get(letter) - 1);
                    if (count.get(letter) == 0) {
                        count.remove(letter);
                    }
                }
            }
        }

        return lToS(pattern);
    }


    //Behavior:
    //  - this method turns a list of strings into a single string
    //Returns:
    //  - String: returns the list of strings all together, no spaces
    //Parameters:
    //  - List<String>: a list of strings to be converted
    public static String lToS(List<String> list) {
        String string = "";
        for(String word : list) {
            string = string + word;
        }
        return string;
    }
}
