package com.aplus.controller;

import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.Random;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.aplus.model.MemberVO;
import com.aplus.service.MemberService;

@Controller
public class LoginController {
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private MemberService memberService;

	@Autowired
	private JavaMailSender mailSender;

	// 로그인 페이지 진입
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String loginGET(HttpSession session) {
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 로그인 페이지 진입");

		session.invalidate(); // 로그인중에 login치고 들어가면 로그아웃 처리
		return "member/login";
	}

	// 로그인 실행
	@RequestMapping(value = "/loginAction", method = { RequestMethod.GET, RequestMethod.POST })
	public String loginAction( MemberVO vo, HttpSession session ,HttpServletResponse response) throws Exception {
		logger.info("MemberVO:" + vo);
		String pw = vo.getPw();
		MemberVO vo2 = memberService.loginAction(vo.getId());
		logger.info("MemberVO2:" + vo);
		logger.info("sessions:" + vo.getName());
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		if (vo2 != null ) { // 로그인 성공 시
			
			if(BCrypt.checkpw(pw, vo2.getPw())==true ) {
				
				session.setAttribute("admin", vo2.getAdmin());
				session.setAttribute("id", vo2.getId());
				session.setAttribute("name", vo2.getName());
				return "redirect:/main";
			}else {
				
				out.print("<script>alert('\" 비밀번호가 틀립니다 . \"'); history.go(-1);</script>");
				out.close();
				return "/login";
			}

			

		} else { // 로그인 실패 시(탈퇴회원, 블랙회원)
			out.print("<script>alert('\" 잘못된 접근입니다. \"'); history.go(-1);</script>");
			out.close();
			return "member/login";
			
		}

	}

	// 로그아웃
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(HttpSession session) throws Exception {

		session.invalidate(); // 세션종료
		return "redirect:/";
	}

	// 아이디찾기 페이지 진입
	@RequestMapping(value = "/findId", method = RequestMethod.GET)
	public String findIdGET() {
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 아이디찾기 페이지 진입");
		return "member/findId";
	}

	// 비밀번호 찾기 페이지 진입
	@RequestMapping(value = "/findPw", method = RequestMethod.GET)
	public String findPwGET() {
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 비밀번호 찾기 페이지 진입");
		return "member/findPw";
	}

	// 아이디 찾기 실행
	@RequestMapping(value = "/findIdAction", method = RequestMethod.POST)
	public String findIdAction(HttpServletResponse response, @ModelAttribute MemberVO vo, Model md) throws Exception {
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 아이디 찾기 실행");
		md.addAttribute("id", memberService.findId(response, vo));
		logger.info("Member55:" + vo);
		return "member/findId";
	}

	// 임시 비밀번호 이메일로 보내기
	@RequestMapping(value = "/findPwAction", method = { RequestMethod.GET, RequestMethod.POST })
	public String findPwAction(HttpServletResponse response, @ModelAttribute MemberVO vo, Model md) throws Exception {
		PrintWriter out = response.getWriter();
		String pw = memberService.findPw(response, vo);
		String email = vo.getEmail();
		String id = vo.getId();
		logger.info("Member55:" + vo);

		if (pw != null) {

			out.print("<script>alert('\" 이메일로 임시 비밀번호를 발송하였습니다. \"'); history.go(-1);</script>");
			out.close();

			logger.info("========================== 이메일 데이터 전송 확인 ============================");
			logger.info("인증번호 : [ " + email + " ]");

			Random random = new Random();
			int pw1 = random.nextInt(888888) + 111111;
			logger.info("인증번호(checkNum) [ " + pw1 + " ]");

			// 이메일 보내기
			String setFrom = "dhkdwk997@gmail.com";
			String toMail = email;
			String title = "비밀번호 이메일 입니다.";
			String content = "방문해주셔서 감사합니다." + "<br>" + id + "님의 임시 비밀번호는 [ " + pw1 + " ]입니다." + "<br>"
					+ "비밀번호를 변경하여 사용하세요.";

			try {

				MimeMessage message = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
				helper.setFrom(setFrom);
				helper.setTo(toMail);
				helper.setSubject(title);
				helper.setText(content, true);
				mailSender.send(message);

			} catch (Exception e) {

				e.printStackTrace();
			}
			String num = Integer.toString(pw1);
			String a=BCrypt.hashpw(num, BCrypt.gensalt());
			vo.setPw(a);
			memberService.updatePw(response, vo);

		} else if (pw == null) {

			out.print("<script>alert('\" 해당하는 아이디나 이메일이 없습니다. \"'); </script>");
			out.close();

		}

		return "redirect:/login";

	}

}