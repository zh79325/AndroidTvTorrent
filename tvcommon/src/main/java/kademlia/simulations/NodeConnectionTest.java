package kademlia.simulations;

import java.io.IOException;
import java.net.InetAddress;

import kademlia.JKademliaNode;
import kademlia.node.KademliaId;
import kademlia.node.Node;

/**
 * Testing connecting 2 nodes to each other
 *
 * @author Joshua Kissoon
 * @created 20140219
 */
public class NodeConnectionTest
{

    public static void main(String[] args)
    {
        try
        {
            //("router.utorrent.com",6881)
            /* Setting up 2 Kad networks */
            Node []bootstrapNodes=new Node[4];
            bootstrapNodes[0]= new Node(new KademliaId(), InetAddress.getByName("router.utorrent.com"), 6881);
            bootstrapNodes[1]= new Node(new KademliaId(), InetAddress.getByName("router.bittorrent.com"), 6881);
            bootstrapNodes[2]= new Node(new KademliaId(), InetAddress.getByName("dht.transmissionbt.com"), 6881);
//            bootstrapNodes[3]= new Node(new KademliaId(), InetAddress.getByName("router.bitcomet.com"), 6881);
            bootstrapNodes[3]= new Node(new KademliaId(), InetAddress.getByName("dht.aelitis.com"), 6881);


            JKademliaNode kad1 = new JKademliaNode("JoshuaK", new KademliaId("ASF45678947584567467"), 7574);
            System.out.println("Created Node Kad 1: " + kad1.getNode().getNodeId());

            System.out.println("Connecting Kad 1 and to bootstrap");

            for (Node node : bootstrapNodes) {
                try{
                    kad1.bootstrap(node);
                    break;
                }catch (Exception e){
                    e.printStackTrace();
                }

            }



            System.out.println("\n\nKad 1: " + kad1.getNode().getNodeId() + " Routing Table: ");
            System.out.println(kad1.getRoutingTable());

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
