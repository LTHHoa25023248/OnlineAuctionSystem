package com.example.auctionmanagementsystem.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;
import java.util.Random;


public class EmailService {


  private static final String SMTP_HOST = "smtp.gmail.com";
  private static final int SMTP_PORT = 587;

  // TODO: Thay bằng Gmail thật + App Password của bạn
  private static final String SENDER_EMAIL = "duc2112602003@gmail.com";
  private static final String SENDER_PASSWORD = "qyvj lmbd azul ssxg";

  // OTP hết hạn sau 10 phút (ms)
  public static final long OTP_EXPIRE_MS = 10 * 60 * 1000L;



  /**
   * Tạo mã OTP 6 chữ số ngẫu nhiên.
   */
  public static String generateCode() {
    return String.format("%06d", new Random().nextInt(1_000_000));
  }

  /**
   * Gửi email chứa mã OTP đặt lại mật khẩu.
   *
   * @param toEmail Địa chỉ nhận
   * @param code Mã OTP 6 chữ số
   * @return true nếu gửi thành công
   */
  public static boolean sendResetCode(String toEmail, String code) {
    try {
      Session session = buildSession();

      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(SENDER_EMAIL, "Auction House"));
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
      message.setSubject(" Your Password Reset Code — Auction House");
      message.setContent(buildEmailBody(code), "text/html; charset=utf-8");

      Transport.send(message);
      System.out.println("[EmailService] OTP sent to: " + toEmail);
      return true;

    } catch (Exception e) {
      System.err.println("[EmailService] Failed to send email: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }


  private static Session buildSession() {
    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", SMTP_HOST);
    props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
    props.put("mail.smtp.ssl.trust", SMTP_HOST);

    return Session.getInstance(props, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
      }
    });
  }

  /**
   * HTML body email đẹp, responsive.
   */
  private static String buildEmailBody(String code) {
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="margin:0;padding:0;background:#0D1018;font-family:Georgia,serif;">
          <table width="100%%" cellpadding="0" cellspacing="0"
                 style="background:#0D1018;padding:40px 0;">
            <tr><td align="center">
              <table width="520" cellpadding="0" cellspacing="0"
                     style="background:#1A1F2E;border-radius:16px;
                            border:1px solid rgba(255,255,255,0.06);
                            overflow:hidden;">

                <!-- Header -->
                <tr>
                  <td align="center"
                      style="background:linear-gradient(135deg,#D4A83A,#F0C060);
                             padding:32px 40px;">
                    <h1 style="margin:0;color:#0D1018;font-size:24px;
                               font-weight:bold;letter-spacing:1px;">
                      🏛 Auction House
                    </h1>
                  </td>
                </tr>

                <!-- Body -->
                <tr>
                  <td style="padding:40px;">
                    <h2 style="margin:0 0 12px;color:#F2F0EC;font-size:20px;">
                      Password Reset Request
                    </h2>
                    <p style="color:rgba(168,164,156,0.80);font-size:14px;
                              line-height:1.6;margin:0 0 28px;">
                      We received a request to reset your password.
                      Use the code below to continue. This code expires in
                      <strong style="color:#F0C060;">10 minutes</strong>.
                    </p>

                    <!-- OTP Box -->
                    <div style="background:#0D1018;border:2px solid #D4A83A;
                                border-radius:12px;padding:24px;text-align:center;
                                margin-bottom:28px;">
                      <p style="margin:0 0 8px;color:rgba(168,164,156,0.60);
                                font-size:12px;letter-spacing:2px;
                                text-transform:uppercase;">
                        Verification Code
                      </p>
                      <p style="margin:0;color:#F0C060;font-size:42px;
                                font-weight:bold;letter-spacing:12px;">
                        %s
                      </p>
                    </div>

                    <p style="color:rgba(168,164,156,0.55);font-size:13px;
                              line-height:1.6;margin:0;">
                      If you did not request a password reset, please ignore
                      this email. Your account remains secure.
                    </p>
                  </td>
                </tr>

                <!-- Footer -->
                <tr>
                  <td style="background:rgba(0,0,0,0.20);padding:20px 40px;
                             border-top:1px solid rgba(255,255,255,0.05);">
                    <p style="margin:0;color:rgba(168,164,156,0.40);
                              font-size:12px;text-align:center;">
                      © 2024 Auction House. All rights reserved.
                    </p>
                  </td>
                </tr>

              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.formatted(code);
  }
}
