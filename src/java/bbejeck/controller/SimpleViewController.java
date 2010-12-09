

package bbejeck.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SimpleViewController {
    
    @RequestMapping({"/","/index.htm"})
    public String showHome(){
        return "index";
    }

    @RequestMapping({"/error.htm"})
    public String error(){
        return "error";
    }

}
