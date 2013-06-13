package shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import makeCell.Histogram;

/**
 * Cell
 * 
 * This class represents a Cell from Discrete Kekule Theory. A cell consists of a set of port assignments. 
 * A port assignment is the set of ports which contain a double bond in this Kekule state or resonance contributor.
 * In this way, A Kekule state shows which ports have a double bond in all possible resoance states of a molecule. A 
 * Kekule Cell has been shown to fully describe the electrical switching behaviour seen in some aromatic hydrocarbons.
 * 
 * Ports are named from 'a' to 'z' as the number of ports increase. A port assignment is visualized as follows
 * 0 or abc or abcde
 * Above shows 3 different ports assignments, one where all ports have no double bond, one where a, b, and c have a double
 * bond, and finally one where all 5 ports contain a double bond. 
 * 
 * Each port assignment is contained within the BitVector data structure. 
 * 
 * A Kekule cell is visualized as follows
 * {0, ab, ac, abcd, abcde}
 * 
 * @author Aaron
 *
 */
public class Cell {
	//STATIC
	
	/**
	 * Returns the intersection of two cells
	 */
	public static Cell intersection(Cell a, Cell b){
		Set<BitVector> set1 = Utils.arToSet(a.getPA());
		Set<BitVector> set2 = Utils.arToSet(b.getPA());
		Set<BitVector> intersection = new HashSet(set1);
		intersection.retainAll(set2);
		Cell both = new Cell(intersection, 0);
		both.sortBySize();
		return both;
	}
	/**
	 * Return a Cell with all even port assignments, as in all possible 
	 * bit Vectors with 0, 2, 4, etc. elements.
	 */
	public static Cell allEvenPA(int rank){
		Set<BitVector> set = new HashSet<BitVector>();
		for(int i = 0; i < (1 << rank); i++){
			BitVector iteration = new BitVector(i);
			if(iteration.countBits() % 2 == 0){
				set.add(new BitVector(i));
			}
		}
		Cell answer = new Cell(set, rank);
		return answer;
	}
	
	//NON-STATIC
	/**
	 * Number of ports in the cell
	 */
	private int numPorts;
	/**
	 * Set of bit vectors, each representing a port assignment of this cell
	 */
	private BitVector[] portAssignments;
	
	/**
	 * Creates an empty BitVector with no port assignments
	 */
	public Cell(){
		this.portAssignments = new BitVector[0];
		this.numPorts = 0;
	}
	/**
	 * Creates a new bitvector based on a set (array) or port assignments.
	 * Number of ports is initialized to 0. If you wish to visualize this
	 * cell you must set the port number
	 * @param set, array of BitVectors
	 */
	public Cell(BitVector[] set){
		this.portAssignments = set;
		this.numPorts = 0;
	}
	/**
	 * Creates a new BitVector based on an actual set of port assignments. 
	 * Resulting cell will be unordered, as sets have no order.
	 * 
	 * @param set, Set of BitVectors
	 * @param numPorts, number of ports
	 */
	public Cell(Set<BitVector> set, int numPorts){
		this.portAssignments = new BitVector[set.size()];
		int index = 0;
		//convert set to array
		//order of array is meaningless here
		Iterator<BitVector> i = set.iterator();
		while(i.hasNext()){
			this.portAssignments[index++] = (BitVector) i.next();
		}
		this.numPorts = numPorts;
	}
	
	/**
	 * Creates a new cell, which is duplicate of the supplied cell
	 * @param c, cell to copy
	 */
	public Cell(Cell c){
		this.portAssignments = new BitVector[c.portAssignments.length];
		for(int i = 0; i < c.portAssignments.length; i++){
			BitVector temp = new BitVector(c.portAssignments[i].getNumber());
			this.portAssignments[i] = temp;
		}
		this.numPorts = c.numPorts;
	}
	
