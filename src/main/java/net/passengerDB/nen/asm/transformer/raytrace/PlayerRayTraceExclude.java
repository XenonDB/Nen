package net.passengerDB.nen.asm.transformer.raytrace;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.entity.Entity;
import net.passengerDB.nen.asm.ASMUtils;
import net.passengerDB.nen.entityparts.*;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.Type;

import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import net.minecraftforge.classloading.FMLForgePlugin;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Predicate;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class PlayerRayTraceExclude {

public static byte[] transform(String name, String transformedName, byte[] clsdata) {
		
		String func = FMLForgePlugin.RUNTIME_DEOBF ? "func_78473_a" : "getMouseOver";
		
		ClassNode clsNode = new ClassNode();
	    ClassReader clsReader = new ClassReader(clsdata);
	    clsReader.accept(clsNode, 0);
		
	    for (MethodNode mn : clsNode.methods) {
            if(!func.equals(FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(name, mn.name, mn.desc))) 
                continue;
            
            InsnList insns = new InsnList();
            try {
                insns.add(new VarInsnNode(Opcodes.ALOAD,2));
                insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/passengerDB/nen/asm/transformer/raytrace/PlayerRayTraceExclude", "PlayerPointerExcludedSelfBodyPart", "(Ljava/util/List;Lnet/minecraft/entity/Entity;)Ljava/util/List;", false));
                
                AbstractInsnNode target = ASMUtils.findFirstVarInsn(mn,Opcodes.ASTORE,14);
                
                mn.instructions.insertBefore(target, insns);
                
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                clsNode.accept(writer);
                
                ASMUtils.info("Successfully transform class EntityRenderer.");
                
                return writer.toByteArray();
            }
            catch(Exception e) {
            	ASMUtils.error("Something went wrong. Print the stack trace...");
            	e.printStackTrace();
            }
            
            break;
        }
	    
		return clsdata;
	}
	

public static List<Entity> PlayerPointerExcludedSelfBodyPart(List<Entity> list, Entity ref){
	
	RayTraceExcludeTarget.instance.setComparedEntity(ref);
	list.removeIf(RayTraceExcludeTarget.instance);
	
	return list;
}

}
