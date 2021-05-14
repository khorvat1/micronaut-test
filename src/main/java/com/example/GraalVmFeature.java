package com.example;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.jni.JNIRuntimeAccess;
import com.oracle.svm.hosted.FeatureImpl;
import com.oracle.svm.hosted.classinitialization.ClassInitializationSupport;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.impl.RuntimeReflectionSupport;
import sun.awt.PlatformFont;
import sun.awt.image.*;
import sun.java2d.Disposer;
import sun.java2d.cmm.lcms.LCMSTransform;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicLookAndFeel;
import java.awt.*;
import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ProfileRGB;
import java.awt.image.*;
import java.util.HashSet;
import java.util.Set;

@AutomaticFeature
@SuppressWarnings({"squid:S1192", "squid:S00112", "unused"})
public class GraalVmFeature implements Feature {

    private static final String AWT_SUPPORT = "AWT Support";
    private static final String AWT_SUPPORT_SUPERCLASS = "AWT Support needs superclass to initialize at runtime";
    private Set<Class<?>> runtimeClasses = new HashSet<>();
    private Set<String> runtimeClassesSimple = new HashSet<>();
    private Set<Class<?>> reflectionClasses = new HashSet<>();

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        try {
            FeatureImpl.BeforeAnalysisAccessImpl a = (FeatureImpl.BeforeAnalysisAccessImpl) access;
            ClassInitializationSupport classInitializationSupport = a.getHostVM().getClassInitializationSupport();

            registerTransform(classInitializationSupport);
            classInitializationSupport.initializeAtRunTime(ImageIO.class, AWT_SUPPORT);

            initAtRuntimeSimple(a, Toolkit.class);

            registerDisposer(classInitializationSupport);
            registerBufferedImage(a);
            registerRaster(a);
            registerColorModel(a);
            registerSampleModel(a);

            registerFont(a);

            registerProfiles(a);

            registerChildClasses(a);

            a.getHostVM().registerClassReachabilityListener((duringAnalysisAccess, c) -> {
                FeatureImpl.DuringAnalysisAccessImpl a2 = (FeatureImpl.DuringAnalysisAccessImpl) duringAnalysisAccess;

                Class<?> superClass = c.getSuperclass();
                boolean isFullChild = false;
                boolean isSimpleChild = false;
                boolean isReflection = false;
                while (superClass != null) {
                    if (runtimeClasses.contains(superClass)) {
                        isFullChild = true;
                        break;
                    }
                    if (runtimeClassesSimple.contains(superClass.getName())) {
                        isSimpleChild = true;
                        break;
                    }
                    superClass = superClass.getSuperclass();
                }
                superClass = c;
                while (superClass != null) {
                    if (reflectionClasses.contains(superClass)) {
                        isReflection = true;
                        break;
                    }
                    superClass = superClass.getSuperclass();
                }
                if (isFullChild) {
                    superClass = c;
                    while (superClass != null) {
                        if (runtimeClasses.contains(superClass)) {
                            break;
                        }
                        System.out.println("initAtRuntime(access, \"" + superClass.getName() + "\");");
                        runtimeClasses.add(superClass);
                        superClass = superClass.getSuperclass();
                    }
                }
                if (isSimpleChild) {
                    superClass = c;
                    while (superClass != null) {
                        if (runtimeClassesSimple.contains(superClass.getName())) {
                            break;
                        }
                        System.out.println("initAtRuntimeSimple(access, \"" + superClass.getName() + "\");");
                        runtimeClassesSimple.add(superClass.getName());
                        superClass = superClass.getSuperclass();
                    }
                }
                if (isReflection) {
                    superClass = c;
                    while (superClass != null) {
                        if (reflectionClasses.contains(superClass)) {
                            break;
                        }
                        System.out.println("registerReflection(\"" + superClass.getName() + "\");");
                        reflectionClasses.add(superClass);
                        superClass = superClass.getSuperclass();
                    }
                }
            });


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void registerTransform(ClassInitializationSupport classInitializationSupport) throws NoSuchFieldException, ClassNotFoundException {
        classInitializationSupport.initializeAtRunTime(LCMSTransform.class, AWT_SUPPORT);
        JNIRuntimeAccess.register(LCMSTransform.class);
        JNIRuntimeAccess.register(LCMSTransform.class.getDeclaredField("renderType"));
        JNIRuntimeAccess.register(LCMSTransform.class.getDeclaredField("ID"));

        JNIRuntimeAccess.register(Class.forName("sun.java2d.cmm.lcms.LCMSImageLayout"));
        JNIRuntimeAccess.register(Class.forName("sun.java2d.cmm.lcms.LCMSImageLayout").getDeclaredField("isIntPacked"));
        JNIRuntimeAccess.register(Class.forName("sun.java2d.cmm.lcms.LCMSImageLayout").getDeclaredField("dataType"));
        JNIRuntimeAccess.register(Class.forName("sun.java2d.cmm.lcms.LCMSImageLayout").getDeclaredField("pixelType"));
        JNIRuntimeAccess.register(Class.forName("sun.java2d.cmm.lcms.LCMSImageLayout").getDeclaredField("width"));
        JNIRuntimeAccess.register(Class.forName("sun.java2d.cmm.lcms.LCMSImageLayout").getDeclaredField("height"));
        JNIRuntimeAccess.register(Class.forName("sun.java2d.cmm.lcms.LCMSImageLayout").getDeclaredField("nextRowOffset"));
        JNIRuntimeAccess.register(Class.forName("sun.java2d.cmm.lcms.LCMSImageLayout").getDeclaredField("offset"));
        JNIRuntimeAccess.register(Class.forName("sun.java2d.cmm.lcms.LCMSImageLayout").getDeclaredField("dataArray"));
        JNIRuntimeAccess.register(Class.forName("sun.java2d.cmm.lcms.LCMSImageLayout").getDeclaredField("imageAtOnce"));
        JNIRuntimeAccess.register(Class.forName("sun.java2d.cmm.lcms.LCMSImageLayout").getDeclaredField("nextPixelOffset"));
    }


    private void registerProfiles(FeatureImpl.BeforeAnalysisAccessImpl a) throws NoSuchFieldException, ClassNotFoundException {
        initAtRuntime(a, ICC_ProfileRGB.class);

        initAtRuntime(a, ICC_Profile.class);
        JNIRuntimeAccess.register(ICC_Profile.class.getDeclaredField("cmmProfile"));
        JNIRuntimeAccess.register(ICC_Profile.class.getDeclaredField("deferralInfo"));
        JNIRuntimeAccess.register(ICC_Profile.class.getDeclaredField("profileActivator"));

        JNIRuntimeAccess.register(Class.forName("sun.java2d.cmm.lcms.LCMSProfile"));
    }

    private void registerImage(FeatureImpl.BeforeAnalysisAccessImpl a) throws NoSuchFieldException {
        initAtRuntime(a, ComponentSampleModel.class);
        JNIRuntimeAccess.register(ComponentSampleModel.class.getDeclaredField("pixelStride"));
        JNIRuntimeAccess.register(ComponentSampleModel.class.getDeclaredField("scanlineStride"));
        JNIRuntimeAccess.register(ComponentSampleModel.class.getDeclaredField("bandOffsets"));
    }

    private void registerBufImgSurfaceData(ClassInitializationSupport classInitializationSupport) throws NoSuchMethodException, NoSuchFieldException {
        classInitializationSupport.initializeAtRunTime(BufImgSurfaceData.ICMColorData.class, AWT_SUPPORT);
        JNIRuntimeAccess.register(BufImgSurfaceData.ICMColorData.class);
        JNIRuntimeAccess.register(BufImgSurfaceData.ICMColorData.class.getDeclaredConstructor(long.class));
        JNIRuntimeAccess.register(BufImgSurfaceData.ICMColorData.class.getDeclaredField("pData"));
    }

    private void registerPoint(ClassInitializationSupport classInitializationSupport) throws NoSuchFieldException {
        classInitializationSupport.initializeAtRunTime(Point.class, AWT_SUPPORT);
        JNIRuntimeAccess.register(Point.class);
        JNIRuntimeAccess.register(Point.class.getDeclaredField("x"));
        JNIRuntimeAccess.register(Point.class.getDeclaredField("y"));
    }

    private void registerPlatformFont(FeatureImpl.BeforeAnalysisAccessImpl access) throws NoSuchFieldException, NoSuchMethodException {
        initAtRuntime(access, PlatformFont.class);
        JNIRuntimeAccess.register(PlatformFont.class.getDeclaredField("componentFonts"));
        JNIRuntimeAccess.register(PlatformFont.class.getDeclaredField("fontConfig"));
        JNIRuntimeAccess.register(PlatformFont.class.getDeclaredMethod("makeConvertedMultiFontString", String.class));
        JNIRuntimeAccess.register(PlatformFont.class.getDeclaredMethod("makeConvertedMultiFontChars", char[].class, int.class, int.class));
    }

    private void registerFont(FeatureImpl.BeforeAnalysisAccessImpl access) throws NoSuchFieldException, NoSuchMethodException {
        initAtRuntime(access, Font.class);
        JNIRuntimeAccess.register(Font.class.getDeclaredField("pData"));
        JNIRuntimeAccess.register(Font.class.getDeclaredField("style"));
        JNIRuntimeAccess.register(Font.class.getDeclaredField("size"));
    }


    private void registerSampleModel(FeatureImpl.BeforeAnalysisAccessImpl access) throws NoSuchFieldException, NoSuchMethodException {
        initAtRuntime(access, SampleModel.class);
        JNIRuntimeAccess.register(SampleModel.class.getDeclaredField("width"));
        JNIRuntimeAccess.register(SampleModel.class.getDeclaredField("height"));
        JNIRuntimeAccess.register(SampleModel.class.getDeclaredMethod("getPixels", int.class, int.class, int.class, int.class, int[].class, DataBuffer.class));
        JNIRuntimeAccess.register(SampleModel.class.getDeclaredMethod("setPixels", int.class, int.class, int.class, int.class, int[].class, DataBuffer.class));

        JNIRuntimeAccess.register(SinglePixelPackedSampleModel.class.getDeclaredField("bitMasks"));
        JNIRuntimeAccess.register(SinglePixelPackedSampleModel.class.getDeclaredField("bitOffsets"));
        JNIRuntimeAccess.register(SinglePixelPackedSampleModel.class.getDeclaredField("bitSizes"));
        JNIRuntimeAccess.register(SinglePixelPackedSampleModel.class.getDeclaredField("maxBitSize"));
    }

    private void registerColorModel(FeatureImpl.BeforeAnalysisAccessImpl access) throws NoSuchFieldException, NoSuchMethodException {
        initAtRuntime(access, ColorModel.class);
        JNIRuntimeAccess.register(ColorModel.class.getDeclaredField("pData"));
        JNIRuntimeAccess.register(ColorModel.class.getDeclaredField("nBits"));
        JNIRuntimeAccess.register(ColorModel.class.getDeclaredField("colorSpace"));
        JNIRuntimeAccess.register(ColorModel.class.getDeclaredField("numComponents"));
        JNIRuntimeAccess.register(ColorModel.class.getDeclaredField("supportsAlpha"));
        JNIRuntimeAccess.register(ColorModel.class.getDeclaredField("isAlphaPremultiplied"));
        JNIRuntimeAccess.register(ColorModel.class.getDeclaredField("transparency"));
        JNIRuntimeAccess.register(ColorModel.class.getDeclaredField("colorSpaceType"));
        JNIRuntimeAccess.register(ColorModel.class.getDeclaredField("is_sRGB"));
        JNIRuntimeAccess.register(ColorModel.class.getDeclaredMethod("getRGB", Object.class));
        JNIRuntimeAccess.register(ColorModel.class.getDeclaredMethod("getRGBdefault"));

        JNIRuntimeAccess.register(IndexColorModel.class.getDeclaredField("rgb"));
        JNIRuntimeAccess.register(IndexColorModel.class.getDeclaredField("allgrayopaque"));
        JNIRuntimeAccess.register(IndexColorModel.class.getDeclaredField("map_size"));
        JNIRuntimeAccess.register(IndexColorModel.class.getDeclaredField("colorData"));
        JNIRuntimeAccess.register(IndexColorModel.class.getDeclaredField("transparent_index"));
    }

    private void registerRaster(FeatureImpl.BeforeAnalysisAccessImpl access) throws NoSuchMethodException, NoSuchFieldException {
        initAtRuntime(access, Raster.class);
        JNIRuntimeAccess.register(Raster.class.getDeclaredMethod("getDataElements", int.class, int.class, int.class, int.class, Object.class));
        JNIRuntimeAccess.register(Raster.class.getDeclaredField("width"));
        JNIRuntimeAccess.register(Raster.class.getDeclaredField("height"));
        JNIRuntimeAccess.register(Raster.class.getDeclaredField("numBands"));
        JNIRuntimeAccess.register(Raster.class.getDeclaredField("minX"));
        JNIRuntimeAccess.register(Raster.class.getDeclaredField("minY"));
        JNIRuntimeAccess.register(Raster.class.getDeclaredField("sampleModelTranslateX"));
        JNIRuntimeAccess.register(Raster.class.getDeclaredField("sampleModelTranslateY"));
        JNIRuntimeAccess.register(Raster.class.getDeclaredField("sampleModel"));
        JNIRuntimeAccess.register(Raster.class.getDeclaredField("numDataElements"));
        JNIRuntimeAccess.register(Raster.class.getDeclaredField("numBands"));
        JNIRuntimeAccess.register(Raster.class.getDeclaredField("dataBuffer"));

        JNIRuntimeAccess.register(ByteComponentRaster.class.getDeclaredField("data"));
        JNIRuntimeAccess.register(ByteComponentRaster.class.getDeclaredField("scanlineStride"));
        JNIRuntimeAccess.register(ByteComponentRaster.class.getDeclaredField("pixelStride"));
        JNIRuntimeAccess.register(ByteComponentRaster.class.getDeclaredField("bandOffset"));
        JNIRuntimeAccess.register(ByteComponentRaster.class.getDeclaredField("dataOffsets"));
        JNIRuntimeAccess.register(ByteComponentRaster.class.getDeclaredField("type"));

        JNIRuntimeAccess.register(BytePackedRaster.class.getDeclaredField("data"));
        JNIRuntimeAccess.register(BytePackedRaster.class.getDeclaredField("scanlineStride"));
        JNIRuntimeAccess.register(BytePackedRaster.class.getDeclaredField("pixelBitStride"));
        JNIRuntimeAccess.register(BytePackedRaster.class.getDeclaredField("type"));
        JNIRuntimeAccess.register(BytePackedRaster.class.getDeclaredField("dataBitOffset"));

        JNIRuntimeAccess.register(ShortComponentRaster.class.getDeclaredField("data"));
        JNIRuntimeAccess.register(ShortComponentRaster.class.getDeclaredField("scanlineStride"));
        JNIRuntimeAccess.register(ShortComponentRaster.class.getDeclaredField("pixelStride"));
        JNIRuntimeAccess.register(ShortComponentRaster.class.getDeclaredField("bandOffset"));
        JNIRuntimeAccess.register(ShortComponentRaster.class.getDeclaredField("dataOffsets"));
        JNIRuntimeAccess.register(ShortComponentRaster.class.getDeclaredField("type"));

        JNIRuntimeAccess.register(IntegerComponentRaster.class.getDeclaredField("data"));
        JNIRuntimeAccess.register(IntegerComponentRaster.class.getDeclaredField("scanlineStride"));
        JNIRuntimeAccess.register(IntegerComponentRaster.class.getDeclaredField("pixelStride"));
        JNIRuntimeAccess.register(IntegerComponentRaster.class.getDeclaredField("dataOffsets"));
        JNIRuntimeAccess.register(IntegerComponentRaster.class.getDeclaredField("bandOffset"));
        JNIRuntimeAccess.register(IntegerComponentRaster.class.getDeclaredField("type"));
        JNIRuntimeAccess.register(IntegerComponentRaster.class.getDeclaredMethod("setDataElements", int.class, int.class, int.class, int.class, Object.class));
    }

    private void registerBufferedImage(FeatureImpl.BeforeAnalysisAccessImpl access) throws NoSuchMethodException, NoSuchFieldException {
        initAtRuntime(access, BufferedImage.class);
        JNIRuntimeAccess.register(BufferedImage.class.getDeclaredMethod("getRGB", int.class, int.class, int.class, int.class, int[].class, int.class, int.class));
        JNIRuntimeAccess.register(BufferedImage.class.getDeclaredMethod("setRGB", int.class, int.class, int.class, int.class, int[].class, int.class, int.class));
        JNIRuntimeAccess.register(BufferedImage.class.getDeclaredField("raster"));
        JNIRuntimeAccess.register(BufferedImage.class.getDeclaredField("imageType"));
        JNIRuntimeAccess.register(BufferedImage.class.getDeclaredField("colorModel"));
    }

    private void registerDisposer(ClassInitializationSupport classInitializationSupport) throws NoSuchMethodException {
        classInitializationSupport.initializeAtRunTime(Disposer.class, AWT_SUPPORT);
        JNIRuntimeAccess.register(Disposer.class);
        JNIRuntimeAccess.register(Disposer.class.getDeclaredMethod("addRecord", Object.class, long.class, long.class));
    }

    private Class<?> initAtRuntime(FeatureImpl.BeforeAnalysisAccessImpl access, Class<?> clazz) {
        ClassInitializationSupport classInitializationSupport = access.getHostVM().getClassInitializationSupport();
        classInitializationSupport.initializeAtRunTime(clazz, AWT_SUPPORT);
        JNIRuntimeAccess.register(clazz);
        runtimeClasses.add(clazz);
        return clazz;
    }

    private Class<?> initAtRuntime(FeatureImpl.BeforeAnalysisAccessImpl access, String className) throws ClassNotFoundException {
        Class<?> clazz = getClass(className);
        return initAtRuntime(access, clazz);
    }

    private Class<?> getClass(String className) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }

