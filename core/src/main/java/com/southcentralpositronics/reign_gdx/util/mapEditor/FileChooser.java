package com.southcentralpositronics.reign_gdx.util.mapEditor;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * Shows Swing open and save dialogs for one file extension.
 */
public final class FileChooser {

    private FileChooser() { /* static only */ }

    /**
     * Shows an open dialog for one extension, such as {@code png}. Returns null if the user cancels.
     */
    public static FileHandle desktopOpen(String extension) {
        return choose(false, extension);
    }

    /**
     * Shows a save dialog for one extension, such as {@code png}, and adds the extension if it's missing. Returns null if the user cancels.
     */
    public static FileHandle desktopSave(String extension) {
        return choose(true, extension);
    }

    private static FileHandle choose(boolean save, String ext) {
        if (Gdx.app.getType() != Application.ApplicationType.Desktop) {
            Gdx.app.log("FileChooser", "Dialog not available on this platform");
            return null;
        }

        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("*." + ext, ext));

        // Swing must run on the event dispatch thread; wait until the dialog closes.
        final int[] result = new int[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                result[0] = save ? fc.showSaveDialog(null) : fc.showOpenDialog(null);
            });
        } catch (Exception e) {
            Gdx.app.error("FileChooser", "Couldn't show the file dialog", e);
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
