package backend.verification.timed_state_exploration.zone;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import backend.lpn.parser.Transition;
import backend.verification.platu.stategraph.State;

import java.util.ArrayList;

/**
 * 
 * Represents a zone with the minimum number of constraints.
 * @author Andrew N. Fisher
 *
 */
public class ZoneGraph{

	/* 
	 * Abstraction Function :
	 * 
	 */
	
	/* 
	 * Representation Invariant :
	 * 
	 */
	
	/* List of nodes in the graph. */
	//ArrayList<Node> _graph;
	HashMap<Integer, Node> _graph;
	
	
	/* 
	 * Temporary zone for calculations. The variable should be null when the ZoneGraph is
	 * stored to reduce memory requirements.
	 */
	Zone _tmpZone;
	
	/* A lexicon between a transitions index and its name. */
	private HashMap<Integer, Transition> _indexToTransition;
	
	/* Cached hash code.*/
	int _hashCode;
	
	/* Maps the index to the timer. The index is row/column of the DBM sub-matrix.
	 * Logically the zero timer is given index -1.
	 *  */
	private int[] _indexToTimer;
	
	/*
	 * Store the enabled upper and lower bounds of the timers.
	 */
	private HashMap<Integer, intPair> _bounds;
	
	/**
	 * Zero argument constructor for use in methods that create ZoneGraphs where the members
	 * variables will be set by the method.
	 */
	private ZoneGraph(){
		//_graph = new ArrayList<Node>();
		_graph = new HashMap<Integer, Node>();
		_hashCode = -1;
		_indexToTimer = new int[0];
		_indexToTransition = new HashMap<Integer, Transition>();
		_bounds = new HashMap<Integer, intPair>();
	}
	
	public ZoneGraph(int[] timers, int[][] matrix){
		
	}
	
	public ZoneGraph(State initialState){
		
	}
	
	public ZoneGraph(Zone z){
		_graph = extractZoneGraph(z)._graph;
	}
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ZoneType clone(){
		throw new UnsupportedOperationException();
		
		//return null;
	}

	@Override
	public boolean equals(Object o) {	
		// Check if the reference is null.
		if(o == null)
		{
			return false;
		}
		
		// Check that the type is correct.
		if(!(o instanceof ZoneGraph))
		{
			return false;
		}
		
		// Check for equality using the Zone equality.
		return equals((ZoneGraph) o);
	}
	
	/**
	 * Tests for equality.
	 * @param otherGraph
	 * @return
	 */
	public boolean equals(ZoneGraph otherGraph){
		
		// Check if the reference is null first.
		if(otherGraph == null)
		{
			return false;
		}
		
		// Check for reference equality.
		if(this == otherGraph)
		{
			return true;
		}
		
		// If the hash codes are different, then the objects are not equal. 		
		if(this.hashCode() != otherGraph.hashCode())
		{
			return false;
		}
		
		// Extract the Nodes.
//		HashSet<Node> thisNodes = new HashSet<Node>(this._graph.values());
//		
//		HashSet<Node> otherNodes = new HashSet<Node>(otherGraph._graph.values());
//		
		// Check same number of nodes.
//		if(thisNodes.size() != otherNodes.size()){
//			return false;
//		}
//		
//		// Check that all the nodes are the same and their edges are the same.
//		for(Node n : thisNodes){
//			if(!otherNodes.contains(n)){
//				return false;
//			}
//		}
		
		// Extract nodes.
		HashMap<Node, Node> thisNodes = new HashMap<Node, Node>();
		
		for(Node n : this._graph.values()){
			thisNodes.put(n, n);
		}
		
		HashMap<Node, Node> otherNodes = new HashMap<Node, Node>();
		
		for(Node n : otherGraph._graph.values()){
			otherNodes.put(n, n);
		}
		
		if(thisNodes.size() != otherNodes.size()){
			return false;
		}
		
		// Check if nodes and edges are the same.
		for(Node n : thisNodes.keySet()){
			if(!otherNodes.containsKey(n)){
				return false;
			}
			
			// Get the node from otherNodes.
			Node oNode = otherNodes.get(n);
			
			// Check the edges for this node.
			if(n._edges.size() != oNode._edges.size()){
				return false;
			}
			
			for(Edge e : n._edges){
				if(!oNode._edges.contains(e)){
					return false;
				}
			}
		}
		
		
		return this._graph.equals(otherGraph._graph);
	}