	/**
	 * Performs a weighted Sort on this cell. This sorts all port assignments of this cell
	 * according to rules defined in Hesselink's Paper.
	 */
	public void weightedSort(){
		//due to integer overflow
		//can only have limited amount of nodes
		//this is max rank
		int[] we = new int[Permutations.MAX_RANK];
		//port assignments in array
		int x;
		
		for(int i = 0; i < this.size(); i++){
			x = this.portAssignments[i].countBits() - 1;
			for(int p = 0; p < this.numPorts; p++){
				if( ( ( this.portAssignments[i].getNumber() >> p) &  1 ) != 0){
					we[p] += x;
				}
			}
		}
		
		for(int i = 0; i < this.size(); i++){
			x = this.portAssignments[i].getNumber();
			this.portAssignments[i] = new BitVector(0);
			for(int p = 0; p < this.numPorts; p++){
				if( ( (x >> p) & 1) != 0){
					this.portAssignments[i] = new BitVector( this.portAssignments[i].getNumber() + we[p] );
				}
			}
			
		}
		
		Arrays.sort(this.portAssignments, new Comparator<BitVector>(){
			@Override
			public int compare(BitVector o1, BitVector o2) {
				if(o1.getNumber() < o2.getNumber()){
					return -1;
				}
				else if(o1.getNumber() > o2.getNumber()){
					return 1;
				}
				return 0;
			}
		});
		
	}
	
	/**
	 * Retain all bit strings of this contained in bitvector
	 * keep all port assignments of this cell whose bitvector is entirely contained
	 * (they have no elemens which bv does not)
	 */
	public void filterSub(BitVector bv){
		BitVector[] array = this.portAssignments;
		ArrayList<BitVector> trimmed = new ArrayList<BitVector>();
		for( int i = 0; i < this.size(); i++){
			BitVector union = BitVector.union(
					bv, array[i]);
			if(union.equals(bv)){
				trimmed.add(array[i]);
			}
		}
		this.portAssignments = Utils.listToArBV(trimmed);
	}
	
	/**
	 * Translates a cell over a port assignment.
	 * @param portAssignment
	 */
	public void translate(BitVector translation){
		//if nothing to translate
		if(translation.isEmpty()){
			return;
		}
		//new set of translated port assignments
		BitVector[] translated = new BitVector[this.size()];
		
		//iterate over all cells current port assignments
		for(int i = 0; i < this.size(); i++){
			BitVector bv = this.portAssignments[i];
			//get symmetric difference of current and translation
			BitVector symD = BitVector.symmetricDifference(
					bv,
					translation);
			//place intersection in new port assignment set
			translated[i] = symD;
		}
		
		this.portAssignments = translated;
	}
	
	/**
	 * Normalizes this cell so it can be classified according to the specifications
	 * in Hesselink's Paper.
	 */
	public void normalize(){
		//sort port assignments by their bitvector sizes
		this.sortBySize();
		//create a new bit Vector holding the port assignment that is the center
		//of this cell, along with translate across center
		BitVector pa = new BitVector(this.center());
		System.out.println("Translated over \"" +pa.getPA(this.numPorts) + "\"" );
		System.out.println("And normalized gets:");
		//normalize
		Cell newKekule = Permutations.firstVariant(this);
		this.portAssignments = newKekule.portAssignments;
	}
	
	/**
	 * Finds the center of this cell, and translates it across it
	 * @return port assignment which is the center
	 */
	public int center(){
		int k = 0;
		
		Cell wl = new Cell(this);
		wl.translate(this.portAssignments[0]);
		wl.weightedSort();
		
		for(int i = 1; i < this.portAssignments.length; i++){
			Cell owl = new Cell(this);
			owl.translate(this.portAssignments[i]);
			owl.weightedSort();
			
			if( Histogram.compareL(owl, wl) < 0 ){
				k = i;
				wl = null;
				wl = owl;
			}
			else{
				owl = null;
			}		
		}
		
		wl = null;
		int portAssignment = this.portAssignments[k].getNumber();
		this.translate(this.portAssignments[k]);
		return portAssignment;
	}
	
