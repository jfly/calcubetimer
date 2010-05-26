package net.gnehzr.cct.scrambles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;

public class CrossSolver {
	public static enum Face {
		FRONT('F'), UP('U'), RIGHT('R'), BACK('B'), LEFT('L'), DOWN('D');
		private char f;
		private Face(char f) {
			this.f = f;
			faces.put(this, f);
		}
		@Override
		public String toString() {
			return "" + f;
		}
		
		public Face getOpposite() {
			switch(this) {
			case FRONT:
				return BACK;
			case BACK:
				return FRONT;
			case LEFT:
				return RIGHT;
			case RIGHT:
				return LEFT;
			case UP:
				return DOWN;
			case DOWN:
				return UP;
			default:
				return null;
			}
		}
	}
	
	public static class Rotate {
		public static Rotate identity = new Rotate();
		public static Rotate x = new Rotate("x", Face.FRONT, Face.UP, Face.BACK, Face.DOWN);
		public static Rotate y = new Rotate("y", Face.FRONT, Face.LEFT, Face.BACK, Face.RIGHT);
		public static Rotate z = new Rotate("z", Face.UP, Face.RIGHT, Face.DOWN, Face.LEFT);
		
		private HashMap<Face, Face> new_og = new HashMap<Face, Face>();
		private Rotate() {
			for(Face f : Face.values())
				new_og.put(f, f);
		}
		private Rotate(String desc, Face a, Face b, Face c, Face d) {
			this();
			this.desc = desc;
			new_og.put(b, a);
			new_og.put(c, b);
			new_og.put(d, c);
			new_og.put(a, d);
		}
		public Rotate invert() {
			Rotate newRotate = new Rotate();
			for(Face newFace : new_og.keySet()) {
				newRotate.new_og.put(getOGFace(newFace), newFace);
			}
			return newRotate;
		}
		public Rotate append(Rotate r) {
			Rotate newRotate = new Rotate();
			for(Face newFace : r.new_og.keySet()) {
				newRotate.new_og.put(newFace, getOGFace(r.getOGFace(newFace)));
			}
			return newRotate;
		}
		public Face getOGFace(Face newFace) {
			return new_og.get(newFace);
		}
		private String desc;
		public String toString() {
			return desc;
		}
	}
	
	private static DoubleHashMap<Face, Character> faces = new DoubleHashMap<Face, Character>();
	private static DoubleHashMap<String, Integer> directions = new DoubleHashMap<String, Integer>();
	static {
		directions.put("'", 3);
		directions.put("2", 2);
		directions.put("", 1);
	}
	
	private static class DoubleHashMap<A, B> {
		private HashMap<A, B> forward;
		private HashMap<B, A> backward;
		public DoubleHashMap() {
			forward = new HashMap<A, B>();
			backward = new HashMap<B, A>();
		}
//		public DoubleHashMap(DoubleHashMap<A, B> old) {
//			forward = new HashMap<A, B>(old.forward);
//			backward = new HashMap<B, A>(old.backward);
//		}
		public void put(A a, B b) {
			forward.put(a, b);
			backward.put(b, a);
		}
		public B get(A a) {
			return forward.get(a);
		}
		public A getReverse(B b) {
			return backward.get(b);
		}
		public String toString() {
			return forward.toString();
		}
	}
	private static class Pair<A, B> {
		private A car;
		private B cdr;
		public Pair(A car, B cdr) {
			this.car = car;
			this.cdr = cdr;
		}
		@Override
		public String toString() {
			return "(" + car + ", " + cdr + ")";
		}
	}
	private static class Cube {
		private Boolean[] eo;
		private Integer[] ep;
		public Cube(Face cross) {
			int count = 0;
			eo = new Boolean[12];
			ep = new Integer[12];
			for(int i : FACE_INDICES.get(cross)) {
				eo[i] = false;
				ep[i] = count++;
			}
		}
		private Cube(Boolean[] eo, Integer[] ep) {
			this.eo = eo.clone();
			this.ep = ep.clone();
		}
		public Cube applyTurns(Rotate r, String turns) {
			Cube c = this;
			for(String turn : turns.split(" ")) {
				if(turn.isEmpty()) continue;
				Character face = turn.charAt(0);
				String dir = turn.substring(1);
				c = c.applyTurn(new Pair<Face, Integer>(r.getOGFace(faces.getReverse(face)), directions.get(dir)));
			}
			return c;
		}

