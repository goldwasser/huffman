import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A DataModel instance stores the underlying data for a single Huffman code.
 * 
 * Specifically, it represents the code book, the corresponding tree structure,
 * and when build from underlying frequency data, it stores those frequencies
 * and a trace
 * 
 * Instances of this class are intentionally
 *
 */
public class DataModel {
	private Tree root;
	private LinkedHashMap<String, String> codebook;
	private LinkedHashMap<String, Tree> leafMap;
	private LinkedHashMap<String, Integer> frequencies;
	private Tree[] algorithmTrace;
	private Tree[][] pqTrace;
	
	/**
	 * Disallow direct instantiation.
	 */
	private DataModel() {
		codebook = new LinkedHashMap<String, String>();
		leafMap = new LinkedHashMap<String, Tree>();
	}
	
	/**
	 * Constructs a data model based on a sample of raw text.
	 * 
	 * This computes the character frequency of the text, and then
	 * uses Huffman algorithm to build the optimal code for those
	 * frequencies.
	 * @param raw A String designating the original text
	 */
	public static DataModel createFromRaw(String raw) {
		TreeMap<String,Integer> map = new TreeMap<String, Integer>();
		for (int i=0; i < raw.length(); i++) {
			String s = Character.toString(raw.charAt(i));
			if (!map.containsKey(s))
				map.put(s, 0);
			map.put(s, 1 + map.get(s));
		}
		return createFromFrequencies(new LinkedHashMap<String,Integer>(map));
	}
	
	/**
	 * Constructs a data model based on given symbol frequencies.
	 * 
	 * The associated codebook map will use the same symbol order
	 * as given in the frequency map.
	 * 
	 * @param freq A Map from string symbols to integer frequencies
	 */
	public static DataModel createFromFrequencies(Map<String,Integer> freq) {
		DataModel model = new DataModel();
		model.frequencies = new LinkedHashMap<String,Integer>(freq);
		model.algorithmTrace = new Tree[freq.size()-1];
		model.pqTrace = new Tree[freq.size()][];
		TreeSet<Tree> pq = new TreeSet<Tree>();  // relying on fact that Tree.compareTo is total order
		for (Map.Entry<String,Integer> entry : freq.entrySet()) {
			Tree leaf = new Tree(entry.getKey(), entry.getValue());
			pq.add(leaf);
			model.leafMap.put(entry.getKey(), leaf);
		}
		for (int step=0; step < freq.size() - 1; step++) {
			model.pqTrace[step] = pq.toArray(new Tree[0]);
			Tree a = pq.pollFirst();
			Tree b = pq.pollFirst();
			Tree c = new Tree(b,a);  // be consistent with animation view
			pq.add(c);
			model.algorithmTrace[step] = c;
		}
		model.root = pq.pollFirst();
		model.pqTrace[freq.size()-1] = new Tree[1];
		model.pqTrace[freq.size()-1][0] = model.root;
		
		// Let's build up the codebook
		for (Map.Entry<String,Tree> leaf : model.leafMap.entrySet()) {
			StringBuilder sb = new StringBuilder();
			Tree walk = leaf.getValue();
			while (walk != model.root) {
				sb.append(walk == walk.parent.left ? '0' : '1');
				walk = walk.parent;
			}
			sb.reverse();
			model.codebook.put(leaf.getKey(), sb.toString());
		}
		
		return model;
	}
	 
	/**
	 * Constructs the data model based on given symbol codewords.
	 * 
	 * The resulting model will not have any frequency information.
	 * 
	 * @param codebook A Map mapping from string symbols to string codewords
	 */
	public static DataModel createFromCodebook(Map<String, String> codebook) {
		DataModel model = new DataModel();
		model.root = new Tree();
		model.codebook = new LinkedHashMap<String,String>(codebook);
		for (Map.Entry<String,String> entry : codebook.entrySet()) {
			model.addCode(entry.getKey(), entry.getValue());
		}
		return model;
	}

	/**
	 * Returns number of symbols in the model's codebook.
	 */
	public int size() {
		return codebook.size();
	}
	
	/**
	 * Returns iterator to the root of the tree.
	 * @return TreeIterator representing root
	 */
	public TreeIterator getRoot() {
		return new TreeIterator(root);
	}
	
	/**
	 * Returns iterators to (sub)trees, as they existed after k merges in the algorithm.
	 * 
	 * @param k number of merges that have taken place, for 0 <= k < size()
	 * @return array of TreeIterators, sorted from highest to lowest frequency.
	 */
	public TreeIterator[] getPQTrace(int k) {
		TreeIterator[] result = new TreeIterator[pqTrace[k].length];
		for (int j=0; j < result.length; j++)
			result[result.length - 1 - j] = new TreeIterator(pqTrace[k][j]);
		return result;
	}
	
	private void addCode(String symbol, String codeword) {
		Tree walk = root;
		for (int k=0; k < codeword.length(); k++) {
			if (codeword.charAt(k) == '0') {
				if (walk.left == null) {
					walk.left = new Tree();
					walk.left.parent = walk;
				}
				walk = walk.left;
			} else {
				if (walk.right == null) {
					walk.right = new Tree();
					walk.right.parent = walk;
				}
				walk = walk.right;

			}
		}
		walk.symbol = symbol;
		leafMap.put(symbol, walk);
	}
	
	/**
	 * Checks if model has underlying frequency data.
	 * 
	 * If so, then the frequencies can be queried via
	 * getFrequencyMap() and partial state of Huffman algorithm
	 * can be queried via getHuffmanState(int step).
	 * @return True if model has frequency data.
	 */
	public boolean hasFrequencyData() {
		return frequencies != null;
	}
	
