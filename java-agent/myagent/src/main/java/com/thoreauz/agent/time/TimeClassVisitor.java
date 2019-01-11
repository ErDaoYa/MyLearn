package com.thoreauz.agent.time;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;


/**
 * 2019/1/8 11:55 PM.
 *
 * @author zhaozhou
 */
public class TimeClassVisitor extends ClassVisitor {
    private String className = null;


    public TimeClassVisitor(ClassVisitor classVisitor, String className) {
        super(Opcodes.ASM7, classVisitor);
        this.className = className.replace('/','.');
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
        //过来待修改类的构造函数
        if (name.equals("<init>") || mv == null) {
            // 对象初始化方法就不增强了
            return mv;
        }
        String key = className + ":" + name;
        mv = new AdviceAdapter(Opcodes.ASM7, mv, access, name, descriptor) {
            //方法进入时获取开始时间
            @Override
            public void onMethodEnter() {
                mv.visitLdcInsn(key);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                mv.visitMethodInsn(INVOKESTATIC,
                        "com/thoreauz/agent/TimeCache",
                        "setStartTime",
                        "(Ljava/lang/String;J)V",
                        false);
            }

            //方法退出时获取结束时间并计算执行时间
            @Override
            public void onMethodExit(int opcode) {
                mv.visitLdcInsn(key);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                mv.visitMethodInsn(INVOKESTATIC,
                        "com/thoreauz/agent/TimeCache",
                        "setEndTime",
                        "(Ljava/lang/String;J)V",
                        false);

                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitLdcInsn(key);
                mv.visitMethodInsn(INVOKESTATIC,
                        "com/thoreauz/agent/TimeCache",
                        "getCostTime",
                        "(Ljava/lang/String;)Ljava/lang/String;",
                        false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

            }
        };
        return mv;
    }
}