	@Override
	public int hashCode() {
	
		// Check if the hash code has already be calculated.
		if(_hashCode >=0){
			return _hashCode;
		}
		
		// Set the hash code.
		_hashCode = _graph.hashCode();
		
		return _hashCode;
	}

	@Override
	public String toString() {

		return _graph.toString();
	}
	
	public static ZoneGraph extractZoneGraph(Zone z){
		
		ZoneGraph newGraph = new ZoneGraph();
		
		if(z.getLexicon() != null){
			newGraph._indexToTransition = z.getLexicon();
		}
		
		// Get the timer index.
		newGraph._indexToTimer = z.getIndexToTimer();
		
		newGraph._bounds = new HashMap<Integer, intPair>();
		
		
		// Partition into zero-equivalence.
		newGraph.createPartition(z);
		
		// Create the part of the graph that connects the partitions.
		newGraph.connectPartition(z);
		
		// Create the part of the graph that lies in a partition.
		HashSet<Node> nodes = new HashSet<Node>(newGraph._graph.values());
		for(Node n : nodes){
			n.connectInteralNodes(z);
		}
		
		// Set the hash code to -1 to indicate that it has not been calculated yet.
		newGraph._hashCode = -1;
		
//		for(Node n : nodes){
//			System.out.println(n);
//		}
		
		// Get the upper and lower bounds.
		for(int i=1; i<newGraph._indexToTimer.length; i++){
			int timerIndex = newGraph._indexToTimer[i];
			int lower = z.getLowerBoundbyTransitionIndex(timerIndex);
			int upper = z.getUpperBoundbyTransitionIndex(timerIndex);
			
			newGraph._bounds.put(timerIndex, newGraph.new intPair(lower, upper));
		}
		
		return newGraph;
	}
	
