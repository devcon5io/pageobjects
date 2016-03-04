package io.devcon5.pageobjects;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;


/**
 *
 */
public class ClassUtils {

    public static Method getMethod(final StackTraceElement stackTraceElement) throws Exception {
        final String stackTraceClassName = stackTraceElement.getClassName();
        final String stackTraceMethodName = stackTraceElement.getMethodName();
        final int stackTraceLineNumber = stackTraceElement.getLineNumber();
        Class<?> stackTraceClass = Class.forName(stackTraceClassName);

        // I am only using AtomicReference as a container to dump a String into, feel free to ignore it for now
        final AtomicReference<String> methodDescriptorReference = new AtomicReference<String>();

        String classFileResourceName = "/" + stackTraceClassName.replaceAll("\\.", "/") + ".class";
        InputStream classFileStream = stackTraceClass.getResourceAsStream(classFileResourceName);

        if (classFileStream == null) {
            throw new RuntimeException("Could not acquire the class file containing for the calling class");
        }

        try {
            ClassReader classReader = new ClassReader(classFileStream);
            classReader.accept(
                    new EmptyVisitor() {
                        @Override
                        public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, String[] exceptions) {
                            if (!name.equals(stackTraceMethodName)) {
                                return null;
                            }
                            return new MethodVisitor(Opcodes.ASM5) {
                                @Override
                                public void visitLineNumber(int line, Label start) {
                                    if (line == stackTraceLineNumber) {
                                        methodDescriptorReference.set(desc);
                                    }
                                }
                            };
                        }
                    },
                    0
            );
        } finally {
            classFileStream.close();
        }

        String methodDescriptor = methodDescriptorReference.get();

        if (methodDescriptor == null) {
            throw new RuntimeException("Could not find line " + stackTraceLineNumber);
        }

        for (Method method : stackTraceClass.getMethods()) {
            if (stackTraceMethodName.equals(method.getName()) && methodDescriptor.equals(Type.getMethodDescriptor(method))) {
                return method;
            }
        }

        throw new RuntimeException("Could not find the calling method");
    }

    static class EmptyVisitor extends ClassVisitor {

        AnnotationVisitor av = new AnnotationVisitor(Opcodes.ASM5) {

            @Override
            public AnnotationVisitor visitAnnotation(String name, String desc) {
                return this;
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                return this;
            }
        };

        public EmptyVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return av;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                     TypePath typePath, String desc, boolean visible) {
            return av;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc,
                                       String signature, Object value) {
            return new FieldVisitor(Opcodes.ASM5) {

                @Override
                public AnnotationVisitor visitAnnotation(String desc,
                                                         boolean visible) {
                    return av;
                }

                @Override
                public AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                             TypePath typePath, String desc, boolean visible) {
                    return av;
                }
            };
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM5) {

                @Override
                public AnnotationVisitor visitAnnotationDefault() {
                    return av;
                }

                @Override
                public AnnotationVisitor visitAnnotation(String desc,
                                                         boolean visible) {
                    return av;
                }

                @Override
                public AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                             TypePath typePath, String desc, boolean visible) {
                    return av;
                }

                @Override
                public AnnotationVisitor visitParameterAnnotation(
                        int parameter, String desc, boolean visible) {
                    return av;
                }

                @Override
                public AnnotationVisitor visitInsnAnnotation(int typeRef,
                                                             TypePath typePath, String desc, boolean visible) {
                    return av;
                }

                @Override
                public AnnotationVisitor visitTryCatchAnnotation(int typeRef,
                                                                 TypePath typePath, String desc, boolean visible) {
                    return av;
                }

                @Override
                public AnnotationVisitor visitLocalVariableAnnotation(
                        int typeRef, TypePath typePath, Label[] start,
                        Label[] end, int[] index, String desc, boolean visible) {
                    return av;
                }
            };
        }
    }

}
