package org.zjh.utils;

import lombok.extern.slf4j.Slf4j;
import org.zjh.exception.NetWorkException;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author zjh
 * @description: TODO
 **/
@Slf4j
public class NetUtils {
    public static String getIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while(interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if(iface.isLoopback() || iface.isVirtual() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if(inetAddress instanceof Inet6Address || inetAddress.isLoopbackAddress()) {
                        continue;
                    }
                    String ipAddress = inetAddress.getHostAddress();
                    log.debug("局域网ip:{}",ipAddress);
                    return ipAddress;
                }
            }
            throw new NetWorkException();
        } catch (SocketException e) {
            log.error("获取局域网ip发生异常",e);
            throw new NetWorkException();
        }
    }
}
