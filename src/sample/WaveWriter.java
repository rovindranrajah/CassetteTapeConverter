package sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WaveWriter {
    private ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    private int mSampleRate;
    private int mChannels;
    private int mSampleBits;

    private int mBytesWritten;

    public WaveWriter(int sampleRate, int sampleBits, int channels) throws IOException {
        this.mSampleRate = sampleRate;
        this.mChannels = channels;
        this.mSampleBits = sampleBits;
        this.mBytesWritten = 0;
        outStream.write(new byte[44]);
    }

    public void write(byte[] src, int offset, int length) throws IOException {
        if (offset > length) {
            throw new IndexOutOfBoundsException(String.format("offset %d is greater than length %d", offset, length));
        }
        for (int i = offset; i < length; i += 4) {
            writeUnsignedShortLE(src[i], src[i + 1]);
            writeUnsignedShortLE(src[i + 2], src[i + 3]);
            mBytesWritten += 4;
        }
    }

    public byte[] getData() throws IOException {
        byte[] result = outStream.toByteArray();
        writeWaveHeader(result);
        return result;
    }

    private void writeWaveHeader(byte[] file) throws IOException {
        int bytesPerSec = (mSampleBits + 7) / 8;

        int position = 0;
        position = setValue(file, position, "RIFF");
        position = setValue(file, position, mBytesWritten + 36);
        position = setValue(file, position, "WAVE");
        position = setValue(file, position, "fmt ");
        position = setValue(file, position, (int) 16);
        position = setValue(file, position, (short) 1);
        position = setValue(file, position, (short) mChannels);
        position = setValue(file, position, mSampleRate);
        position = setValue(file, position, mSampleRate * mChannels * bytesPerSec);
        position = setValue(file, position, (short) (mChannels * bytesPerSec));
        position = setValue(file, position, (short) mSampleBits);
        position = setValue(file, position, "data");
        position = setValue(file, position, mBytesWritten);
    }

    private void writeUnsignedShortLE(byte sample1, byte sample2) throws IOException {
        outStream.write(sample1);
        outStream.write(sample2);
    }

    private static int setValue(byte[] buffer, int position, String value) {
        for (int i = 0; i < value.length(); i++) {
            buffer[position + i] = (byte) value.charAt(i);
        }
        return position + value.length();
    }

    private static int setValue(byte[] buffer, int position, int value) {
        buffer[position + 3] = (byte) (value >> 24);
        buffer[position + 2] = (byte) (value >> 16);
        buffer[position + 1] = (byte) (value >> 8);
        buffer[position + 0] = (byte) (value);
        return position + 4;
    }

    private static int setValue(byte[] buffer, int position, short value) {
        buffer[position + 1] = (byte) (value >> 8);
        buffer[position] = (byte) value;
        return position + 2;
    }
}

