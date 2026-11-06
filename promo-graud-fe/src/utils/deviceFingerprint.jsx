import FingerprintJS from '@fingerprintjs/fingerprintjs';
// Khởi tạo một lần, dùng lại cho mọi request
const fpPromise = FingerprintJS.load();
let cachedVisitorId = null;

export async function getVisitorId() {
  if (cachedVisitorId) return cachedVisitorId;
  const fp = await fpPromise;
  const { visitorId } = await fp.get();
  cachedVisitorId = visitorId;
  return visitorId;
}
