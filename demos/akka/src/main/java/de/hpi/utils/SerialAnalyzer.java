package de.hpi.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SerialAnalyzer {
	
	private static int maxPassword = 1000000;
	private static int prefixLength = 5;
	
	public void analyze(String[] lines) {
		List<String> names = new ArrayList<>(42);
		List<String> secrets = new ArrayList<>(42);
		List<String> sequences = new ArrayList<>(42);
		
		for (String line : lines) {
			String[] lineSplit = line.split(";");
			names.add(lineSplit[1]);
			secrets.add(lineSplit[2]);
			sequences.add(lineSplit[3]);
		}
		
		long t = System.currentTimeMillis();
		int[] cleartexts = this.decrypt(secrets);
		System.out.println("Decryption: " + (System.currentTimeMillis() - t));

		t = System.currentTimeMillis();
		int[] prefixes = this.solve(cleartexts);
		System.out.println("Linear Combination: " + (System.currentTimeMillis() - t));
			
		t = System.currentTimeMillis();
		int[] partners = this.match(sequences);
		System.out.println("Substring: " + (System.currentTimeMillis() - t));
			
		t = System.currentTimeMillis();
		List<String> hashes = this.encrypt(partners, prefixes, this.prefixLength);
		System.out.println("Encryption: " + (System.currentTimeMillis() - t));
			
		for (int i = 0; i < names.size(); i++)
			System.out.println((i + 1) + ";" + names.get(i) + ";" + cleartexts[i] + ";" + prefixes[i] + ";" + (partners[i] + 1) + ";" + hashes.get(i));
	}
	
	private int[] decrypt(List<String> secrets) {
		int[] cleartexts = new int[secrets.size()];
		for (int i = 0; i < secrets.size(); i++)
			cleartexts[i] = this.unhash(secrets.get(i));
		return cleartexts;
	}
		
	public static int unhash(String hexHash) {
		for (int i = 0; i < maxPassword; i++)
			if (hash(i).equals(hexHash))
				return i;
		throw new RuntimeException("Cracking failed for " + hexHash);
	}

	public static String hash(int number) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashedBytes = digest.digest(String.valueOf(number).getBytes("UTF-8"));
			
			StringBuffer stringBuffer = new StringBuffer();
			for (int i = 0; i < hashedBytes.length; i++) {
				stringBuffer.append(Integer.toString((hashedBytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			return stringBuffer.toString();
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
		
	private int[] solve(int[] numbers) {
		for (long a = 0; a < Long.MAX_VALUE; a++) {
		    String binary = Long.toBinaryString(a);
		    
		    int[] prefixes = new int[62];
		    for (int i = 0; i < prefixes.length; i++)
		    	prefixes[i] = 1;
		    
		    int i = 0;
		    for (int j = binary.length() - 1; j >= 0; j--) {
		    	if (binary.charAt(j) == '1')
		    		prefixes[i] = -1;
		    	i++;
		    }
		    
		    if (this.sum(numbers, prefixes) == 0)
				return prefixes;
		}
		
		throw new RuntimeException("Prefix not found!");
	}
	
	private int sum(int[] numbers, int[] prefixes) {
		int sum = 0;
		for (int i = 0; i < numbers.length; i++)
			sum += numbers[i] * prefixes[i];
		return sum;
	}
		
	private int[] match(List<String> sequences) {
		int[] partners = new int[sequences.size()];
		for (int i = 0; i < sequences.size(); i++)
			partners[i] = this.longestOverlapPartner(i, sequences);
		return partners;
	}
		
	public static int longestOverlapPartner(int thisIndex, List<String> sequences) {
		int bestOtherIndex = -1;
		String bestOverlap = "";
		for (int otherIndex = 0; otherIndex < sequences.size(); otherIndex++) {
			if (otherIndex == thisIndex)
				continue;
			
			String longestOverlap = longestOverlap(sequences.get(thisIndex), sequences.get(otherIndex));

			if (bestOverlap.length() < longestOverlap.length()) {
				bestOverlap = longestOverlap;
				bestOtherIndex = otherIndex;
			}
		}
		return bestOtherIndex;
	}
		
	private static String longestOverlap(String str1, String str2) {
        if (str1.isEmpty() || str2.isEmpty()) 
        	return "";
        
        if (str1.length() > str2.length()) {
            String temp = str1;
            str1 = str2;
            str2 = temp;
        }

        int[] currentRow = new int[str1.length()];
        int[] lastRow = str2.length() > 1 ? new int[str1.length()] : null;
        int longestSubstringLength = 0;
        int longestSubstringStart = 0;

        for (int str2Index = 0; str2Index < str2.length(); str2Index++) {
            char str2Char = str2.charAt(str2Index);
            for (int str1Index = 0; str1Index < str1.length(); str1Index++) {
                int newLength;
                if (str1.charAt(str1Index) == str2Char) {
                    newLength = str1Index == 0 || str2Index == 0 ? 1 : lastRow[str1Index - 1] + 1;
                    
                    if (newLength > longestSubstringLength) {
                    	longestSubstringLength = newLength;
                    	longestSubstringStart = str1Index - (newLength - 1);
                    }
                } else {
                    newLength = 0;
                }
                currentRow[str1Index] = newLength;
            }
            int[] temp = currentRow;
            currentRow = lastRow;
            lastRow = temp;
        }
        return str1.substring(longestSubstringStart, longestSubstringStart + longestSubstringLength);
	}
	
	private List<String> encrypt(int[] partners, int[] prefixes, int prefixLength) {
		List<String> hashes = new ArrayList<>(partners.length);
		for (int i = 0; i < partners.length; i++) {
			int partner = partners[i];
			String prefix = (prefixes[i] > 0) ? "1" : "0";
			hashes.add(this.findHash(partner, prefix, prefixLength));
		}
		return hashes;
	}
	
	public static String findHash(int content, String prefix, int prefixLength) {
		StringBuilder fullPrefixBuilder = new StringBuilder();
		for (int i = 0; i < prefixLength; i++)
			fullPrefixBuilder.append(prefix);
		
		Random rand = new Random(13);
		
		String fullPrefix = fullPrefixBuilder.toString();
		int nonce = 0;
		while (true) {
			nonce = rand.nextInt();
			String hash = hash(content + nonce);
			if (hash.startsWith(fullPrefix))
				return hash;
		}
	}
}
