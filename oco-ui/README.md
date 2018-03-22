# README #

This is the entirely optional UI part.

Install NPM (`sudo apt-get install npm`) then run `npm start` in the root folder to start a local dev server.  You can access it at http://localhost:3000.

Use `npm run build` to create a static deployable build you can drop on any static web server.

I deploy it to Heroku using this buildpack (full instructions there):

https://github.com/mars/create-react-app-buildpack

When doing so, I ensure to set up `API_URL` to point to my API instance.