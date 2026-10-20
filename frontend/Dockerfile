# syntax=docker/dockerfile:1

# ---------- Build stage ----------
FROM node:20-alpine AS build
WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---------- Runtime stage ----------
FROM nginx:1.27-alpine AS runtime

ENV NGINX_ENVSUBST_FILTER=^BACKEND_URL$
ENV BACKEND_URL=http://backend:8085

COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /build/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=3s --start-period=5s --retries=3 \
  CMD sh -c 'wget -qO- http://127.0.0.1/ >/dev/null || exit 1'
