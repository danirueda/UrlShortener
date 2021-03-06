package urlshortener.blackgoku.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import urlshortener.common.domain.CoordinatesHelper;
import urlshortener.common.domain.MessageHelper;
import urlshortener.blackgoku.domain.User;
import urlshortener.blackgoku.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;

@RestController
public class UserController extends CoordinatesHelper {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    protected UserRepository userRepo;

    @RequestMapping(value="/")
    public ModelAndView home(HttpServletRequest request){

        String usuario = (String) request.getSession().getAttribute("user");
        if(usuario!=null){
            logger.info("Detected registered user, redirecting to shortener");
            ModelAndView mav = new ModelAndView("shortener");
            mav.addObject("blockedLatitude", blockedLatitude);
            mav.addObject("blockedLongitude", blockedLongitude);
            return mav;
        } else{
            logger.info("Detected unregistered user, redirecting to register");
            return new ModelAndView("register");
        }
    }

    @RequestMapping(value="/register", method= RequestMethod.POST)
    public ModelAndView register(@RequestParam("email")String email,
                                 @RequestParam("password")String password,
                                 HttpServletRequest request,
                                 RedirectAttributes ra){
        logger.info("Detected petition to register user "+email);
        User usuario = new User(email,password);

        if(userRepo.save(usuario)){
            request.getSession().setAttribute("user",usuario.getEmail());
            MessageHelper.addSuccessAttribute(ra,"success.register",email);
        } else{
            //error al registrarse
            MessageHelper.addErrorAttribute(ra,"error.register",email);
        }

        return new ModelAndView("redirect:/");
    }

    @RequestMapping(value="/login", method=RequestMethod.POST)
    public ModelAndView login(@RequestParam("email")String email,
                              @RequestParam("password")String password,
                              HttpServletRequest request,
                              RedirectAttributes ra){
        logger.info("Detected petition to login user "+email);
        User usuario = userRepo.findByEmail(email);
        if(usuario != null){
            if(usuario.getPassword().equals(password)){
                logger.info("User " + email + " entered valid credentials");
                request.getSession().setAttribute("user",usuario.getEmail());
            } else{
                MessageHelper.addErrorAttribute(ra,"error.login.password",email);
                logger.error("Wrong password for user " + email);
            }
        } else{
            MessageHelper.addErrorAttribute(ra,"error.login.email",email);
            logger.error("User "+ email + " not found");
        }

        return new ModelAndView("redirect:/");
    }

    @RequestMapping(value="/logout")
    public ModelAndView logout(HttpServletRequest request,
                               RedirectAttributes ra){
        logger.info("Detected petition to logout");
        MessageHelper.addSuccessAttribute(ra,"success.session.close","");
        request.getSession().invalidate();
        return new ModelAndView("redirect:/");
    }
}
