package hello.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class OverrideAdapter extends MethodVisitor { 
	String name;
	protected OverrideAdapter(int access, String name, String desc, MethodVisitor mv) { 
		super(Opcodes.ASM4, mv);
		this.name = name;
// TODO 모르겠다 아 짜증나 그냥 직접 구현할래
		onMethodTop();
		onMethodMiddle();
		onMethodBottom();
	} 

	protected void onMethodTop() {
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

	protected void onMethodMiddle() {
		// super.onLife();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(
				Opcodes.INVOKESPECIAL, 
				"android/support/v7/app/AppCompatActivity", 
				this.name, 
				"()V");
	}

	protected void onMethodBottom() {
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
}