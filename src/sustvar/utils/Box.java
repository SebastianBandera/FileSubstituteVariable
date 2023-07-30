package sustvar.utils;

public class Box<T> {

	protected T value;
	
	public Box() {
		this.value = null;
	}
	
	public Box(T value) {
		this.value = value;
	}
	
	public void set(T value) {
		this.value = value;
	}
	
	public T get() {
		return this.value;
	}
}
