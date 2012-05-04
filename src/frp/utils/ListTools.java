package frp.utils;

import java.util.ArrayList;
import java.util.List;

public class ListTools {
	public static <T> List<T> union(List<T> l, List<T> r) throws Exception{
			throw new Exception("Not implemented yet!!!!");
	}
	
	public static <T> List<T> intersect(List<T> request, List<T> insert){
		List<T> col = new ArrayList<T>();
		for(T t : request){
			if(insert.contains(t))
				col.add(t);
		}
		return col;
	}
}
