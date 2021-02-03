package google.maps.webview.intercept;

import java.io.*;

public class XORCypher {

    /**
     * XOR algorithm encryption/decryption
     *
     * @param data data (ciphertext/clear text)
     * @param key  key
     * @return returns decrypted/encrypted data
     */
    public static byte[] encrypt(byte[] data, byte[] key) {
        if (data == null || data.length == 0 || key == null || key.length == 0)
            throw new IllegalArgumentException();

        byte[] result = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length] ^ (i & 0xFF));
        }

        return result;
    }

    public static byte[] encryptSingle(byte[] data, int key) {
        if (data == null || data.length == 0)
            throw new IllegalArgumentException();

        byte[] result = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ ((byte) key) ^ (i & 0xFF));
        }

        return result;
    }

    /**
     * Encryption/decryption of file XOR algorithm
     *
     * @param inFile  input file (ciphertext / plain text)
     * @param outFile result output file
     * @param key     key
     */
    public static void encryptFile(File inFile, File outFile, byte[] key) throws Exception {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(inFile);
            out = new BufferedOutputStream(new FileOutputStream(outFile), 10240);

            int b = -1;
            long i = 0;

            while ((b = in.read()) != -1) {
                b = (b ^ key[(int) (i % key.length)] ^ (int) (i & 0xFF));
                out.write(b);
                i++;
            }
            out.flush();

        } finally {
            close(in);
            close(out);
        }
    }

    private static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }

}