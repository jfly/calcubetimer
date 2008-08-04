package net.gnehzr.cct.misc;
/*
 Some SMTP servers require a username and password authentication before you
 can use their Server for Sending mail. This is most common with couple
 of ISP's who provide SMTP Address to Send Mail.

 This Program gives any example on how to do SMTP Authentication
 (User and Password verification)

 This is a free source code and is provided as it is without any warranties and
 it can be used in any your code for free.

 Author : Sudhir Ancha
 */

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;

/*
 * To use this program, change values for the following three constants,
 *
 * SMTP_HOST_NAME -- Has your SMTP Host Name SMTP_AUTH_USER -- Has your SMTP
 * Authentication UserName SMTP_AUTH_PWD -- Has your SMTP Authentication
 * Password
 *
 * Next change values for fields
 *
 * emailMsgTxt -- Message Text for the Email emailSubjectTxt -- Subject for
 * email emailFromAddress -- Email Address whose name will appears as "from"
 * address
 *
 * Next change value for "emailList". This String array has List of all Email
 * Addresses to Email Email needs to be sent to.
 *
 *
 * Next to run the program, execute it as follows,
 *
 * SendMailUsingAuthentication authProg = new SendMailUsingAuthentication();
 *
 */

public class SendMailUsingAuthentication {
	/*
	public static void main(String args[]) throws Exception {

		String emailMsgTxt = "hello";
		String emailSubjectTxt = "hello";

		// Add List of Email address to who email needs to be sent to
		String[] emailList = {"jeremyfleischman@sbcglobal.net"};
		SendMailUsingAuthentication smtpMailSender = new SendMailUsingAuthentication(new Configuration());
		smtpMailSender.postMail(emailList, emailSubjectTxt, emailMsgTxt);
		System.out.println("Sucessfully sent mail to All Users");
	}*/

	char[] pass = null;
	public SendMailUsingAuthentication(char[] pass) {
		this.pass = pass;
	}

	public void postMail(String recipients[], String subject, String message) throws MessagingException {
		boolean debug = false;

		// Set the host smtp address
		Properties props = new Properties();

		props.setProperty("mail.smtp.host", Configuration.getString(VariableKey.SMTP_HOST, false));
		props.setProperty("mail.smtp.port", Configuration.getString(VariableKey.SMTP_PORT, false));
		props.setProperty("mail.smtp.auth", Boolean.toString(Configuration.getBoolean(VariableKey.SMTP_AUTHENTICATION, false)));
		props.put("mail.smtp.starttls.enable", "true");

		Session session = null;
		if(Configuration.getBoolean(VariableKey.SMTP_AUTHENTICATION, false)) {
			Authenticator auth = new SMTPAuthenticator();
			session = Session.getInstance(props, auth);
		} else {
			session = Session.getInstance(props);
		}

		session.setDebug(debug);
		// create a message
		Message msg = new MimeMessage(session);

		// set the from and to address
		InternetAddress addressFrom = new InternetAddress(Configuration.getString(VariableKey.SMTP_FROM_ADDRESS, false));
		msg.setFrom(addressFrom);
		InternetAddress[] addressTo = new InternetAddress[recipients.length];
		for (int i = 0; i < recipients.length; i++) {
			addressTo[i] = new InternetAddress(recipients[i]);
		}
		msg.setRecipients(Message.RecipientType.TO, addressTo);

		// Setting the Subject and Content Type
		msg.setSubject(subject);
		msg.setContent(message, "text/plain");
		Transport.send(msg);
	}

	/**
	 * SimpleAuthenticator is used to do simple authentication when the SMTP
	 * server requires it.
	 */
	private class SMTPAuthenticator extends javax.mail.Authenticator {
		public SMTPAuthenticator() {}
		public PasswordAuthentication getPasswordAuthentication() {
			String username = Configuration.getString(VariableKey.SMTP_USERNAME, false);
			String password;
			if(pass == null) {
				password = Configuration.getString(VariableKey.SMTP_PASSWORD, false);
			} else {
				password = new String(pass);
				for(int i = 0; i < pass.length; i++){
					pass[i] = 0;
				}
			}
			PasswordAuthentication p = new PasswordAuthentication(username, password);
			return p;
		}
	}

}