	/**
	 * isFlexible
	 * 0 must be in cell
	 */
	public boolean isFlexible(BitVector ports){
		BitVector all = new BitVector(0);
		for(int i = 0; i < this.size(); i++){
			all = BitVector.union(all, this.portAssignments[i]);
		}
		return (all.equals(ports));
	}
	
	public boolean isFlexible(int ports){
		int i, upb = 0;
		for(i = 0; i < this.size(); i++){
			upb |= this.getPA()[i].getNumber();
		}
		return (upb == ports);
	}
	
	/**
	 * Adds a BitVector to this cell. Warning, order is lost
	 */
	public void add(BitVector bv){
		BitVector[] newSet = new BitVector[this.size()+1];
		System.arraycopy(this.portAssignments, 0, newSet, 0, this.size());
		newSet[newSet.length - 1] = bv;
		this.portAssignments = newSet;
	}
	
	/**
	 * Removes the last bitVector of this cell
	 * returns it
	 */
	public BitVector removeLast(){
		BitVector[] newSet = new BitVector[this.size()-1];
		System.arraycopy(this.portAssignments, 0, newSet, 0, this.size()-1);
		BitVector last = this.portAssignments[this.size()-1];
		this.portAssignments = newSet;
		return last;
	}
	
	/**
	 * Prints the list of all port assignments of this Cell
	 */
	public void printList(){
		String answer = "";
		for(int i = 0; i < this.size(); i++){
			answer += this.portAssignments[i].getNumber() + " ";
		}
		System.out.println(answer);
	}
	
	/**
	 * Sorts the port assignments of this cell by the size of the corresponding
	 * BitVectors
	 */
	public void sortBySize(){
		Arrays.sort(this.portAssignments, new Comparator<BitVector>(){
			@Override
			public int compare(BitVector one, BitVector two){
				if(one.getNumber() < two.getNumber()){
					return -1;
				}
				else if(one.getNumber() > two.getNumber()){
					return 1;
				}
				return 0;
			}
		});
	}
	
	/**
	 * equals method
	 */
	@Override
	public boolean equals(Object obj) {
		Cell another = (Cell) obj;
		this.sortBySize();
		another.sortBySize();
		
		Set<BitVector> thisSet = Utils.arToSet(this.portAssignments);
		Set<BitVector> anotherSet = Utils.arToSet(another.portAssignments);
		
		if(thisSet.equals(anotherSet)){
			if(this.numPorts == another.numPorts){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Prints a final visualization of this cell. 
	 * //TODO Should rename method
	 * it doesn't print it returns a string
	 * @return, String
	 */
	public String printUnweighted(){
		String[] portAssigns = new String[this.size()];
		int index = 0;
		int[] bitVectors = new int[this.size()];
		String answer = "Cell: ";

		for (BitVector bv : this.portAssignments) {
			bitVectors[index++] = bv.getNumber();
		}

		Arrays.sort(bitVectors);
		index = 0;

		for (int number : bitVectors) {
			if (number == 0) {
				portAssigns[index] = "0";
				index++;
			} else {
				String ports = "";
				for (int j = this.numPorts; j > 0; j--) {
					if ((number & (1 << (j - 1))) != 0) {
						ports += (char) ('a' + j - 1);
					}
				}
				ports = Utils.sort(ports);
				portAssigns[index] = ports;
				index++;
			}
		}

		for (int k = 0; k < portAssigns.length; k++) {
			answer += portAssigns[k] + " ";
		}
		return answer;

	}
	
	public String printNumbers(){
		String answer = "";
		for(int i = 0; i < this.size(); i++){
			answer += this.portAssignments[i].getNumber() + " ";
		}
		return answer;
	}
	
	public String toString() {
		return printUnweighted();
	}

	public int size(){
		return this.portAssignments.length;
	}
	
	public int getNumPorts() {
		return numPorts;
	}

	public void setNumPorts(int numPorts) {
		this.numPorts = numPorts;
	}
	
	public BitVector[] getPA(){
		return this.portAssignments;
	}
}