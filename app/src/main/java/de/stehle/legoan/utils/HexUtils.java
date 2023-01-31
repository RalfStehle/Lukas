package de.stehle.legoan.utils;

import java.util.ArrayList;

public class HexUtils {
    public static byte[] hexStringToByteArray(String s) {
        ArrayList<Byte> temp = new ArrayList<Byte>();

        String[] parts = s.split(" ");

        for (String part: parts) {
            if (part.length() == 1) {
                int value = Character.digit(part.charAt(0), 16);

                temp.add((byte)value);
            } else if (part.length() == 2) {
                int n1 = Character.digit(part.charAt(0), 16);
                int n2 = Character.digit(part.charAt(1), 16);

                byte value = (byte) ((n1 << 4) + n2);

                temp.add((byte)value);
            }
        }

        byte[] result = new byte[temp.size()];

        for (int i = 0; i < temp.size(); i++) {
            result[i] = temp.get(i);
        }

        return result;
    }
}
