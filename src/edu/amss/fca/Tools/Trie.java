package edu.amss.fca.Tools;

import java.util.ArrayList;

import java.util.List;

public class Trie {

	private Vertex root = new Vertex();

	protected class Vertex {

		protected int words; // ���ʸ���

		protected int prefixes; // ǰ׺����

		protected Vertex[] edges; // �ӽڵ�

		Vertex() {

			this.words = 0;

			this.prefixes = 0;

			edges = new Vertex[26];

			for (int i = 0; i < edges.length; i++) {

				edges[i] = null;

			}

		}

	}

	/**
	 * 
	 * ��ȡtire�������еĴ�
	 * 
	 * 
	 * 
	 * @return
	 */

	public List<String> listAllWords() {

		List<String> words = new ArrayList<String>();

		Vertex[] edges = root.edges;

		for (int i = 0; i < edges.length; i++) {

			if (edges[i] != null) {

				String word = "" + (char) ('a' + i);

				depthFirstSearchWords(words, edges[i], word);

			}

		}

		return words;

	}

	/**
	 * 
	 * 
	 * 
	 * @param words
	 * 
	 * @param vertex
	 * 
	 * @param wordSegment
	 */

	private void depthFirstSearchWords(List words, Vertex vertex,

	String wordSegment) {

		if (vertex.words != 0) {

			words.add(wordSegment);

		}

		Vertex[] edges = vertex.edges;

		for (int i = 0; i < edges.length; i++) {

			if (edges[i] != null) {

				String newWord = wordSegment + (char) ('a' + i);

				depthFirstSearchWords(words, edges[i], newWord);

			}

		}

	}

	/**
	 * 
	 * ����ָ��ǰ׺���ʵĸ���
	 * 
	 * 
	 * 
	 * @param prefix
	 * 
	 * @return
	 */

	public int countPrefixes(String prefix) {

		return countPrefixes(root, prefix);

	}

	private int countPrefixes(Vertex vertex, String prefixSegment) {

		if (prefixSegment.length() == 0) { // reach the last character of the

			// word

			return vertex.prefixes;

		}

		char c = prefixSegment.charAt(0);

		int index = c - 'a';

		if (vertex.edges[index] == null) { // the word does NOT exist

			return 0;

		} else {

			return countPrefixes(vertex.edges[index],

			prefixSegment.substring(1));

		}

	}

	/**
	 * 
	 * ������ȫƥ�䵥�ʵĸ���
	 * 
	 * 
	 * 
	 * @param word
	 * 
	 * @return
	 */

	public int countWords(String word) {

		return countWords(root, word);

	}

	private int countWords(Vertex vertex, String wordSegment) {

		if (wordSegment.length() == 0) { // reach the last character of the word

			return vertex.words;

		}

		char c = wordSegment.charAt(0);

		int index = c - 'a';

		if (vertex.edges[index] == null) { // the word does NOT exist

			return 0;

		} else {

			return countWords(vertex.edges[index], wordSegment.substring(1));

		}

	}

	/**
	 * 
	 * ��tire�����һ����
	 * 
	 * 
	 * 
	 * @param word
	 * 
	 * 
	 */

	public void addWord(String word) {

		addWord(root, word);

	}

	/**
	 * 
	 * Add the word from the specified vertex.
	 * 
	 * 
	 * 
	 * @param vertex
	 * 
	 *            The specified vertex.
	 * 
	 * @param word
	 * 
	 *            The word to be added.
	 */

	private void addWord(Vertex vertex, String word) {

		if (word.length() == 0) { // if all characters of the word has been

			// added

			vertex.words++;

		} else {

			vertex.prefixes++;

			char c = word.charAt(0);

			c = Character.toLowerCase(c);

			int index = c - 'a';

			if (vertex.edges[index] == null) { // if the edge does NOT exist

				vertex.edges[index] = new Vertex();

			}

			addWord(vertex.edges[index], word.substring(1)); // go the the next

			// character

		}

	}

	/**
	 * 
	 * ����ָ���ֶ�ǰ׺ƥ����ĵ��ʡ�
	 * 
	 * 
	 * 
	 * @param word
	 * 
	 * @return
	 */

	public String getMaxMatchWord(String word) {

		String s = "";

		String temp = "";// ��¼���һ��ƥ����ĵ���

		char[] w = word.toCharArray();

		Vertex vertex = root;

		for (int i = 0; i < w.length; i++) {

			char c = w[i];

			c = Character.toLowerCase(c);

			int index = c - 'a';

			if (vertex.edges[index] == null) {// ���û���ӽڵ�

				if (vertex.words != 0)// �����һ�����ʣ��򷵻�

					return s;

				else

					// �������һ�������򷵻�null

					return null;

			} else {

				if (vertex.words != 0)

					temp = s;

				s += c;

				vertex = vertex.edges[index];

			}

		}

		// trie�д��ڱ�ָ�����ʸ���������ָ���ʣ��ĵ���

		if (vertex.words == 0)//

			return temp;

		return s;

	}

	/**public static void main(String args[]) // Just used for test

	{

		Trie trie = new Trie();

		trie.addWord("abcedfddddddd");

		trie.addWord("a");

		trie.addWord("ba");

		trie.addWord("abce");

		trie.addWord("abcedfdddd");

		trie.addWord("abcef");

		String maxMatch = trie.getMaxMatchWord("abcedfddd");

		System.out.println(maxMatch);

		List<String> all = new ArrayList<>();
		all = trie.listAllWords();
		System.out.println(all.size());

		// List<String> list = trie.listAllWords();

		// Iterator listiterator = list.listIterator();

		// while (listiterator.hasNext()) {

		// String s = (String) listiterator.next();

		// System.out.println(s);

		// }

		// int count = trie.countPrefixes("abcdef");

		// int count1 = trie.countWords("abcd");

		// System.out.println("prefixes:" + count);

		// System.out.println("countWords:" + count1);

	}**/

}