	public Zone extractZone(){
		// TODO : Finish. Remember to add the lexicon.
		
		// Need to add all timers from all nodes, then add the timing relations.
		
		// Get all the timers.
//		ArrayList<Integer> timersList = new ArrayList<Integer>();
//		for(Node n : _graph.values()){
//			timersList.addAll(n._partition);
//		}
//		
//		int[] timers = new int[timersList.size()];
//		
//		for(int i=0; i<timers.length; i++){
//			timers[i] = timersList.get(i);
//		}
//		
//		int[][] matrix = new int[timers.length+1][timers.length+1];
//		for(int i=0; i<matrix.length; i++){
//			for(int j=0; j<matrix.length; j++){
//				matrix[i][j] = INFINITY;
//			}
//		}
		
		
		// Get the Nodes and order them.
		//Node[] nodes = _graph.values().toArray(new Node[0]);
		HashSet<Node> nodeSet = new HashSet<Node>(_graph.values());
		
		Node[] nodes = nodeSet.toArray(new Node[0]);
		
		// This ensures that the zero timer is the first timer.
		Arrays.sort(nodes);
		
		// Get all the timers.
		ArrayList<Integer> timersList = new ArrayList<Integer>();
		for(Node n : nodes){
			timersList.addAll(n._partition);
		}
		
		// Set up the timers array. Note the array does not include the zero timer.
		int[] timers = new int[timersList.size()-1];
		
		for(int i=0; i<timers.length; i++){
			// i+1 is used to remove the zero timer.
			//timers[i] = timersList.get(i+1);
			timers[i] = _indexToTimer[timersList.get(i+1)];
		}
		
		// Set up the matrix.
		// Matrix has 2 extra entries than the number of timers:
		// The zero timer entry and the upper and lower bounds.
		int[][] matrix = new int[timers.length+2][timers.length+2];
		
		// First, set every value high. Either set them all high to begin with or search
		// for the entries to set high later. I'm choosing the former.
		for(int i=1; i<matrix.length; i++){
			for(int j=1; j<matrix.length; j++){
				if(i == j){
					continue;
				}
				
				matrix[i][j] = ZoneType.INFINITY;
			}
		}
		
		// For a Node, assign the weights in the Node and the weights between this Node
		// and the following.
		
		// Gives the total number processed up to this node.
		int totalLength = 0;
		for(int i=0; i<nodes.length; i++){
			// Set up the weights in the Node.
			Node n = nodes[i];
			
			//if(n.size() > 2){
			if(n.size() > 1){
				for(int j=0; j<n.size(); j++){
					// Suppose the Node has timers 1 --(3)--> 4 -- (2)--> 7 --(2)--> 1,
					// I want the (1,4) entry of the DBM to be 3. Similarly the 
					// (4,7) entry of the DBM is 2 and the (7,1) entry is 2.
					// To get the DBM index of the matrix, each index must be increased by
					// 1.
					//matrix[(i+1)+j][(i+1)+(j+1)%n.size()] = n.getInternalEdgeWeight(j);
					matrix[(totalLength+1)+j][(totalLength+1)+(j+1)%n.size()] = 
						n.getInternalEdgeWeight(j);
				}
			}
			
			// Set up the weights between the Nodes.
			for(Edge e : n.getEdges()){
				Node terminal = e.getTerminalNode();
				matrix[totalLength+1][timersList.indexOf(terminal.getLeastTimer())+1]
				            = e.getWeight();
			}
			
			totalLength += n.size();
		}
		
		Zone newZone = new Zone(timers, matrix);
		
		Zone.setLexicon(_indexToTransition);
		
		for(int i=1; i<_indexToTimer.length; i++){
			int timerIndex = _indexToTimer[i];
			intPair p = _bounds.get(timerIndex);
			
			newZone.setLowerBoundbyTransitionIndex(timerIndex, p.getLeftInt());
			newZone.setUpperBoundbyTransitionIndex(timerIndex, p.getRightInt());
		}
		
		//return new Zone(timers, matrix);
		return newZone;
	}
	
	public void clearZone(){
		_tmpZone = null;
	}
	
	public void toDot(PrintStream writer){
		
		// Write the header.
		writer.println("digraph G {");
		
		// Define the nodes.
		HashSet<Node> nodes = new HashSet<Node>(_graph.values());
		
		for(Node n : nodes){
			writer.println("\"t" + n.getLeastTimer() + "\"" +  
					"[label=\"" + n.partitionString() + "\"]");
		}
		
		// Print edges.
		for(Node n : nodes){
			for(Edge e : n.getEdges()){
				writer.println("\"t" + n.getLeastTimer() + "\"" + "->" + 
						"\"t" + e.getTerminalNode().getLeastTimer() + "\"" + 
						"[label=\"" + e.getWeight() + "\"]");
			}
		}
		
		// Close the main block.
		writer.println("}");
	}
	
	/*
	 * Create nodes for each zero-equivalent classes.
	 */
	public void createPartition(Zone z){
		
		// Put each timer in the correct partition.
		//for(int i=0; i<_indexToTransition.size(); i++){
		for(int i=0; i<_indexToTimer.length; i++){
			boolean wasAdded = false;
			
			for(Node n : _graph.values()){
				wasAdded |= n.add(i, z);
				if(wasAdded){
					_graph.put(i, n);
					break;
				}
			}
			
			// If the element was not contained in any previous Node, then
			// add a new node.
			if(!wasAdded){
				_graph.put(i, new Node(i));
			}
		}
	}

