FROM node:18 as build-deps
WORKDIR /usr/src/app
COPY package.json ./
RUN yarn
COPY . ./
RUN yarn build

FROM nginx:1.23.2-alpine
COPY --from=build-deps /usr/src/app/target/classes/META-INF/resources /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
