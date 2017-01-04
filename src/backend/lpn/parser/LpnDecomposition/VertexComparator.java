package backend.lpn.parser.LpnDecomposition;

import java.util.Comparator;


public class VertexComparator implements Comparator<Vertex>{
	private int maxNumVarsInOneComp;
	
	public VertexComparator(Integer maxNumVarsInOneComp) {
		this.maxNumVarsInOneComp = maxNumVarsInOneComp;
	}

	@Override
	public int compare(Vertex v1, Vertex v2) {
		if (v1.calculateBestNetGain(maxNumVarsInOneComp) >= v2.calculateBestNetGain(maxNumVarsInOneComp)) 
			return -1;
		return 1;
//		else
//			return 0;
	}
}
