package de.project.lukas.utils;

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

    // nur für Logcat
    public static String byteToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder(bytes.length);
        for (byte byteChar : bytes) {
            stringBuilder.append(String.format("%02X ", byteChar));
        }
        return(stringBuilder.toString());
    }

    public static Integer hexToDec(String hex) {
        return Integer.parseInt(hex.substring(2),16);
    }


}
