/**
 * 错误码映射工具
 * 将后端错误代码转换为用户友好的提示信息
 */

/**
 * 错误码到友好消息的映射
 */
const errorCodeMap: Record<string, string> = {
  // 认证相关
  '401': '登录已过期，请重新登录',
  '403': '没有权限执行此操作',
  
  // 资源相关
  '404': '请求的资源不存在',
  
  // 服务器错误
  '500': '服务器内部错误，请稍后重试',
  '502': '服务暂时不可用，请稍后重试',
  '503': '服务维护中，请稍后重试',
  '504': '请求超时，请稍后重试',
  
  // 业务错误
  // 认证相关 (1001-1099)
  '1001': '用户不存在',
  '1002': '用户已被禁用',
  '1003': '密码错误',
  '1004': '用户已被锁定，请稍后再试',
  '1005': '登录已过期，请重新登录',
  '1006': '无效的Token',
  
  // 项目管理 (2001-2099)
  '2001': '项目不存在',
  '2002': '进行中的项目不能删除，请先暂停或完成项目',
  '2003': '已归档的项目不允许删除',
  '2004': '已归档的项目不允许修改',
  '2005': '只有已完成的项目可以归档',
  '2006': '只有已归档的项目可以恢复',
  '2007': '不允许的状态转换',
  
  // 点位管理 (3001-3099)
  '3001': '点位不存在',
  
  // 模板管理 (4001-4099)
  '4001': '模板不存在',
  '4002': '文件上传失败',
  '4003': '文件格式不支持',
  '4004': '文件大小超出限制',
  
  // 数据导入 (5001-5099)
  '5001': '导入数据格式错误',
  '5002': '导入数据验证失败',
  
  // 默认消息
  'default': '操作失败，请稍后重试'
};

/**
 * HTTP 状态码映射
 */
const httpStatusMap: Record<number, string> = {
  400: '请求参数错误',
  401: '未授权，请先登录',
  403: '没有访问权限',
  404: '请求的资源不存在',
  405: '请求方法不允许',
  408: '请求超时',
  429: '请求过于频繁，请稍后重试',
  500: '服务器错误',
  502: '网关错误',
  503: '服务不可用',
  504: '网关超时'
};

/**
 * 获取友好的错误消息
 * @param code 错误代码（后端返回的 code 或 HTTP 状态码）
 * @param defaultMessage 默认消息
 * @returns 友好的错误消息
 */
export function getFriendlyErrorMessage(code: string | number, defaultMessage?: string): string {
  const codeStr = String(code);
  
  // 先查找业务错误码
  if (errorCodeMap[codeStr]) {
    return errorCodeMap[codeStr];
  }
  
  // 再查找 HTTP 状态码
  const httpCode = Number(codeStr);
  if (httpStatusMap[httpCode]) {
    return httpStatusMap[httpCode];
  }
  
  // 返回默认消息或传入的消息
  return defaultMessage || errorCodeMap['default'];
}

/**
 * 判断是否是网络错误
 */
export function isNetworkError(error: any): boolean {
  return !error.response && error.message?.includes('Network Error');
}

/**
 * 判断是否是超时错误
 */
export function isTimeoutError(error: any): boolean {
  return error.code === 'ECONNABORTED' || error.message?.includes('timeout');
}

/**
 * 获取网络错误的友好提示
 */
export function getNetworkErrorMessage(): string {
  return '网络连接失败，请检查您的网络连接';
}

/**
 * 获取超时错误的友好提示
 */
export function getTimeoutErrorMessage(): string {
  return '请求超时，请检查网络连接或稍后重试';
}
