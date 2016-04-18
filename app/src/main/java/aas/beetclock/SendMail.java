package aas.beetclock;

/* Adapted with modifications from code by Davanum Srinivas
https://davanum.wordpress.com/2007/12/22/android-send-email-via-gmail-actually-via-smtp/
 */

import com.sun.mail.smtp.SMTPAddressFailedException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;

public class SendMail {

    //private Multipart _multipart;

    public void send(String[] args) throws Exception { //was public static void; removed static to access context

        //I use a boolean to track send success; will set to false if sendMail throws exception
        Boolean sendSuccess = true;

        //Accepts a string array with five components: sender, recipient, content, attachment1 location, attachment2 location
        Properties props = new Properties();

        //Get system time to timestamp attachments
        Long time = System.currentTimeMillis();
        DateFormat formatter = new SimpleDateFormat("MMddyy");
        // Codes for re-writing this format available at http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        String timeStamp = formatter.format(time);

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "mail.beetclock.com");//smtp.gmail.com
        props.put("mail.smtp.port", "26");//587
        props.put("mail.debug", "true");
        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("reporter@beetclock.com", "21BK#f3");
                    }
                });
        //try {
            MimeMessage msg = new MimeMessage(session);

            //Attach .csv files using a multipart message
            Multipart multipart = new MimeMultipart();

        //Adds message text as part 1
            MimeBodyPart messageBodyPart1 = new MimeBodyPart();
            messageBodyPart1.setText(args[2]);


        //Checks whether attachment locations are specified; if not, adds only part 1.  If so, creates and adds parts 2 and 3 also

        if(args[4].equals("")){

            multipart.addBodyPart(messageBodyPart1);

        }      else{
            MimeBodyPart messageBodyPart2 = new MimeBodyPart();
            String filename = args[3];
            DataSource source = new FileDataSource(filename);
            messageBodyPart2.setDataHandler(new DataHandler(source));
            messageBodyPart2.setFileName("BeetClock_report_" + args[0] + "_" + timeStamp + ".csv");
            messageBodyPart2.setDisposition(MimeBodyPart.ATTACHMENT);

            MimeBodyPart messageBodyPart3 = new MimeBodyPart();
            filename = args[4];
            source = new FileDataSource(filename);
            messageBodyPart3.setDataHandler(new DataHandler(source));
            messageBodyPart3.setFileName("BeetClock_records_" + args[0] + "_" + timeStamp + ".csv");
            messageBodyPart3.setDisposition(MimeBodyPart.ATTACHMENT);

            multipart.addBodyPart(messageBodyPart1);
            multipart.addBodyPart(messageBodyPart2);
            multipart.addBodyPart(messageBodyPart3);//end args if
        }


            //BodyPart messageBodyPart2 = new MimeBodyPart();
            //messageBodyPart2.setText(subject);
            //_multipart.addBodyPart(messageBodyPart2);

            msg.setFrom(new InternetAddress("reporter@beetclock.com"));
            msg.setRecipients(Message.RecipientType.TO, args[1]);
            msg.setSubject("BeetClock Report from " + args[0]);
            msg.setContent(multipart);//This is to add the attacment
            msg.setSentDate(new Date());
            //msg.setContent(args[2],"text/html; charset=utf-8");
            //msg.setText(args[2]);
            Transport.send(msg);
       // } //catch (SMTPAddressFailedException fail){
          //  sendSuccess = false;
        //} catch (MessagingException mex) {
          //  mex.printStackTrace();
          //  sendSuccess = false;
        //}
        //return sendSuccess;
    }


}