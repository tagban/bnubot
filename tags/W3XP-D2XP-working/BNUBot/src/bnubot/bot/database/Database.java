package bnubot.bot.database;

public class Database {
	UserDatabase ud = null;
	
	public Database() {
		ud = new UserDatabase();
		ud.addUser("bnu-camel@azeroth", new User(100));
		ud.addUser("bnu-camel@useast", new User(100));
		ud.addUser("bnu-camel", new User(100));
		ud.addUser("bnu-bot@azeroth", new User(100));
		ud.addUser("bnu-bot@useast", new User(100));
		ud.addUser("bnu-bot", new User(100));
	}
	
	public UserDatabase getUserDatabase() {
		return ud;
	}
}
