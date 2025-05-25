package edu.purdue.cs.fast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Morton {
    private final int code;
    private final int level;

    public Morton() {
        this.code = 0;
        this.level = 0;
    }

    public Morton(int code, int level) {
        this.code = code;
        this.level = level;
    }

    public int getChildCode(int quadrant) {
        return (this.code << 2) | (quadrant & 0x3);
    }

    public Morton getChild(int quadrant) {
        return new Morton(getChildCode(quadrant), level + 1);
    }

    public int length() {
        return (int) Math.pow(2, level);
    }

    public int getCode() {
        return code;
    }

    public int getLevel() {
        return level;
    }

    public List<Integer> getMortonPath() {
        if (level < 1) {
            throw new IllegalArgumentException("Level must be ≥ 1");
        }
        List<Integer> path = new ArrayList<>(level);
        // For each intermediate level ℓ = 1..level,
        // shift the full code right by the unused lower bits:
        // keep the top 2*i bits of `code`
        for (int i = 1; i <= level; i++) {
            int shift = 2 * (level - i);
            int prefixCode = code >> shift;
            path.add(prefixCode);
        }
        return path;
    }

    public String getString() {
        String s = Integer.toBinaryString(code);
        int padCount = (2 * level) - s.length();
        String pad = padCount > 0 ? String.format("%0"+ padCount +"d", 0) : "";
        return pad + s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Morton morton = (Morton) o;
        return code == morton.code && level == morton.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, level);
    }

    @Override
    public String toString() {
        return "Morton{" +
                "code=" + code + ", " +
                "level=" + level + ", " +
                "rep=" + getString() +
                '}';
    }

    public static void main(String[] args) {
        Morton m = new Morton();
        System.out.println(m);

        Morton SW = m.getChild(0);
        Morton SE = m.getChild(1);
        Morton NW = m.getChild(2);
        Morton NE = m.getChild(3);

        System.out.println(SW);
        System.out.println(SE);
        System.out.println(NW);
        System.out.println(NE);

        Morton SWSW = SW.getChild(0);
        Morton SWSE = SW.getChild(1);
        Morton SWNW = SW.getChild(2);
        Morton SWNE = SW.getChild(3);

        System.out.println(SWSW);
        System.out.println(SWSE);
        System.out.println(SWNW);
        System.out.println(SWNE);

        Morton NESWNWNE = NW.getChild(2);//.getChild(2).getChild(3);

        List<Integer> path = NESWNWNE.getMortonPath();
        System.out.println(NESWNWNE);
        System.out.println(path);
    }
}


