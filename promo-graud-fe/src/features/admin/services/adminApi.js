import { API_BASE } from "../../../config/constants.js";
import { getStoredToken } from "../../auth/services/authService.js";

export async function adminFetch(path, { headers: extraHeaders = {}, ...rest } = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${getStoredToken()}`,
      ...extraHeaders,
    },
    ...rest,
  });
  return res.json();
}
