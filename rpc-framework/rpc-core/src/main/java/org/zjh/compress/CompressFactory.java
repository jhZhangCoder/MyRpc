package org.zjh.compress;

import org.zjh.exception.CompressException;
import org.zjh.exception.SerializeException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zjh
 * @description: TODO
 **/
public class CompressFactory {
    private static final Map<String, Compressor> COMPRESSOR_MAP = new ConcurrentHashMap<>(16);

    static {
        COMPRESSOR_MAP.put("gzip",new GzipCompressor());
        //
    }

    public static Compressor getCompressor(String type) {
        Compressor compressor = COMPRESSOR_MAP.get(type);
        if(compressor == null) {
            throw new CompressException("未找到相应压缩器!");
        }
        return compressor;
    }
}
