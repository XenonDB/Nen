package net.passengerDB.nen.asm.transformer.raytrace;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraftforge.classloading.FMLForgePlugin;
import net.passengerDB.nen.asm.ASMUtils;

public class WorldAABBexcluding {

public static byte[] transform(String name, String transformedName, byte[] clsdata) {
		
		String func = FMLForgePlugin.RUNTIME_DEOBF ? "func_175674_a" : "getEntitiesInAABBexcluding";
		
		ClassNode clsNode = new ClassNode();
	    ClassReader clsReader = new ClassReader(clsdata);
	    clsReader.accept(clsNode, 0);
		
	    MethodNode mn = ASMUtils.findFirstMethod(clsNode, name, func);
	    
	    InsnList insns = new InsnList();
	    
	    insns.add(new VarInsnNode(Opcodes.ALOAD,1));
        insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ASMUtils.getInsertMethodCls(), "getEntitiesExcludingSelfBodyPart", "(Ljava/util/List;Lnet/minecraft/entity/Entity;)Ljava/util/List;", false));
        insns.add(new VarInsnNode(Opcodes.ASTORE,4));
        insns.add(new VarInsnNode(Opcodes.ALOAD,4));
        
        AbstractInsnNode target = ASMUtils.findNthInsn(mn,Opcodes.ARETURN,1,true);
        
        mn.instructions.insertBefore(target, insns);
        
        ClassWriter writer = new ClassWriter(0);
        clsNode.accept(writer);
        
        ASMUtils.info(String.format("Successfully transform class %s.", transformedName));
        
        return writer.toByteArray();
	}
	
	
}
