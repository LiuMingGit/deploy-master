package com.bsoft.deploy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MasterApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void cleanPath() {
		String path = "d:\\a/b_c/d e/f";
		assert StringUtils.cleanPath(path).equals("d:/a/b_c/d e/f");
	}

}
