package com.product.Controller.Home;

import java.io.File;
import java.io.IOException;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.product.Bean.MailInfo;
import com.product.Serive.CookieService;
import com.product.Serive.MailService;
import com.product.dao.ICustomerDao;
import com.product.entity.Customer;

@Controller
public class AccountController {
	@Autowired
	ICustomerDao customerDao;
	@Autowired
	HttpSession session;
	@Autowired
	CookieService cookieService;
	@Autowired
	ServletContext app;
	@Autowired
	MailService mailService;
	@Autowired
	HttpServletRequest request;
	
	
	@GetMapping("/account/login")
	public String Login(Model model) {
		Cookie ckid = cookieService.Read("userid");
		Cookie ckpw = cookieService.Read("pass");
		if(ckid != null) {
			String uid = ckid.getValue();
			String pwd = ckpw.getValue();
			model.addAttribute("uid", uid);
			model.addAttribute("pwd", pwd);
		}
		return "account/login";
		
	}
	
	@PostMapping("/account/login")
	public String login(Model model, 
			@RequestParam("id")Integer id,
			@RequestParam("pw")String pw,
			@RequestParam(value = "rm", defaultValue = "false")Boolean rm) {
		Customer user = customerDao.findById(id);
		if(user == null) {
			model.addAttribute("message", "Invalid username!");
		}else if(!pw.equals(user.getPassWord())) {
			model.addAttribute("message", "Invalid password!");
		}else if(!user.isActivated()) {
			model.addAttribute("message", "Your account is Inactivated!");
			model.addAttribute("isActivated", "/account/activate/"+id);
		}else {
			model.addAttribute("message", "Login SuccessFully");
			session.setAttribute("user", user);
			//ghi nho tai khoai bang cookie
			if(rm == true) {
				String GetIdUser = String.valueOf(user.getId()) ;
				cookieService.Create("userid", GetIdUser, 30);
				cookieService.Create("pass", user.getPassWord(), 30);
				
			}else {
				cookieService.Delete("userid");
				cookieService.Delete("pass");
			}
			// quay lai trang duoc bao ve()
			String backUrl = (String) session.getAttribute("back-url");
			if(backUrl != null) {
				return "redirect:" + backUrl;
			}
		}
		return "account/login";
		
	}
	
	@GetMapping("/account/register")
	public String register(Model model) {
		Customer user = new Customer();
		model.addAttribute("form", user);
		return "account/register";
	}
	
	@PostMapping("/account/register")
	public String register(Model model, 
			@Validated @ModelAttribute("form")Customer user,
			BindingResult errors,
			@RequestParam("photo_file")MultipartFile file) throws IllegalStateException, IOException, MessagingException {
		if(errors.hasErrors()) {
			model.addAttribute("message", "please fix some following errors");
			return "account/register";
		}else {
			try {
				Customer c = customerDao.finByName(user.getFullName());
				if(c != null) {
					model.addAttribute("message", "Username is in used");
					return "account/register";
				}
			} catch (Exception e) {
				System.out.println("No value present");
			}
		}
		if(file.isEmpty()) {
			user.setPhoto("user.png");
		}else {
			String dir = app.getRealPath("/static/product/image/customers");
			File f = new File(dir, file.getOriginalFilename());
			file.transferTo(f);
			user.setPhoto(f.getName());
		}
		user.setActivated(false);
		user.setAdmin(false);
		customerDao.create(user);
		model.addAttribute("message", "Register successfully");
		String from = "c1812m.sem2@gmail.com";
		String to = user.getEmail();
		String subject = "welcom";
		String url = request.getRequestURL().toString().replace("register", "activate" + user.getId());
		String body = "Click <a href='"+url+"'>activate</a>";
		MailInfo mail = new MailInfo(from, to, subject, body);
		mailService.send(mail);
		return "account/register";
	}
	
	@GetMapping("/account/activate/{id}")
	public String forgot(Model model,@PathVariable("id")Integer id) {
		Customer user = customerDao.findById(id);
		user.setActivated(true);
		customerDao.update(user);
		model.addAttribute("message", "You activate successFully");
		return "redirect:/account/login";
	}
	
	@PostMapping("/account/forgot")
	public String forgot(Model model,
			@RequestParam("id")int id,
			@RequestParam("email")String email) throws MessagingException {
		Customer user = customerDao.findById(id);
		if(user == null) {
			model.addAttribute("message", "Invalid username");
		}else if(!email.equals(user.getEmail())) {
			model.addAttribute("message", "Invalid email address");
		}else {
			String from = "c1812m.sem2@gmail.com";
			String to = user.getEmail();
			String subject = "Welcome";
			String url = request.getRequestURL().toString().replace("register", "activate/"+user.getId());
			String body = "Your password is" + user.getPassWord();
			MailInfo mail = new MailInfo(from, to, subject, body);
			mailService.send(mail);
			model.addAttribute("message", "you password was sent to your inbox");
			
		}
		return "redirect:/account/login";
	}
	@GetMapping("/account/change")
	public String change(Model model) {
		return "account/change";
	}
	@PostMapping("/account/change")
	public String change(Model model,
			@RequestParam("id")int id,
			@RequestParam("pw")String password,
			@RequestParam("pw1")String password1,
			@RequestParam("pw2")String password2) throws MessagingException {
		if(!password1.equals(password2)) {
			model.addAttribute("message", "Confirm password is not match!");
		}else {
			Customer user = customerDao.findById(id);
			if(user == null) {
				model.addAttribute("message", "Invalid username");
			}else if(!password.equals(user.getPassWord())) {
				model.addAttribute("message", "Invalid password ");
			}else {
				user.setPassWord(password2);
				customerDao.update(user);
				model.addAttribute("message", "change password Successfully");
				
			}
		}
		
		return "redirect:/account/login";
	}
	@GetMapping("/account/edit")
	public String edit(Model model) {
		Customer user = (Customer) session.getAttribute("user");
		model.addAttribute("form", user);
		return "account/edit";
	}
	@PostMapping("/account/edit")
	public String edit(Model model, 
			@ModelAttribute("form") Customer user,
			@RequestParam("photo_file") MultipartFile file) throws IllegalStateException, IOException, MessagingException {
		if(file.isEmpty()) {
			user.setPhoto("user.png");
		}else {
			String dir = app.getRealPath("/static/images/customers");
			File f = new File(dir, file.getOriginalFilename()); 
			file.transferTo(f);
			user.setPhoto(f.getName());
			
		}
		customerDao.update(user);
		session.setAttribute("user", user);
		model.addAttribute("message", "update account SuccessFully");
		return "account/edit";
	}
	
	
	
	//dang xuat
		@GetMapping("/account/logoff")
		public String logoff() {
			session.removeAttribute("user");
			return "redirect:/home/index";
		}

	
	
	
	
}
