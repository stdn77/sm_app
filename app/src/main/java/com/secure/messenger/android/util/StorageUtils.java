package com.secure.messenger.android.util;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Утиліти для роботи зі сховищем
 */
public class StorageUtils {

    /**
     * Отримує розмір директорії (включно з піддиректоріями)
     *
     * @param dir директорія
     * @return розмір в байтах
     */
    public static long getDirSize(File dir) {
        if (dir == null || !dir.exists()) {
            return 0;
        }

        long size = 0;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += getDirSize(file);
                    }
                }
            }
        } else {
            size = dir.length();
        }
        return size;
    }

    /**
     * Видаляє директорію та всі її піддиректорії
     *
     * @param dir директорія
     * @return true, якщо вдалося видалити
     */
    public static boolean deleteDir(File dir) {
        if (dir == null || !dir.exists()) {
            return true;
        }

        boolean success = true;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    success &= deleteDir(file);
                }
            }
        }

        return success && dir.delete();
    }

    /**
     * Форматує розмір у людиночитабельний формат
     *
     * @param size розмір в байтах
     * @return форматований рядок
     */
    public static String formatSize(long size) {
        if (size <= 0) {
            return "0 B";
        }

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}