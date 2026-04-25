// package sumith.hinglish;

// import java.io.ByteArrayOutputStream;
// import java.io.PrintStream;
// import java.util.*;
// /*
//  ==========================================
//   Hinglish Programming Language Interpreter
//  ==========================================

//   Keywords:
//    - set x = expr
//    - likh "text" | likh x
//    - lik "text"   (same line)
//    - agar <cond> ... warna ... khatam
//    - jabtak <cond> ... khatam
//    - kaam fname params...
//    - wapas expr
// */

// public class HinglishInterpreter {

//     static Map<String, Integer> vars = new HashMap<>();
//     static Map<String, Integer> builtinArgCount = new HashMap<>();
//     HinglishInterpreter(){
//         builtinArgCount.put("pow", 2);
//         builtinArgCount.put("abs", 1);
//         builtinArgCount.put("max", 2);
//         builtinArgCount.put("min", 2);
//         builtinArgCount.put("sqrt", 1);

//     }
//     static class Function {
//         List<String> params;
//         List<String> body;
//         Function(List<String> p, List<String> b) {
//             params = p;
//             body = b;
//         }
//     }

//     static Map<String, Function> functions = new HashMap<>();

//     /* -------- Return control signal -------- */
//     static class ReturnSignal extends RuntimeException {
//         int value;
//         ReturnSignal(int v) { value = v; }
//     }
//     // 🔥 SPRING KE LIYE ENTRY POINT
//     public static String runProgram(String input) {
//         vars.clear();
//         functions.clear();
//         ByteArrayOutputStream baos = new ByteArrayOutputStream();
//         PrintStream oldOut = System.out;
//         System.setOut(new PrintStream(baos));

//         try {
//             List<String> program = new ArrayList<>();
//             for (String line : input.split("\\R")) {
//                 line = line.trim();
//                 if (!line.isEmpty()) program.add(line);
//             }
//             executeLines(program, 0, program.size());
//         } catch (RuntimeException re) {
//             System.out.println("Galti: " + re.getMessage());
//         }

//         System.setOut(oldOut);
//         return baos.toString();
//     }

//     // ------------------Main logic for interpreter---------

//     static void executeLines(List<String> lines, int start, int end) {
//         for (int i = start; i < end; i++) {
//             String line = lines.get(i).trim();
//             if (line.isEmpty()) continue;

//             if (line.startsWith("agar ")) {
//                 String condition = line.substring(5).trim();
//                 boolean result = evalCondition(condition);

//                 List<String> trueBlock = new ArrayList<>();
//                 List<String> falseBlock = new ArrayList<>();
//                 boolean inElse = false;
//                 int depth = 0;

//                 int j = i + 1;
//                 while (j < end) {
//                     String nxt = lines.get(j).trim();

//                     if (nxt.startsWith("agar ")) {
//                         if (!inElse) trueBlock.add(nxt); else falseBlock.add(nxt);
//                         depth++; j++; continue;
//                     }

//                     if (nxt.equals("warna") && depth == 0) {
//                         inElse = true; j++; continue;
//                     }

//                     if (nxt.equals("khatam")) {
//                         if (depth == 0) { j++; break; }
//                         depth--;
//                     }

//                     if (!inElse) trueBlock.add(nxt);
//                     else falseBlock.add(nxt);
//                     j++;
//                 }

//                 i = j - 1;
//                 if (result) executeLines(trueBlock, 0, trueBlock.size());
//                 else executeLines(falseBlock, 0, falseBlock.size());
//             }

//             else if (line.startsWith("jabtak ")) {
//                 String condition = line.substring(7).trim();
//                 List<String> loopBlock = new ArrayList<>();
//                 int j = i + 1, depth = 0;

//                 while (j < end) {
//                     String nxt = lines.get(j).trim();
//                     if (nxt.startsWith("jabtak ")) depth++;
//                     if (nxt.equals("khatam")) {
//                         if (depth == 0) { j++; break; }
//                         depth--;
//                     }
//                     loopBlock.add(nxt);
//                     j++;
//                 }

