/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RTPower;

import java.security.Security;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * 邮件发送
 *
 * @author jerry
 */
public class RTMail {

    public static String FromMail = "ceo@ruituo.net";
    public static String MailPass = "m8m8m8sa";
    public static String MailHost = "smtp.exmail.qq.com";
    public static String MailPort = "465";

    public static boolean Send(String ToMail, String Title, String MailText) {
        try {

            sendmail(ToMail, Title, MailText);
            return true;
        } catch (MessagingException ex) {
            RTFile.d("邮件发送失败" + ToMail + Title);
            return false;
        }
    }

    private static void sendmail(String ToMail, String Title, String MailText) throws MessagingException {

        //设置SSL连接、邮件环境
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", MailHost);
        props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.port", MailPort);
        props.setProperty("mail.smtp.socketFactory.port", MailPort);
        props.setProperty("mail.smtp.auth", "true");

        //建立邮件会话
        Session session = Session.getDefaultInstance(props, new Authenticator() {
            //身份认证
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FromMail, MailPass);
            }
        });
        //建立邮件对象
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FromMail));
        message.setRecipients(Message.RecipientType.TO, ToMail);
        message.setSubject(Title);
        // 设置消息体
//        message.setText(MailText);
        //设置html
        Multipart mp = new MimeMultipart("related");
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setContent(MailText, "text/html;charset=utf-8");
        mp.addBodyPart(mbp);
        message.setContent(mp);
        // 发送消息
        Transport.send(message);

    }

}
