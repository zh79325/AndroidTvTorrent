package kademlia.util.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import kademlia.KadConfiguration;
import kademlia.routing.Contact;
import kademlia.routing.KademliaRoutingTable;

/**
 * A KadSerializer that serializes routing tables to JSON format
 The generic serializer is not working for routing tables

 Why a JKademliaRoutingTable specific serializer?
 The routing table structure:
 - JKademliaRoutingTable
 -- Buckets[]
 --- Map<NodeId, Node>
 * ---- NodeId:KeyBytes
 * ---- Node: NodeId, InetAddress, Port
 *
 * The above structure seems to be causing some problem for Gson,
 * especially at the Map part.
 *
 * Solution
 - Make the Buckets[] transient
 - Simply store all Nodes in the serialized object
 - When reloading, re-add all nodes to the JKademliaRoutingTable
 *
 * @author Joshua Kissoon
 *
 * @since 20140310
 */
public class JsonRoutingTableSerializer implements KadSerializer<KademliaRoutingTable>
{


    private final KadConfiguration config;

    

    /**
     * Initialize the class
     *
     * @param config
     */
    public JsonRoutingTableSerializer(KadConfiguration config)
    {
        this.config = config;
    }

    @Override
    public void write(KademliaRoutingTable data, DataOutputStream out) throws IOException
    {

        JSON.writeJSONStringTo(data,new OutputStreamWriter(out));

    }



    @Override
    public KademliaRoutingTable read(DataInputStream in, Class<KademliaRoutingTable> kademliaRoutingTableClass) throws IOException, ClassNotFoundException
    {

            String json= IOUtils.readAll(new InputStreamReader(in));
            KademliaRoutingTable tbl=JSON.parseObject(json,KademliaRoutingTable.class);

            /* Read the basic JKademliaRoutingTable */
            tbl.setConfiguration(config);
            
            /* Now get the Contacts and add them back to the JKademliaRoutingTable */
            List<Contact> contacts =tbl.getAllContacts();

            tbl.initialize();

            for (Contact c : contacts)
            {
                tbl.insert(c);
            }

            /* Read and return the Content*/
            return tbl;
    }
}
