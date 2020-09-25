package com.szb;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class MainController {

    @Resource
//    DiscoveryClient client;
    EurekaClient client;

    @Resource
    LoadBalancerClient loadBalancerClient;

    @GetMapping("/home")
    public String home() {
        System.out.println("get in");
        return "Spring Cloud ,hello world";
    }

    @GetMapping("/client")
    public String client() {
        List<String> services = ((DiscoveryClient) client).getServices();
        for (String service : services) {
            System.out.println(service);
        }
        return "success";
    }

    @GetMapping("/instance")
    public Object instance() {
        return ((DiscoveryClient) client).getInstances("provider");
    }

    @GetMapping("/instance2")
    public String instance2() {
        List<InstanceInfo> provider = client.getInstancesByVipAddress("provider", false);
        for (InstanceInfo info : provider) {
            System.out.println(info);
        }
        return "success";
    }

    @GetMapping("/instance3")
    public String instance3() {
        //获取服务名
        List<InstanceInfo> instances = client.getInstancesByVipAddress("provider", false);
        if (instances.size() > 0) {
            InstanceInfo instanceInfo = instances.get(0);
            if (instanceInfo.getStatus().equals(InstanceInfo.InstanceStatus.UP)) {
                String url = "http://" + instanceInfo.getHostName() + ":" + instanceInfo.getPort() + "/home";
                System.out.println(url);
                //调用服务
                RestTemplate restTemplate = new RestTemplate();
                String forObject = restTemplate.getForObject(url, String.class);
                System.out.println(forObject);
            }
        }
        return "success";
    }

    /**
     * 负载均衡简单的调用
     * @return
     */
    @GetMapping("rebbon")
    public String rebbon(){
        //获取服务，默认是剔除了DOWN的服务
        ServiceInstance provider = loadBalancerClient.choose("provider");
        String url = "http://" + provider.getHost() + ":" + provider.getPort() + "/home";
        RestTemplate restTemplate = new RestTemplate();
        String forObject = restTemplate.getForObject(url, String.class);
        System.out.println(forObject);
        return "success";
    }
}
