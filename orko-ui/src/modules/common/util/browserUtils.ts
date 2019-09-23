export function showBrowserNotification(
  title: string,
  message: string,
  timeout: number = 5000
): void {
  if (Notification.permission !== "granted") Notification.requestPermission()
  else {
    const n = new Notification(title, { body: message })
    setTimeout(n.close.bind(n), timeout)
    n.onclick = () => n.close()
  }
}
