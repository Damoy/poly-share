package com.lama.polyshare.mails;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/***
 *	Utilitaire pour l'envoi de mail 
 */
public class MailSender  {

	private static final long serialVersionUID = 2316390422015026794L;
	public static volatile MailSender instance = new MailSender();
	public final static String SENDER_MAIL = "amandinebenza@gmail.com";
	
	public MailSender() {}
	

	
	public void sendUploadMail(String recipient, String link, String filename) {
		sendMail(SENDER_MAIL, recipient, "", "Successful upload of your file", filename + " : " + link);	
	}
	
	public void sendDownloadMail(String recipient, String link) {
		sendMail(SENDER_MAIL, recipient, "", "Download link for your file", link);	
	}
	 
	public void sendNoob(String recipient) {
		sendMail(SENDER_MAIL, recipient, "", "Here's your link", "Lol. No noob.");	
	}

	public void sendMail(String sender, String recipient, String recipientName, String subject, String body) {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(sender));
			msg.addRecipient(Message.RecipientType.TO,
					new InternetAddress(recipient, "Dear Mr or Ms " + recipientName));
			msg.setSubject(subject);
			msg.setText(body);
			Transport.send(msg);

		} catch (AddressException e) {
			System.out.println("Invalid adress");
		} catch (MessagingException e) {
			System.out.println("Invalid message");
		} catch (UnsupportedEncodingException e) {
			System.out.println("Unsupported Encoding");
		}
	}
	
	
}