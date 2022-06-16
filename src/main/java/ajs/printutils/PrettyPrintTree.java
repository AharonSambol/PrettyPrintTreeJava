package ajs.printutils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.*;

public final class PrettyPrintTree<Node> {
    private static final Pattern slashNRegex = Pattern.compile("(\\\\n|\n)");
    private static final Map<Color, String> colorToNum = Map.of(
            Color.RED, "41",
            Color.GREEN, "42",
            Color.YELLOW, "43",
            Color.BLUE, "44",
            Color.PINK, "45",
            Color.LIGHT_BLUE, "46",
            Color.GRAY, "47",
            Color.GREY, "47"
    );

    private static final Map<Character, Character> addBranch = Map.of(
            '─', '┴',
            '┬', '┼',
            '┌', '├',
            '┐', '┤'
    );
    private final Function<Node, List<Node>> getChildren;
    private final Function<Node, String> getNodeVal;
    private int maxDepth = -1, trim = -1;
    private Color color = Color.GRAY;
    private boolean border = false, escapeNewline = false;
    public PrettyPrintTree(
            Function<Node, List<Node>> getChildren,
            Function<Node, String> getVal
    ) {
        this.getChildren = getChildren;
        this.getNodeVal = getVal;
    }

    public PrettyPrintTree<Node> setBorder(boolean border) { this.border = border;  return this; }
    public PrettyPrintTree<Node> setColor(Color color) { this.color = color;    return this; }
    public PrettyPrintTree<Node> setEscapeNewline(boolean escapeNewline) {   this.escapeNewline = escapeNewline;    return this; }
    public PrettyPrintTree<Node> setMaxDepth(int maxDepth) { this.maxDepth = maxDepth;  return this; }
    public PrettyPrintTree<Node> setTrim(int trim) { this.trim = trim;  return this; }

    public void display(Node node, int depth) { System.out.println(toStr(node, depth)); }
    public void display(Node node) {    System.out.println(toStr(node)); }
    public String toStr(Node node) {    return toStr(node, 0); }

