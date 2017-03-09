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

public class Transformer {

	public void doTransform(String className, InputStream inputStream) {
		System.out.println("Class Name: "+className);
		try {
			final String c_name = className.replace(".class",""); 

			ClassWriter cw = new ClassWriter(0); 
			ClassReader cr = new ClassReader(inputStream);
			cr.accept( new ClassVisitor(Opcodes.ASM4, cw) { 
					@Override 
					public MethodVisitor visitMethod(int access, String name, 
							String desc, String signature, String[] exceptions) { 
						System.out.println("Name: "+name+", "+desc + ", "+signature+", "+access);
						MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions); 

						if ("onCreate".equals(name)) {

							return new TransformerAdapter(access, name, desc, mv); 
						} else {
							return mv;
						}
					}

				}, ClassReader.EXPAND_FRAMES
				// ClassReader.EXPAND_FRAMES or ClassReader.SKIP_FRAMES when original class is commpiled >= Java 7 
			); 
			FileOutputStream stream = new FileOutputStream(c_name+".class"); 
			stream.write(cw.toByteArray());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}