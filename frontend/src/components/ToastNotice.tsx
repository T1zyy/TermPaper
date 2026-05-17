import { useEffect } from "react";

export type ToastNoticeProps = {
  message: string | null;
  onClose: () => void;
};

export default function ToastNotice({ message, onClose }: ToastNoticeProps) {
  useEffect(() => {
    if (!message) {
      return;
    }
    const timer = window.setTimeout(onClose, 1800);
    return () => window.clearTimeout(timer);
  }, [message, onClose]);

  if (!message) {
    return null;
  }

  return <div className="toast-notice">{message}</div>;
}
