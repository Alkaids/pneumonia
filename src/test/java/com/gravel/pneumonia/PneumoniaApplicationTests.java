package com.gravel.pneumonia;

import com.gravel.pneumonia.scheduleTask.SendMailTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.mail.MessagingException;
import java.io.IOException;

@SpringBootTest
class PneumoniaApplicationTests {

	@Autowired
	SendMailTask sendMailTask;

	@Test
	void testSendMailTask() throws IOException {
		sendMailTask.crawAllPenumouiaMessage();
	}
	@Test
	void testSendMail() throws IOException, MessagingException {
		sendMailTask.sendMail();
	}

}
