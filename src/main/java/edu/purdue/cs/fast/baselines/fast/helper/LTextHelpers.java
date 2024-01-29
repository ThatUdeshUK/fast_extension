/**
 * Copyright Jul 5, 2015
 * Author : Ahmed Mahmood
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.purdue.cs.fast.baselines.fast.helper;

import java.util.*;

public class LTextHelpers {
	public static final String[] SET_VALUES = new String[] { "a", "ab", "abt", "attwaction", "attwicted", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint",
			"all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere",
			"apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "b", "b4", "bcnu", "be", "became", "because", "become", "becomes",
			"becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "bff", "bfn", "bgd", "both", "br", "brb", "brief", "btw", "bulltwit", "but", "by",
			"b4n", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "chk", "cld", "clearly", "clk", "co", "com", "come", "comes", "concerning", "consequently", "consider",
			"considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "cre8", "currently", "cya", "da", "deets", "detweet", "definitely", "described", "despite", "dhmu", "did", "didnt", "different",
			"do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "drunktwittering", "during", "dweet", "eavestweeting", "each", "edu", "eg", "egotwistical", "eight", "either", "else", "elsewhere", "em", "eml", "ema",
			"enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "fab", "faq", "far", "fav", "few", "ff", "f2f", "fifth",
			"first", "five", "followed", "follower", "following", "follows", "fomo", "for", "former", "formerly", "forth", "four", "from", "ftl", "ftw", "further", "furthermore", "get", "gets", "getting", "given", "gives", "gl", "go",
			"goes", "going", "gone", "got", "gotten", "greetings", "gtma", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter",
			"hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "h2h", "i", "ic", "icymi", "id", "idk", "ill", "im", "ive", "ie", "if", "ignored", "ijk",
			"immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "iono", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "jooc", "keep",
			"keeps", "kept", "kk", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "lol", "look", "looking", "looks", "ltd", "mainly",
			"many", "may", "maybe", "me", "mean", "meanwhile", "mention", "merely", "miff", "might", "mistweet", "more", "moreover", "most", "mostly", "mrt", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly",
			"necessary", "need", "needs", "neither", "never", "nevertheless", "new", "neweeter", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "nts", "nv", "nvm",
			"obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own",
			"particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "prt", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless",
			"regards", "relatively", "respectively", "retweet", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible",
			"sent", "serious", "seriously", "seven", "several", "shall", "she", "shm", "should", "shouldnt", "since", "six", "snm", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat",
			"somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tbh", "tbt", "tftf", "tmb", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that",
			"thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre",
			"theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying",
			"tty", "twabe", "twaffic", "twalking", "tweeple", "tweet", "tweeter", "tweetroduce", "tweetsult", "twerminology", "twettiquette", "twewbie", "twice", "twinkedln", "twis", "twishing", "twitosphere", "twitterer", "twittworking",
			"two", "u", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was",
			"wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein",
			"whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt",
			"woz", "wtf", "wtv", "xoxo", "yes", "yet", "ykyat", "yolo", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "yoyo", "zero", "ztwitt", "2moro",
			"2nite" /* ,"http" ,"https" */ };
	protected static final HashSet<String> stop_list = new HashSet<String>(Arrays.asList(SET_VALUES));

	/**
	 * This method transforms an string into a distinct sorted keyword list
	 * 
	 * @param inputText
	 * @return
	 */
	public static ArrayList<String> transformIntoSortedArrayListOfString(String inputText) {
		inputText = inputText.toLowerCase();
		String[] splitText = inputText.replaceAll("[^A-Za-z0-9@/.:_]", " ").split(LSpatioTextualConstants.textDelimiter);
		Arrays.sort(splitText);
		ArrayList<String> sortedTextList = new ArrayList<String>(Arrays.asList(splitText));
		ArrayList<String> finalSortedList = new ArrayList<String>();
		String previousString = "";
		for (String s : sortedTextList)
			if (s != null && !s.toLowerCase().equals(previousString.toLowerCase()) && isReleventKeyword(s)) {
				previousString = s;
				finalSortedList.add(s.toLowerCase());
			}
		
		return finalSortedList;
	}
	public static ArrayList<String> transformIntoArrayListOfString(String inputText) {
		inputText = inputText.toLowerCase();
		String[] splitText = inputText.replaceAll("[^A-Za-z0-9@/.:_]", " ").split(LSpatioTextualConstants.textDelimiter);
		
		HashSet<String> tempSet = new HashSet<String>();
		ArrayList<String> finalList = new ArrayList<String>();
		for(String term:splitText){
			if( isReleventKeyword(term)&&!tempSet.contains(term)){
				finalList.add(term);
				tempSet.add(term);
			}
		}
		
		return finalList;
	}

	public static ArrayList<String> transformIntoSortedArrayListOfStringOld(String inputText) {
		String[] splitText = inputText.replaceAll("[^A-Za-z0-9@/.:]", " ").split(LSpatioTextualConstants.textDelimiter);
		Arrays.sort(splitText);
		ArrayList<String> sortedTextList = new ArrayList<String>(Arrays.asList(splitText));
		ArrayList<String> finalSortedList = new ArrayList<String>();
		String previousString = "";
		for (String s : sortedTextList)
			if (s != null && !s.toLowerCase().equals(previousString.toLowerCase()) && isReleventKeyword(s)) {
				previousString = s;
				finalSortedList.add(s.toLowerCase());
			}
		return finalSortedList;
	}

	public static ArrayList<String> sortTextArrayList(ArrayList<String> textContent) {
		if (textContent == null)
			return null;
		Collections.sort(textContent);
		ArrayList<String> finalSortedList = new ArrayList<String>();
		for (String s : textContent)
			if (s != null && isReleventKeyword(s))
				finalSortedList.add(s.toLowerCase());
		return finalSortedList;
	}

	/**
	 * 
	 * @param textMap
	 *            Hashmap of keywords and their count
	 * @param textList
	 *            sorted array of strings
	 * @return
	 */
	public static boolean overlapsTextually(HashMap<String, Integer> textMap, ArrayList<String> textList) {

		for (String text : textList)
			if (textMap.containsKey(text))
				return true;
		return false;
	}

	public static boolean overlapsTextually(HashSet<String> textMap, ArrayList<String> textList) {

		for (String text : textList)
			if (textMap.contains(text))
				return true;
		return false;
	}

	public static boolean overlapsTextually(Set<String> textMap, ArrayList<String> textList) {

		for (String text : textList)
			if (textMap.contains(text))
				return true;
		return false;
	}

	public static boolean overlapsTextuallyWithtime(HashMap<String, Long> textMap, ArrayList<String> textList) {

		for (String text : textList)
			if (textMap.containsKey(text))
				return true;
		return false;
	}

	/**
	 * This function evaluates textual predicates the function is order
	 * sensetive , so you evlaute as eval(list1,list2) this function evluates
	 * contains textual predicate which is order sensetive
	 * 
	 * @param textList1
	 * @param textList2
	 * @param textualPredicate
	 * @return
	 */
	public static boolean evaluateTextualPredicate(ArrayList<String> textList1, ArrayList<String> textList2, LTextualPredicate textualPredicate) {

		if (LTextualPredicate.NONE.equals(textualPredicate))
			return true;
		else if (LTextualPredicate.CONTAINS.equals(textualPredicate))
			return containsTextually(textList1, textList2);
		else if (textualPredicate == null || LTextualPredicate.OVERlAPS.equals(textualPredicate))
			return overlapsTextually(textList1, textList2);
		return false;
	}

	public static boolean evaluateTextualPredicate(HashMap<String, Integer> textMap, ArrayList<String> textList2, LTextualPredicate textualPredicate) {

		if (LTextualPredicate.NONE.equals(textualPredicate))
			return true;
		else if (LTextualPredicate.CONTAINS.equals(textualPredicate))
			return containsTextually(textMap, textList2);
		else if (textualPredicate == null || LTextualPredicate.OVERlAPS.equals(textualPredicate))
			return overlapsTextually(textMap, textList2);
		return false;
	}

	/**
	 * This method returns true if input list have shared keywords This method
	 * assumes that input lists are sorted on text to speed up the overlap
	 * identification process and entries in this arraylist are distinict
	 * 
	 * @param textList1
	 *            sorted array of strings
	 * @param textList2
	 *            sorted array of strings
	 * @return
	 */
	private static boolean overlapsTextually(ArrayList<String> textList1, ArrayList<String> textList2) {
		int n1 = textList1.size();
		int n2 = textList2.size();
		int i = 0, j = 0;
		while (i < n1 && j < n2) {
			int val = textList1.get(i).compareToIgnoreCase(textList2.get(j));
			if (val < 0) { //str1 is greater than str2
				i++;
			} else if (val > 0) {//str2 is greater than str1
				j++;
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method returns the count of overlapping keywords This method assumes
	 * that input lists are sorted on text to speed up the overlap
	 * identification process and entries in this arraylist are distinict
	 * 
	 * @param textList1
	 *            sorted array of strings
	 * @param textList2
	 *            sorted array of strings
	 * @return
	 */
	public static Integer getTextOverlapCount(ArrayList<String> textList1, ArrayList<String> textList2) {
		int n1 = textList1.size();
		int n2 = textList2.size();
		int i = 0, j = 0;
		Integer count = 0;
		while (i < n1 && j < n2) {
			int val = textList1.get(i).compareToIgnoreCase(textList2.get(j));
			if (val < 0) { //str1 is greater than str2
				i++;
			} else if (val > 0) {//str2 is greater than str1
				j++;
			} else {
				count++;
				i++;
				j++;
			}
		}
		return count;
	}

	/**
	 * This method determines if one keyword list contains all keywords of the
	 * other list This method assumes that input lists are sorted on text to
	 * speed up the overlap identification process
	 * 
	 * @param textListContainer
	 *            sorted array of strings
	 * @param textListSubset
	 *            sorted array of strings
	 * @return
	 */
	public static boolean containsTextually(List<String> textListContainer, List<String> textListSubset) {

		int n1 = textListContainer.size();
		int n2 = textListSubset.size();
		if (n1 < n2)
			return false;
		int i = 0, j = 0;
		while (i < n1 && j < n2) {
			int val = textListContainer.get(i).compareToIgnoreCase(textListSubset.get(j));
			if (val < 0) { //str1 is greater than str2
				i++;
			} else if (val > 0) {//str2 is greater than str1
				return false; // a String is not matched
			} else {
				i++;
				j++;
			}
		}
		if (j == n2)
			return true;
		return false;
	}

	public static boolean containsTextually(List<String> textListContainer, List<String> textListSubset, int containerBegin, int sublistBegin) {
		int n1 = textListContainer.size();
		int n2 = textListSubset.size();
		int matchingCount = 0;
		int i = containerBegin, j = sublistBegin;
		while (i < n1 && j < n2) {
			int val = textListContainer.get(i).compareToIgnoreCase(textListSubset.get(j));
			if (val < 0) { //str1 is greater than str2
				i++;
			} else if (val > 0) {//str2 is greater than str1
				return false; // a String is not matched
			} else {
				i++;
				j++;
				matchingCount++;
			}
		}
		if (matchingCount == (n2-sublistBegin))
			return true;
		return false;
	}

	/**
	 * This method determines if one keyword list contains all keywords of the
	 * other list This method assumes that input lists are sorted on text to
	 * speed up the overlap identification process
	 * 
	 * @param textMap
	 *            sorted array of strings
	 * @param textListSubset
	 *            sorted array of strings
	 * @return
	 */
	public static boolean containsTextually(HashMap<String, Integer> textMap, ArrayList<String> textListSubset) {
		if (textMap.size() < textListSubset.size())
			return false;
		for (String s : textListSubset)
			if (!textMap.containsKey(s))
				return false;
		return true;
	}

	public static boolean containsTextually(HashSet<String> textMap, ArrayList<String> textListSubset) {
		if (textMap.size() < textListSubset.size())
			return false;
		for (String s : textListSubset)
			if (!textMap.contains(s))
				return false;
		return true;
	}

	public static String convertArrayListOfStringToText(ArrayList<String> textList) {
		if (textList != null) {
			String output = "";
			for (String s : textList) {
				output += (s + LSpatioTextualConstants.textDelimiter);
			}
			return output;
		} else
			return null;
	}

	private static boolean isAlphanumeric(String keyword, int length) {
		boolean isAlphanum = true;
		for (int i = 0; i < length; i++) {
			char c = keyword.charAt(i);
			if (c < 0x30 || (c >= 0x3a && c <= 0x40) || (c > 0x5a && c <= 0x60) || c > 0x7a) {
				isAlphanum = false;
				break;
			}
		}
		return isAlphanum;
	}

	private static Boolean isReleventKeyword(String keyword) {
		boolean isImpt = true;
		int length;
		length = keyword.length();
		if (length >= LSpatioTextualConstants.relevenatKeyWordMinSize && length < LSpatioTextualConstants.relevenatKeyWordMaxSize) {
			if (keyword.charAt(0) == '@' || keyword.contains("http"))
				isImpt = false;
			if (isAlphanumeric(keyword, length)) {
				if (stop_list.contains(keyword.toLowerCase())) {
					isImpt = false;
				}
			} else {
				// System.out.println("Special Character");
				isImpt = false;
			}
		} else {
			isImpt = false;
		}
		return isImpt;
	}
}
