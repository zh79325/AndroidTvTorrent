package kademlia.util.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * A KadSerializer that serializes content to JSON format
 *
 * @param <T> The type of content to serialize
 *
 * @author Joshua Kissoon
 *
 * @since 20140225
 */
public class JsonSerializer<T> implements KadSerializer<T>
{

    @Override
    public void write(T data, DataOutputStream out) throws IOException
    {
        JSON.writeJSONStringTo(data,new OutputStreamWriter(out));

    }

    @Override
    public T read(DataInputStream in, Class<T> tClass) throws IOException, ClassNotFoundException
    {
        String json= IOUtils.readAll(new InputStreamReader(in));
        T dht=JSON.parseObject(json,tClass);
        return dht;
    }
}
