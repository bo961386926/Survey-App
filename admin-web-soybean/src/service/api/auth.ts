import { request } from '../request';

/**
 * Login
 *
 * @param username User name
 * @param password Password
 * @param captcha Captcha code
 * @param captchaKey Captcha key
 */
export function fetchLogin(username: string, password: string, captcha: string, captchaKey: string) {
  return request<Api.Auth.LoginToken>({
    url: '/auth/login',
    method: 'post',
    data: {
      username,
      password,
      captcha,
      captchaKey
    }
  });
}

/** Get user info */
export function fetchGetUserInfo() {
  return request<Api.Auth.UserInfo>({ url: '/auth/getUserInfo' });
}

/** Get captcha */
export function fetchGetCaptcha() {
  return request<{ image: string; key: string }>({ url: '/auth/captcha' });
}

/**
 * Refresh token
 *
 * @param refreshToken Refresh token
 */
export function fetchRefreshToken(refreshToken: string) {
  return request<Api.Auth.LoginToken>({
    url: '/auth/refreshToken',
    method: 'post',
    data: {
      refreshToken
    }
  });
}

/**
 * return custom backend error
 *
 * @param code error code
 * @param msg error message
 */
export function fetchCustomBackendError(code: string, msg: string) {
  return request({ url: '/auth/error', params: { code, msg } });
}
