import java.util.Set;
import java.util.TreeSet;


public class EdgePair implements Comparable{
	/**
	 * Vertex 1
	 */
	private int firstNode;
	/**
	 * Vertex two
	 */
	private int secondNode;
	
	public EdgePair(int first, int second){
		this.firstNode = first;
		this.secondNode = second;
	}
	
	public Set<Integer> getNodeSet(){
		Set<Integer> nodeSet = new TreeSet<Integer>();
		nodeSet.add(this.firstNode);
		nodeSet.add(this.secondNode);
		return nodeSet;
	}
	
	public boolean contains(int node){
		if(this.firstNode == node || this.secondNode == node){
			return true;
		}
		return false;
	}
	
	public int getNeighbour(int me){
		if(me == this.firstNode){
			return this.secondNode;
		}
		else if(me == this.secondNode){
			return this.firstNode;
		}
		else{
			throw new IllegalArgumentException();
		}
	}

	public int getFirstNode() {
		return firstNode;
	}

	public void setFirstNode(int firstNode) {
		this.firstNode = firstNode;
	}

	public int getSecondNode() {
		return secondNode;
	}

	public void setSecondNode(int secondNode) {
		this.secondNode = secondNode;
	}

	@Override
	public int compareTo(Object arg0) {
		EdgePair another = (EdgePair) arg0;
		if(this.firstNode == another.firstNode){
			if(this.secondNode == another.secondNode){
				return 0;
			}
		}
		return 1;
	}
	
	
}