package bitvectors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Cell {
	/**
	 * Number of ports in the cell
	 */
	private int numPorts;
	/**
	 * Set of bitvectors, each representing a port assignment of this cell
	 */
	private Set<BitVector> portAssignments;
	
	public static Cell union(Cell a, Cell b){
		Set<BitVector> union = new HashSet<BitVector>();
		union.addAll(a.portAssignments);
		union.addAll(b.portAssignments);
		return new Cell(union);
	}
	
	public Cell(){
		this.portAssignments = new HashSet<BitVector>();
	}
	
	public Cell(Set<BitVector> portsAssigns){
		this.portAssignments = portsAssigns;
	}
	
	/**
	 * Translates a cell over port assignment 
	 * @param portAssignment
	 */
	public void translate(BitVector translation){
		//if nothing to translate
		if(translation.isEmpty()){
			return;
		}
		//new set of translated port assignments
		Set<BitVector> translated = new HashSet<BitVector>();
		
		//iterate over all cells current port assignments
		Iterator i = this.portAssignments.iterator();
		while(i.hasNext()){
			//grab current port assignment
			BitVector portAssignment = (BitVector) i.next();
			//get symmetric differenc of current and translation
			BitVector symD = BitVector.symmetricDifference(
					portAssignment,
					translation);
			//place intersection in new port assignment set
			translated.add(symD);
		}
		
		this.portAssignments = translated;
	}
	
	public String toString(){
		//return this.portAssignments.toString();
		String[] portAssigns = new String[this.portAssignments.size()];
		int index = 0;
		
		Iterator i = this.portAssignments.iterator();
		while(i.hasNext()){
			BitVector bv = (BitVector) i.next();
			if(bv.isEmpty()){
				portAssigns[index] = "0";
				index++;
			}
			else{
				String ports = "";
				int numPorts = this.numPorts;
				int bitVec = bv.getNumber();
				for(int j = this.numPorts; j > 0; j--){
					if( ( bitVec & (1 << (j-1) ) ) != 0){
						ports += (char)('a' + j - 1);
					}
				}
				ports = Utils.sort(ports);
				portAssigns[index] = ports;
				index++;
			}
		}
		Arrays.sort(portAssigns);
		
		String answer = "Cell: ";
		for(int k = 0; k < portAssigns.length; k++){
			answer += portAssigns[k] + " ";
		}
		return answer;
		
	}
	
	public void add(BitVector bv){
		this.portAssignments.add(bv);
	}
	
	public void clear(){
		this.portAssignments.clear();
	}

	public int getNumPorts() {
		return numPorts;
	}

	public void setNumPorts(int numPorts) {
		this.numPorts = numPorts;
	}
	
	
}