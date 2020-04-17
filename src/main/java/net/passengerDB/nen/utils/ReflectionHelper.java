package net.passengerDB.nen.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionHelper {

	public static Object getFieldValue(Class objCls, Object o, String fieldname) {
		try {
			Field f = objCls.getDeclaredField(fieldname);
			if(!f.isAccessible()) f.setAccessible(true);
			return f.get(o);
		} catch(Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}
	
	public static Object invokeMethodFrom(Object o, String methodName, Class[] paramTypes, Object... args) {
		try {
			boolean isClass = o instanceof Class;
			Method m = isClass ? ((Class) o).getDeclaredMethod(methodName, paramTypes) : o.getClass().getDeclaredMethod(methodName, paramTypes);
			if(!m.isAccessible()) m.setAccessible(true);
			return m.invoke(isClass ? null : o, args);
		} catch(Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}
	
}