//                 i = j - 1;
//                 while (evalCondition(condition)) {
//                     executeLines(loopBlock, 0, loopBlock.size());
//                 }
//             }
//  /* ---------- FUNCTION DEF ---------- */
//             else if (line.startsWith("kaam ")) {
//                 String[] t = line.split("\\s+");
//                 String name = t[1];

//                 List<String> params = new ArrayList<>();
//                 for (int k = 2; k < t.length; k++) params.add(t[k]);

//                 List<String> body = new ArrayList<>();
//                 int depth = 0;
//                 int j = i + 1;

//                 while (j < end) {
//                     String l = lines.get(j);

//                     if (l.startsWith("agar ") || l.startsWith("jabtak "))
//                         depth++;


//                     if (l.equals("khatam")) {
//                         if (depth == 0) break;
//                         depth--;
//                     }

//                     body.add(l);
//                     j++;
//                 }

//                 functions.put(name, new Function(params, body));
//                 i = j;
//             }

//             /* ---------- RETURN ---------- */
//             else if (line.startsWith("wapas ")) {
//                 int v = evalExpression(line.substring(6));
//                 throw new ReturnSignal(v);
//             }
//             else runSingle(line);
//         }
//     }

//     static void runSingle(String line) {
// // -------- FUNCTION CALL --------
//        String[] parts = line.split("\\s+");
//        String fname = parts[0];

//        if (functions.containsKey(fname)) {
//            String[] args = Arrays.copyOfRange(parts, 1, parts.length);
//            callFunction(fname, args);
//            return;
//         }
    

//         if (line.startsWith("set ")) {
//             int eq = line.indexOf('=');
//             String lhs = line.substring(3, eq).trim();
//             String rhs = line.substring(eq + 1).trim();
//             vars.put(lhs, evalExpression(rhs));
//             return;
//         }

//         if (line.startsWith("likh ")) {
//             String r = line.substring(5).trim();
//             //System.out.println(resolve(r));
//             String[] part = r.split("\\s+");
//            if (functions.containsKey(part[0])) {
//                  int val = callFunction(part[0], Arrays.copyOfRange(part, 1, part.length));
//                  System.out.println(val);
//             } else if (builtinArgCount.containsKey(part[0])) {

//                   int n = builtinArgCount.get(part[0]);
//                   int[] args = new int[n];
//                   for (int i = 0; i < n; i++) {
//                   args[i] = getValOrThrow(part[i + 1]);

//                   }
//                   System.out.println(callBuiltin(part[0], args));

//                 } else {
//               System.out.println(resolve(r));
//                }

//             return;
//         }

//         if (line.startsWith("lik ")) {
//             String r = line.substring(4).trim();
//             System.out.print(resolve(r));
//             return;
//         }

//         if (line.startsWith("jod ")) arithmeticMulti(line.substring(4), "jod");
//         else if (line.startsWith("ghata ")) arithmeticMulti(line.substring(6), "ghata");
//         else if (line.startsWith("guna ")) arithmeticMulti(line.substring(5), "guna");
//         else if (line.startsWith("bhaag ")) arithmeticMulti(line.substring(6), "bhaag");
//         else throw new RuntimeException("Samajh nahi aaya: " + line);
//     }


//      /* ---------------- Function call ---------------- */
//     static int callFunction(String name, String[] args) {

//     Function f = functions.get(name);

//     // 🔥 NEW LOCAL SCOPE
//     Map<String, Integer> backup = vars;
//     vars = new HashMap<>(backup);

//     for (int i = 0; i < f.params.size(); i++) {
//         vars.put(f.params.get(i), getValOrThrow(args[i]));
//     }

//     try {
//         executeLines(f.body, 0, f.body.size());
//     } catch (ReturnSignal r) {
//         vars = backup;
//         return r.value;
//     }

//     vars = backup;
//     return 0;
// }


