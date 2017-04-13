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

			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS); 
			ClassReader cr = new ClassReader(inputStream);
			ClassVisitor cv = new TransformClassVisitor(cw);
			cr.accept(cv, ClassReader.EXPAND_FRAMES); 
			
			FileOutputStream stream = new FileOutputStream(c_name+".class"); 
			stream.write(cw.toByteArray());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}