		private Cube applyTurn(Pair<Face, Integer> turn) {
			Cube c = new Cube(this.eo, this.ep);
			Face face = turn.car;
			int dir = turn.cdr;
			for(int i = 0; i < dir; i++) {
				int[] indices = FACE_INDICES.get(face);
				if(face == Face.FRONT || face == Face.BACK) {
					for(int index : indices)
						if(c.eo[index] != null)
							c.eo[index] = !c.eo[index];
				}
				cycle(c.eo, indices);
				cycle(c.ep, indices);
			}
			
			return c;
		}
		private <H> void cycle(H[] arr, int[] indices) {
			cycle(arr, indices[0], indices[1], indices[2], indices[3]);
		}
		private <H> void cycle(H[] arr, int i, int j, int k, int l) {
			H temp = arr[l];
			arr[l] = arr[k];
			arr[k] = arr[j];
			arr[j] = arr[i];
			arr[i] = temp;
		}
//		public boolean isSolved() {
//			return Arrays.deepEquals(eo, eo_solved) && Arrays.deepEquals(ep, ep_solved);
//		}
		public String toString() {
			return Arrays.toString(eo) + " " + Arrays.toString(ep);
//			return heuristic() + " " + value() + " " + history.toString();
		}
		public int hash_eo_count() {
//			return 12*11*10*9/(4*3*2*1) * 2*2*2*2; TODO - better hash!
			return 9*9*9*9 * 2*2*2*2 + 2*2*2*2;
		}
		public int hash_eo() {
			int hash = 0;
			int orientations = 0;
			int distance_to_last_edge = 0;
			int sum = 0;
			int shift = 1;
			for(int i = 0; i < eo.length; i++) {
				if(eo[i] != null) {
					orientations <<= 1;
					if(eo[i])
						orientations++;
					hash += shift * distance_to_last_edge;
					shift *= 9-sum;
					sum += distance_to_last_edge;
					distance_to_last_edge = 0;
				} else
					distance_to_last_edge++;
			}
			hash = (hash << 4) | orientations;
			return hash;
		}
		public static Boolean[] unhash_eo(int eo_hash) {
			Boolean[] ordinal_orientations = new Boolean[4];
			int ordinal_o = eo_hash & 0xF;
			ordinal_orientations[0] = ((ordinal_o & 0x8) != 0);
			ordinal_orientations[1] = ((ordinal_o & 0x4) != 0);
			ordinal_orientations[2] = ((ordinal_o & 0x2) != 0);
			ordinal_orientations[3] = ((ordinal_o & 0x1) != 0);
			int edges = eo_hash >> 4;
			int i0 = edges % 9;
			edges = edges / 9;
			int i1 = edges % (9 - i0);
			edges = edges / (9 - i0);
			int i2 = edges % (9 - i0 - i1);
			edges = edges / (9 - i0 - i1);
			int i3 = edges % (9 - i0 - i1 - i2);
			Boolean[] eo = new Boolean[12];
			eo[i0] = ordinal_orientations[0];
			eo[1+i0+i1] = ordinal_orientations[1];
			eo[2+i0+i1+i2] = ordinal_orientations[2];
			eo[3+i0+i1+i2+i3] = ordinal_orientations[3];
			return eo;
		}
		
		public int hash_ep_count() {
			return 12*11*10*9;
		}
		public int hash_ep() {
			int hash = 0;
			ArrayList<Integer> edges = new ArrayList<Integer>(Arrays.asList(ep));
			for(int c = 0; c < 4; c++) {
				int i = edges.indexOf(c);
				edges.remove(i);
				hash *= 12 - c;
				hash += i;
			}
			return hash;
		}
		public static Integer[] unhash_ep(int ep_hash) {
			ArrayList<Integer> ep = new ArrayList<Integer>();
			for(int c = 3; c >= 0; c--) {
				int i = ep_hash % (12-c);
				ep_hash /= (12-c);
				for(int ch = ep.size() - 1; ch < i; ch++)
					ep.add(null);
				ep.add(i, c);
			}
			return ep.toArray(new Integer[12]);
		}

		public boolean equals(Cube other) {
			return Arrays.deepEquals(eo, other.eo) && Arrays.deepEquals(ep, other.ep);
		}
		public boolean equals(Object obj) {
			return obj instanceof Cube && this.equals((Cube)obj);
		}
	}
	
