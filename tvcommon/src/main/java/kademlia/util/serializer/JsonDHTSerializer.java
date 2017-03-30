package kademlia.util.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import kademlia.dht.DHT;
import kademlia.dht.KademliaDHT;
import kademlia.dht.KademliaStorageEntryMetadata;

/**
 * A KadSerializer that serializes DHT to JSON format
 * The generic serializer is not working for DHT
 *
 * Why a DHT specific serializer?
 * The DHT structure:
 * - DHT
 * -- StorageEntriesManager
 * --- Map<NodeId, List<StorageEntry>>
 * ---- NodeId:KeyBytes
 * ---- List<StorageEntry>
 * ----- StorageEntry: Key, OwnerId, Type, Hash
 *
 * The above structure seems to be causing some problem for Gson, especially at the Map part.
 *
 * Solution
 * - Make the StorageEntriesManager transient
 * - Simply store all StorageEntry in the serialized object
 * - When reloading, re-add all StorageEntry to the DHT
 *
 * @author Joshua Kissoon
 *
 * @since 20140310
 */
public class JsonDHTSerializer implements KadSerializer<KademliaDHT>
{



    @Override
    public void write(KademliaDHT data, DataOutputStream out) throws IOException
    {
        JSON.writeJSONStringTo(data,new OutputStreamWriter(out));

    }
    @Override
    public KademliaDHT read(DataInputStream in, Class<KademliaDHT> tClass) throws IOException, ClassNotFoundException
    {
        String json=IOUtils.readAll(new InputStreamReader(in));
        DHT dht=JSON.parseObject(json,DHT.class);
        List<KademliaStorageEntryMetadata> entries= dht.getStorageEntries();
        dht.putStorageEntries(new ArrayList<KademliaStorageEntryMetadata>());
        dht.initialize();
        dht.putStorageEntries(entries);
        return dht;
    }
}
