package com.lantern.asm;

import org.objectweb.asm.*; 

public class OverrideAdapter extends ClassVisitor {

	public OverrideAdapter(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
	}

	@Override
	public void visit(int version, int access, String name,
		String signature, String superName, String[] interfaces) {
		cv.visit(version, access, name, signature, superName, interfaces);
	}

	@Override 
	public MethodVisitor visitMethod(int access, String name, 
			String desc, String signature, String[] exceptions) { 
		System.out.println("Name: "+name+", "+desc + ", "+signature+", "+access);
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions); 

		String fieldName = "";
		switch (name) {
			case "onCreate":
				fieldName = "CREATED";
				break;
			case "onStart":
				fieldName = "STARTED";
				break;
			case "onResume":
				fieldName = "RESUMED";
				break;
		}
mv.visitCode();
		// TOP
		mv.visitMethodInsn(
				Opcodes.INVOKESTATIC, 
				"com/lantern/lantern/RYLA", 
				"getInstance", 
				"()Lcom/lantern/lantern/RYLA;"); 
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL, 
				"com/lantern/lantern/RYLA", 
				"setActivityContext", 
				"(Landroid/content/Context;)Lcom/lantern/lantern/RYLA;"); 
		mv.visitFieldInsn(
				Opcodes.GETSTATIC, 
				"com/lantern/lantern/dump/ActivityRenderData", 
				fieldName, 
				"Ljava/lang/String;"); 
		//mv.visitLdcInsn(this.name);
		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL, 
				"com/lantern/lantern/RYLA", 
				"startRender", 
				"(Ljava/lang/String;)V"); 

		// MIDDLE
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(
				Opcodes.INVOKESPECIAL, 
				"android/support/v7/app/AppCompatActivity", 
				name, 
				"()V");

		// BOTTOM
		mv.visitMethodInsn(
				Opcodes.INVOKESTATIC, 
				"com/lantern/lantern/RYLA", 
				"getInstance", 
				"()Lcom/lantern/lantern/RYLA;"); 
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL, 
				"com/lantern/lantern/RYLA", 
				"setActivityContext", 
				"(Landroid/content/Context;)Lcom/lantern/lantern/RYLA;"); 
		mv.visitFieldInsn(
				Opcodes.GETSTATIC, 
				"com/lantern/lantern/dump/ActivityRenderData",
				fieldName, 
				"Ljava/lang/String;"); 
		//mv.visitLdcInsn(this.name);
		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL, 
				"com/lantern/lantern/RYLA", 
				"endRender", 
				"(Ljava/lang/String;)V"); 
		return mv;
	}

	@Override
	public void visitEnd() {
	}
}