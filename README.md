# TinderBot4J

# Introduction
Welcome to my Tinder Java bot. It features almost everything you would need to use Tinder without losing time reviewing profiles.
Don't expect overcomplicated features like facial recognition or real conversation capabilities.

# Features
 - Daemon-like bot, that will run forever and will do its job when your account have likes available.
 - Automatic facebook token registration via puppeteer script. If your token is expired, automatically will retrieve you a fresh one.
 - Automatic like/pass on retrieved profiles, based on a like percentage. It's recommended to avoid a 1 ratio, because you will get less matches. Ex: If you set a 0.7 in the application.properties, the bot will do a random 70% of likes and a 30% of pass.
 - Automatic conversation start in case of match, sending one of the preloaded messages in application.properties.
 - Optional MySQL database store of retrieved profiles, with information like name, bio, if you liked/passed it, if it was a match, etc.
 
 # Setup
 You will need the latest version of Node.js installed and, obviously, Java.
  - After you import the project, compile it via maven.
  - Once compiled, run npm install in target folder to get node resources for the facebook authentication script (main.js).
  - Check configuration files (.properties files) and fill it with your personal information.
  - Optionally, setup your database with the SQL script provided and add the information to your db.properties file.
  - After that, you will be able to run the bot from the jar with dependencies.