	/**
	 * Add edges between nodes.
	 * 
	 * @param z
	 * 		The zone for the connections.
	 */
	public void connectPartition(Zone z){
		int i=0, j=0;
		HashSet<Node> nodes = new HashSet<Node>(_graph.values());
		
		HashSet<intPair> redundantPairs = getRedundantEdge(nodes, z);
		
		for(Node iNode : nodes){
			j=0;
			int iTimer = iNode.getLeastTimer();
			for(Node jNode : nodes){
				int jTimer = jNode.getLeastTimer();
				if(i<j){
					if(!redundantPairs.contains(new intPair(iTimer, jTimer))){
						iNode._edges.add(new Edge(jNode, z.getDbmEntry(iTimer, jTimer)));
					}
					if(!redundantPairs.contains(new intPair(j, i))){
						jNode._edges.add(new Edge(iNode, z.getDbmEntry(jTimer, iTimer)));
					}
				}
				j++;
			}
			i++;
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see verification.timed_state_exploration.zone.ZoneType#getLexicon()
	 */
	public HashMap<Integer, Transition> getLexicon(){
		return new HashMap<Integer, Transition> (_indexToTransition);
	}
	
	
	private HashSet<intPair> getRedundantEdge(HashSet<Node> nodes, Zone z){
		HashSet<intPair> redundantEdges = new HashSet<intPair>();
		
		for(Node kNode : nodes){
			int k = kNode.getLeastTimer();
			for(Node iNode : nodes){
				int i = iNode.getLeastTimer();
				for(Node jNode : nodes){
					int j = jNode.getLeastTimer();
					if(z.getDbmEntry(i, k) != ZoneType.INFINITY && 
							z.getDbmEntry(k, j) != ZoneType.INFINITY
							&& z.getDbmEntry(i, j) > z.getDbmEntry(i, k) + z.getDbmEntry(k, j))
					{
						//setDbmEntry(i, j, getDbmEntry(i, k) + getDbmEntry(k, j));
						redundantEdges.add(new intPair(i, j));
					}
				}
			}
		}
		
		
		return redundantEdges;
	}
	
	//-------------------Inner Classes---------------------------------------------------
	/**
	 * A node of the graph.
	 * @author Andrew N. Fisher
	 *
	 */
	private class Node implements Comparable<Node>{
		
		/* 
		 * Abstraction Function :
		 * 
		 */
		
		/* 
		 * Representation Invariant :
		 * 
		 */
		
		/*
		 * Label for the node.
		 */
		//String _name;
		
		/* 
		 * List of out edges.
		 */
		ArrayList<Edge> _edges;
		
		/* 
		 * Previous nodes, ie list of nodes prevNode such that there is an edge
		 * from preNode to this node.
		 */
		//ArrayList<Node> _previousNode;
		
		/* The partition. */
		TreeSet<Integer> _partition;
		
		/* The connecting edges in the node. The zero element gives the weight of the 
		 * edge from the least timer ti in the _partition to tj the next timer in
		 * _partition, and so on.
		 */
		ArrayList<Integer> _internalEdges;
		
		/* Cached hash code. Code should be -1 if not created yet. */
		int _nodeHashCode;
		
		@SuppressWarnings("unused")
		public Node(String name){
			_edges = new ArrayList<Edge>();
			//_previousNode = new ArrayList<Node>();
			_partition = new TreeSet<Integer>();
			_nodeHashCode = -1;
			
			//_name = name;
		}
		
		/**
		 * Create a node containing the timer i.
		 * @param i
		 * 		The element to start the node with.
		 */
		public Node(int i) {
			_edges = new ArrayList<Edge>();
			//_previousNode = new ArrayList<Node>();
			_partition = new TreeSet<Integer>();
			_internalEdges = new ArrayList<Integer>();
			_nodeHashCode = -1; 		// Indicates the hash code has not been calculated.
			
			_partition.add(i);
		}
		
		/**
		 * Retrieve the smallest timer of the Node.
		 * @return
		 * 		The smallest timer contained in this Node.
		 */
		public int getLeastTimer() {
			return _partition.first();
		}

		/**
		 * Gives the weight of the edge from the ith timer to the i+1st timer in the 
		 * Node.
		 * @param i
		 * @return
		 * 		The weight of the edge from the ith timer to the i+1st timer in the Node.
		 */
		public int getInternalEdgeWeight(int i) {

			return _internalEdges.get(i);
		}

		public int size(){
			return _partition.size();
		}
		
		/**
		 * Adds the element to the current node if the element is equivalent to the
		 * elements in the node.
		 * @param i
		 * 		The timer to add.
		 * @param z
		 * 		The zone to use for determining the equivalence.
		 * @return
		 * 		True if the element was added, false otherwise.
		 */
		public boolean add(int i, Zone z) {
			
			// Check if any elements are in the partition.
			if(_partition.size() == 0){
				_partition.add(i);
				return true;
			}
			
			// Check if the element is already in the partition.
			if(_partition.contains(i)){
				// Already in the set, no need to add it again.
				return false;
			}
			
			// Get the least value of the partition to check against.
			int least = _partition.first();
			
			int fromPartToi = z.getDbmEntry(least, i);
			
			int fromiToPart = z.getDbmEntry(i, least);
			
			if( fromPartToi == -1*fromiToPart){
				_partition.add(i);
				return true;
			}
			
			return false;
		}
		
		
		public ArrayList<Edge> getEdges(){
			ArrayList<Edge> newList = new ArrayList<Edge>();
			
			for(Edge e : _edges){
				newList.add(e);
			}
			
			return newList;
		}

		/**
		 * Connects the nodes in the partition.
		 * @param z
		 * 		The zone to get the connections from.
		 */
		public void connectInteralNodes(Zone z){
			
			if(_partition.size() < 2){
				return;
			}
			
			Iterator<Integer> iteratePartition = _partition.iterator();
			
			int i = iteratePartition.next();
			
			int j = 0;
			
			while(iteratePartition.hasNext()){
				j = iteratePartition.next();
				_internalEdges.add(z.getDbmEntry(i, j));
				
				i = j;
			}
			
			j = _partition.first();
			
			_internalEdges.add(z.getDbmEntry(i, j));
		}
		
		/**
		 * Overrides Object's toString().
		 */
//		public String toString(){
//			String s ="";
//			
//			// Print name.
//			s += "Node : " + _name + "\n";
//			
//			// Print next nodes.
//			s += "Next nodes : \n";
//			
//			for(Edge e : _edges){
//				s += e.toString();
//			}
//			
//			// Print previous nodes.
//			for(Node n : _previousNode){
//				s += n._name;
//			}
//			
//			return s;
//		}
		
		/**
		 * Overrides the Object's toString method.
		 */
		@Override
		public String toString(){
			String result = "------------------------\n Partition nodes." +
					"\n------------------------\n";
			
			// Print the partition.
			if(_partition.size() < 2 && _partition.size() > 0){
				
				result += _partition.first() + "\n";
			}
			else if(_internalEdges.size()>0){
				Iterator<Integer> iteratePartition = _partition.iterator();
				
				int k = iteratePartition.next();
				
				int j = 0, i=k, l=0;
				
				while(iteratePartition.hasNext()){
					j = iteratePartition.next();
					result += "" + i + "-" + _internalEdges.get(l) + "->" + j + "\n"; 
					
					i = j;
					l++;
				}
				
				//j = _partition.first();
				
				result += "" + i + "-" + _internalEdges.get(l) + "->" + k + "\n";
			}
			else{
				for(Integer i : _partition){
					result += "" + i + " ";
				}
				
				result += "\n";
			}
			
			result += "\n----------------------\n Out Edges \n --------------------------\n";
			
			for(Edge e : _edges){
				result += e.toString() + "\n";
			}
			
			result += "\n";
			return result;
		}
		
		/**
		 * Creates a string representation of the partition.
		 * @return
		 * 		A string of the for {x, y, ...} where x, y, etc. are the elements
		 * 		 of the partition.
		 */
		public String partitionString(){

			String result = "[";
			
			if(_partition.size() > 0){
				Iterator<Integer> partition = _partition.iterator();
				
				result += " " + _indexToTimer[partition.next()];
				
				while(partition.hasNext()){
					result += ", " + _indexToTimer[partition.next()];
				}
			}
			
			result += "]";
			return result;
			//return _partition.toString();
		}
		
		/**
		 * Provides a lexigraphical ordering of the node according to the partition elements
		 * and internal edges.
		 */
		@Override
		public int compareTo(Node o) {

			// Should be defined to be consistent with the equals property.
			// Write so as to have early outs.
			
			// First check the partition.
			Iterator<Integer> thisValue = this._partition.iterator();
			Iterator<Integer> otherValue = o._partition.iterator();
			
			while(thisValue.hasNext() && otherValue.hasNext()){
				int currentThisValue = thisValue.next();
				int currentOtherValue = otherValue.next();
				
				// If a value of the partition is different, then
				// the comparison can be made already.
				if(currentThisValue != currentOtherValue){
					return currentThisValue - currentOtherValue;
				}
			}
			
			// Making it here, means the partition are either equal
			// or one is the subset of the other. So we check
			// if one has remaining values.
			if(otherValue.hasNext()){
				// This 'if' says that the other Node has more elements in its partition than
				// the this Node. Consider the smaller partition as being smaller.
				return -1;
			}
			else if(thisValue.hasNext()){
				// This 'if' says that this Node has more elements in its partition than
				// the other Node. Consider the smaller partition as being smaller.
				return 1;
			}
			
			// Making it here means that the partitions contain the same elements.
			// Now check the edges.
			
			Iterator<Integer> thisWeight = this._internalEdges.iterator();
			Iterator<Integer> otherWeight = o._internalEdges.iterator();
			
			while(thisWeight.hasNext() && otherWeight.hasNext()){
				int currentThisWeight = thisWeight.next();
				int currentOtherWeight = otherWeight.next();
				
				// If a value of the partition is different, then
				// the comparison can be made already.
				if(currentThisWeight != currentOtherWeight){
					return currentThisWeight - currentOtherWeight;
				}
			}
			
			// Making it here means that the edges are either the same,
			// or one is a subset of the other.
			
			return this._internalEdges.size() - o._internalEdges.size();
		}

		
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {	
			// Check if the reference is null.
			if(o == null)
			{
				return false;
			}
			
			// Check that the type is correct.
			if(!(o instanceof Node))
			{
				return false;
			}
			
			// Check for equality using the Node equality.
			return equals((Node) o);
		}
		
		/**
		 * Tests for equality.
		 * @param otherGraph
		 * @return
		 */
		public boolean equals(Node otherNode){
			
			// Check if the reference is null first.
			if(otherNode == null)
			{
				return false;
			}
			
			// Check for reference equality.
			if(this == otherNode)
			{
				return true;
			}
			
			// If the hash codes are different, then the objects are not equal. 		
			if(this.hashCode() != otherNode.hashCode())
			{
				return false;
			}
			
			// Check if the partitions are the same.
			if(this._partition.size() != otherNode._partition.size()){
				return false;
			}
			
			
			if(!this._partition.equals(otherNode._partition)){
				return false;
			}
		
			if(this._internalEdges.size() != otherNode._internalEdges.size()){
				return false;
			}
			
			for(int i=0; i<this._internalEdges.size(); i++){
				if(this._internalEdges.get(i) != otherNode._internalEdges.get(i)){
					return false;
				}
			}
			
			// Check if the external edges are the same.
//			if(this._edges.size() != otherNode._edges.size()){
//				return false;
//			}
//			
//			for(Edge e : this._edges){
//				if(!otherNode._edges.contains(e)){
//					return false;
//				}
//			}
			
			return true;
		}
		
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode(){
			if(_nodeHashCode >=0){
				return _nodeHashCode;
			}
			
			// Set the hash code.
			_nodeHashCode = Math.abs(_partition.hashCode()^_internalEdges.hashCode());
			
			return _nodeHashCode;
		}
		
	}
	
	/**
	 * A directed edge of the graph.
	 * @author Andrew N. Fisher
	 *
	 */
	private class Edge{
		
		/* 
		 * Abstraction Function :
		 * 
		 */
		
		/* 
		 * Representation Invariant :
		 * 
		 */
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((_terminal == null) ? 0 : _terminal.hashCode());
			result = prime * result + _weight;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Edge other = (Edge) obj;
			return this.equals(other);
		}
		
		public boolean equals(Edge other){
			if(this == other){
				return true;
			}
			
			if(other == null){
				return false;
			}
			
			if(this.hashCode() != other.hashCode()){
				return false;
			}
			
			if (_terminal == null) {
				if (other._terminal != null){
					return false;
				}
			} else if (!_terminal.equals(other._terminal))
				return false;
			if (_weight != other._weight)
				return false;
			return true;
		}

		/* 
		 * Weight of edge.
		 */
		int _weight;
				
		/*
		 * Ending node.
		 */
		
		Node _terminal;
		
		/**
		 * Creates an edge.
		 * @param initialNode
		 * 		The starting node for the edge.
		 * @param weight
		 * 		The weight of the edge.
		 * @param terminalNode
		 * 		The terminal node for the edge.
		 */
		public Edge(Node terminalNode, int weight){

			_weight = weight;
			_terminal = terminalNode;
		}
		
		/**
		 * Get the weight associated with this Edge.
		 * @return
		 * 		The weight associated with this Edge.
		 */
		public int getWeight() {
			
			return _weight;
		}

		public Node getTerminalNode() {
			
			return _terminal;
		}

		/**
		 * Overrides Object's toString().
		 */
		@Override
		public String toString(){
			String s = "";
			
			//s += "-" + _weight +"-> " + _terminal._name;
			
			s += "-" + _weight + "-> " + _terminal.partitionString();
			
			return s;
		}

		private ZoneGraph getOuterType() {
			return ZoneGraph.this;
		}
	}
	
	private class intPair{

		int _leftInt, _rightInt;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + _leftInt;
			result = prime * result + _rightInt;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			intPair other = (intPair) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (_leftInt != other._leftInt)
				return false;
			if (_rightInt != other._rightInt)
				return false;
			return true;
		}
		
		public intPair(int leftInt, int rightInt) {
			this._leftInt = leftInt;
			this._rightInt = rightInt;
		}

		public int getLeftInt() {
			return _leftInt;
		}

		@SuppressWarnings("unused")
		public void setLeftInt(int leftInt) {
			this._leftInt = leftInt;
		}

		public int getRightInt() {
			return _rightInt;
		}

		@SuppressWarnings("unused")
		public void setRightInt(int rightInt) {
			this._rightInt = rightInt;
		}

		private ZoneGraph getOuterType() {
			return ZoneGraph.this;
		}
		
		@Override
		public String toString(){
			return "(" + this._leftInt + "," + this._rightInt + ")"; 
		}
	}

	public static int getUpperBoundbyTransitionIndex(int timer) {
		return 0;
	}

	public static int getUpperBoundbydbmIndex(int index) {
		return 0;
	}

	public static int getLowerBoundbyTransitionIndex(int timer) {
		return 0;
	}

	public static int getLowerBoundbydbmIndex(int index) {
		return 0;
	}

	public static int getDbmEntry(int i, int j) {
		return 0;
	}

	public static boolean exceedsLowerBoundbyTransitionIndex(int timer) {
		return false;
	}

	public static boolean exceedsLowerBoundbydbmIndex(int index) {
		return false;
	}

	public static ZoneType fireTransitionbyTransitionIndex(int timer,
			int[] enabledTimers, State state) {
		return null;
	}

	public static ZoneType fireTransitionbydbmIndex(int index, int[] enabledTimers,
			State state) {
		return null;
	}

	public static List<Transition> getEnabledTransitions() {
		return null;
	}
	
	
}
