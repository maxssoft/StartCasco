package ru.telematica.casco2go.service.JourneyService;

import android.location.Location;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.UUID;

import ru.telematica.casco2go.service.ScoringService;

/**
 * Сегмент данных о поездке, передаваемых по TCP
 */
public final class JourneyDataChunk {
    @JsonProperty
    private String uuid;
    @JsonProperty
    private Type type;
    @JsonProperty
    private String user_id;
    @JsonProperty
    private long timestamp;
    @JsonProperty
    private int lon;
    @JsonProperty
    private int lat;
    @JsonProperty
    private float speed;
    @JsonProperty
    private float accelerationX;
    @JsonProperty
    private float accelerationY;
    @JsonProperty
    private float course;
    @JsonProperty
    private float hdop;

    @JsonCreator
    private JourneyDataChunk(@JsonProperty("type") Type type) {
        this.type = type;
        this.uuid = UUID.randomUUID().toString();
        this.user_id = String.valueOf(ScoringService.getAuthData().getUserId());
    }

    public static JourneyDataChunk id() {
        return new JourneyDataChunk(Type.cmd_id).setTimestamp(System.currentTimeMillis());
    }

    public static JourneyDataChunk journeyStart() {
        return new JourneyDataChunk(Type.cmd_start).setTimestamp(System.currentTimeMillis());
    }

    public static JourneyDataChunk journeyData(Location location, float... acceleration) {
        return new JourneyDataChunk(Type.cmd_data).setLocation(location).setAcceleration(acceleration);
    }

    private JourneyDataChunk setTimestamp(long timestampMillis) {
        timestamp = timestampMillis / 1000;
        return this;
    }

    private JourneyDataChunk setLocation(Location location) {
        setTimestamp(location.getTime());
        lon = (int) (location.getLongitude() * 10000000);
        lat = (int) (location.getLatitude() * 10000000);
        speed = location.getSpeed();
        course = location.getBearing();
        hdop = location.getAccuracy() / 6.069f;
        return this;
    }

    private JourneyDataChunk setAcceleration(float... values) {
        accelerationX = values[0];
        accelerationY = values[1];
        return this;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{type=%s; ts=%d}", type, timestamp);
    }

    @Nullable
    public ByteArrayOutputStream toByteStream() {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writeType(baos);

            switch (type) {
                case cmd_id:
                    writeUserId(baos);
                    break;
                case cmd_start:
                    writeInt(baos, (int) timestamp);
                    break;
                case cmd_data:
                    writeInt(baos, (int) timestamp);
                    writeInt(baos, lat);
                    writeInt(baos, lon);
                    writeFloat(baos, speed);
                    writeFloat(baos, accelerationX);
                    writeFloat(baos, accelerationY);
                    writeFloat(baos, course);
                    writeFloat(baos, hdop);
                    break;
                default:
                    throw new IllegalArgumentException("Wrong adress type");
            }

            return baos;
        } catch (Exception e) {
            return null;
        }
    }

    private void writeType(ByteArrayOutputStream baos) throws IOException {
        baos.write(ByteBuffer.allocate(4).putInt(type.mValue).array());
    }

    private void writeUserId(ByteArrayOutputStream baos) throws IOException {
        final byte[] value = user_id.getBytes(Charset.forName("UTF-8"));
        baos.write(ByteBuffer.allocate(value.length).order(ByteOrder.LITTLE_ENDIAN).put(value).array());
    }

    private void writeInt(ByteArrayOutputStream baos, int value) throws IOException {
        baos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array());
    }

    private void writeFloat(ByteArrayOutputStream baos, float value) throws IOException {
        baos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array());
    }

    private enum Type {
        cmd_id(0x0A010200),
        cmd_start(0x0A020200),
        cmd_data(0x0A030100);

        private final int mValue;

        Type(int value) {
            mValue = value;
        }
    }
}
