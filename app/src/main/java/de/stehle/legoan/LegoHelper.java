package de.stehle.legoan;

public class LegoHelper {
    public static byte[] dataToEnvelope(byte[] data) {
        byte[] envelope = new byte[data.length + 2];

        // The first value must be the length.
        envelope[0] = (byte)(data.length + 2);
        envelope[1] = 0;

        // Copy the rest of the value.
        System.arraycopy(data, 0, envelope, 2, data.length);
        return envelope;
    }

    public static byte[] envelopeToData(byte[] envelope) {
        byte[] data = new byte[envelope.length - 2];

        System.arraycopy(envelope, 2, data, 0, envelope.length - 2);
        return data;
    }
}
