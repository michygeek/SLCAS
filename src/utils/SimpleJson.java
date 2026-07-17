package utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class SimpleJson {

    private SimpleJson() { }


    public static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static String write(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(value, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(Object value, StringBuilder sb) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            sb.append('"').append(escape((String) value)).append('"');
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value.toString());
        } else if (value instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> e : ((Map<String, Object>) value).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escape(e.getKey())).append("\":");
                writeValue(e.getValue(), sb);
            }
            sb.append('}');
        } else if (value instanceof List) {
            sb.append('[');
            boolean first = true;
            for (Object o : (List<Object>) value) {
                if (!first) sb.append(',');
                first = false;
                writeValue(o, sb);
            }
            sb.append(']');
        } else {
            // fallback: treat as string
            sb.append('"').append(escape(value.toString())).append('"');
        }
    }


    public static Object parse(String text) {
        Parser p = new Parser(text);
        p.skipWhitespace();
        Object result = p.parseValue();
        return result;
    }

    private static class Parser {
        private final String s;
        private int pos = 0;

        Parser(String s) { this.s = s; }

        void skipWhitespace() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        Object parseValue() {
            skipWhitespace();
            if (pos >= s.length()) return null;
            char c = s.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (s.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
            if (s.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
            if (s.startsWith("null", pos)) { pos += 4; return null; }
            return parseNumber();
        }

        Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // consume {
            skipWhitespace();
            if (pos < s.length() && s.charAt(pos) == '}') { pos++; return map; }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                pos++; // consume :
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (pos < s.length() && s.charAt(pos) == ',') { pos++; continue; }
                break;
            }
            skipWhitespace();
            if (pos < s.length() && s.charAt(pos) == '}') pos++;
            return map;
        }

        List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++; // consume [
            skipWhitespace();
            if (pos < s.length() && s.charAt(pos) == ']') { pos++; return list; }
            while (true) {
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                if (pos < s.length() && s.charAt(pos) == ',') { pos++; continue; }
                break;
            }
            skipWhitespace();
            if (pos < s.length() && s.charAt(pos) == ']') pos++;
            return list;
        }

        String parseString() {
            StringBuilder sb = new StringBuilder();
            pos++; // consume opening quote
            while (pos < s.length() && s.charAt(pos) != '"') {
                char c = s.charAt(pos);
                if (c == '\\' && pos + 1 < s.length()) {
                    char next = s.charAt(pos + 1);
                    switch (next) {
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        default: sb.append(next);
                    }
                    pos += 2;
                } else {
                    sb.append(c);
                    pos++;
                }
            }
            pos++; // consume closing quote
            return sb.toString();
        }

        Double parseNumber() {
            int start = pos;
            while (pos < s.length() && "-+0123456789.eE".indexOf(s.charAt(pos)) >= 0) pos++;
            String num = s.substring(start, pos);
            if (num.isEmpty()) { pos++; return 0.0; } // guard against malformed input
            return Double.parseDouble(num);
        }
    }
}
