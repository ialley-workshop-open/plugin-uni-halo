// 附件相关纯工具函数

// AttachmentSelector 选中项转 URL（兼容 string / {url} / Attachment 三种形态）
export function attachmentUrl(attachment: unknown): string {
  if (typeof attachment === "string") {
    return attachment;
  }
  if (attachment && typeof attachment === "object") {
    const obj = attachment as { url?: string; status?: { permalink?: string } };
    if (obj.url) {
      return obj.url;
    }
    if (obj.status && obj.status.permalink) {
      return obj.status.permalink;
    }
  }
  return "";
}
