FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy a custom nginx config if you have one, or just use default.
# The default works fine for static React apps unless you need client-side routing (React Router), 
# then you'd need a custom nginx config. Assuming standard Vite setup.
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
