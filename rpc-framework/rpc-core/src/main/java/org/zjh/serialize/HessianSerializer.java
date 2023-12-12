package org.zjh.serialize;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author zjh
 * @description: TODO
 **/
@Slf4j
public class HessianSerializer implements Serializer{
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return new byte[0];
        }
        byte[] result = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
        try {
            hessian2Output.startMessage();
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            hessian2Output.completeMessage();
            result = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != hessian2Output) {
                    hessian2Output.close();
                    byteArrayOutputStream.close();
                }
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes.length == 0 || clazz == null) {
            return null;
        }
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
        Hessian2Input hessian2Input = new Hessian2Input(byteInputStream);
        T object = null;
        try {
            hessian2Input.startMessage();
            object = (T) hessian2Input.readObject();
            hessian2Input.completeMessage();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                hessian2Input.close();
                byteInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return (T) object;
        }
    }
}
