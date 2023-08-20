package cn.mklaus.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author klausxie
 * @since 2023/8/15
 */
@RestController
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "hello, world";
    }

}