	/**
	 * Returns unmodifiable view of frequency map.
	 */
	public Map<String,Integer> getFrequencyMap() {
		if (frequencies == null)
			return null;
		else
			return Collections.unmodifiableMap(frequencies);
	}
	
	/**
	 * Returns unmodifiable view of codebook map.
	 */
	public Map<String,String> getCodebookMap() {
		return Collections.unmodifiableMap(codebook);
	}
	
	/**
	 * Returns iterator to leaf of tree associated with symbol
	 * @param symbol
	 * @return TreeIterator (or null if symbol not found)
	 */
	public TreeIterator getLeaf(String symbol) {
		Tree leaf = leafMap.get(symbol);
		return (leaf == null ? null : new TreeIterator(leaf));
	}
	
	/**
	 * Represents a position within a tree.
	 */
	public static class TreeIterator {
		private Tree current;

		/**
		 * Constructor is protected.
		 * @param current
		 */
		protected TreeIterator(Tree current) {
			this.current = current;
		}

		/**
		 * Returns iterator of left child (or null, if no left child)
		 * @return TreeIterator for left child (or null if no left child)
		 */
		public TreeIterator getLeft() {
			Tree left = current.getLeft();
			if (left != null)
				return new TreeIterator(left);
			else
				return null;
		}
		
		/**
		 * Returns iterator of right child (or null, if no right child)
		 * @return TreeIterator for right child (or null if no right child)
		 */
		public TreeIterator getRight() {
			Tree right = current.getRight();
			if (right != null)
				return new TreeIterator(right);
			else
				return null;
		}
		
		/**
		 * Returns iterator of parent (or null, if no parent)
		 * @return TreeIterator for parent (or null if no parent)
		 */
		public TreeIterator getParent() {
			Tree parent = current.getParent();
			if (parent != null)
				return new TreeIterator(parent);
			else
				return null;
		}
		
		/**
		 * Returns frequency associated with subtree.
		 * 
		 * @return Will be 0 if frequency data not available
		 */
		public int getFrequency() {
			return current.getFrequency();
		}
		
		/**
		 * Returns string symbol associated with given position.
		 */
		public String getSymbol() {
			return current.getSymbol();
		}
		
		/**
		 * Returns length of longest path from this location to a leaf.
		 * 
		 * For example, if it is a leaf, it has depth 0.
		 * @return int depth
		 */
		public int getDepth() {
			int depth = 0;
			if (current.left != null)
				depth = 1 + getLeft().getDepth();
			if (current.right != null)
				depth = Math.max(depth, 1 + getRight().getDepth());
			return depth;
		}
		
		/**
		 * Returns true if two iterator instances reference same position.
		 */
		@Override
		public boolean equals(Object other) {
			if (this == other) return true;
			if (other == null) return false;
			if (getClass() != other.getClass()) return false;
			TreeIterator ti = (TreeIterator) other;
			return current == ti.current;
		}
		
		/**
		 * Allows iterators to be used properly as keys.
		 */
		@Override
		public int hashCode() {
			return current.hashCode();
		}
		
	}
	
	/**
	 * Recursive tree representation.
	 *
	 */
	protected static class Tree implements Comparable<Tree> {
		private Tree left;
		private Tree right;
		private Tree parent;
		private String symbol="";
		private int freq;
		
		/**
		 * Creates new tree composed of given subtrees.
		 * @param left
		 * @param right
		 */
		public Tree(Tree left, Tree right) {
			freq = 0;
			this.left = left;
			this.right = right;
			if (left != null) {
				left.parent = this;
				freq += left.freq;
			}
			if (right != null) {
				right.parent = this;
				freq += right.freq;
			}
		}

		/**
		 * Creates new leaf for given symbol with specified frequency
		 * @param symbol
		 * @param freq
		 */
		public Tree(String symbol, int freq) {
			this.symbol = symbol;
			this.freq = freq;
		}
		
		public Tree() {
			// TODO Auto-generated constructor stub
		}

		public Tree getLeft() {
			return left;
		}
		
		public Tree getRight() {
			return right;
		}
		
		public Tree getParent() {
			return parent;
		}
		
		public int getFrequency() {
			return freq;
		}
		
		public String getSymbol() {
			return symbol;
		}

		public int compareTo(Tree other) {
			if (freq < other.freq)
				return -1;
			else if (freq > other.freq)
				return +1;
			else {
				int leftCmp = compareHelper(left, other.left);
				if (leftCmp != 0)
					return leftCmp;
				else {
					int rightCmp = compareHelper(right, other.right);
					if (rightCmp != 0)
						return rightCmp;
					else
						return symbol.compareTo(other.symbol);
				}
			}
		}
		
		/**
		 * compare two (possibly null) trees.
		 * @param a
		 * @param b
		 * @return
		 */
		private static int compareHelper(Tree a, Tree b) {
			if (a == null)
				return (b == null ? 0 : -1);
		    if (b == null)
		    	return +1;
			return a.compareTo(b);
		}
	}
	
	/**
	 * Unit test
	 * @param args
	 */
	public static void main(String[] args) {
		String sample = "This is a test.\nThis is only a test.\nTesting, one, two three.$";
		DataModel model = createFromRaw(sample);
		for (Map.Entry<String,Integer> e : model.getFrequencyMap().entrySet()) {
			System.out.println(e.getKey() + ' ' + e.getValue());
		}
		System.out.println();
		for (Map.Entry<String,String> e : model.getCodebookMap().entrySet()) {
			System.out.println(e.getKey() + ' ' + e.getValue());
		}
	}

}

