package org.zjh;

/**
 * @author zjh
 * @description: TODO
 **/
public class MessageFormatConstant {
    public static final byte[] MAGIC = "lrpc".getBytes();

    public static final byte VERSION = 1;

    public static final int VERSION_LENGTH = 1;

    public static final short HEADER_LENGTH = (short) (MAGIC.length + 18 + 8);

    public static final int HEADER_FIELD_LENGTH = 2;


    public static final int MAX_FRAME_LENGTH = 1024 * 1024;
    public static final int FULL_FIELD_LENGTH = 4;
}
