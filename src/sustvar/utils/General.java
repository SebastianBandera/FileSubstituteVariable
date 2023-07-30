package sustvar.utils;

import java.util.Iterator;
import java.util.function.Supplier;

public class General {

	@SafeVarargs
	public final static <T> T coalesce(Supplier<T> ... args) {
		if (args == null) return null;
		
		for(Supplier<T> supply : args) {
			if(supply != null) {
				T item = supply.get();
				if(item != null) {
					return item;
				}
			}
		}
		
		return null;
	}
	
	public final static <T> T safeGet(Iterable<T> iterable) {
		if (iterable == null) return null;
		
		try {
			Iterator<T> iter = iterable.iterator();
			if (iter.hasNext()) {
				return iter.next();
			}
		} catch (Exception e) {
			return null;
		}
		
		return null;
	}
}
