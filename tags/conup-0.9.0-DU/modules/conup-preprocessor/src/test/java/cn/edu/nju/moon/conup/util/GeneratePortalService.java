package cn.edu.nju.moon.conup.util;


import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
/**
 * 
 * @author Ping Su
 *
 */

public class GeneratePortalService implements Opcodes {

	private void generateClass(ClassVisitor cv) {

		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cv.visit(
				V1_6,
				ACC_PUBLIC + ACC_SUPER,
				"cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl",
				null,
				"java/lang/Object",
				new String[] { "cn/edu/nju/moon/conup/sample/portal/services/PortalService" });

		cv.visitSource("PortalServiceImpl.java", null);

		{
			av0 = cv.visitAnnotation("Lorg/oasisopen/sca/annotation/Service;",
					true);
			{
				AnnotationVisitor av1 = av0.visitArray("value");
				av1.visit(
						null,
						Type.getType("Lcn/edu/nju/moon/conup/sample/portal/services/PortalService;"));
				av1.visitEnd();
			}
			av0.visitEnd();
		}
		{
			fv = cv.visitField(
					ACC_PRIVATE,
					"tokenService",
					"Lcn/edu/nju/moon/conup/sample/portal/services/TokenService;",
					null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(
					ACC_PRIVATE,
					"procService",
					"Lcn/edu/nju/moon/conup/sample/portal/services/ProcService;",
					null, null);
			fv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(27, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V");
			mv.visitInsn(RETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable(
					"this",
					"Lcn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl;",
					null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(
					ACC_PUBLIC,
					"getTokenService",
					"()Lcn/edu/nju/moon/conup/sample/portal/services/TokenService;",
					null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(37, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(
					GETFIELD,
					"cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl",
					"tokenService",
					"Lcn/edu/nju/moon/conup/sample/portal/services/TokenService;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable(
					"this",
					"Lcn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl;",
					null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(
					ACC_PUBLIC,
					"setTokenService",
					"(Lcn/edu/nju/moon/conup/sample/portal/services/TokenService;)V",
					null, null);
			{
				av0 = mv.visitAnnotation(
						"Lorg/oasisopen/sca/annotation/Reference;", true);
				av0.visitEnd();
			}
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(41, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(
					PUTFIELD,
					"cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl",
					"tokenService",
					"Lcn/edu/nju/moon/conup/sample/portal/services/TokenService;");
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(42, l1);
			mv.visitInsn(RETURN);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLocalVariable(
					"this",
					"Lcn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl;",
					null, l0, l2, 0);
			mv.visitLocalVariable(
					"tts",
					"Lcn/edu/nju/moon/conup/sample/portal/services/TokenService;",
					null, l0, l2, 1);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		getProcService(cv, mv);
		setProcService(cv, mv);
		getToken(cv, mv);
		generateConupTransactionMethod(cv, mv);
		{
			mv = cv.visitMethod(ACC_PUBLIC, "ifTest", "(Ljava/lang/String;Ljava/lang/String;)V", null, null);
			{
			av0 = mv.visitAnnotation("Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;", true);
			av0.visitEnd();
			}
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(98, l0);
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
			mv.visitLineNumber(99, l1);
			mv.visitLdcInsn("");
			mv.visitVarInsn(ASTORE, 4);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLineNumber(100, l2);
			mv.visitLdcInsn("aa");
			mv.visitVarInsn(ASTORE, 5);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitLineNumber(101, l3);
			mv.visitInsn(ICONST_1);
			mv.visitVarInsn(ISTORE, 6);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitLineNumber(102, l4);
			mv.visitVarInsn(ILOAD, 6);
			mv.visitInsn(ICONST_1);
			Label l5 = new Label();
			mv.visitJumpInsn(IF_ICMPLE, l5);
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitLineNumber(103, l6);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl", "tokenService", "Lcn/edu/nju/moon/conup/sample/portal/services/TokenService;");
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEINTERFACE, "cn/edu/nju/moon/conup/sample/portal/services/TokenService", "getToken", "(Ljava/lang/String;)Ljava/lang/String;");
			mv.visitInsn(POP);
			Label l7 = new Label();
			mv.visitLabel(l7);
			mv.visitLineNumber(104, l7);
			Label l8 = new Label();
			mv.visitJumpInsn(GOTO, l8);
			Label l9 = new Label();
			mv.visitLabel(l9);
			mv.visitLineNumber(105, l9);
			mv.visitFrame(Opcodes.F_FULL, 7, new Object[] {"cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl", "java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String", Opcodes.INTEGER}, 0, new Object[] {});
			mv.visitIincInsn(6, 1);
			Label l10 = new Label();
			mv.visitLabel(l10);
			mv.visitLineNumber(106, l10);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl", "procService", "Lcn/edu/nju/moon/conup/sample/portal/services/ProcService;");
			mv.visitVarInsn(ALOAD, 5);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEINTERFACE, "cn/edu/nju/moon/conup/sample/portal/services/ProcService", "process", "(Ljava/lang/String;Ljava/lang/String;)Ljava/util/List;");
			mv.visitInsn(POP);
			mv.visitLabel(l8);
			mv.visitLineNumber(104, l8);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ILOAD, 6);
			mv.visitIntInsn(BIPUSH, 10);
			mv.visitJumpInsn(IF_ICMPLT, l9);
			Label l11 = new Label();
			mv.visitLabel(l11);
			mv.visitLineNumber(108, l11);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl", "procService", "Lcn/edu/nju/moon/conup/sample/portal/services/ProcService;");
			mv.visitVarInsn(ALOAD, 5);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEINTERFACE, "cn/edu/nju/moon/conup/sample/portal/services/ProcService", "process", "(Ljava/lang/String;Ljava/lang/String;)Ljava/util/List;");
			mv.visitInsn(POP);
			Label l12 = new Label();
			mv.visitJumpInsn(GOTO, l12);
			mv.visitLabel(l5);
			mv.visitLineNumber(111, l5);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl", "tokenService", "Lcn/edu/nju/moon/conup/sample/portal/services/TokenService;");
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEINTERFACE, "cn/edu/nju/moon/conup/sample/portal/services/TokenService", "getToken", "(Ljava/lang/String;)Ljava/lang/String;");
			mv.visitInsn(POP);
			mv.visitLabel(l12);
			mv.visitLineNumber(113, l12);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl", "tokenService", "Lcn/edu/nju/moon/conup/sample/portal/services/TokenService;");
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEINTERFACE, "cn/edu/nju/moon/conup/sample/portal/services/TokenService", "getToken", "(Ljava/lang/String;)Ljava/lang/String;");
			mv.visitInsn(POP);
			Label l13 = new Label();
			mv.visitLabel(l13);
			mv.visitLineNumber(114, l13);
			mv.visitInsn(RETURN);
			Label l14 = new Label();
			mv.visitLabel(l14);
			mv.visitLocalVariable("this", "Lcn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl;", null, l0, l14, 0);
			mv.visitLocalVariable("userName", "Ljava/lang/String;", null, l0, l14, 1);
			mv.visitLocalVariable("passwd", "Ljava/lang/String;", null, l0, l14, 2);
			mv.visitLocalVariable("cred", "Ljava/lang/String;", null, l1, l14, 3);
			mv.visitLocalVariable("data", "Ljava/lang/String;", null, l2, l14, 4);
			mv.visitLocalVariable("token", "Ljava/lang/String;", null, l3, l14, 5);
			mv.visitLocalVariable("i", "I", null, l4, l14, 6);
			mv.visitMaxs(3, 7);
			mv.visitEnd();
			}
		cv.visitEnd();

	}

	private void getProcService(ClassVisitor cv, MethodVisitor mv) {
		mv = cv.visitMethod(ACC_PUBLIC, "getProcService",
				"()Lcn/edu/nju/moon/conup/sample/portal/services/ProcService;",
				null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(45, l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(
				GETFIELD,
				"cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl",
				"procService",
				"Lcn/edu/nju/moon/conup/sample/portal/services/ProcService;");
		mv.visitInsn(ARETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable(
				"this",
				"Lcn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl;",
				null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private void getToken(ClassVisitor cv, MethodVisitor mv) {
		mv = cv.visitMethod(ACC_PUBLIC, "getToken",
				"(Ljava/lang/String;)Ljava/lang/String;", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(53, l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(
				GETFIELD,
				"cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl",
				"tokenService",
				"Lcn/edu/nju/moon/conup/sample/portal/services/TokenService;");
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEINTERFACE,
				"cn/edu/nju/moon/conup/sample/portal/services/TokenService",
				"getToken", "(Ljava/lang/String;)Ljava/lang/String;");
		mv.visitInsn(ARETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable(
				"this",
				"Lcn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl;",
				null, l0, l1, 0);
		mv.visitLocalVariable("cred", "Ljava/lang/String;", null, l0, l1, 1);
		mv.visitMaxs(2, 2);
		mv.visitEnd();
	}

	private void setProcService(ClassVisitor cv, MethodVisitor mv) {
		mv = cv.visitMethod(
				ACC_PUBLIC,
				"setProcService",
				"(Lcn/edu/nju/moon/conup/sample/portal/services/ProcService;)V",
				null, null);
		mv.visitAnnotation("Lorg/oasisopen/sca/annotation/Reference;", true);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(49, l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitFieldInsn(
				PUTFIELD,
				"cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl",
				"procService",
				"Lcn/edu/nju/moon/conup/sample/portal/services/ProcService;");
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLineNumber(50, l1);
		mv.visitInsn(RETURN);
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitLocalVariable(
				"this",
				"Lcn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl;",
				null, l0, l2, 0);
		mv.visitLocalVariable("procService",
				"Lcn/edu/nju/moon/conup/sample/portal/services/ProcService;",
				null, l0, l2, 1);
		mv.visitMaxs(2, 2);
		mv.visitEnd();

	}

	private void generateConupTransactionMethod(ClassVisitor cv,
			MethodVisitor mv) {
		mv = cv.visitMethod(
				ACC_PUBLIC,
				"execute",
				"(Ljava/lang/String;Ljava/lang/String;)Ljava/util/List;",
				"(Ljava/lang/String;Ljava/lang/String;)Ljava/util/List<Ljava/lang/String;>;",
				null);

		mv.visitAnnotation(
				"Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;", true);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(61, l0);
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
				"Ljava/io/PrintStream;");
		mv.visitLdcInsn("enter execute.......\n\n");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
				"(Ljava/lang/String;)V");
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLineNumber(70, l1);
		mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf",
				"(Ljava/lang/Object;)Ljava/lang/String;");
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>",
				"(Ljava/lang/String;)V");
		mv.visitLdcInsn(",");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
				"(Ljava/lang/String;)Ljava/lang/StringBuilder;");
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
				"(Ljava/lang/String;)Ljava/lang/StringBuilder;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder",
				"toString", "()Ljava/lang/String;");
		mv.visitVarInsn(ASTORE, 3);
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitLineNumber(73, l2);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(
				GETFIELD,
				"cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl",
				"tokenService",
				"Lcn/edu/nju/moon/conup/sample/portal/services/TokenService;");
		mv.visitVarInsn(ALOAD, 3);
		mv.visitMethodInsn(INVOKEINTERFACE,
				"cn/edu/nju/moon/conup/sample/portal/services/TokenService",
				"getToken", "(Ljava/lang/String;)Ljava/lang/String;");
		mv.visitVarInsn(ASTORE, 4);
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLineNumber(79, l3);
		mv.visitLdcInsn("");
		mv.visitVarInsn(ASTORE, 5);
		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitLineNumber(80, l4);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(
				GETFIELD,
				"cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl",
				"procService",
				"Lcn/edu/nju/moon/conup/sample/portal/services/ProcService;");
		mv.visitVarInsn(ALOAD, 4);
		mv.visitVarInsn(ALOAD, 5);
		mv.visitMethodInsn(INVOKEINTERFACE,
				"cn/edu/nju/moon/conup/sample/portal/services/ProcService",
				"process",
				"(Ljava/lang/String;Ljava/lang/String;)Ljava/util/List;");
		mv.visitVarInsn(ASTORE, 6);
		Label l5 = new Label();
		mv.visitLabel(l5);
		mv.visitLineNumber(87, l5);
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
				"Ljava/io/PrintStream;");
		mv.visitLdcInsn("exit execute.......\n\n");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
				"(Ljava/lang/String;)V");
		Label l6 = new Label();
		mv.visitLabel(l6);
		mv.visitLineNumber(88, l6);
		mv.visitVarInsn(ALOAD, 6);
		mv.visitInsn(ARETURN);
		Label l7 = new Label();
		mv.visitLabel(l7);
		mv.visitLocalVariable(
				"this",
				"Lcn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl;",
				null, l0, l7, 0);
		mv.visitLocalVariable("userName", "Ljava/lang/String;", null, l0, l7, 1);
		mv.visitLocalVariable("passwd", "Ljava/lang/String;", null, l0, l7, 2);
		mv.visitLocalVariable("cred", "Ljava/lang/String;", null, l2, l7, 3);
		mv.visitLocalVariable("token", "Ljava/lang/String;", null, l3, l7, 4);
		mv.visitLocalVariable("data", "Ljava/lang/String;", null, l4, l7, 5);
		mv.visitLocalVariable("result", "Ljava/util/List;",
				"Ljava/util/List<Ljava/lang/String;>;", l5, l7, 6);
		mv.visitMaxs(3, 7);
		mv.visitEnd();

	}
	public ClassNode generateBasicClass() {
		ClassNode cn = new ClassNode();
		generateClass(cn);
		return cn;
	}
}