//     static String resolve(String r) {
//         if (r.startsWith("\"")) return r.substring(1, r.length() - 1);
//         return String.valueOf(getValOrThrow(r));
//     }

//     static int evalExpression(String expr) {
//         String[] parts = expr.split("\\s+");
        

//         if (builtinArgCount.containsKey(parts[0])) {
//             int n = builtinArgCount.get(parts[0]);
//             if (parts.length != n + 1) {
//                 throw new RuntimeException("Galti: arguments mismatch");
//            }

//             int[] args = new int[n];
//             for (int i = 0; i < n; i++) {
//                 args[i] = getValOrThrow(parts[i + 1]);

//            }

//             return callBuiltin(parts[0], args);
//         }

//           if (functions.containsKey(parts[0])) {
//                   return callFunction(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
//             }

//         expr = expr.replaceAll("([+\\-*/])", " $1 ").replaceAll("\\s+", " ").trim();
//         if (expr.contains(" aur ")) {
//             int sum = 0;
//             for (String p : expr.split("\\s+aur\\s+")) sum += getValOrThrow(p);
//             return sum;
//         }
//         String[] t = expr.split(" ");
//         int acc = getValOrThrow(t[0]);
//         for (int i = 1; i + 1 < t.length; i += 2) {
//             int r = getValOrThrow(t[i+1]);
//             switch (t[i]) {
//                 case "+": acc += r; break;
//                 case "-": acc -= r; break;
//                 case "*": acc *= r; break;
//                 case "/": acc /= r; break;
//             }
//         }
//         return acc;
//     }

//     static void arithmeticMulti(String rest, String w) {
//         String[] p = rest.split("\\s+aur\\s+");
//         int r = getValOrThrow(p[0]);
//         for (int i = 1; i < p.length; i++) {
//             int v = getValOrThrow(p[i]);
//             if (w.equals("jod")) r += v;
//             if (w.equals("ghata")) r -= v;
//             if (w.equals("guna")) r *= v;
//             if (w.equals("bhaag")) r /= v;
//         }
//         System.out.println(r);
//     }

//     static boolean evalCondition(String c) {
//         String[] ops = {">=", "<=", "==", "!=", ">", "<"};
//         for (String o : ops) {
//             int i = c.indexOf(o);
//             if (i >= 0) {
//                 int L = getValOrThrow(c.substring(0, i).trim());
// int R = getValOrThrow(c.substring(i + o.length()).trim());

//                 return switch (o) {
//                     case ">" -> L > R;
//                     case "<" -> L < R;
//                     case "==" -> L == R;
//                     case "!=" -> L != R;
//                     case ">=" -> L >= R;
//                     case "<=" -> L <= R;
//                     default -> false;
//                 };
//             }
//         }
//         throw new RuntimeException("Condition galat: " + c);
//     }

//     static int getValOrThrow(String t) {
//         if (t.matches("-?\\d+")) return Integer.parseInt(t);
//         if (vars.containsKey(t)) return vars.get(t);
//         throw new RuntimeException(t + " set nahi hai");
//     }

//     static int callBuiltin(String name, int[] args) {

//     switch (name) {

//         case "sqrt":
//             return (int) Math.sqrt(args[0]);

//         case "pow":
//             return (int) Math.pow(args[0], args[1]);

//         case "abs":
//             return Math.abs(args[0]);

//         case "max":
//             return Math.max(args[0], args[1]);

//         case "min":
//             return Math.min(args[0], args[1]);

//         default:
//             throw new RuntimeException("Unknown inbuilt function: " + name);
//     }
// }
// }

package sumith.hinglish;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

