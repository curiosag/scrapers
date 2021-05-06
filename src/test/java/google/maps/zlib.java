package google.maps;

import util.FileUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class zlib {

    private static final int chunksize = 1024;

    public static void main(String[] args) {
        byte[] bytes = FileUtil.readBytes("/home/ssmertnig/temp/f.bin");

        System.out.println(zlib.decompressString(decode(0x5f, bytes))); //   0x5f;
    }

    public static byte[] decode(int key, byte[] data)
    {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key);
        }
        return result;
    }

    public static byte[] decompress(byte[] compressed) {
        Inflater inflater = new Inflater();
        inflater.setInput(compressed);

        List<byte[]> chunks = new ArrayList<>();
        int sizeLastChunk = 0;
        try {
            while (!inflater.needsInput()) {
                byte[] buffer = new byte[chunksize];
                sizeLastChunk = inflater.inflate(buffer);
                chunks.add(buffer);
            }
            inflater.end();
        } catch (DataFormatException e) {
            throw new RuntimeException(e);
        }

        return pack(chunks, sizeLastChunk);
    }

    private static byte[] pack(List<byte[]> chunks, int sizeLastChunk) {
        int size = (chunks.size() - 1) * chunksize + sizeLastChunk;
        byte[] result = new byte[size];

        int offset = 0;
        for (byte[] bb : chunks.subList(0, chunks.size() - 1)) {
            System.arraycopy(bb, 0, result, offset, chunksize);
            offset = offset + chunksize;
        }
        System.arraycopy(chunks.get(chunks.size() - 1), 0, result, offset, sizeLastChunk);

        return result;
    }

    public static String decompressString(byte[] compressed) {
        return new String(decompress(compressed), StandardCharsets.UTF_8);
    }
}
