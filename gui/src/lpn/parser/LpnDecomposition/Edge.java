package lpn.parser.LpnDecomposition;


public class Edge {
	public final Vertex target;
	private Integer weight;
	
	public Edge(Vertex target) {
		this.target = target;
		this.weight = 0;
	}
	
	public void addWeight() {
		this.weight ++ ;
	}
	
	public Integer getWeight() {
		return this.weight;
	}
}
