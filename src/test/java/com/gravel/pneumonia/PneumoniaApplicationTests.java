package com.gravel.pneumonia;

import com.gravel.pneumonia.task.DxyMessageTask;
import com.gravel.pneumonia.task.TgMessageTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class PneumoniaApplicationTests {

	@Autowired
	DxyMessageTask dxyMessageTask;

	@Autowired
	TgMessageTask tgMessageTask;
	@Test
	void testSendMailTask() throws IOException {
		dxyMessageTask.crawAllPenumouiaMessage();
	}
	@Test
	void testSendMail() throws IOException {
		dxyMessageTask.sendMail();
	}

	@Test
	void testRssSendMail() throws IOException {
		tgMessageTask.scheduledTgTask();
	}

}
