package com.wechat_android.wechat_android.login.contorller;

        import com.wechat_android.wechat_android.Business;
        import com.wechat_android.wechat_android.Client;
        import com.wechat_android.wechat_android.Dnsip;
        import com.wechat_android.wechat_android.login.service.LoginService;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;

        import java.io.IOException;
        import java.util.HashMap;
        import java.util.Map;

@RestController
@RequestMapping(value = "/wechat_api")
public class LoginContorller {
    @Autowired
    private LoginService loginService;
    @GetMapping(value = "/login")
    public String login() throws IOException, InterruptedException {
        Map clientObj = new HashMap();
        Dnsip dnsip = new Dnsip();
        clientObj.put("host", dnsip.fetch_longlink_ip());
        clientObj.put("usrName", "1316");
        clientObj.put("passwd", "11");
        Client client = new Client(clientObj);
        client.start("", "");
        return "loginSuccer";
    }
}
