import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * Reads in a graph and outputs it's Kekule cell
 * @author Aaron
 *
 */
public class GraphtoCell {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
		Graph inputGraph = readGraph();
		
		//set histogram static rank
		
		//write graph to output
		
		//find Kekule cell
		
		//Normalize
		
		//output
		
		//Int stack is set is stack of integers
		//each integer represents a bit vector
		
		//Bit vector = set
		//Int stack == set of sets???
	}
	
	private static Set<String> makeCell(Set<Integer> nodes, Graph g){
		
		if(nodes.isEmpty()){
			
		}
		return null;
	}
	
	/**
	 * Reads a single graph from graphs.txt and returns it as Graph object
	 * @return
	 * @throws FileNotFoundException
	 */
	private static Graph readGraph() throws FileNotFoundException{
		File f = new File("graphs.txt");
		Scanner s = new Scanner(f);
		
		String name = s.nextLine();
		int numNodes = s.nextInt();
		int numPorts = s.nextInt();
		
		String inputEdges = s.nextLine();
		String inputExtraEdges = s.nextLine();
		
		Set<String> edges = parseForEdgesCompact(inputEdges);
		Set<String> extraEdges = parseForEdges(inputExtraEdges);
		edges.addAll(extraEdges);
		
		return new Graph(name, numPorts, numNodes, edges);
	}

	/**
	 * Parses long string of form
	 * 0-1-2-3-4-5-6-7-8-9 and returns 
	 * string array of format {0 1, 1 2, 2 3, 3 4, 4 5, etc}
	 * @param inputEdges
	 * @return
	 */
	private static Set<String> parseForEdgesCompact(String inputEdges) {
		Set<String> edges = new TreeSet<String>();
		
		//should get 
		//{ "1", "-", "2", "-", "3", "-", "4" } etc
		//numbers are at every even index of array
		String[] edgeArray = inputEdges.split("-");
		
		int index = 0;
		String first = edgeArray[index];
		index += 2;
		String second = edgeArray[index];
		index += 2;
		
		edges.add(first + " " + second);
		
		while(index < edgeArray.length){
			first = second;
			second = edgeArray[index];
			index += 2;
			edges.add(first + " " + second);
		}
		
		return edges;
	}
	
	/**
	 * Parses long string of form
	 * 0-1, 5-6, 8-9 and returns 
	 * string array of format {0 1, 1 2, 2 3, 3 4, 4 5, etc}
	 * @param inputEdges
	 * @return
	 */
	private static Set<String> parseForEdges(String inputEdges) {
		Set<String> extraEdges = new TreeSet<String>();
		
		//should get 
		//{ "0-1", ",", "5-6", ",", "8-9"} etc
		//ranges are at every even index of array
		String[] edgeArray = inputEdges.split(",");
		
		int index = 0;
		while(index < edgeArray.length){
			//grab every second element
			String edge = edgeArray[index];
			index += 2;
			//use other method to parse 0-1, and return "0 1"
			Set<String> singleEdge = parseForEdgesCompact(edge);
			//add to set of extra edges
			extraEdges.addAll(singleEdge);
		}
		
		return extraEdges;
	}

}