package hello.asm;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Set;

public class Transformer {

	public void doTransform(String className, InputStream inputStream) {
		System.out.println("Class Name: "+className);
		try {
			final String c_name = className.replace(".class",""); 
			final HashMap<String, Boolean> lifeCycle = new HashMap<>(); // onCreate, onStart, onResume
			lifeCycle.put("onCreate", false);
			lifeCycle.put("onStart", false);
			lifeCycle.put("onResume", false);

			ClassWriter cw = new ClassWriter(0); 
			ClassReader cr = new ClassReader(inputStream);
			cr.accept( new ClassVisitor(Opcodes.ASM4, cw) { 
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

				}, ClassReader.EXPAND_FRAMES
				// ClassReader.EXPAND_FRAMES or ClassReader.SKIP_FRAMES when original class is commpiled >= Java 7 
			); 
			Set<String> cycleNames = lifeCycle.keySet();
			for(String cycleName : cycleNames) {
				if (!lifeCycle.get(cycleName)) {
					MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, cycleName,"()V", null, null);
					mv = new OverrideAdapter(Opcodes.ACC_PUBLIC, cycleName, "()V", mv);
					/*mv.visitMethodInsn(
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
					mv.visitLdcInsn(cycleName);
					mv.visitMethodInsn(
							Opcodes.INVOKEVIRTUAL, 
							"com/lantern/lantern/RYLA", 
							"startRender", 
							"(Ljava/lang/String;)V"); 

					// super.onLife();
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitMethodInsn(
							Opcodes.INVOKESPECIAL, 
							"android/support/v7/app/AppCompatActivity", 
							cycleName, 
							"()V");

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
					mv.visitLdcInsn(cycleName);
					mv.visitMethodInsn(
							Opcodes.INVOKEVIRTUAL, 
							"com/lantern/lantern/RYLA", 
							"endRender", 
							"(Ljava/lang/String;)V"); */
					mv.visitEnd();
				}
			}
			
			FileOutputStream stream = new FileOutputStream(c_name+".class"); 
			stream.write(cw.toByteArray());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}