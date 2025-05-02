package com.secure.messenger.android.data.local.converter;

import androidx.room.TypeConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Конвертер для перетворення LocalDateTime у формат, який підтримується Room
 */
public class DateConverter {

    /**
     * Перетворює LocalDateTime у Long для збереження в базі даних
     *
     * @param dateTime дата та час
     * @return мілісекунди від епохи або null
     */
    @TypeConverter
    public static Long fromLocalDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Перетворює Long у LocalDateTime для отримання з бази даних
     *
     * @param value мілісекунди від епохи
     * @return об'єкт LocalDateTime або null
     */
    @TypeConverter
    public static LocalDateTime toLocalDateTime(Long value) {
        return value == null ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }
}