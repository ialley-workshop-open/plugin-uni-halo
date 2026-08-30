// 请求封装：复用 @halo-dev/api-client 的 axiosInstance 默认配置（baseURL、XSRF Cookie 等），
// 但创建独立实例，仅注册本插件自己的拦截器——避免宿主控制台给共享 axiosInstance
// 注册的全局错误拦截器（会额外弹出「400: Bad Request」类 Toast）与本插件业务提示重复。
// 统一错误处理：优先透出后端返回的 message。

import axios from "axios";
import { axiosInstance } from "@halo-dev/api-client";

// 复制宿主实例的默认配置（baseURL / withCredentials / XSRF Cookie 配置等），
// 不继承宿主注册的拦截器。
const httpClient = axios.create({
  ...axiosInstance.defaults,
});

// 统一错误处理：优先透出后端返回的 message（axios 默认只有状态码文案）
httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error?.response?.data?.message || error?.message || "请求失败（" + (error?.response?.status ?? "") + "）";
    return Promise.reject(new Error(message));
  }
);

export const http = {
  get: <T>(url: string, params?: object) =>
    httpClient.get<T>(url, { params }).then((response) => response.data),
  post: <T>(url: string, body?: unknown, params?: object) =>
    httpClient.post<T>(url, body, { params }).then((response) => response.data),
  put: <T>(url: string, body?: unknown) =>
    httpClient.put<T>(url, body).then((response) => response.data),
  delete: <T>(url: string) =>
    httpClient.delete<T>(url).then((response) => response.data),
};