	private static ArrayList<ArrayList<Pair<Face, Integer>>> iddfs(int hash1, int hash2, int solved1, int solved2, int[][] trans1, int[][] trans2, byte[] prune1, byte[] prune2, Pair<Face, Integer> lastTurn, int depth) {
		if(depth == 0) {
			if(hash1 == solved1 && hash2 == solved2) {
				ArrayList<Pair<Face, Integer>> sol = new ArrayList<Pair<Face,Integer>>();
				if(lastTurn != null)
					sol.add(lastTurn);
				ArrayList<ArrayList<Pair<Face, Integer>>> sols = new ArrayList<ArrayList<Pair<Face, Integer>>>();
				sols.add(sol);
				return sols;
			} else
				return null;
		} else {
			ArrayList<ArrayList<Pair<Face, Integer>>> sols = null;
			if((prune1 == null || prune1[hash1] <= depth) && (prune2 == null || prune2[hash2] <= depth)) {
				for(Face f : Face.values()) {
					if(lastTurn != null && (f == lastTurn.car || (f == lastTurn.car.getOpposite() && f.ordinal() > lastTurn.car.ordinal())))
						continue;
					for(int dir = 1; dir <= 3; dir++) {
						Pair<Face, Integer> turn = new Pair<Face, Integer>(f, dir);
						int turnIndex = f.ordinal()*3 + dir - 1;
						int newHash1 = hash1;
						if(trans1 != null)
							newHash1 = trans1[hash1][turnIndex];
						int newHash2 = hash2;
						if(trans2 != null)
							newHash2 = trans2[hash2][turnIndex];
						ArrayList<ArrayList<Pair<Face, Integer>>> newSols = iddfs(newHash1, newHash2, solved1, solved2, trans1, trans2, prune1, prune2, turn, depth-1);
						if(newSols != null) {
							if(lastTurn != null)
								for(ArrayList<Pair<Face, Integer>> sol : newSols)
									sol.add(0, lastTurn);
							if(sols == null)
								sols = new ArrayList<ArrayList<Pair<Face, Integer>>>();
							sols.addAll(newSols);
						}
					}
				}
			}
			return sols;
		}
	}
	
	private static EnumMap<Face, int[]> FACE_INDICES = new EnumMap<Face, int[]>(Face.class);
	static {
		FACE_INDICES.put(Face.FRONT, new int[] { 0, 4, 8, 5 });
		FACE_INDICES.put(Face.BACK, new int[] { 2, 6, 10, 7 });
		FACE_INDICES.put(Face.LEFT, new int[] { 1, 5, 9, 6 });
		FACE_INDICES.put(Face.RIGHT, new int[] { 3, 7, 11, 4 });
		FACE_INDICES.put(Face.UP, new int[] { 0, 1, 2, 3 });
		FACE_INDICES.put(Face.DOWN, new int[] { 11, 10, 9, 8 });
	}
	private static void buildTables(Face crossSide) {
		Cube solved = new Cube(crossSide);
		eo_solved_hash = solved.hash_eo();
		ep_solved_hash = solved.hash_ep();
		
		//building transition tables
		trans_eo = new int[solved.hash_eo_count()][6*3];
		for(int i = 0; i < trans_eo.length; i++) {
			for(Face f : Face.values()) {
				solved.eo = Cube.unhash_eo(i);
				Pair<Face, Integer> turn = new Pair<Face, Integer>(f, 1);
				for(int d = 0; d < 3; d++) {
					solved = solved.applyTurn(turn);
					trans_eo[i][f.ordinal()*3 + d] = solved.hash_eo();
				}
			}
		}
		trans_ep = new int[solved.hash_ep_count()][6*3];
		for(int i = 0; i < trans_ep.length; i++) {
			for(Face f : Face.values()) {
				solved.ep = Cube.unhash_ep(i);
				Pair<Face, Integer> turn = new Pair<Face, Integer>(f, 1);
				for(int d = 0; d < 3; d++) {
					solved = solved.applyTurn(turn);
					trans_ep[i][f.ordinal()*3 + d] = solved.hash_ep();
				}
			}
		}

		//TODO - something here is breaking it
//		prune_eo = new byte[solved.hash_eo_count()];
//		ArrayList<Integer> fringe = new ArrayList<Integer>();
//		fringe.add(eo_solved_hash);
//		while(!fringe.isEmpty()) {
//			int pos = fringe.remove(0);
//			for(Face f : Face.values()) {
//				for(int dir = 1; dir <= 3; dir++) {
//					int turnIndex = f.ordinal()*3 + dir - 1;
//					int newPos = trans_eo[pos][turnIndex];
//					if(prune_eo[newPos] == 0) {
//						prune_eo[newPos] = (byte) (prune_eo[pos] + 1);
//						fringe.add(newPos);
//					}
//				}
//			}
//		}
		
		prune_ep = new byte[solved.hash_ep_count()];
		ArrayList<Integer> fringe = new ArrayList<Integer>();
		fringe.add(ep_solved_hash);
		while(!fringe.isEmpty()) {
			int pos = fringe.remove(0);
			for(Face f : Face.values()) {
				for(int dir = 1; dir <= 3; dir++) {
					int turnIndex = f.ordinal()*3 + dir - 1;
					int newPos = trans_ep[pos][turnIndex];
					if(prune_ep[newPos] == 0) {
						prune_ep[newPos] = (byte) (prune_ep[pos] + 1);
						fringe.add(newPos);
					}
				}
			}
		}
	}
	
