package net.passengerDB.nen.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class ReflectionHelper {

	public static Object getFieldValue(Class objCls, Object o, String fieldname) {
		try {
			Field f = objCls.getDeclaredField(fieldname);
			if(!f.isAccessible()) f.setAccessible(true);
			return f.get(o);
		} catch(Exception exc) {
			throw new net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException(new String[]{fieldname}, exc);
		}
	}
	
	public static Object invokeMethodFrom(Object o, String methodName, Class[] paramTypes, Object... args) {
		try {
			Method m = o instanceof Class ? ((Class) o).getDeclaredMethod(methodName, paramTypes) : o.getClass().getDeclaredMethod(methodName, paramTypes);
			if(!m.isAccessible()) m.setAccessible(true);
			return m.invoke(o, args);
		}
		catch(Exception exc) {
			throw new ReflectionHelper.InvokeMethodException(exc);
		}
	}
	
	public static class InvokeMethodException extends RuntimeException{
		public InvokeMethodException(Throwable t) {
			super(t);
		}
	}
	
}
