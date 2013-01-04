package cn.edu.nju.moon.conup.util;


import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author ping su
 * @version Dec 11, 2012 7:05:10 PM
 *  */
public class TryCatch implements Opcodes{

	private void generateClass(ClassVisitor cv) {		
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "cn/edu/nju/moon/conup/sample/db/services/DBServiceImpl", null, "java/lang/Object", new String[] { "cn/edu/nju/moon/conup/sample/db/services/DBService" });

		cv.visitSource("DBServiceImpl.java", null);

		{
		av0 = cv.visitAnnotation("Lorg/oasisopen/sca/annotation/Service;", true);
		{
		AnnotationVisitor av1 = av0.visitArray("value");
		av1.visit(null, Type.getType("Lcn/edu/nju/moon/conup/sample/db/services/DBService;"));
		av1.visitEnd();
		}
		av0.visitEnd();
		}
		{
		mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(11, l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		mv.visitInsn(RETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "Lcn/edu/nju/moon/conup/sample/db/services/DBServiceImpl;", null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		}
		{
		mv = cv.visitMethod(ACC_PUBLIC, "dbOperation", "()Ljava/util/List;", "()Ljava/util/List<Ljava/lang/String;>;", null);
		{
		av0 = mv.visitAnnotation("Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;", true);
		av0.visitEnd();
		}
		mv.visitCode();
		Label l0 = new Label();
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitTryCatchBlock(l0, l1, l2, "java/lang/InterruptedException");
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLineNumber(17, l3);
		mv.visitTypeInsn(NEW, "java/util/ArrayList");
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
		mv.visitVarInsn(ASTORE, 1);
		mv.visitLabel(l0);
		mv.visitLineNumber(19, l0);
		mv.visitLdcInsn(new Long(2000L));
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V");
		mv.visitLabel(l1);
		Label l4 = new Label();
		mv.visitJumpInsn(GOTO, l4);
		mv.visitLabel(l2);
		mv.visitLineNumber(20, l2);
		mv.visitFrame(Opcodes.F_FULL, 2, new Object[] {"cn/edu/nju/moon/conup/sample/db/services/DBServiceImpl", "java/util/List"}, 1, new Object[] {"java/lang/InterruptedException"});
		mv.visitVarInsn(ASTORE, 2);
		Label l5 = new Label();
		mv.visitLabel(l5);
		mv.visitLineNumber(21, l5);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/InterruptedException", "printStackTrace", "()V");
		mv.visitLabel(l4);
		mv.visitLineNumber(23, l4);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn("hello tuscany...");
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
		mv.visitInsn(POP);
		Label l6 = new Label();
		mv.visitLabel(l6);
		mv.visitLineNumber(25, l6);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ARETURN);
		Label l7 = new Label();
		mv.visitLabel(l7);
		mv.visitLocalVariable("this", "Lcn/edu/nju/moon/conup/sample/db/services/DBServiceImpl;", null, l3, l7, 0);
		mv.visitLocalVariable("result", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/String;>;", l0, l7, 1);
		mv.visitLocalVariable("e", "Ljava/lang/InterruptedException;", null, l5, l4, 2);
		mv.visitMaxs(2, 3);
		mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "tryCatch", "(Ljava/lang/String;Ljava/lang/String;)V", null, null);
			{
			av0 = mv.visitAnnotation("Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;", true);
			av0.visitEnd();
			}
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(115, l0);
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
			mv.visitLdcInsn(",");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
			mv.visitVarInsn(ASTORE, 3);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(116, l1);
			mv.visitLdcInsn("");
			mv.visitVarInsn(ASTORE, 4);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLineNumber(117, l2);
			mv.visitLdcInsn("aa");
			mv.visitVarInsn(ASTORE, 5);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitLineNumber(118, l3);
			mv.visitInsn(RETURN);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitLocalVariable("this", "Lcn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl;", null, l0, l4, 0);
			mv.visitLocalVariable("userName", "Ljava/lang/String;", null, l0, l4, 1);
			mv.visitLocalVariable("passwd", "Ljava/lang/String;", null, l0, l4, 2);
			mv.visitLocalVariable("cred", "Ljava/lang/String;", null, l1, l4, 3);
			mv.visitLocalVariable("data", "Ljava/lang/String;", null, l2, l4, 4);
			mv.visitLocalVariable("token", "Ljava/lang/String;", null, l3, l4, 5);
			mv.visitMaxs(3, 6);
			mv.visitEnd();
			}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "dbOperationFinally", "()Ljava/util/List;", "()Ljava/util/List<Ljava/lang/String;>;", null);
			{
			av0 = mv.visitAnnotation("Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;", true);
			av0.visitEnd();
			}
			mv.visitCode();
			Label l0 = new Label();
			Label l1 = new Label();
			Label l2 = new Label();
			mv.visitTryCatchBlock(l0, l1, l2, "java/lang/InterruptedException");
			Label l3 = new Label();
			Label l4 = new Label();
			mv.visitTryCatchBlock(l0, l3, l4, null);
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitLineNumber(17, l5);
			mv.visitTypeInsn(NEW, "java/util/ArrayList");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
			mv.visitVarInsn(ASTORE, 1);
			mv.visitLabel(l0);
			mv.visitLineNumber(19, l0);
			mv.visitLdcInsn(new Long(2000L));
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V");
			mv.visitLabel(l1);
			Label l6 = new Label();
			mv.visitJumpInsn(GOTO, l6);
			mv.visitLabel(l2);
			mv.visitLineNumber(20, l2);
			mv.visitFrame(Opcodes.F_FULL, 2, new Object[] {"cn/edu/nju/moon/conup/sample/db/services/DBServiceImpl", "java/util/List"}, 1, new Object[] {"java/lang/InterruptedException"});
			mv.visitVarInsn(ASTORE, 2);
			Label l7 = new Label();
			mv.visitLabel(l7);
			mv.visitLineNumber(21, l7);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/InterruptedException", "printStackTrace", "()V");
			mv.visitLabel(l3);
			mv.visitLineNumber(24, l3);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn("finally...");
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
			mv.visitInsn(POP);
			Label l8 = new Label();
			mv.visitJumpInsn(GOTO, l8);
			mv.visitLabel(l4);
			mv.visitLineNumber(23, l4);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/Throwable"});
			mv.visitVarInsn(ASTORE, 3);
			Label l9 = new Label();
			mv.visitLabel(l9);
			mv.visitLineNumber(24, l9);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn("finally...");
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
			mv.visitInsn(POP);
			Label l10 = new Label();
			mv.visitLabel(l10);
			mv.visitLineNumber(25, l10);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitInsn(ATHROW);
			mv.visitLabel(l6);
			mv.visitLineNumber(24, l6);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn("finally...");
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
			mv.visitInsn(POP);
			mv.visitLabel(l8);
			mv.visitLineNumber(26, l8);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn("hello tuscany...");
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
			mv.visitInsn(POP);
			Label l11 = new Label();
			mv.visitLabel(l11);
			mv.visitLineNumber(28, l11);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitInsn(ARETURN);
			Label l12 = new Label();
			mv.visitLabel(l12);
			mv.visitLocalVariable("this", "Lcn/edu/nju/moon/conup/sample/db/services/DBServiceImpl;", null, l5, l12, 0);
			mv.visitLocalVariable("result", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/String;>;", l0, l12, 1);
			mv.visitLocalVariable("e", "Ljava/lang/InterruptedException;", null, l7, l3, 2);
			mv.visitMaxs(2, 4);
			mv.visitEnd();
			}
		cv.visitEnd();

		}
	public ClassNode generateBasicClass() {
		ClassNode cn = new ClassNode();
		generateClass(cn);
		return cn;
	} 
}
