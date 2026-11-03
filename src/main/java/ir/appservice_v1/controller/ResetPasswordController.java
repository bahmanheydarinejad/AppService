package ir.appservice_v1.controller;

import ir.appservice_v1.model.service.ResetPasswordTokenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


//@RequestMapping("/resetPassword")
//@Controller
public class ResetPasswordController extends BaseController {

    private ResetPasswordTokenService resetPasswordTokenService;

    public ResetPasswordController(ResetPasswordTokenService resetPasswordTokenService) {
        this.resetPasswordTokenService = resetPasswordTokenService;
    }

    @GetMapping()
    public String index() {
        return resetPassword("");
    }

    @GetMapping({"/{resetPasswordToken}"})
    public String resetPassword(@PathVariable String resetPasswordToken) {
        logger.trace("resetPasswordToken: {}", resetPasswordToken);
        try {
            if (resetPasswordToken != null && !resetPasswordToken.trim().isEmpty() && resetPasswordTokenService.existById(resetPasswordToken)) {
                return "/resetPassword.xhtml?token=" + resetPasswordToken;
            }
            return "/resetPassword.xhtml";
        } catch (Exception e) {
            e.printStackTrace();
            return "/resetPassword.xhtml";
        }
    }
}
