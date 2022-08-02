package start.project.higia.controllers;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import start.project.higia.models.Doctor;
import start.project.higia.models.DoctorPasswordTokens;
import start.project.higia.security.ISecurityService;
import start.project.higia.services.DoctorPasswordTokensServices;
import start.project.higia.services.DoctorService;
import start.project.higia.utils.Construct;
import start.project.higia.utils.GenericResponse;
import start.project.higia.utils.OnRegistrationCompleteEvent;
import start.project.higia.utils.PasswordDto;

@Controller
public class DoctorPasswordTokensController {

	@Autowired
	DoctorPasswordTokensServices doctorPasswordTokensServicesservices;

	@Autowired
	DoctorService doctorService;

	@Autowired
	private MessageSource messages;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private ISecurityService securityService;

	@Autowired
	private Construct construct;

	@PostMapping("/doctor/reset/password")
	public GenericResponse create(HttpServletRequest request, @RequestParam("email") String email,
			DoctorPasswordTokens doc, Model model, OnRegistrationCompleteEvent obj) {
		Doctor doctor = doctorService.findByEmail(email);
		if (doctor == null) {
			model.addAttribute("message", "E-mail não cadastrado.");
		}
		String token = UUID.randomUUID().toString();
		doctorPasswordTokensServicesservices.create(doctor, token);
		mailSender.send(this.construct.constructResetTokenEmail(obj.getAppUrl(), request.getLocale(), token, doctor));
		return new GenericResponse(messages.getMessage("message.resetPasswordEmail", null, request.getLocale()));
	}

	@GetMapping("/doctor/change/password")
	public String showChangePasswordPage(Locale locale, Model model, @RequestParam("token") String token) {
		String result = securityService.validatePasswordResetToken(token);
		if (result != null) {
			String message = messages.getMessage("auth.message." + result, null, locale);
			return "redirect:/login.html?lang=" + locale.getLanguage() + "&message=" + message;
		} else {
			model.addAttribute("token", token);
			return "redirect:/updatePassword.html?lang=" + locale.getLanguage();
		}
	}

	@PostMapping("/user/savePassword")
	public GenericResponse savePassword(final Locale locale, @Valid PasswordDto passwordDto) {

		String result = securityService.validatePasswordResetToken(passwordDto.getToken());

		if (result != null) {
			return new GenericResponse(messages.getMessage("auth.message." + result, null, locale));
		}

		Optional<Doctor> doctor = doctorPasswordTokensServicesservices
				.getDoctorByPasswordResetToken(passwordDto.getToken());
		if (doctor.isPresent()) {
			doctorPasswordTokensServicesservices.changePassword(doctor.get(), passwordDto.getNewPassword());
			return new GenericResponse(messages.getMessage("message.resetPasswordSuc", null, locale));
		} else {
			return new GenericResponse(messages.getMessage("auth.message.invalid", null, locale));
		}
	}

}
