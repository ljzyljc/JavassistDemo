package com.jackie.plugin

import com.google.common.io.Files
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

import java.awt.Desktop
import java.lang.Byte
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import com.google.common.io.ByteStreams

public class MyInject {

    private final static ClassPool pool = ClassPool.getDefault()

    public static void inject(String path, Project project, Map<String, Object> map, boolean isJar) {

        //将当前路径加入类池,不然找不到这个类
        if (isJar) {
            path = "/Users/jackie/Desktop/WorkPlace/AndroidWorkPlace/JavassistDemo/app/libs/TestLib.jar!/com/jackie/testlib/MyClass.class"
        }
        pool.appendClassPath(path)
        //println("==========path==================" + path)
        //pool.appendClassPath("com.jackie.testlib")
        //project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString())
        //引入android.os.Bundle包，因为onCreate方法参数有Bundle
        pool.importPackage("android.os.Bundle")
        File dir = new File(path)
//        if (dir.isFile()) {
//            println("==========path==================是个文件")
//        } else if (dir.isDirectory()) {
//            println("==========path==================是个文件夹")
//        } else if (!dir.exists()) {
//            println("==========path==================文件不存在")
//        }

        String pathName = map.get(TryCatchExtension.PATHNAME)
        String methodName = map.get(TryCatchExtension.METHOD_NAME)
        String exceptionName = map.get(TryCatchExtension.EXCEPTION_NAME)
        String returnValue = map.get(TryCatchExtension.RETURNVALUE)
        if (pathName.isEmpty() || methodName.isEmpty() || exceptionName.isEmpty()) {
            return
        }
        //println("path:  " + pathName + "  method: " + methodName + "  exceptionName" + exceptionName)

        modify(dir, pathName, methodName, exceptionName, returnValue)
    }

    static void modify(File dir, String pathName, String methodName, String exceptionName, String returnValue) {
        if (dir.isDirectory()) {

            //遍历文件夹
            dir.eachFileRecurse { File file ->

                String filePath = file.absolutePath
                //println("filePath = " + filePath)
                String pac

                String packageClassPath = filePath.replace('/', '.')
                //search the pathName
                //println("packageClassPath:           " + packageClassPath)
                if (packageClassPath.contains(pathName)) {

                    //CtClass ctClass = pool.getCtClass("com.jackie.javassistdemo.MainActivity")
                    CtClass ctClass = pool.getCtClass(pathName)

                    println("ctClass = " + ctClass)

                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }
                    CtMethod ctMethod = ctClass.getDeclaredMethod(methodName)
                    println("方法名 = " + ctMethod)
//                    String insetBeforeStr = """ android.widget.Toast.makeText(this,"WTF emmmmmmm.....我是被插入的Toast代码~!!",android.widget.Toast.LENGTH_LONG).show();
//                                                """
                    //在方法开头插入代码

//                    ctMethod.insertBefore(insetBeforeStr)
                    StringBuilder sb = new StringBuilder()
                    //方法有返回值
                    if (!returnValue.isEmpty()) {
                        if (!ctMethod.getReturnType().getName().contains("void")) {


                            //sb.append("return ")
                            String result = ctMethod.getReturnType().getName()
                            def resultType
                            println("===========jackie=====type: " + result)
                            switch (result) {
                                case "int":
                                    sb.append(Integer.valueOf(returnValue))
                                    break
                                case "long":
                                    println("========进入了groovy 10000000L================long:type: " + returnValue)
                                    //sb.append("$returnValue")
                                    break
                                case "String":
                                    sb.append(String.valueOf(returnValue))
                                    break
                                case "float":
                                    println("========进入================float:type: " + returnValue)
                                    sb.append(Float.valueOf(returnValue))
                                    break
                                case "double":
                                    println("========进入================double:type: " + returnValue)
                                    sb.append(returnValue)
                                    break
                                case "char":
                                    sb.append(Character.valueOf(returnValue))
                                    break
                                case "short":
                                    sb.append(returnValue)
                                    break
                                case "byte":
                                    println("========进入================byte:type: " + returnValue)
                                    //sb.append(Byte.valueOf(returnValue))
                                    break
                                default:

                                    //sb.append(null)
                                    println("========进入====default:type: ")
                                    break
                            }
                        }
//                    }
                        println("==============添加try========catch========")
                        //在try-catch
                        CtClass etype = pool.get(exceptionName)
                        ctMethod.addCatch('{ System.out.println($e); return ' + returnValue + ';}', etype)


                        ctClass.writeFile(path)
                        ctClass.detach() //释放
                    }
                }
            }
        }
    }

    static void modifyJar(CtClass ctClass, String pathName, String methodName, String exceptionName, String returnValue) {

                    println("ctClass = " + ctClass)

                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }
                    CtMethod ctMethod = ctClass.getDeclaredMethod(methodName)
                    //方法有返回值
                    if (!returnValue.isEmpty()) {
                        if (!ctMethod.getReturnType().getName().contains("void")) {

                        }
                        println("==============添加try========catch========")
                        //在try-catch
                        CtClass etype = pool.get(exceptionName)
                        ctMethod.addCatch('{ System.out.println($e); return 9' + ';}', etype)

                        ctClass.detach() //释放
                    }
                }

    static void injectJar(File inputFile, File outFile,Map<String, Object> map) throws IOException {
        ArrayList entries = new ArrayList()
        Files.createParentDirs(outFile)
        FileInputStream fis = null
        ZipInputStream zis = null
        FileOutputStream fos = null
        ZipOutputStream zos = null
        try {
            fis = new FileInputStream(inputFile)
            zis = new ZipInputStream(fis)
            fos = new FileOutputStream(outFile)
            zos = new ZipOutputStream(fos)
            ZipEntry entry = zis.getNextEntry()
            while (entry != null) {
                String fileName = entry.getName()
                if (!entries.contains(fileName)) {
                    entries.add(fileName)
                    zos.putNextEntry(new ZipEntry(fileName))
                    if (!entry.isDirectory() && fileName.endsWith(".class")
                            && !fileName.contains('R$')
                            && !fileName.contains('R.class')
                            && !fileName.contains("BuildConfig.class"))
                        transform(zis, zos,map)
                    else {
                        ByteStreams.copy(zis, zos)
                    }
                }
                entry = zis.getNextEntry()
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (zos != null)
                zos.close()
            if (fos != null)
                fos.close()
            if (zis != null)
                zis.close()
            if (fis != null)
                fis.close()
        }
    }

    static void transform(InputStream input, OutputStream out,Map<String, Object> map) {
        try {
            CtClass c = pool.makeClass(input)
            transformClass(c, map)
            out.write(c.toBytecode())
            c.detach()
        } catch (Exception e) {
            e.printStackTrace()
            input.close()
            out.close()
            throw new RuntimeException(e.getMessage())
        }
    }

    private static void transformClass(CtClass c, Map<String, Object> map) {
        String pathName = map.get(TryCatchExtension.PATHNAME)
        String methodName = map.get(TryCatchExtension.METHOD_NAME)
        String exceptionName = map.get(TryCatchExtension.EXCEPTION_NAME)
        String returnValue = map.get(TryCatchExtension.RETURNVALUE)
        if (pathName.isEmpty() || methodName.isEmpty() || exceptionName.isEmpty()) {
            return
        }
        println("path:  " + pathName + "  method: " + methodName + "  exceptionName" + exceptionName)
        println("=========="+c.getName())
        if (c.getName().contains("com.jackie.testlib.MyClass")) {
            modifyJar(c, pathName, methodName, exceptionName, returnValue)

        }
    }
}