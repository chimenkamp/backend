package icpmapp.services.impl;

import icpmapp.dto.requests.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import icpmapp.services.EmailService;
import icpmapp.services.JWTService;
import icpmapp.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender mailSender;
    private final JWTService jwtService;
    private final UserService userService;

    @Value("${conferia.mail-from:}")
    private String mailFrom;

    @Value("${conferia.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public void sendSignup(EmailRequest emailRequest) throws AccessDeniedException, MessagingException {
        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(emailRequest.getReceiver());
        boolean hasRequiredRole = userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("INACTIVE"));


        if (!hasRequiredRole) {
            throw new AccessDeniedException("User already activated.");
        }
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setFrom(requiredMailFrom());
        helper.setTo(emailRequest.getReceiver());
        helper.setSubject("Conference app account activation");
        String token =  jwtService.generateToken(userDetails);
        helper.setText(
                "<html><body><p>To activate your conference app account, open "
                        + frontendLink("/#/auth/register/", token)
                        + ".</p></body></html>",
                true
        );
        mailSender.send(mimeMessage);
    }

    public void sendResetPassword(EmailRequest emailRequest) throws AccessDeniedException, MessagingException {
        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(emailRequest.getReceiver());
        boolean hasWrongRole = userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("INACTIVE"));


        if (hasWrongRole) {
            throw new AccessDeniedException("User needs to be activated");
        }
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setFrom(requiredMailFrom());
        helper.setTo(emailRequest.getReceiver());
        helper.setSubject("Reset password");
        String token =  jwtService.generateToken(userDetails);
        helper.setText(
                "<html><body><p>To reset your conference app account password, open "
                        + frontendLink("/#/auth/login/resetpassword/", token)
                        + ".</p></body></html>",
                true
        );
        mailSender.send(mimeMessage);
    }

    @Override
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setFrom(requiredMailFrom());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText("<html><body><p>" + body + "</p></body></html>", true);
        mailSender.send(mimeMessage);
    }

    private String requiredMailFrom() throws MessagingException {
        if (mailFrom == null || mailFrom.isBlank()) {
            throw new MessagingException("CONFERIA_MAIL_FROM must be configured before sending email");
        }
        return mailFrom;
    }

    private String frontendLink(String path, String token) {
        String base = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
        return base + path + token;
    }
}
