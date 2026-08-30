// 请求封装：复用 @halo-dev/api-client 的 axiosInstance（已注入 XSRF Cookie 逻辑），
// 统一错误处理，优先透出后端返回的 message。

import { axiosInstance } from "@halo-dev/api-client";

// 统一错误处理：优先透出后端返回的 message（axios 默认只有状态码文案）
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error?.response?.data?.message || error?.message || "请求失败（" + (error?.response?.status ?? "") + "）";
    return Promise.reject(new Error(message));
  }
);

export const http = {
  get: <T>(url: string, params?: object) =>
    axiosInstance.get<T>(url, { params }).then((response) => response.data),
  post: <T>(url: string, body?: unknown, params?: object) =>
    axiosInstance.post<T>(url, body, { params }).then((response) => response.data),
  put: <T>(url: string, body?: unknown) =>
    axiosInstance.put<T>(url, body).then((response) => response.data),
  delete: <T>(url: string) =>
    axiosInstance.delete<T>(url).then((response) => response.data),
};
