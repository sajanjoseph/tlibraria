package bootstrap;

import models.BookShopUser;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class BootStrap extends Job {

	@Override
	public void doJob() throws Exception {
		if (BookShopUser.count()==0) {
			Fixtures.deleteDatabase();
			Fixtures.loadModels("initial-data.yml");
		}
	}
	

}
