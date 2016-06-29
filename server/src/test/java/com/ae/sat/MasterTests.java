package com.ae.sat;

import com.ae.sat.servers.master.App;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@ActiveProfiles("local")
public class MasterTests {

	//@Ignore
	@Test
	public void contextLoads() {
	}

}
