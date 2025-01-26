package tech.smartboot.feat.demo;

import tech.smartboot.feat.Feat;
import tech.smartboot.feat.cloud.annotation.Autowired;
import tech.smartboot.feat.cloud.annotation.Controller;
import tech.smartboot.feat.cloud.annotation.RequestMapping;

@Controller
public class FeatCloudDemo {
    @Autowired
    private String hello;

    @RequestMapping("/cloud")
    public String helloWorld() {
        return hello;
    }

    public static void main(String[] args) {
        Feat.cloudServer(opts -> opts.addExternalBean("hello", "你好~")).listen();
    }

    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }
}
