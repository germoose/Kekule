package bitvectors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

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
		} catch(NoSuchElementException e){
			System.out.println("file invalid");
			System.exit(0);
		}
		
		while(inputGraph != null){
			Cell kekule = makeCell(inputGraph.getNodeVector(), inputGraph);
			kekule.setNumPorts(inputGraph.getNumPorts());
			
			System.out.println(kekule.toString());
		
			try{
				inputGraph = readGraph();
			} catch(NoSuchElementException e){
				inputGraph = null;
			}
		}
		
	}
	
	private static Cell makeCell(BitVector bvNodes, Graph g){
		
		Cell kekuleCell = new Cell();
		Cell addend = new Cell();
		
		BitVector ports = g.getPortVector();
		Set<BitVector> edges = g.getEdges();
		
		//Base case
		if(bvNodes.isEmpty()){
			kekuleCell.add(new BitVector(0));
		}
		else{
			//grab first Vertex from nodes
			//does not remove it
			int nodeU = bvNodes.firstNode();
			//remove vertex from nodes
			bvNodes.remove(nodeU);
			
			if(ports.contains(nodeU)){
				//place new bit vector to ensure different object
				kekuleCell = makeCell(new BitVector(bvNodes), g);
			}
			else{
				kekuleCell.clear();
			}
			
			
			//treat ndh(u,g)
			//iterate over all edges
			Iterator<BitVector> i = edges.iterator();
			while(i.hasNext()){
				BitVector edge = (BitVector) i.next(); 
				//if u in edge
				if(edge.contains(nodeU)){
					
					//remove node U from edge
					BitVector newEdge = new BitVector(edge);
					newEdge.remove(nodeU);
					//grab other node from edge
					int nodeV = newEdge.firstNode();
							
					///if nodes has nodeV
					if(bvNodes.contains(nodeV)){
						
						//remove v from nodes
						BitVector newNodes = new BitVector(bvNodes);
						newNodes.remove(nodeV);
						
						//find sub cell
						addend = makeCell(newNodes, g);
						
						//create bitvector we will use as translation
						BitVector portAssignment = BitVector.intersection(
								ports,
								new BitVector(nodeU + nodeV) );
						//translate over port assignment bitvector
						addend.translate(portAssignment);
						
						//take union of answer and addend
						kekuleCell = Cell.union(
								kekuleCell,
								addend );
										
					}
				}
			}
		}
		
		return kekuleCell;
	}
	
	/**
	 * Reads a single graph from graphs.txt and returns it as Graph object
	 * @return
	 * @throws FileNotFoundException
	 */
	private static Graph readGraph() throws NoSuchElementException{
		
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
		Set<String> edges = new HashSet<String>();
		
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
		Set<String> extraEdges = new HashSet<String>();
		
		//should get 
		//{ "0-1", "5-6", "8-9"} etc
		//ranges are at every even index of array
		String[] edgeArray = inputEdges.split(",");
		
		int index = 0;
		while(index < edgeArray.length){
			//grab every second element
			String edge = edgeArray[index];
			index++;
			//use other method to parse 0-1, and return "0 1"
			Set<String> singleEdge = parseForEdgesCompact(edge);
			//add to set of extra edges
			extraEdges.addAll(singleEdge);
		}
		
		return extraEdges;
	}

}