/*
 ==========================================
    Hinglish Programming Language Interpreter
 ==========================================
 Keywords:
   - x = expr          (assignment — 'set' keyword optional)
   - set x = expr      (also works)
   - likh expr         (println)
   - lik  expr         (print, no newline)
   - lo x              (read user input into x)
   - agar cond ... warna ... khatam
   - jabtak cond ... khatam
   - kaam fname p1 p2 ... body khatam
   - wapas expr

 Expressions:
   - Integers, "strings"
   - Variables
   - + - * / %  (correct precedence)
   - ( ) parentheses
   - Unary minus
   - Builtins: sqrt abs pow max min len
   - String + string  (concat)
   - Comparisons: == != < > <= >=
*/
public class HinglishInterpreter {

    // ── Value type ────────────────────────────────────────────────
    static class HVal {
        enum T { INT, STR }
        final T type; final int i; final String s;
        HVal(int v)    { type = T.INT; i = v; s = null; }
        HVal(String v) { type = T.STR; s = v; i = 0; }
        boolean isInt() { return type == T.INT; }
        boolean isStr() { return type == T.STR; }
        int asInt() {
            if (type == T.INT) return i;
            try { return Integer.parseInt(s.trim()); }
            catch (NumberFormatException e) { throw new HinglishException("'" + s + "' integer nahi hai"); }
        }
        String asStr() { return type == T.STR ? s : String.valueOf(i); }
        @Override public String toString() { return asStr(); }
    }

    // ── Exceptions / Signals ──────────────────────────────────────
    static class HinglishException extends RuntimeException {
        HinglishException(String m) { super(m); }
    }
    static class ReturnSignal extends Error {
        final HVal value;
        ReturnSignal(HVal v) { super(null, null, true, false); value = v; }
    }

    // ── Global state ──────────────────────────────────────────────
    static Map<String, HVal>     vars            = new HashMap<>();
    static Map<String, Function> functions       = new HashMap<>();
    static Map<String, Integer>  builtinArgCount = new HashMap<>();
    static Queue<String>         inputQueue      = new LinkedList<>();

    static {
        builtinArgCount.put("pow",  2);
        builtinArgCount.put("abs",  1);
        builtinArgCount.put("max",  2);
        builtinArgCount.put("min",  2);
        builtinArgCount.put("sqrt", 1);
        builtinArgCount.put("len",  1);
    }

    static class Function {
        List<String> params, body;
        Function(List<String> p, List<String> b) { params = p; body = b; }
    }

    // ── Interactive input supplier (set during SSE runs) ──────────
    static volatile InterpreterController.InteractiveInput activeInteractiveInput = null;

    // ── Entry points ──────────────────────────────────────────────

    /**
     * Interactive SSE mode — output streams live, lo blocks until user types.
     */
    public static void runInteractive(
            String code,
            InterpreterController.InteractiveOutput out,
            InterpreterController.InteractiveInput  in) {

        vars.clear(); functions.clear(); inputQueue.clear();
        activeInteractiveInput = in;

        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(new java.io.OutputStream() {
            private final StringBuilder buf = new StringBuilder();
            @Override public void write(int b) {
                char c = (char) b;
                if (c == '\n') { out.println(buf.toString()); buf.setLength(0); }
                else            buf.append(c);
            }
            @Override public void flush() {
                if (buf.length() > 0) { out.print(buf.toString()); buf.setLength(0); }
            }
        }));