    private void initAtRuntimeSimple(FeatureImpl.BeforeAnalysisAccessImpl access, Class<?> clazz) {
        ClassInitializationSupport classInitializationSupport = access.getHostVM().getClassInitializationSupport();
        classInitializationSupport.initializeAtRunTime(clazz, AWT_SUPPORT);
        runtimeClassesSimple.add(clazz.getName());
    }

    private void initAtRuntimeSimple(FeatureImpl.BeforeAnalysisAccessImpl access, String className) {
        ClassInitializationSupport classInitializationSupport = access.getHostVM().getClassInitializationSupport();
        classInitializationSupport.initializeAtRunTime(className, AWT_SUPPORT);
        runtimeClassesSimple.add(className);
    }

    private boolean registerReflection(String className) {
        RuntimeReflectionSupport reflectionSupport = ImageSingletons.lookup(RuntimeReflectionSupport.class);
        try {
            Class<?> clazz = getClass(className);
            reflectionClasses.add(clazz);
            reflectionSupport.register(clazz);
            reflectionSupport.register(clazz.getDeclaredConstructors());
            reflectionSupport.register(clazz.getMethods());
            reflectionSupport.register(true, false, clazz.getFields());
            return true;
        } catch (ClassNotFoundException e) {
            // Ooops... It's not a class!
            return false;
        }
    }

    private void registerChildClasses(FeatureImpl.BeforeAnalysisAccessImpl access) throws ClassNotFoundException {
        initAtRuntime(access, "java.awt.image.PixelInterleavedSampleModel");
        initAtRuntimeSimple(access, "sun.java2d.HeadlessGraphicsEnvironment");
        initAtRuntimeSimple(access, "sun.java2d.SunGraphicsEnvironment");
        initAtRuntimeSimple(access, "sun.awt.X11GraphicsEnvironment");
    }
}
