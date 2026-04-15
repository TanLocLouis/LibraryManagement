package org.example.util;

import java.util.ArrayList;
import java.util.List;

public final class CsvUtil {
    private CsvUtil() {
    }

    public static List<String> parseRow(String line) {
        List<String> fields = new ArrayList<>();
        if (line == null) {
            return fields;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }

            if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(c);
        }

        fields.add(current.toString());
        return fields;
    }

    public static String formatRow(List<String> fields) {
        List<String> safeFields = fields == null ? List.of() : fields;
        StringBuilder row = new StringBuilder();

        for (int i = 0; i < safeFields.size(); i++) {
            if (i > 0) {
                row.append(',');
            }
            row.append(escapeField(safeFields.get(i)));
        }

        return row.toString();
    }

    private static String escapeField(String field) {
        String value = field == null ? "" : field;
        boolean needsQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (!needsQuote) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}

