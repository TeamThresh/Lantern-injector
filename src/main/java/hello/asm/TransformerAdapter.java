package hello.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class TransformerAdapter extends AdviceAdapter { 
	String name;
	protected TransformerAdapter(int access, String name, String desc, MethodVisitor mv) { 
		super(Opcodes.ASM4, mv, access, name, desc);
		this.name = name;
	} 
	
	@Override 
	protected void onMethodEnter() {
		/*mv.visitFieldInsn(
				Opcodes.GETSTATIC, 
				"java/lang/System", 
				"out", 
				"Ljava/io/PrintStream;"); 
		mv.visitMethodInsn(
				Opcodes.INVOKESTATIC, 
				"java/lang/System", 
				"currentTimeMillis", 
				"()J"); 
		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL, 
				"java/io/PrintStream", 
				"println", 
				"(J)V"); */
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
		mv.visitLdcInsn(this.name);
		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL, 
				"com/lantern/lantern/RYLA", 
				"startRender", 
				"(Ljava/lang/String;)V"); 
	}
	@Override
	protected void onMethodExit(int opcode) {
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
		mv.visitLdcInsn(this.name);
		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL, 
				"com/lantern/lantern/RYLA", 
				"endRender", 
				"(Ljava/lang/String;)V"); 
	}

	
	@Override 
	public void visitMaxs(int maxStack, int maxLocals) { 
		super.visitMaxs(maxStack, maxLocals); 
	} 


}
