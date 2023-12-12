package org.zjh.spi;

import org.zjh.compress.Compressor;
import org.zjh.config.Configuration;
import org.zjh.loadbalance.LoadBalancer;
import org.zjh.serialize.Serializer;

/**
 * @author zjh
 * @description: TODO
 **/
public class SpiResolver {
    public void loadFromSpi(Configuration configuration) {
        LoadBalancer loadBalancer = SpiHandler.get(LoadBalancer.class);
        configuration.setLoadBalancer(loadBalancer);

        Serializer serializer = SpiHandler.get(Serializer.class);
        configuration.setSerializer(serializer);

        Compressor compressor = SpiHandler.get(Compressor.class);
        configuration.setCompressor(compressor);

    }
}
