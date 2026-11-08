package com.southcentralpositronics.reign_gdx.util.mapEditor;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * Very thin wrapper around Swing JFileChooser.
 */
public final class FileChooser {

    private FileChooser() { /* static only */ }

    /**
     * Show an *open* dialog filtered to a single extension (e.g. "png").
     */
    public static FileHandle desktopOpen(String extension) {
        return choose(false, extension);
    }

    /**
     * Show a *save* dialog filtered to a single extension (e.g. "png").
     */
    public static FileHandle desktopSave(String extension) {
        return choose(true, extension);
    }

    // ---------------------------------------------------------------------

    private static FileHandle choose(boolean save, String ext) {
        if (Gdx.app.getType() != Application.ApplicationType.Desktop) {
            Gdx.app.log("FileChooser", "Dialog not available on this platform");
            return null;
        }

        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("*." + ext, ext));

        /* Swing must run on the EDT – block until the user closes the dialog. */
        final int[] result = new int[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                result[0] = save ? fc.showSaveDialog(null) : fc.showOpenDialog(null);
            });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (result[0] != JFileChooser.APPROVE_OPTION) return null;

        File file = fc.getSelectedFile();

        // Ensure the chosen filename has the right extension when saving
        if (save && !file.getName().toLowerCase().endsWith("." + ext)) {
            file = new File(file.getParentFile(), file.getName() + "." + ext);
        }

        return Gdx.files.absolute(file.getAbsolutePath());
    }
}