        try {
            List<String> lines = parseLines(code);
            executeLines(lines, 0, lines.size());
        } catch (HinglishException he) {
            out.println("Galti: " + he.getMessage());
        } catch (RuntimeException re) {
            out.println("Galti: " + re.getMessage());
        } finally {
            System.setOut(oldOut);
            activeInteractiveInput = null;
        }
    }

    public static String runProgram(String code, List<String> userInputs) {
        vars.clear(); functions.clear(); inputQueue.clear();
        if (userInputs != null) inputQueue.addAll(userInputs);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            executeLines(parseLines(code), 0, parseLines(code).size());
        } catch (HinglishException he) {
            System.out.println("Galti: " + he.getMessage());
        } catch (RuntimeException re) {
            System.out.println("Galti: " + re.getMessage());
        } finally {
            System.setOut(oldOut);
        }
        return baos.toString();
    }

    public static String runProgram(String code) {
        return runProgram(code, Collections.emptyList());
    }

    static List<String> parseLines(String input) {
        List<String> out = new ArrayList<>();
        for (String line : input.split("\\R")) {
            line = line.trim();
            if (!line.isEmpty()) out.add(line);
        }
        return out;
    }

    // ── Block executor ────────────────────────────────────────────
    static void executeLines(List<String> lines, int start, int end) {
        int i = start;
        while (i < end) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) { i++; continue; }

            /* agar */
            if (line.startsWith("agar ")) {
                String cond = line.substring(5).trim();
                List<String> trueB = new ArrayList<>(), falseB = new ArrayList<>();
                boolean inElse = false; int depth = 0, j = i + 1;
                while (j < end) {
                    String nxt = lines.get(j).trim();
                    if (isBlockOpener(nxt)) { depth++; (inElse ? falseB : trueB).add(nxt); j++; continue; }
                    if (nxt.equals("warna") && depth == 0) { inElse = true; j++; continue; }
                    if (nxt.equals("khatam")) { if (depth == 0) { j++; break; } depth--; }
                    (inElse ? falseB : trueB).add(nxt); j++;
                }
                i = j;
                if (evalCondition(cond)) executeLines(trueB, 0, trueB.size());
                else                     executeLines(falseB, 0, falseB.size());
                continue;
            }

            /* jabtak */
            if (line.startsWith("jabtak ")) {
                String cond = line.substring(7).trim();
                List<String> loop = new ArrayList<>();
                int depth = 0, j = i + 1;
                while (j < end) {
                    String nxt = lines.get(j).trim();
                    if (isBlockOpener(nxt)) depth++;
                    if (nxt.equals("khatam")) { if (depth == 0) { j++; break; } depth--; }
                    loop.add(nxt); j++;
                }
                i = j;
                int iters = 0;
                while (evalCondition(cond)) {
                    if (++iters > 100_000) throw new HinglishException("Infinite loop rok diya");
                    executeLines(loop, 0, loop.size());
                }
                continue;
            }

            /* kaam */
            if (line.startsWith("kaam ")) {
                String[] t = line.split("\\s+");
                String name = t[1];
                List<String> params = new ArrayList<>(Arrays.asList(t).subList(2, t.length));
                List<String> body   = new ArrayList<>();
                int depth = 0, j = i + 1;
                while (j < end) {
                    String l = lines.get(j).trim();
                    if (isBlockOpener(l)) depth++;
                    if (l.equals("khatam")) { if (depth == 0) { j++; break; } depth--; }
                    body.add(l); j++;
                }
                functions.put(name, new Function(params, body));
                i = j; continue;
            }

            /* wapas */
            if (line.startsWith("wapas ")) {
                throw new ReturnSignal(evalExpr(line.substring(6).trim()));
            }

            /* lo */
            if (line.startsWith("lo ")) {
                String varName = line.substring(3).trim();
                String raw;
                if (activeInteractiveInput != null) {
                    // Interactive SSE mode: block until user types in the terminal
                    raw = activeInteractiveInput.readLine(varName);
                } else {
                    // Batch mode: read from pre-supplied queue
                    if (inputQueue.isEmpty())
                        throw new HinglishException("Input chahiye tha '" + varName + "' ke liye, queue khaali hai");
                    raw = inputQueue.poll();
                }
                HVal val;
                try { val = new HVal(Integer.parseInt(raw.trim())); }
                catch (NumberFormatException e) { val = new HVal(raw); }
                vars.put(varName, val);
                i++; continue;
            }

            runSingle(line);
            i++;
        }
    }

    static boolean isBlockOpener(String l) {
        return l.startsWith("agar ") || l.startsWith("jabtak ") || l.startsWith("kaam ");
    }

    // ── Single-line handler ───────────────────────────────────────
    static void runSingle(String line) {

        // 'set' keyword optional: both "set x = expr" and "x = expr" work
        // But we must NOT mistake == as assignment
        // Strategy: find first '=' that is NOT preceded or followed by another '='
        int assignIdx = findAssignEquals(line);

        if (assignIdx > 0) {
            // Could be "set x = ..." or "x = ..."
            String lhsFull = line.substring(0, assignIdx).trim();
            String rhs     = line.substring(assignIdx + 1).trim();

            // Strip optional 'set' keyword
            String varName = lhsFull.startsWith("set ") ? lhsFull.substring(4).trim() : lhsFull;

            // varName must be a plain identifier (no spaces, starts with letter/_)
            if (isIdentifier(varName)) {
                vars.put(varName, evalExpr(rhs));
                return;
            }
        }

        if (line.startsWith("likh ")) { System.out.println(evalExpr(line.substring(5).trim())); return; }
        if (line.equals("likh"))      { System.out.println(); return; }
        if (line.startsWith("lik "))  { System.out.print(evalExpr(line.substring(4).trim())); return; }

        if (line.startsWith("jod "))   { arithmeticMulti(line.substring(4).trim(),  "+"); return; }
        if (line.startsWith("ghata ")) { arithmeticMulti(line.substring(6).trim(),  "-"); return; }
        if (line.startsWith("guna "))  { arithmeticMulti(line.substring(5).trim(),  "*"); return; }
        if (line.startsWith("bhaag ")) { arithmeticMulti(line.substring(6).trim(),  "/"); return; }

        String fname = line.split("\\s+")[0];
        if (functions.containsKey(fname)) {
            callFunction(fname, tokenizeArgs(line.substring(fname.length()).trim()).toArray(new String[0]));
            return;
        }

        throw new HinglishException("Samajh nahi aaya: " + line);
    }

    /**
     * Find the index of a lone '=' that is assignment (not ==, !=, <=, >=).
     * Returns -1 if none found.
     */
    static int findAssignEquals(String line) {
        // Must be outside a string literal
        boolean inStr = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') { inStr = !inStr; continue; }
            if (inStr) continue;
            if (c == '=') {
                char prev = i > 0            ? line.charAt(i-1) : 0;
                char next = i < line.length()-1 ? line.charAt(i+1) : 0;
                // Skip ==, !=, <=, >=
                if (next == '=') continue;   // ==
                if (prev == '!' || prev == '<' || prev == '>' || prev == '=') continue;
                return i;
            }
        }
        return -1;
    }

    static boolean isIdentifier(String s) {
        if (s == null || s.isEmpty()) return false;
        if (!Character.isLetter(s.charAt(0)) && s.charAt(0) != '_') return false;
        for (char c : s.toCharArray()) if (!Character.isLetterOrDigit(c) && c != '_') return false;
        return true;
    }

    // ── Function call ─────────────────────────────────────────────
    static HVal callFunction(String name, String[] args) {
        Function f = functions.get(name);
        if (f == null) throw new HinglishException("Function nahi mila: " + name);
        if (args.length != f.params.size())
            throw new HinglishException("'" + name + "' ko " + f.params.size() + " args chahiye, mile " + args.length);

        Map<String, HVal> saved = vars;
        vars = new HashMap<>(saved);
        for (int i = 0; i < f.params.size(); i++)
            vars.put(f.params.get(i), evalExpr(args[i]));

        HVal result = new HVal(0);
        try { executeLines(f.body, 0, f.body.size()); }
        catch (ReturnSignal r) { result = r.value; }
        finally { vars = saved; }
        return result;
    }

    static List<String> tokenizeArgs(String s) {
        List<String> args = new ArrayList<>();
        s = s.trim(); if (s.isEmpty()) return args;
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '"') {
                int end = s.indexOf('"', i+1);
                if (end < 0) throw new HinglishException("String band nahi hui");
                args.add(s.substring(i, end+1)); i = end+1;
            } else if (c != ' ') {
                int end = i;
                while (end < s.length() && s.charAt(end) != ' ') end++;
                args.add(s.substring(i, end)); i = end;
            } else i++;
        }
        return args;
    }

    static void arithmeticMulti(String rest, String op) {
        String[] parts = rest.split("\\s+aur\\s+");
        HVal result = evalExpr(parts[0].trim());
        for (int i = 1; i < parts.length; i++) result = applyOp(result, op, evalExpr(parts[i].trim()));
        System.out.println(result);
    }

    static HVal applyOp(HVal a, String op, HVal b) {
        if (op.equals("+") && (a.isStr() || b.isStr())) return new HVal(a.asStr() + b.asStr());
        int L = a.asInt(), R = b.asInt();
        return switch (op) {
            case "+" -> new HVal(L + R);
            case "-" -> new HVal(L - R);
            case "*" -> new HVal(L * R);
            case "/" -> { if (R==0) throw new HinglishException("Zero se divide nahi kar sakte!"); yield new HVal(L/R); }
            case "%" -> { if (R==0) throw new HinglishException("Zero se mod nahi kar sakte!"); yield new HVal(L%R); }
            default  -> throw new HinglishException("Unknown operator: " + op);
        };
    }

    // ── Condition evaluator ───────────────────────────────────────
    static boolean evalCondition(String c) {
        c = c.trim();
        for (String op : new String[]{">=","<=","==","!=",">","<"}) {
            int idx = c.indexOf(op);
            if (idx < 0) continue;
            String ls = c.substring(0, idx).trim();
            String rs = c.substring(idx + op.length()).trim();
            if (ls.isEmpty() || rs.isEmpty()) throw new HinglishException("Condition adhoori: " + c);
            HVal L = evalExpr(ls), R = evalExpr(rs);
            if (L.isStr() || R.isStr()) {
                int cmp = L.asStr().compareTo(R.asStr());
                return switch(op){ case"=="->cmp==0;case"!="->cmp!=0;case"<"->cmp<0;case">"->cmp>0;case"<="->cmp<=0;case">="->cmp>=0;default->false; };
            }
            int li = L.asInt(), ri = R.asInt();
            return switch(op){ case">"->li>ri;case"<"->li<ri;case"=="->li==ri;case"!="->li!=ri;case">="->li>=ri;case"<="->li<=ri;default->false; };
        }
        throw new HinglishException("Condition mein operator nahi mila: " + c);
    }

    // ═══════════════════════════════════════════════════════════
    //  RECURSIVE DESCENT EXPRESSION PARSER
    //  Grammar:
    //    expr    → term   (('+' | '-') term)*
    //    term    → unary  (('*' | '/' | '%') unary)*
    //    unary   → '-' unary | primary
    //    primary → NUM | STR | '(' expr ')' | IDENT [args...]
    // ═══════════════════════════════════════════════════════════

    enum TType { NUM, STR, IDENT, PLUS, MINUS, STAR, SLASH, PERCENT, LPAREN, RPAREN, EOF }

    static class Token {
        final TType type; final String val;
        Token(TType t, String v) { type=t; val=v; }
        @Override public String toString(){ return type+"("+val+")"; }
    }

    static List<Token> tokenize(String expr) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (c==' ')  { i++; continue; }
            if (c=='+')  { tokens.add(new Token(TType.PLUS,"+"));    i++; continue; }
            if (c=='-')  { tokens.add(new Token(TType.MINUS,"-"));   i++; continue; }
            if (c=='*')  { tokens.add(new Token(TType.STAR,"*"));    i++; continue; }
            if (c=='/')  { tokens.add(new Token(TType.SLASH,"/"));   i++; continue; }
            if (c=='%')  { tokens.add(new Token(TType.PERCENT,"%")); i++; continue; }
            if (c=='(')  { tokens.add(new Token(TType.LPAREN,"("));  i++; continue; }
            if (c==')')  { tokens.add(new Token(TType.RPAREN,")"));  i++; continue; }
            if (c=='"') {
                int end = expr.indexOf('"', i+1);
                if (end < 0) throw new HinglishException("String band nahi hui");
                tokens.add(new Token(TType.STR, expr.substring(i+1, end))); i=end+1; continue;
            }
            if (Character.isDigit(c)) {
                int s=i; while(i<expr.length()&&Character.isDigit(expr.charAt(i)))i++;
                tokens.add(new Token(TType.NUM, expr.substring(s,i))); continue;
            }
            if (Character.isLetter(c)||c=='_') {
                int s=i; while(i<expr.length()&&(Character.isLetterOrDigit(expr.charAt(i))||expr.charAt(i)=='_'))i++;
                tokens.add(new Token(TType.IDENT, expr.substring(s,i))); continue;
            }
            throw new HinglishException("Unknown character '" + c + "' in: " + expr);
        }
        tokens.add(new Token(TType.EOF,""));
        return tokens;
    }

    static List<Token> curTokens; static int curPos;
    static Token peek()    { return curTokens.get(curPos); }
    static Token consume() { return curTokens.get(curPos++); }
    static boolean check(TType t) { return peek().type==t; }
    static Token expect(TType t) { if(!check(t)) throw new HinglishException("Expected "+t+" got "+peek()); return consume(); }

    static HVal evalExpr(String expr) {
        expr=expr.trim();
        if(expr.isEmpty()) throw new HinglishException("Expression khaali hai");
        curTokens=tokenize(expr); curPos=0;
        HVal r=parseExpr();
        if(!check(TType.EOF)) throw new HinglishException("Unexpected: '"+peek().val+"' in: "+expr);
        return r;
    }

    static HVal parseExpr() {
        HVal l=parseTerm();
        while(check(TType.PLUS)||check(TType.MINUS)){ String op=consume().val; l=applyOp(l,op,parseTerm()); }
        return l;
    }
    static HVal parseTerm() {
        HVal l=parseUnary();
        while(check(TType.STAR)||check(TType.SLASH)||check(TType.PERCENT)){ String op=consume().val; l=applyOp(l,op,parseUnary()); }
        return l;
    }
    static HVal parseUnary() {
        if(check(TType.MINUS)){ consume(); return new HVal(-parseUnary().asInt()); }
        return parsePrimary();
    }
    static HVal parsePrimary() {
        Token t=peek();
        if(t.type==TType.NUM)  { consume(); return new HVal(Integer.parseInt(t.val)); }
        if(t.type==TType.STR)  { consume(); return new HVal(t.val); }
        if(t.type==TType.LPAREN){ consume(); HVal v=parseExpr(); expect(TType.RPAREN); return v; }
        if(t.type==TType.IDENT) {
            consume(); String name=t.val;
            if(builtinArgCount.containsKey(name)){
                int n=builtinArgCount.get(name); HVal[] args=new HVal[n];
                for(int k=0;k<n;k++) args[k]=parsePrimary();
                return callBuiltin(name,args);
            }
            if(functions.containsKey(name)){
                Function f=functions.get(name); String[] raw=new String[f.params.size()];
                for(int k=0;k<f.params.size();k++) raw[k]=parsePrimary().asStr();
                return callFunction(name,raw);
            }
            if(vars.containsKey(name)) return vars.get(name);
            throw new HinglishException("'"+name+"' set nahi hai");
        }
        throw new HinglishException("Unexpected token: "+t);
    }

    static HVal callBuiltin(String name, HVal[] args) {
        return switch(name){
            case "sqrt"->new HVal((int)Math.sqrt(args[0].asInt()));
            case "pow" ->new HVal((int)Math.pow(args[0].asInt(),args[1].asInt()));
            case "abs" ->new HVal(Math.abs(args[0].asInt()));
            case "max" ->new HVal(Math.max(args[0].asInt(),args[1].asInt()));
            case "min" ->new HVal(Math.min(args[0].asInt(),args[1].asInt()));
            case "len" ->new HVal(args[0].asStr().length());
            default    ->throw new HinglishException("Unknown builtin: "+name);
        };
    }
}