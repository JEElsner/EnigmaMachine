/**
 * @author Jonathan Elsner
 * 2018-02-22
 *
 * This program encodes, decodes, and cracks codes employed by Nazi Germany with the infamous 'Enigma Machine'
 * 
 * I use a lot of Enigma-related nomenclature in the comments,
 *  the Enigma machine wiki would be a good read: <url>https://en.wikipedia.org/wiki/Enigma_machine</url>
 * 
 * 
 * Plaintext: unencrypted text
 * Ciphertext: encrypted text
 * Cog: one of three rotors used in an Enigma Machine used to substitute text. In this program, I was lazy
 * so each letter is paired with the next one, on actual Enigma Machines, this would not be so.
 */

package com.elsner.crypto;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class EnigmaMachine {
	
	public static void main(String[] args) {
		
		Scanner in = new Scanner(System.in); // The scanner for the user to type in information
		
		int[] cogSettings = new int[3]; // Holds the inputed settings for each cog
		
		for(int i = 0; i < cogSettings.length; i++) { // Cycles through the cogs to receive input for their setting
			
			do { // Used to ensure a valid setting is inputed
				System.out.print("Pick a number between 0 and 25 for the setting of cog #" + i + ": ");
				
				try {
					cogSettings[i] = in.nextInt();
				}catch(InputMismatchException ex) { // Prevents someone from entering in words, instead of integers
					in.nextLine(); // Flushes the words
					cogSettings[i] = -1; // Tells the system the input was invalid
				}
				
			}while(cogSettings[i] < 0 || cogSettings[i] > 25); // Checks to make sure the setting was in range
			
		} // End for()
		
		in.nextLine(); // Flushes the input of newlines (a bit of a kludge, but ok enough)
		System.out.println("Type the plaintext to encrypt or the ciphertext to decrypt:");
		String text = in.nextLine(); // Recieves the message to encrypt/decrypt
		
		// Prints out the converted text
		System.out.println("The converted text is:\n" + convert(text, cogSettings[0], cogSettings[1], cogSettings[2]));
		
		in.close(); // Close the scanner
		
	} // End main()
	
	/**
	 * The Enigma Machine is self-reciprocal, meaning encryption is the same as decryption
	 * 
	 * @param message The text to encrypt/decrypt
	 * @param setting1 The setting for the first cog (between 0 and 25 inclusive)
	 * @param setting2 The setting for the second cog (between 0 and 25 inclusive)
	 * @param setting3 The setting for the third cog (between 0 and 25 inclusive)
	 * @return The converted text
	 */
	public static String convert(String message, int setting1, int setting2, int setting3) {
		
		/*
		 * On the original Enigma machines, the rotors (or cogs) had settings A-Z (represented as 0-25 here).
		 * Because this is in fact a digital Enigma Machine, I suppose the settings could be negative or
		 * greater than 25, and this check could have been omitted, but I digress...
		 * 
		 * Verify the cog setting is in range
		 */
		if(setting1 < 0 || setting2 < 0 || setting3 < 0)
			throw new IllegalArgumentException("Improper Cog Setting!\n(Must be between 0 and 25)");
		if(setting1 > 25 || setting2 > 25 || setting3 > 25)
			throw new IllegalArgumentException("Improper Cog Setting!\\n(Must be between 0 and 25)");
		
		
		/*
		 * Formats the text to be only Alphanumeric and all uppercase. In cryptography, spaces and punctuations
		 * are often removed to further obfuscate the message
		 */
		message = message.toUpperCase();
		message = message.replaceAll("[^A-Z]", "");
		
		/* The reflector that allows the cipher to be self-reciprocal, on military models of the Enigma Machine, 
		 * this could be changed by the user, but I'm lazy, so I decided not to implement it
		 */
		PairList reflector = new PairList("AB CD EF GH IJ KL MN OP QR ST UV WX YZ");
		
		/* The pairings for each of the three cogs
		 * On an Enigma Machine, these could not be changed,
		 * and would be standard, thus, it is not a parameter of the function
		 */
		
		PairList cog1 = new PairList("AK CN EZ VH IJ BL MD SR QP OT UG WX YF");
		PairList cog2 = new PairList("AC EG IJ KM OQ SU WY XZ TV PR LN HF DB");
		PairList cog3 = new PairList("AN BO CP DQ ER FS GT HU IV JW KX LY MZ");
		
		String ciphertext = "";
		
		for(char c : message.toCharArray()) { // Iterate through each character of the message
			
			c = cogSub(c, cog1, setting1);
			c = cogSub(c, cog2, setting2);
			c = cogSub(c, cog3, setting3);
			
			c = reflector.getMatch(c);
			
			c = (char) (cogSub(c, cog3, 0) - setting3);
			if(c < 'A') c = (char) (91 + c - 65);
			c = (char) (cogSub(c, cog2, 0) - setting2);
			if(c < 'A') c = (char) (91 + c - 65);
			c = (char) (cogSub(c, cog1, 0) - setting1);
			if(c < 'A') c = (char) (91 + c - 65);
			
			ciphertext += c; // Add the converted text to the final message
			
			// Increment the rotors, as it would happen in the Enigma Machine
			
			setting1++;
			if(setting1 / 25 == 1) {
				setting1 = 0;
				setting2++;
				
				if(setting2 / 25 == 1) {
					setting2 = 0;
					setting3 = setting3++ % 26;
				}
			} // End if
			
		} // End for
		
		return ciphertext; // Return the completed converted text
		
	} // End convert()
	
	/**
	 * 
	 * @param input The character to substitute
	 * @param cog The PairList used to substitute
	 * @param setting The setting of the cog 
	 * @return The substituted character
	 */
	private static char cogSub(char input, PairList cog, int setting){
		
		/*
		 * References the PairList to find the character to substitute
		 * The input is determined by 'turning' the rotor to the correct setting
		 * The modulus divides by 91, because in ASCII: Z = 90
		 */
		
		int t = (input - 65 + setting) % 26;
		if(t >= 0) return cog.getMatch(t + 65);
		if(t < 0) return cog.getMatch(t + 91);
		
		throw new IllegalStateException("For some reason, the input was not a real number!");
		
	} // End cogSub()
	
	/*
	 * An object to simplify the paring and substitution of characters
	 * 
	 * During the creation of this program, I wanted to keep the program simple and elegant, but ultimately
	 * I decided to create this Object as a method of convenience
	 */
	public static class PairList{
		
		// An unformatted 2D array of the pairs
		private char[][] pairs = new char[13][2];
		
		/**
		 * 
		 * @param pattern A string of 13 pairs of letters delimited by spaces to indicate the substitutions.
		 * The each pair should have unique letters (i.e. not 'AA'), and all 26 alphabets should be represented
		 */
		public PairList(String pattern) {
			
			int i = 0; // A counter to keep track of the current pairs[] index
			
			if(!pattern.toUpperCase().matches("([A-Z]{2,2} ){12,12}[A-Z]{2,2}")) // Verify the pattern is correct
				throw new IllegalArgumentException("Invalid PairList Pattern!");
			
			for(String strPair : pattern.toUpperCase().split("[^A-Z]")){ // Iterate through each pair
				
				// Add the pair to the array
				pairs[i][0] = strPair.charAt(0);
				pairs[i][1] = strPair.charAt(1);
				
				i++; // Increment the counter
				
			} // End for
			
		} // End PairList()
		
		/**
		 * 
		 * @param c the character to substitute
		 * @return the matching character in the list
		 */
		public char getMatch(char c) {
			
			/*
			 * This method isn't actually called, I just put it here to better indicate the parameters of the function.
			 * 
			 * The method below is what is called by the rest of the program.
			 */
			
			// Ensures the character is a letter
			if(!Character.isAlphabetic(c)) throw new IllegalArgumentException((int) c + " is not an alphabet!");
			
			c = Character.toUpperCase(c);
			
			// Loops through the pairs to find which one the character belongs to
			for(char[] pair : pairs) {
				
				// Returns the matching character
				if(pair[0] == c) return pair[1];
				if(pair[1] == c) return pair[0];
				
			} // End for
			
			// Returns nothing if for some reason the character cannot be found.
			return Character.MIN_VALUE;
			
		} // End getMatch(char c)
		
		/**
		 * 
		 * @param c the character to substitute
		 * @return the matching character in the list
		 */
		public char getMatch(int c) {
			// A convenience method to cast an integer representation of a character to the character representation
			// There are probably better, more elegant ways of doing this.
			return getMatch((char) c);
			
		} // End getMatch(int c)
		
	} // End PairList
	
	/**
	 * A simple method to use brute force to crack the enigma
	 * @param ciphertext The encrypted message to be cracked
	 * @param key A portion of the suspected plaintext message
	 * @return An array of possible solutions
	 */
	public static String[] crack(String ciphertext, String key) {
		
		ArrayList<String> solutions = new ArrayList<String>(1);
		
		// Loop through all the rotor settings
		for(int rotor1 = 0; rotor1 < 26; rotor1++) {
			
			for(int rotor2 = 0; rotor2 < 26; rotor2++) {
				
				for(int rotor3 = 0; rotor3 < 26; rotor3++) {
					
					// Try this rotor setting
					String test = convert(ciphertext, rotor1, rotor2, rotor3);
					// Add a solution and the list of solutions, along with the rotor setting
					if(test.contains(key.toUpperCase())) solutions.add(String.format("%1$2s %2$2s %3$2s: %4$2s", rotor1, rotor2, rotor3, test));
					
				} // End for(rotor3)
				
			} // End for(rotor2)
			
		} // End for(rotor1)
		
		// return the array
		return solutions.toArray(new String[] {});
		
	} // End crack()

} // End EnigmaMachine
