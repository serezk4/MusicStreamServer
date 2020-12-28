package com.serezka.server;

public class App {
    public static final String NAME = "MusicStream Server";
    public static final double VERSION = 0.1;


    public enum Config {
        // DATABASE {future}
        DB_NAME("db_name"),
        DB_USER("db_user"),
        DB_PASS("db_pass"),

        // SERVER
        SERVER_IP("server_ip"),
        SERVER_PORT_LIVE("server_port_live"),
        SERVER_PORT_FILE("server_port_file"),
        SERVER_PORT_INFO("server_port_info"),

        // AUDIO
        AUDIO_SAMPLE_RATE("audio_sampleRate"),
        AUDIO_SAMPLE_SIZE("audio_sampleSizeInBits"),
        AUDIO_CHANNELS_COUNT("audio_channelsCount"),
        AUDIO_FRAME_SIZE("audio_frameSize"),
        AUDIO_FRAME_RATE("audio_frameRate"),
        AUDIO_SIGNED("audio_isSigned"),
        AUDIO_BIG_ENDIAN("audio_isBigEndian");

        private final String name;

        Config(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }


    public enum Files {
        CONFIG("cfg.properties", Type.FILE),
        TRACKS("tracks", Type.DIR);

        public enum Type {
            DIR,FILE;
        }

        public static final String MAIN_DIR = "C:\\MusicStreamServer";
        public static final String AUDIO_FILE_EXTENSION = ".wav";

        private final String name;
        private final Type type;

        Files(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }
    }
}