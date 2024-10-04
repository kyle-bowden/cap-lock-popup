package co.uk.bittwisted.util;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Locale;

public class Helpers {
    public static String formatWithOneDecimalPlace(float f) {
        return String.format(Locale.US, "%.1f", f);
    }
    public static String getClipboardContentAsString() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
        }
        return "";
    }

    public static String convertToLowerCaseWithCorrectPunctuation(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        boolean capitalizeNext = true;
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                c = Character.toLowerCase(c);
                if (capitalizeNext) {
                    c = Character.toUpperCase(c);
                    capitalizeNext = false;
                }
            } else if (c == '.' || c == '?' || c == '!') {
                capitalizeNext = true;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
