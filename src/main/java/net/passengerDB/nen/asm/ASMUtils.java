package net.passengerDB.nen.asm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.passengerDB.nen.utils.NenLogger;

public class ASMUtils {
	
	private static Logger logger = LogManager.getLogger("NenASM");
	
	public static void info(String s) {
		logger.info(s);
	}
	
	public static void error(String s) {
		logger.error(s);
	}
	
	public static String getInsertMethodCls() {
		return "net/passengerDB/nen/asm/ASMInjectMethods";
	}
	
	public static AbstractInsnNode findNthMethodInsn(MethodNode m, String owner, String name, String desc, int nth, boolean reverse) {
		//find first specific instruction in specific method
		int count = 0;
		AbstractInsnNode ain = reverse ? m.instructions.getLast() : m.instructions.getFirst();
		while(ain != null) {
			if (ain instanceof MethodInsnNode){
				MethodInsnNode min = (MethodInsnNode)ain;
				//NenLogger.info(String.format("%s   %s   %s", min.owner, min.name, min.desc));
				if ((min.owner.equals(owner)) && (min.name.equals(name)) && (min.desc.equals(desc)) && (++count == nth)) return min;
			}
			ain = reverse ? ain.getPrevious() : ain.getNext();
		}
		
		return null;
	}
	
	public static AbstractInsnNode findNthVarInsn(MethodNode m, int opcode, int var, int nth, boolean reverse) {
		int count = 0;
		AbstractInsnNode ain = reverse ? m.instructions.getLast() : m.instructions.getFirst();
		while(ain != null) {
			if ((ain instanceof VarInsnNode)){
				VarInsnNode vin = (VarInsnNode)ain;
				//NenLogger.info(String.format("%d   %d", vin.getOpcode(), vin.var));
				if (vin.getOpcode() == opcode && vin.var == var){
					if(++count == nth) return vin;
				}
			}
			ain = reverse ? ain.getPrevious() : ain.getNext();
		}
		
		return null;
	}
	
	public static MethodNode findFirstMethod(ClassNode clsNode, String clsobfName, String methodName) {
		for (MethodNode mn : clsNode.methods) {
            if(methodName.equals(FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(clsobfName, mn.name, mn.desc))) return mn;
		}
		return null;
	}
	
}
