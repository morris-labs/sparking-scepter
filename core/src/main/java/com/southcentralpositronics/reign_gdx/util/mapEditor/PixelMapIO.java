package com.southcentralpositronics.reign_gdx.util.mapEditor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

public final class PixelMapIO {
    public static void writePNG(FileHandle out, Pixmap pix) {
        PixmapIO.writePNG(out, pix);
        System.out.println("Saved map → " + out.path());
    }

    /** converts 0xAARRGGBB ➜ 0xRRGGBBAA */
    static int argbToRgba(int argb){
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>>  8) & 0xFF;
        int b =  argb         & 0xFF;
        return (r << 24) | (g << 16) | (b << 8) | a;
    }

    /** converts 0xRRGGBBAA ➜ 0xAARRGGBB */
    static int rgbaToArgb(int rgba) {
        int r = (rgba >>> 24) & 0xFF;
        int g = (rgba >>> 16) & 0xFF;
        int b = (rgba >>>  8) & 0xFF;
        int a =  rgba         & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private PixelMapIO() {
    }
}
