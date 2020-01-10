package com.jackie.plugin

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

public class JavassistTransform extends Transform{


    Project project

    JavassistTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "ssy transform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {

        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }
//当前是否是增量编译
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        println "=============hello, body!=============="
        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each { TransformInput input ->
            //对类型为“文件夹”的input进行遍历
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等

            MyInject.inject(directoryInput.file.absolutePath,project)
                //是否是目录
//                if (directoryInput.file.isDirectory()) {
//                    //列出目录所有文件（包含子文件夹，子文件夹内文件）
//                    directoryInput.file.eachFileRecurse { File file ->
//                        def name = file.name
//                        if (name.endsWith(".class") && !name.startsWith("R\$")
//                                && !"R.class".equals(name) && !"BuildConfig.class".equals(name)
////                                && "android/support/v4/app/FragmentActivity.class".equals(name)
//                        ) {
//                            println '----------- deal with "class" file <' + name + '> -----------'
//                            ClassReader classReader = new ClassReader(file.bytes)
//                            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
//                            ClassVisitor cv = new TimeClassVisitor(classWriter)
//                            classReader.accept(cv, ClassReader.EXPAND_FRAMES)
//                            byte[] code = classWriter.toByteArray()
//                            FileOutputStream fos = new FileOutputStream(
//                                    file.parentFile.absolutePath + File.separator + name)
//                            fos.write(code)
//                            fos.close()
//                        }
//                    }
//                }
                //处理完输入文件之后，要把输出给下一个任务
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)


//                // 获取output目录
//                def dest = outputProvider.getContentLocation(directoryInput.name,
//                        directoryInput.contentTypes, directoryInput.scopes,
//                        Format.DIRECTORY)
//
//                ClassReader cr = new ClassReader(com.jackie.transformdemo.Student.class.getName());
//                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
//                ClassVisitor cv = new TimeClassVisitor(cw);
//                cr.accept(cv, EXPAND_FRAMES);
//
//                byte[] code = cw.toByteArray()
//
//
//                // 将input的目录复制到output指定目录
//                FileUtils.copyDirectory(directoryInput.file, dest)
            }




            //对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->

                //jar文件一般是第三方依赖库jar文件

                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //生成输出路径
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                //将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }
}