    public String toStr(Node node, int depth) {
        String[][] res = treeToStr(node, depth);
        var str = new StringBuilder();
        for (var line: res) {
            for (var x: line) {
                str.append(isNode(x) ? colorTxt(x) : x);
            }
            str.append("\n");
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }

    private String[][] getVal(Node node) {
        var stVal = this.getNodeVal.apply(node);
        if (this.trim != -1 && this.trim < stVal.length()) {
            stVal = stVal.substring(0, this.trim) + "...";
        }
        if (this.escapeNewline) {
            stVal = slashNRegex.matcher(stVal).replaceAll(x -> x.group(0).equals("\n") ? "\\\\n" : "\\\\\\\\n");
        }
        if (!stVal.contains("\n")) {
            return new String[][]{
                    new String[]{ stVal }
            };
        }
        var lstVal = stVal.split("\n");
        var longest = 0;
        for (var item: lstVal) {
            longest = Math.max(item.length(), longest);
        }
        var res = new String[lstVal.length][];
        for (int i = 0; i < lstVal.length; i++) {
            res[i] = new String[] { lstVal[i] + " ".repeat(longest - lstVal[i].length()) };
        }
        return res;
    }
    private LinkedList<Node> removeNull(List<Node> list){
        var res = new LinkedList<Node>();
        for (var node : list){
            if(node == null){   continue; }
            res.addLast(node);
        }
        return res;
    }
    private String[][] treeToStr(Node node, int depth) {
        var val = getVal(node);
        var children = this.getChildren.apply(node);
        children = removeNull(children);
        if (children.size() == 0) {
            String[][] res;
            if (val.length == 1) {
                res = new String[][]{new String[]{"[" + val[0][0] + "]"}};
            } else {
                res = formatBox("", val);
            }
            return res;
        }
        var toPrint = new ArrayList<ArrayList<String>>();
        toPrint.add(new ArrayList<>());
        var spacing_count = 0;
        var spacing = "";
        if (depth + 1 != this.maxDepth) {
            for (var child: children) {
                var childPrint = treeToStr(child, depth + 1);
                for (int l = 0; l < childPrint.length; l++) {
                    var line = childPrint[l];
                    if (l + 1 >= toPrint.size()) {
                        toPrint.add(new ArrayList<>());
                    }
                    if (l == 0) {
                        var lineLen = lenJoin(List.of(line));
                        var middleOfChild = lineLen - (int)Math.ceil(line[line.length-1].length() / 2d);
                        var toPrint0Len = lenJoin(toPrint.get(0));
                        toPrint.get(0).add(" ".repeat(spacing_count - toPrint0Len + middleOfChild) + "┬");
                    }
                    var toPrintNxtLen = lenJoin(toPrint.get(l + 1));
                    toPrint.get(l + 1).add(" ".repeat(spacing_count - toPrintNxtLen));
                    toPrint.get(l + 1).addAll(List.of(line));
                }
                spacing_count = 0;
                for (var item: toPrint) {
                    var itemLen = lenJoin(item);
                    spacing_count = Math.max(itemLen, spacing_count);
                }
                spacing_count++;
            }
            int pipePos;
            if (toPrint.get(0).size() != 1) {
                var newLines = String.join("", toPrint.get(0));
                var spaceBefore = newLines.length() - (newLines = newLines.trim()).length();
                int lenOfTrimmed = newLines.length();
                newLines = " ".repeat(spaceBefore) +
                        "┌" + newLines.substring(1, newLines.length() - 1).replace(' ', '─') + "┐";
                var middle = newLines.length() - (int)Math.ceil(lenOfTrimmed / 2d);
                pipePos = middle;

                var newCh = addBranch.get(newLines.charAt(middle));
                newLines = newLines.substring(0, middle) + newCh + newLines.substring(middle + 1);
                var al = new ArrayList<String>();
                al.add(newLines);
                toPrint.set(0, al);
            } else {
                toPrint.get(0).set(0, toPrint.get(0).get(0).substring(0, toPrint.get(0).get(0).length() - 1) + '│');
                pipePos = toPrint.get(0).get(0).length() - 1;
            }
            if (val[0][0].length() < pipePos * 2) {
                spacing = " ".repeat(pipePos - (int)Math.ceil(val[0][0].length() / 2d));
            }
        }
        if (val.length == 1) {
            val = new String[][] { new String[] { spacing, "[" + val[0][0] + "]" } };
        } else {
            val = formatBox(spacing, val);
        }

        var asArr = new String[val.length + toPrint.size()][];
        int row = 0;
        for (var item: val) {
            asArr[row] = new String[item.length];
            System.arraycopy(item, 0, asArr[row], 0, item.length);
            row++;
        }
        for (var item: toPrint) {
            asArr[row] = new String[item.size()];
            int i = 0;
            for (var x: item) {
                asArr[row][i] = x;
                i++;
            }
            row++;
        }

        return asArr;
    }
    private static boolean isNode(String x) {
        if (x == null || x.equals("")) { return false; }
        char xat0 = x.charAt(0);
        if (xat0 == '[' || xat0 == '|' || (xat0 == '│' && x.trim().length() > 1)) {
            return true;
        }
        if (x.length() < 2) { return false; }
        var middle = "─".repeat(x.length() - 2);
        return x.equals("┌" + middle + "┐") || x.equals("└" + middle + "┘");
    }
    private String colorTxt(String txt) {
        var spaces = " ".repeat(txt.length() - (txt = txt.trim()).length());
        boolean is_label = txt.startsWith("|");
        if (is_label) {
            // todo "Not implemented yet"
        }
        txt = this.border ? txt : " " + txt.substring(1, txt.length() - 1) + " ";
        txt = this.color == Color.NONE ? txt : "\u001b[" +  colorToNum.get(this.color) + "m" + txt + "\u001b[0m";
        return spaces + txt;
    }

    private int lenJoin(Collection<String> lst) {
        return String.join("", lst).length();
    }

    private String[][] formatBox(String spacing, String[][] val) {
        String[][] res;
        int start = 0;
        if (this.border) {
            res = new String[val.length + 2][];
            start = 1;
            var middle = "─".repeat(val[0][0].length());
            res[0] = new String[] { spacing, '┌' + middle + '┐' };
            res[res.length - 1] = new String[] { spacing, '└' + middle + '┘' };
        } else {
            res = new String[val.length][];
        }
        for (int r = 0; r < val.length; r++) {
            res[r + start] = new String[] { spacing, "│" + val[r][0] + "│" };
        }
        return res;
    }
}
