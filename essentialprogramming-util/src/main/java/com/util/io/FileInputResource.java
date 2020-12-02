package com.util.io;

import java.io.*;
import java.net.URI;
import java.net.URL;


public class FileInputResource implements InputResource {
    private static final int BYTE_RANGE = 1024;
    private final URL file;

    public FileInputResource(String fileName) throws IOException {
        file = InputResource.getURL(fileName);
    }

    @Override
    public InputStream getInputStream()
            throws IOException {
        return new BufferedInputStream(file.openStream());
    }

    public byte[] getBytes() throws IOException {
        try (InputStream inputStream = getInputStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[BYTE_RANGE];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            return buffer.toByteArray();
        }

    }

    public byte[] getBytes(long start, int length) throws IOException {
        try (InputStream inputStream = getInputStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            inputStream.skip(start);
            long toRead = length;
            int nRead;
            byte[] data = new byte[BYTE_RANGE];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                if ((toRead -= nRead) > 0) {
                    buffer.write(data, 0, nRead);
                    buffer.flush();
                } else {
                    buffer.write(data, 0, (int) toRead + nRead);
                    buffer.flush();
                    break;
                }
            }
            return buffer.toByteArray();
        }
    }

    public URL getFile() {
        return file;
    }


}
