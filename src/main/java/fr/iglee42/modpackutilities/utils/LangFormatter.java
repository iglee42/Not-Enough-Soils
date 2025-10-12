package fr.iglee42.modpackutilities.utils;

import java.util.Map;

public class LangFormatter {


    public static String format(String expr, Map<String, Object> ctx) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < expr.length()) {
            if (expr.startsWith("{{", i)) {
                String sub = expr.substring(i);
                int end = findClosing(sub);
                String inside = sub.substring(2, end - 1);
                out.append(eval(inside, ctx));
                i += end + 1;
            } else {
                out.append(expr.charAt(i));
                i++;
            }
        }
        return out.toString();
    }

    private static int findClosing(String s) {
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.startsWith("{{", i)){
                depth++;
                i++;
            }
            if (s.startsWith("}}", i)){
                depth--;
                i++;
            }
            if (depth == 0) return i;
        }
        throw new IllegalArgumentException("Unclosed {{...}}");
    }

    private static String eval(String token, Map<String, Object> ctx) {
        // Conditionnel
        if (token.startsWith("?")) {
            return handleCondition(token.substring(1), ctx);
        }
        // Variable
        return String.valueOf(ctx.getOrDefault(token.replaceAll("\\s+",""), ""));
    }

    private static String handleCondition(String expr, Map<String, Object> ctx) {
        int colon = expr.indexOf(":");
        if (colon == -1) return "";

        String condition = expr.substring(0, colon).replaceAll("\\s+","");
        String rest = expr.substring(colon + 1);

        String textIfTrue;
        String textIfFalse = "";
        int pipe = rest.indexOf("|");
        if (pipe >= 0) {
            textIfTrue = rest.substring(0, pipe);
            textIfFalse = rest.substring(pipe + 1);
        } else {
            textIfTrue = rest;
        }

        boolean ok = evalCondition(condition, ctx);
        return format(ok ? textIfTrue : textIfFalse, ctx);
    }

    private static boolean evalCondition(String cond, Map<String, Object> ctx) {
        if (cond.contains(">=")) {
            String[] p = cond.split(">=");
            int left = getInt(ctx, p[0].trim());
            int right = Integer.parseInt(p[1].trim());
            return left >= right;
        }
        if (cond.contains("<=")) {
            String[] p = cond.split("<=");
            int left = getInt(ctx, p[0].trim());
            int right = Integer.parseInt(p[1].trim());
            return left <= right;
        }
        if (cond.contains(">")) {
            String[] p = cond.split(">");
            int left = getInt(ctx, p[0].trim());
            int right = Integer.parseInt(p[1].trim());
            return left > right;
        }
        if (cond.contains("<")) {
            String[] p = cond.split("<");
            int left = getInt(ctx, p[0].trim());
            int right = Integer.parseInt(p[1].trim());
            return left < right;
        }
        if (cond.contains("=")) {
            String[] p = cond.split("=");
            int left = getInt(ctx, p[0].trim());
            int right = Integer.parseInt(p[1].trim());
            return left == right;
        }
        return false;
    }

    private static int getInt(Map<String, Object> ctx, String key) {
        return Integer.parseInt(String.valueOf(ctx.getOrDefault(key, "0")));
    }

}
