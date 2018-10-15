# build environment
FROM node:9.8.0 as builder
RUN mkdir /usr/src/app
WORKDIR /usr/src/app
ENV PATH /usr/src/app/node_modules/.bin:$PATH
COPY package.json /usr/src/app/package.json
COPY . /usr/src/app
RUN npm install gulp
RUN npm run build

# production environment
FROM nginx:1.13.9-alpine
COPY --from=builder /usr/src/app/build /usr/share/nginx/html
COPY default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
ENV API_URL=http://web
CMD ["nginx", "-g", "daemon off;"]
