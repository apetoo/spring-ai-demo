package cn.apeto.mcp.client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author apeto
 * @create 2025/5/21 19:11
 */
@Controller
public class ViewController {

    @GetMapping("/")
    public String chat() {
        return "chat";
    }
}
