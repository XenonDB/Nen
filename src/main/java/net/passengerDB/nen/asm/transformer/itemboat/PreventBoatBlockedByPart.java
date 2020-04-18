package net.passengerDB.nen.asm.transformer.itemboat;

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
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.passengerDB.nen.asm.ASMUtils;

public class PreventBoatBlockedByPart {

	public static byte[] transform(String name, String transformedName, byte[] clsdata) {
		String func = FMLForgePlugin.RUNTIME_DEOBF ? "func_77659_a" : "onItemRightClick";
		
		ClassNode clsNode = new ClassNode();
	    ClassReader clsReader = new ClassReader(clsdata);
	    clsReader.accept(clsNode, 0);
		
	    MethodNode mn = ASMUtils.findFirstMethod(clsNode, name, func);
	    
	    InsnList insns = new InsnList();
        try {
            insns.add(new VarInsnNode(Opcodes.ALOAD,2));
            insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/passengerDB/nen/asm/transformer/raytrace/PlayerRayTraceExclude", "PlayerPointerExcludedSelfBodyPart", "(Ljava/util/List;Lnet/minecraft/entity/Entity;)Ljava/util/List;", false));
            
            AbstractInsnNode target = ASMUtils.findFirstVarInsn(mn,Opcodes.ASTORE,28);
            
            mn.instructions.insertBefore(target, insns);
            
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            clsNode.accept(writer);
            
            ASMUtils.info("Successfully transform class ItemBoat.");
            
            return writer.toByteArray();
        }
        catch(Exception e) {
        	ASMUtils.error("Something went wrong. Print the stack trace...");
        	e.printStackTrace();
        }
	    
		return clsdata;
	}
	
}
