package net.passengerDB.nen.asm.transformer;

import java.util.Random;

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

public class EnchantmentResponse {

	public static byte[] transformForPlayer(String name, String transformedName, byte[] clsdata) {
		
		String func = FMLForgePlugin.RUNTIME_DEOBF ? "func_71059_n" : "attackTargetEntityWithCurrentItem";
		
		ClassNode clsNode = new ClassNode();
	    ClassReader clsReader = new ClassReader(clsdata);
	    clsReader.accept(clsNode, 0);
		
	    MethodNode mn = ASMUtils.findFirstMethod(clsNode, name, func);
	    
	    InsnList insns = new InsnList();
        try {
        	AbstractInsnNode target = ASMUtils.findNthVarInsn(mn,Opcodes.FSTORE,3,2,false);
        	AbstractInsnNode target2 = ASMUtils.findNthMethodInsn(mn, "net/minecraft/entity/player/EntityPlayer", "func_184614_ca", "()Lnet/minecraft/item/ItemStack;", 3, false);
        	//讓不死剋星及節肢剋星可以根據宿主生效
        	insns.add(new VarInsnNode(Opcodes.ALOAD,0));
        	insns.add(new VarInsnNode(Opcodes.ALOAD,1));
        	insns.add(new VarInsnNode(Opcodes.FLOAD,3));
            insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ASMUtils.getInsertMethodCls(), "applyEnchantmentToPartForPlayer", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;F)F", false));
            insns.add(new VarInsnNode(Opcodes.FSTORE,3));
            
            mn.instructions.insert(target, insns);
            
            //使itemstack1.hitEntity及火焰附加可以生效
            insns = new InsnList();
            
            insns.add(new VarInsnNode(Opcodes.ALOAD,0));
            insns.add(new VarInsnNode(Opcodes.ALOAD,1));
            insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ASMUtils.getInsertMethodCls(), "attackHandlerForPlayer", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V", false));
            
            mn.instructions.insert(target2, insns);
            
            ClassWriter writer = new ClassWriter(0);
            clsNode.accept(writer);
            
            ASMUtils.info("Successfully transform class EntityPlayer.");
            
            return writer.toByteArray();
        }
        catch(Exception e) {
        	ASMUtils.error("Something went wrong. Print the stack trace...");
        	e.printStackTrace();
        }
	    
		return clsdata;
	}
	
	public static byte[] transformForMob(String name, String transformedName, byte[] clsdata) {
		
		String func = FMLForgePlugin.RUNTIME_DEOBF ? "func_70652_k" : "attackEntityAsMob";
		
		ClassNode clsNode = new ClassNode();
	    ClassReader clsReader = new ClassReader(clsdata);
	    clsReader.accept(clsNode, 0);
		
	    MethodNode mn = ASMUtils.findFirstMethod(clsNode, name, func);
	    
	    InsnList insns = new InsnList();
        try {
        	AbstractInsnNode target = ASMUtils.findNthVarInsn(mn,Opcodes.ALOAD,1,1,false);
        	AbstractInsnNode target2 = ASMUtils.findNthMethodInsn(mn,"net/minecraft/entity/monster/EntityMob","func_174815_a","(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;)V",1,true);
        	//
        	insns.add(new VarInsnNode(Opcodes.ALOAD,0));
        	insns.add(new VarInsnNode(Opcodes.ALOAD,1));
        	insns.add(new VarInsnNode(Opcodes.FLOAD,2));
            insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ASMUtils.getInsertMethodCls(), "applyEnchantmentToPartForMob", "(Lnet/minecraft/entity/monster/EntityMob;Lnet/minecraft/entity/Entity;F)F", false));
            insns.add(new VarInsnNode(Opcodes.FSTORE,2));
            
            mn.instructions.insertBefore(target, insns);
            
            //
            insns = new InsnList();
            
            insns.add(new VarInsnNode(Opcodes.ALOAD,0));
        	insns.add(new VarInsnNode(Opcodes.ALOAD,1));
            insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ASMUtils.getInsertMethodCls(), "attackHandlerForMob", "(Lnet/minecraft/entity/monster/EntityMob;Lnet/minecraft/entity/Entity;)V", false));
            
            mn.instructions.insert(target2, insns);
            
            ClassWriter writer = new ClassWriter(0);
            clsNode.accept(writer);
            
            ASMUtils.info("Successfully transform class EntityMob.");
            
            return writer.toByteArray();
        }
        catch(Exception e) {
        	ASMUtils.error("Something went wrong. Print the stack trace...");
        	e.printStackTrace();
        }
	    
		return clsdata;
	}

	
}
