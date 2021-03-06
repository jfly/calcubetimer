package net.gnehzr.cct.miscUtils;
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

import javax.mail.*;
import javax.mail.internet.*;

import net.gnehzr.cct.configuration.Configuration;

import java.util.*;

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

	private char[] pass;
	public void setPassword(char[] pass){
		this.pass = pass;
	}

	public void postMail(String recipients[], String subject, String message) throws MessagingException {
		boolean debug = false;

		// Set the host smtp address
		Properties props = new Properties();

		props.setProperty("mail.smtp.host", Configuration.getSMTPHost());
		props.setProperty("mail.smtp.port", Configuration.getPort());
		props.setProperty("mail.smtp.auth", Boolean.toString(Configuration.isSMTPauth()));

		Session session = null;
		if(Configuration.isSMTPauth()) {
			Authenticator auth = new SMTPAuthenticator();
			session = Session.getInstance(props, auth);
		} else {
			session = Session.getInstance(props);
		}

		session.setDebug(debug);
		// create a message
		Message msg = new MimeMessage(session);

		// set the from and to address
		InternetAddress addressFrom = new InternetAddress(Configuration.getUserEmail());
		msg.setFrom(addressFrom);
		InternetAddress[] addressTo = new InternetAddress[recipients.length];;
		for (int i = 0; i < recipients.length; i++) {
			addressTo[i] = new InternetAddress(recipients[i]);
		}
		msg.setRecipients(Message.RecipientType.TO, addressTo);

		// Setting the Subject and Content Type
		msg.setSubject(subject);
		msg.setContent(message, "text/plain");
		Transport.send(msg);
	}

	public static boolean isNotSetup() {
		return Configuration.getSMTPHost().equals("") || Configuration.getUsername().equals("") || Configuration.getUserEmail().equals("") || Configuration.getPort().equals("");
	}

	/**
	 * SimpleAuthenticator is used to do simple authentication when the SMTP
	 * server requires it.
	 */
	private class SMTPAuthenticator extends javax.mail.Authenticator {

		public PasswordAuthentication getPasswordAuthentication() {
			String username = Configuration.getUsername();
			String password = new String(pass == null ? Configuration.getPassword() : pass);
			PasswordAuthentication p = new PasswordAuthentication(username, password);
			if(pass != null){
				for(int i = 0; i < pass.length; i++){
					pass[i] = 0;
				}
			}

			return p;
		}
	}

}
