package net.passengerDB.nen.asm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ASMUtils {
	
	private static Logger logger = LogManager.getLogger("NenASM");
	
	public static void info(String s) {
		logger.info(s);
	}
	
	public static void error(String s) {
		logger.error(s);
	}
	
	public static AbstractInsnNode findFirstMethodInsn(MethodNode m, String owner, String name, String returnType) {
		//find first specific instruction in specific method
		
		for (int i = 0; i < m.instructions.size(); i++)
	    {
	      AbstractInsnNode ain = m.instructions.get(i);
	      if ((ain instanceof MethodInsnNode))
	      {
	        MethodInsnNode min = (MethodInsnNode)ain;
	        if ((min.owner.equals(owner)) && (min.name.equals(name)) && (min.desc.equals(returnType)))
	        {
	        	return min;
	        }
	      }
	    }
		
		return null;
	}
	
	public static AbstractInsnNode findFirstVarInsn(MethodNode m, int opcode, int var) {
		
		for (int i = 0; i < m.instructions.size(); i++)
	    {
	      AbstractInsnNode ain = m.instructions.get(i);
	      if ((ain instanceof VarInsnNode))
	      {
	    	  VarInsnNode vin = (VarInsnNode)ain;
	        if (vin.getOpcode() == opcode && vin.var == var)
	        {
	        	return vin;
	        }
	      }
	    }
		
		return null;
	}
	
}
