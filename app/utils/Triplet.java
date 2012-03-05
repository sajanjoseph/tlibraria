package utils;

public class Triplet <T1, T2, T3> {
	private T1 x;
	private T2 y;
	private T3 z;
	
	public Triplet(T1 x, T2 y, T3 z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public T1 getFirst() {
		return x;
	}

	public T2 getSecond() {
		return y;
	}

	public T3 getThird() {
		return z;
	}
	
	@Override
	public boolean equals(Object o) {
		Triplet<T1, T2, T3> t = (Triplet<T1, T2, T3>) o;
		return (x.equals(t.x)) && (y.equals(t.y))&& (z.equals(t.z));
	}
	
	@Override
	public int hashCode() {
		return x.hashCode() + 31 * y.hashCode() + 31 * z.hashCode();
	}

}
