import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * Reads in a graph and outputs it's Kekule cell
 * @author Aaron
 *
 */
public class GraphtoCell {

	private static File f;
	private static Scanner s;
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		f = new File("graphs.txt");
		s = new Scanner(f);
		Graph inputGraph = null;
		
		try{
			inputGraph = readGraph();			
		} catch(IOException e){
			System.out.println("file invalid");
			System.exit(0);
		}
		while(inputGraph != null){
			Set<Set<Integer>> Kcell = makeCell(inputGraph.getNodeSet(), inputGraph);
		
			printCell(Kcell);
			
			try{
				inputGraph = readGraph();
			} catch(IOException e){
				inputGraph = null;
			}
		}
		
		//set histogram static rank
		
		//write graph to output
		
		//find Kekule cell
		
		//Normalize
		
		//output
		
		//Int stack is set is stack of integers
		//each integer represents a bit vector
		
		//Bit vector = set
		//Int stack == set of sets???
		
		//port assignments are represented as sets of integers (node number)
		//a set of port assignments is a cell
	}
	
	private static Set<Set<Integer>> makeCell(Set<Integer> nodes, Graph g){
		//the answer
		Set<Set<Integer>> answer = new TreeSet<Set<Integer>>();
		//sub answer
		Set<Set<Integer>> subAnswer = new TreeSet<Set<Integer>>();
		
		//set of ports
		Set<Integer> ports = g.getPortSet();
		
		//Base case
		if(nodes.isEmpty()){
			answer.clear();
			TreeSet<Integer> zero = new TreeSet<>();
			zero.add(0);
			answer.add( zero );
		}
		else{
			//grab a vertex
			Integer nodeU = nodes.iterator().next();
			//remove vertex from nodes
			nodes.remove(nodeU);
			//recursion
			if(ports.contains(nodeU)){
				answer = makeCell(nodes, g);
			}
			else{
				answer.clear();
				Set<Integer> temp = new TreeSet<Integer>();
				temp.add(0);
				answer.add(temp);
			}
			//treat ndh(u,g)
			Set<EdgePair> edg = g.getAllEdges();
			Iterator<EdgePair> i = edg.iterator();
			while(i.hasNext()){
				EdgePair currentEdge = (EdgePair)i.next();
				//if u in edge
				if(currentEdge.contains(nodeU)){
					//v is node in u or in edge, but not both
					//symmetric difference of u and edge
					Set<Integer> uset = new TreeSet<Integer>();
					uset.add(nodeU);
					Set<Integer> uOrEdge = Utils.symmetricDifference(uset, currentEdge.getNodeSet());
									
					int nodeV;
					
					if(uOrEdge.isEmpty()){
						nodeV = 0;
					}
					else{
						nodeV = uOrEdge.iterator().next();
					}
					
					///if nodes has nodeV
					if(nodes.contains(nodeV)){
						//remove v from nodes and get cell
						Set<Integer> nodeDuplicate = new TreeSet<Integer>(nodes);
						nodeDuplicate.remove(nodeV);
						subAnswer = makeCell(nodeDuplicate, g);
						
						//create set of u and v, if they are ports
						//practically a port assignment
						Set<Integer> uAndV = new TreeSet<Integer>();
						uAndV.add(nodeU);
						uAndV.add(nodeV);
						uAndV.retainAll(ports);
						
						//translate subANswer over port assignment
						subAnswer = translate(subAnswer, uAndV);
						
						//add all of subanswer
						answer.addAll(subAnswer);
						subAnswer.clear();
					}
				}
			}
		}
		return answer;
	}
	
	/**
	 * translates a cell over port assignment
	 * @param subAnswer
	 * @param uAndV
	 */
	private static Set<Set<Integer>> translate(Set<Set<Integer>> subAnswer, Set<Integer> uAndV) {
		if(uAndV.isEmpty()){
			return subAnswer;
		}
		
		Set<Set<Integer>> translated = new TreeSet<Set<Integer>>();
		
		Iterator<Set<Integer>> i = subAnswer.iterator();
		while(i.hasNext()){
			Set<Integer> portAssignment = (Set<Integer>)i.next();
			
			translated.add(Utils.symmetricDifference(portAssignment, uAndV));
		}
		return translated;
	}
	
	private static void printCell(Set<Set<Integer>> cell){
		System.out.print("Cell = ");
		Iterator i = cell.iterator();
		while(i.hasNext()){
			Set<Integer> portAss = (Set<Integer>) i.next();
			System.out.print(portAss.toString() + " ");
		}
	}

	/**
	 * Reads a single graph from graphs.txt and returns it as Graph object
	 * @return
	 */
	private static Graph readGraph() throws IOException{
		
		String name = s.nextLine();
		int numNodes = s.nextInt();
		int numPorts = s.nextInt();
		
		s.nextLine();
		
		String inputEdges = s.nextLine();
		String inputExtraEdges = s.nextLine();
		
		Set<String> edges = parseForEdgesCompact(inputEdges);
		
		if(!inputExtraEdges.isEmpty()){
			Set<String> extraEdges = parseForEdges(inputExtraEdges);
			edges.addAll(extraEdges);
			s.nextLine();
		}
		
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
		//{ "1", "2", "3", "4" } etc
		//numbers are at every even index of array
		String[] edgeArray = inputEdges.split("-");
		
		int index = 0;
		String first = edgeArray[index];
		index++;
		String second = edgeArray[index];
		index++;
		
		edges.add(first + " " + second);
		
		while(index < edgeArray.length){
			first = second;
			second = edgeArray[index];
			index++;
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