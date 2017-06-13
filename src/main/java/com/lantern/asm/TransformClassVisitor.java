package com.lantern.asm;

import org.objectweb.asm.*; 

import java.util.HashMap;
import java.util.Set;

public class TransformClassVisitor extends ClassVisitor {
	HashMap<String, Boolean> lifeCycle = new HashMap<>(); // onCreate, onStart, onResume
	String superClass = "";

	public TransformClassVisitor(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
		lifeCycle.put("onCreate", false);
		lifeCycle.put("onStart", false);
		lifeCycle.put("onResume", false);
	}

	@Override
	public void visit(int version, int access, String name,
		String signature, String superName, String[] interfaces) {
		cv.visit(Opcodes.V1_5, access, name, signature, superName, interfaces);
		superClass = superName;
		System.out.println("super class name : " + superClass + "\n");
	}

	@Override 
	public MethodVisitor visitMethod(int access, String name, 
			String desc, String signature, String[] exceptions) { 
		System.out.println("Name: "+name+", "+desc + ", "+signature+", "+access);
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions); 

		switch (name) {
			case "onCreate":
				lifeCycle.replace("onCreate", true);
				return new TransformerAdapter(access, name, desc, mv); 
			case "onStart":
				lifeCycle.replace("onStart", true);
				return new TransformerAdapter(access, name, desc, mv); 
			case "onResume":
				lifeCycle.replace("onResume", true);
				return new TransformerAdapter(access, name, desc, mv); 
			default:
				return mv;
		}
	}
	
	@Override
	public void visitEnd() {
		Set<String> cycleNames = lifeCycle.keySet();
		for(String cycleName : cycleNames) {
			if (!lifeCycle.get(cycleName)) {
				// Adapter 새로 달아서 하고 싶은데..
				//ClassVisitor customCv = new OverrideAdapter(cv);
				//customCv.visitMethod(Opcodes.ACC_PROTECTED, cycleName,"()V", null, null).visitEnd();
				// TODO visitMethod 해서 나온 mv 를 어댑터를 달아서 visitCode를 변경

				String fieldName = "";
				switch (cycleName) {
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

				MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PROTECTED, cycleName,"()V", null, null);
				mv.visitCode();
				onMethodTop(mv, fieldName);
				onMethodMiddle(mv, cycleName);
				onMethodBottom(mv, fieldName);
				mv.visitMaxs(1+4,1);
				mv.visitEnd();
			}
		}

		cv.visitEnd();
	}



	protected void onMethodTop(MethodVisitor mv, String fieldName) {
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
	}

	protected void onMethodMiddle(MethodVisitor mv, String name) {
		// super.onLife();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(
				Opcodes.INVOKESPECIAL, 
				superClass,
				//"android/support/v7/app/AppCompatActivity", 
				name, 
				"()V");
	}

	protected void onMethodBottom(MethodVisitor mv, String fieldName) {
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

		// 반환값 없어도 RETURN 해줘야함
		mv.visitInsn(Opcodes.RETURN);
	}

}