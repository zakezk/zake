// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /healthy/ */
export async function healthy(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/healthy/', {
    method: 'GET',
    ...(options || {}),
  })
}