	private static int[][] trans_eo, trans_ep;
	private static byte[] prune_eo, prune_ep;
	private static int eo_solved_hash, ep_solved_hash;
	private static ArrayList<ArrayList<Pair<Face, Integer>>> solveCross(Cube cube) {
		int eo_hash = cube.hash_eo();
		int ep_hash = cube.hash_ep();
		for(int maxDepth = 0; maxDepth < 10; maxDepth++) {
//			System.out.println("Searching depth: " + maxDepth);
			ArrayList<ArrayList<Pair<Face, Integer>>> sols = iddfs(eo_hash, ep_hash, eo_solved_hash, ep_solved_hash, trans_eo, trans_ep, prune_eo, prune_ep, null, maxDepth);
			if(sols != null && !sols.isEmpty())
				return sols;
		}
		return new ArrayList<ArrayList<Pair<Face, Integer>>>();
	}
	
	public static String toString(Rotate setup_rotations, Rotate rotation, ArrayList<Pair<Face, Integer>> sol) {
		Rotate unsetup = setup_rotations.invert();
		rotation = rotation.invert();
		String sep = " ";
		String solution = "";
		if(sol != null)
			for(Pair<Face, Integer> turn : sol)
				solution += sep + unsetup.getOGFace(rotation.getOGFace(turn.car)).toString() + directions.getReverse(turn.cdr);
		if(!solution.isEmpty())
			solution = solution.substring(sep.length());
		return setup_rotations + " " + solution;
	}
	
	public static ArrayList<String> solveCross(char solveFaceName, char solveSideName, String scramble) {
		if(trans_eo == null) {
			buildTables(Face.UP);
		}
		Face solveCross = faces.getReverse(solveFaceName);
		Face solveSide = faces.getReverse(solveSideName);
		if(solveCross == null)
			solveCross = Face.UP;
		if(solveSide == null)
			solveSide = Face.UP;
		Rotate rotateUpToCrossSide = null;
		Rotate crossToSolveSide = null;
		for(Rotate r : new Rotate[] { Rotate.x, Rotate.y, Rotate.z }) {
			Rotate res = Rotate.identity;
			for(int d = 0; d <= 3; d++) {
				if(res.getOGFace(solveCross) == Face.UP) {
					rotateUpToCrossSide = res;
					break;
				}
				res = res.append(r);
			}
			res = Rotate.identity;
			for(int d = 0; d <= 3; d++) {
				if(res.getOGFace(solveSide) == solveCross) {
					crossToSolveSide = res;
					if(d == 0)
						crossToSolveSide.desc = "";
					else
						crossToSolveSide.desc = r.toString() + directions.getReverse(d);
					break;
				}
				res = res.append(r);
			}
		}
		
		Cube c = new Cube(Face.UP);
		c = c.applyTurns(rotateUpToCrossSide, scramble);
//		long start = System.nanoTime();
		ArrayList<ArrayList<Pair<Face, Integer>>> sols = solveCross(c);
//		double elapsed = (System.nanoTime() - start)/1e9;
//		System.out.println("Seconds " + elapsed);
		ArrayList<String> solutions = new ArrayList<String>();
		for(ArrayList<Pair<Face, Integer>> sol : sols)
			solutions.add(toString(crossToSolveSide, rotateUpToCrossSide, sol).trim());
		
		return solutions;
	}
	
	public static void main(String[] args) {
		System.out.println(solveCross('U', 'D', "B' F D U2 L2 R2 D B F' L' F2 L' R2 D' U' B' R' U2 F D L R D2 U L2"));
//		System.out.println(solveCross('U', 'D', "B' F D U2"));
//		System.out.println(solveCross('U', 'U', "B' F D U2"));
	}
}
